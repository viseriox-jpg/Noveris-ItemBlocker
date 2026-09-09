package com.noveris.itemrestrictor.restriction;
import java.util.List;
import java.util.Map;
import java.util.Set;
public record RestrictionSnapshot(Map<String, RestrictionType> items, Map<String, Set<String>> authorized, List<String> mods, List<AuditEntry> audit, int permissionLevel, boolean operatorBypass) { }
