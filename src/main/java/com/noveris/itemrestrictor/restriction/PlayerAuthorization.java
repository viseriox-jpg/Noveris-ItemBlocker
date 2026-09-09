package com.noveris.itemrestrictor.restriction;
import java.util.UUID;
public record PlayerAuthorization(UUID playerId, String itemId) { }
