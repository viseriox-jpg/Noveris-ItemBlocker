package com.noveris.itemrestrictor.restriction;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.saveddata.SavedData;
import java.util.*;

/** Persisted state; callers must obtain it from the Overworld storage. */
public final class RestrictionData extends SavedData {
    public static final int MAX_AUDIT_ENTRIES = 200;
    public final Map<String, RestrictionType> itemRules = new TreeMap<>();
    public final Map<String, Set<UUID>> playerAllowlist = new TreeMap<>();
    public final Set<String> restrictedMods = new TreeSet<>();
    public final Map<UUID, String> knownPlayers = new TreeMap<>();
    public final List<AuditEntry> auditLog = new ArrayList<>();
    public int permissionLevel = 2;
    public boolean operatorBypass = true;
    public int scanIntervalTicks = 40;
    public RemovalPolicy removalPolicy = RemovalPolicy.ADMIN_RETURN;
    public enum RemovalPolicy { DROP, ADMIN_RETURN }

    public static RestrictionData load(CompoundTag tag, HolderLookup.Provider provider) {
        RestrictionData data = new RestrictionData();
        CompoundTag items = tag.getCompound("items");
        for (String id : items.getAllKeys()) try { if (RestrictionManager.validItemId(id)) data.itemRules.put(id, RestrictionType.valueOf(items.getString(id))); } catch (Exception ignored) { }
        CompoundTag allow = tag.getCompound("allowlist");
        for (String id : allow.getAllKeys()) {
            if (!data.itemRules.containsKey(id)) continue;
            Set<UUID> ids = new HashSet<>();
            for (Tag entry : allow.getList(id, Tag.TAG_STRING)) try { ids.add(UUID.fromString(entry.getAsString())); } catch (IllegalArgumentException ignored) { }
            data.playerAllowlist.put(id, ids);
        }
        for (Tag entry : tag.getList("mods", Tag.TAG_STRING)) if (RestrictionManager.validNamespace(entry.getAsString())) data.restrictedMods.add(entry.getAsString());
        CompoundTag players = tag.getCompound("players");
        for (String uuid : players.getAllKeys()) try { String name = players.getString(uuid); if (name.length() <= 64) data.knownPlayers.put(UUID.fromString(uuid), name); } catch (IllegalArgumentException ignored) { }
        for (Tag entry : tag.getList("audit", Tag.TAG_COMPOUND)) { AuditEntry audit = AuditEntry.load((CompoundTag) entry); if (audit != null) data.auditLog.add(audit); }
        data.permissionLevel = Math.max(0, Math.min(4, tag.contains("permissionLevel") ? tag.getInt("permissionLevel") : 2));
        data.operatorBypass = !tag.contains("operatorBypass") || tag.getBoolean("operatorBypass");
        data.scanIntervalTicks = Math.max(20, Math.min(72000, tag.contains("scanIntervalTicks") ? tag.getInt("scanIntervalTicks") : 40));
        try { data.removalPolicy = RemovalPolicy.valueOf(tag.getString("removalPolicy")); } catch (Exception ignored) { }
        return data;
    }
    @Override public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        CompoundTag items = new CompoundTag(); itemRules.forEach((id, type) -> items.putString(id, type.name())); tag.put("items", items);
        CompoundTag allow = new CompoundTag(); playerAllowlist.forEach((id, uuids) -> { ListTag list = new ListTag(); uuids.forEach(uuid -> list.add(StringTag.valueOf(uuid.toString()))); allow.put(id, list); }); tag.put("allowlist", allow);
        ListTag mods = new ListTag(); restrictedMods.forEach(id -> mods.add(StringTag.valueOf(id))); tag.put("mods", mods);
        CompoundTag players = new CompoundTag(); knownPlayers.forEach((uuid, name) -> players.putString(uuid.toString(), name)); tag.put("players", players);
        ListTag audit = new ListTag(); auditLog.stream().limit(MAX_AUDIT_ENTRIES).forEach(entry -> audit.add(entry.save())); tag.put("audit", audit);
        tag.putInt("permissionLevel", permissionLevel); tag.putBoolean("operatorBypass", operatorBypass); tag.putInt("scanIntervalTicks", scanIntervalTicks); tag.putString("removalPolicy", removalPolicy.name());
        return tag;
    }
    public boolean remember(UUID uuid, String name) { return name != null && name.length() <= 64 && !name.equals(knownPlayers.put(uuid, name)); }
    public void audit(AuditEntry entry) { auditLog.add(0, entry); if (auditLog.size() > MAX_AUDIT_ENTRIES) auditLog.remove(auditLog.size() - 1); }
}
