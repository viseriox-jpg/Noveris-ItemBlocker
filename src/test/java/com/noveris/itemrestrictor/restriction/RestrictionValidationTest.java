package com.noveris.itemrestrictor.restriction;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class RestrictionValidationTest {
    @Test void namespacesAndUuidsAreValidatedWithoutExceptions() {
        assertTrue(RestrictionManager.validNamespace("minecraft"));
        assertTrue(RestrictionManager.validNamespace("my_mod.2"));
        assertFalse(RestrictionManager.validNamespace("Minecraft"));
        assertFalse(RestrictionManager.validNamespace("minecraft:item"));
        assertDoesNotThrow(() -> UUID.fromString("00000000-0000-0000-0000-000000000000"));
        assertThrows(IllegalArgumentException.class, () -> UUID.fromString("not-a-uuid"));
    }
}
