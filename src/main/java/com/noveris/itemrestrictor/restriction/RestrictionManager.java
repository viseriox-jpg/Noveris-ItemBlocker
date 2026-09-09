package com.noveris.itemrestrictor.restriction;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/** Single server authority for all restriction decisions. */
public final class RestrictionManager {
    private static final String DATA_ID = "noveris_item_restrictions";
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<ServerLevel, Cache> CACHES = new WeakHashMap<>();
    private static volatile RestrictionData latest;
    private RestrictionManager() { }
    public static RestrictionData data(ServerLevel level) { ServerLevel overworld = level.getServer().overworld(); latest = overworld.getDataStorage().computeIfAbsent(new SavedData.Factory<>(RestrictionData::new, RestrictionData::load, null), DATA_ID); return latest; }
    public static RestrictionData data(ServerPlayer player) { return data(player.serverLevel()); }
    public static ResourceLocation id(ItemStack stack) { return BuiltInRegistries.ITEM.getKey(stack.getItem()); }
    public static boolean validItemId(String raw) { try { ResourceLocation id = ResourceLocation.parse(raw); return raw.length() <= 256 && BuiltInRegistries.ITEM.containsKey(id); } catch (Exception e) { return false; } }
    public static boolean validNamespace(String raw) { return raw != null && raw.length() <= 64 && raw.matches("[a-z0-9_.-]+") && !raw.contains(":" ); }
    private static Cache cache(ServerPlayer p) { RestrictionData d = data(p); synchronized (CACHES) { return CACHES.computeIfAbsent(p.serverLevel().getServer().overworld(), k -> new Cache(d)); } }
    private static void invalidate(ServerLevel l) { synchronized (CACHES) { CACHES.put(l.getServer().overworld(), new Cache(data(l))); } }
    private record Cache(Set<String> blocked, Set<String> allowlisted, Set<String> mods) { Cache(RestrictionData d) { this(Set.copyOf(d.itemRules.entrySet().stream().filter(e -> e.getValue() == RestrictionType.BLOCKED).map(Map.Entry::getKey).toList()), Set.copyOf(d.itemRules.entrySet().stream().filter(e -> e.getValue() == RestrictionType.PLAYER_ALLOWLIST).map(Map.Entry::getKey).toList()), Set.copyOf(d.restrictedMods)); } }
    public static boolean isModRestricted(String namespace, RestrictionData d) { return d.restrictedMods.contains(namespace); }
    public static boolean isModRestricted(String namespace) { return latest != null && latest.restrictedMods.contains(namespace); }
    public static boolean isModRestricted(String namespace, ServerPlayer p) { return cache(p).mods.contains(namespace); }
    public static boolean isGloballyBlocked(ResourceLocation id, RestrictionData d) { return d.itemRules.get(id.toString()) == RestrictionType.BLOCKED; }
    public static boolean isGloballyBlocked(ResourceLocation id) { return latest != null && isGloballyBlocked(id, latest); }
    public static boolean isPlayerRestricted(ResourceLocation id, RestrictionData d) { return d.itemRules.get(id.toString()) == RestrictionType.PLAYER_ALLOWLIST; }
    public static boolean isPlayerRestricted(ResourceLocation id) { return latest != null && isPlayerRestricted(id, latest); }
    public static boolean isAuthorizedPlayer(UUID uuid, ResourceLocation id, RestrictionData d) { return d.playerAllowlist.getOrDefault(id.toString(), Set.of()).contains(uuid); }
    public static boolean isAuthorizedPlayer(UUID uuid, ResourceLocation id) { return latest != null && isAuthorizedPlayer(uuid, id, latest); }
    private static boolean bypass(ServerPlayer p, RestrictionData d) { return d.operatorBypass && p.hasPermissions(d.permissionLevel); }
    public static RestrictionReason getRestrictionReason(ServerPlayer p, ItemStack stack) {
        if (stack.isEmpty()) return RestrictionReason.NONE;
        RestrictionData d = data(p); ResourceLocation item = id(stack); String key = item.toString();
        if (bypass(p, d)) return RestrictionReason.NONE;
        if (d.itemRules.get(key) == RestrictionType.BLOCKED) return new RestrictionReason(RestrictionReason.Kind.ITEM_BLOCKED, "noveris_item_restrictor.reason.item_blocked", key);
        if (d.itemRules.get(key) == RestrictionType.PLAYER_ALLOWLIST && !isAuthorizedPlayer(p.getUUID(), item, d)) return new RestrictionReason(RestrictionReason.Kind.ALLOWLIST, "noveris_item_restrictor.reason.allowlist", key);
        if (d.restrictedMods.contains(item.getNamespace())) return new RestrictionReason(RestrictionReason.Kind.MOD_BLOCKED, "noveris_item_restrictor.reason.mod_blocked", item.getNamespace());
        return RestrictionReason.NONE;
    }
    public static boolean canUseItem(ServerPlayer p, ItemStack s) { return getRestrictionReason(p, s).kind() == RestrictionReason.Kind.NONE; }
    public static boolean canPossessItem(ServerPlayer p, ItemStack s) { return canUseItem(p, s); }
    public static boolean canCraftItem(ServerPlayer p, ItemStack s) { return canUseItem(p, s); }
    public static boolean canEquipItem(ServerPlayer p, ItemStack s) { return canUseItem(p, s); }
    public static boolean canTransferItem(ServerPlayer p, ItemStack s) { return canUseItem(p, s); }
    public static boolean canInteractWithBlock(ServerPlayer p, ItemStack s) { return canUseItem(p, s); }
    public static boolean canInteractWithEntity(ServerPlayer p, ItemStack s) { return canUseItem(p, s); }
    public static boolean setItemRule(ServerPlayer p, ResourceLocation id, RestrictionType type) { RestrictionData d = data(p); if (d.itemRules.get(id.toString()) == type) return false; d.itemRules.put(id.toString(), type); if (type == RestrictionType.PLAYER_ALLOWLIST) d.playerAllowlist.computeIfAbsent(id.toString(), k -> new HashSet<>()); else d.playerAllowlist.remove(id.toString()); d.setDirty(); invalidate(p.serverLevel()); return true; }
    public static boolean removeItemRule(ServerPlayer p, ResourceLocation id) { RestrictionData d = data(p); boolean changed = d.itemRules.remove(id.toString()) != null | d.playerAllowlist.remove(id.toString()) != null; if (changed) { d.setDirty(); invalidate(p.serverLevel()); } return changed; }
    public static boolean setModRule(ServerPlayer p, String mod, boolean restricted) { RestrictionData d = data(p); boolean changed = restricted ? d.restrictedMods.add(mod) : d.restrictedMods.remove(mod); if (changed) { d.setDirty(); invalidate(p.serverLevel()); } return changed; }
    public static RestrictionSnapshot snapshot(ServerPlayer p) { RestrictionData d = data(p); Map<String, Set<String>> a = new TreeMap<>(); d.playerAllowlist.forEach((k,v) -> a.put(k, v.stream().map(UUID::toString).collect(java.util.stream.Collectors.toUnmodifiableSet()))); return new RestrictionSnapshot(Map.copyOf(d.itemRules), Map.copyOf(a), List.copyOf(d.restrictedMods), List.copyOf(d.auditLog), d.permissionLevel, d.operatorBypass); }
    public static void touch(ServerPlayer p) { RestrictionData d = data(p); if (d.remember(p.getUUID(), p.getGameProfile().getName())) d.setDirty(); }
    public static void audit(ServerPlayer p, String action, String subject, UUID affected) { RestrictionData d = data(p); d.audit(AuditEntry.now(p.getUUID(), p.getGameProfile().getName(), action, subject, affected)); d.setDirty(); LOGGER.info("Noveris admin={} uuid={} action={} subject={} affected={} time={}", p.getGameProfile().getName(), p.getUUID(), action, subject, affected, java.time.Instant.now()); }
}
