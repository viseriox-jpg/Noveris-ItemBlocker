package com.noveris.itemrestrictor.restriction;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import java.util.*;

public final class RestrictionManager {
    private static final String DATA_ID = "noveris_item_restrictions";
    private RestrictionManager() { }

    public static RestrictionData data(ServerLevel level) {
        SavedData.Factory<RestrictionData> factory = new SavedData.Factory<>(RestrictionData::new, RestrictionData::load, null);
        return level.getDataStorage().computeIfAbsent(factory, DATA_ID);
    }

    public static RestrictionData data(ServerPlayer player) { return data(player.serverLevel()); }

    public static ResourceLocation id(ItemStack stack) { return BuiltInRegistries.ITEM.getKey(stack.getItem()); }
    public static boolean isModRestricted(String namespace, RestrictionData data) { return data.restrictedMods.contains(namespace); }
    public static boolean isGloballyBlocked(ResourceLocation id, RestrictionData data) { return data.itemRules.get(id.toString()) == RestrictionType.BLOCKED; }
    public static boolean isPlayerRestricted(ResourceLocation id, RestrictionData data) { return data.itemRules.get(id.toString()) == RestrictionType.PLAYER_ALLOWLIST; }
    public static boolean isAuthorizedPlayer(UUID uuid, ResourceLocation id, RestrictionData data) { return data.playerAllowlist.getOrDefault(id.toString(), Set.of()).contains(uuid); }

    public static boolean canUseItem(ServerPlayer player, ItemStack stack) {
        if (stack.isEmpty()) return true;
        RestrictionData d = data(player);
        ResourceLocation id = id(stack);
        RestrictionType type = d.itemRules.get(id.toString());
        if (type == RestrictionType.BLOCKED || (type == RestrictionType.PLAYER_ALLOWLIST && !isAuthorizedPlayer(player.getUUID(), id, d))) return false;
        return !isModRestricted(id.getNamespace(), d) || player.hasPermissions(2);
    }

    public static boolean canPossessItem(ServerPlayer player, ItemStack stack) { return canUseItem(player, stack); }
    public static boolean canCraftItem(ServerPlayer player, ItemStack result) { return canUseItem(player, result); }
    public static String reason(ServerPlayer player, ItemStack stack) {
        RestrictionData d = data(player); ResourceLocation id = id(stack);
        if (isModRestricted(id.getNamespace(), d) && !player.hasPermissions(2)) return "MOD RESTRITO\nSomente operadores podem utilizar itens deste mod.";
        if (d.itemRules.get(id.toString()) == RestrictionType.PLAYER_ALLOWLIST) return "ACESSO RESTRITO\nItem permitido somente para jogadores autorizados.";
        return "ITEM BLOQUEADO\nVocê não possui autorização para utilizar este item.";
    }

    public static void touch(ServerPlayer player) { data(player).knownPlayers.put(player.getUUID(), player.getGameProfile().getName()); data(player).setDirty(); }
    public static void audit(ServerPlayer admin, String message) { RestrictionData d = data(admin); d.auditLog.add(0, admin.getGameProfile().getName() + " " + message); while (d.auditLog.size() > 100) d.auditLog.remove(d.auditLog.size()-1); d.setDirty(); }
}
