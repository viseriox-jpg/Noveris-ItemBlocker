package com.noveris.itemrestrictor.restriction;
import net.minecraft.resources.ResourceLocation;
import java.util.Set;
import java.util.UUID;
public record ItemRestriction(ResourceLocation id, RestrictionType type, Set<UUID> authorizedPlayers) { }
