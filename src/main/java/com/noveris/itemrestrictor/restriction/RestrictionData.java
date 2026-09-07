package com.noveris.itemrestrictor.restriction;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.saveddata.SavedData;
import java.util.*;

public final class RestrictionData extends SavedData {
    public final Map<String, RestrictionType> itemRules = new TreeMap<>();
    public final Map<String, Set<UUID>> playerAllowlist = new TreeMap<>();
    public final Set<String> restrictedMods = new TreeSet<>();
    public final Map<UUID, String> knownPlayers = new HashMap<>();
    public final List<String> auditLog = new ArrayList<>();

    public static RestrictionData load(CompoundTag tag) {
        RestrictionData data = new RestrictionData();
        CompoundTag items = tag.getCompound("items");
        for (String id : items.getAllKeys()) {
            try { data.itemRules.put(id, RestrictionType.valueOf(items.getString(id))); } catch (Exception ignored) { }
        }
        CompoundTag allow = tag.getCompound("allowlist");
        for (String id : allow.getAllKeys()) {
            Set<UUID> ids = new HashSet<>();
            ListTag list = allow.getList(id, Tag.TAG_STRING);
            for (Tag entry : list) try { ids.add(UUID.fromString(entry.getAsString())); } catch (IllegalArgumentException ignored) { }
            data.playerAllowlist.put(id, ids);
        }
        for (Tag entry : tag.getList("mods", Tag.TAG_STRING)) data.restrictedMods.add(entry.getAsString());
        CompoundTag players = tag.getCompound("players");
        for (String uuid : players.getAllKeys()) try { data.knownPlayers.put(UUID.fromString(uuid), players.getString(uuid)); } catch (IllegalArgumentException ignored) { }
        for (Tag entry : tag.getList("audit", Tag.TAG_STRING)) if (data.auditLog.size() < 100) data.auditLog.add(entry.getAsString());
        return data;
    }

    @Override public CompoundTag save(CompoundTag tag) {
        CompoundTag items = new CompoundTag();
        itemRules.forEach((id, type) -> items.putString(id, type.name()));
        tag.put("items", items);
        CompoundTag allow = new CompoundTag();
        playerAllowlist.forEach((id, uuids) -> { ListTag list = new ListTag(); uuids.forEach(uuid -> list.add(StringTag.valueOf(uuid.toString()))); allow.put(id, list); });
        tag.put("allowlist", allow);
        ListTag mods = new ListTag(); restrictedMods.forEach(id -> mods.add(StringTag.valueOf(id))); tag.put("mods", mods);
        CompoundTag players = new CompoundTag(); knownPlayers.forEach((uuid, name) -> players.putString(uuid.toString(), name)); tag.put("players", players);
        ListTag audit = new ListTag(); auditLog.stream().limit(100).forEach(line -> audit.add(StringTag.valueOf(line))); tag.put("audit", audit);
        return tag;
    }
}

