package com.noveris.itemrestrictor.restriction;
import net.minecraft.nbt.CompoundTag;
import java.time.Instant;
import java.util.UUID;
public record AuditEntry(UUID admin, String adminName, String action, String subject, UUID affectedPlayer, long timestamp) {
    public CompoundTag save() { CompoundTag tag = new CompoundTag(); tag.putString("admin", admin.toString()); tag.putString("name", adminName); tag.putString("action", action); tag.putString("subject", subject); if (affectedPlayer != null) tag.putString("affected", affectedPlayer.toString()); tag.putLong("time", timestamp); return tag; }
    public static AuditEntry load(CompoundTag tag) { try { return new AuditEntry(UUID.fromString(tag.getString("admin")), tag.getString("name"), tag.getString("action"), tag.getString("subject"), tag.contains("affected") ? UUID.fromString(tag.getString("affected")) : null, tag.getLong("time")); } catch (Exception ignored) { return null; } }
    public static AuditEntry now(UUID admin, String name, String action, String subject, UUID affected) { return new AuditEntry(admin, name, action, subject, affected, Instant.now().toEpochMilli()); }
}
