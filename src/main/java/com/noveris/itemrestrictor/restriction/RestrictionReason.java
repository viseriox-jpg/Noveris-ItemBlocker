package com.noveris.itemrestrictor.restriction;
public record RestrictionReason(Kind kind, String translationKey, String subject) {
    public enum Kind { NONE, ITEM_BLOCKED, ALLOWLIST, MOD_BLOCKED, INVALID }
    public static final RestrictionReason NONE = new RestrictionReason(Kind.NONE, "", "");
}
