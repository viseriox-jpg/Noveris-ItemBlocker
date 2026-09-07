package com.noveris.itemrestrictor.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.noveris.itemrestrictor.network.NetworkHandler;
import com.noveris.itemrestrictor.restriction.*;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;

public final class RestrictionCommands {
    private RestrictionCommands() { }
    public static void register(CommandDispatcher<CommandSourceStack> d) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("itemrestrict").requires(s -> s.hasPermission(2));
        root.then(Commands.literal("open").executes(c -> { ServerPlayer p = c.getSource().getPlayerOrException(); NetworkHandler.sendOpen(p); return 1; }));
        root.then(Commands.literal("item").then(Commands.literal("block").then(
            Commands.argument("item", StringArgumentType.word()).suggests((c,b) -> SharedSuggestionProvider.suggestResource(BuiltInRegistries.ITEM.keySet(), b))
                .executes(c -> item(c.getSource(), StringArgumentType.getString(c, "item"), RestrictionType.BLOCKED)))));
        root.then(Commands.literal("allow").then(Commands.argument("item", StringArgumentType.word()).then(
            Commands.argument("player", StringArgumentType.word()).executes(c -> allow(c.getSource(), StringArgumentType.getString(c, "item"), StringArgumentType.getString(c, "player"))))));
        root.then(Commands.literal("mod").then(Commands.literal("block").then(
            Commands.argument("modid", StringArgumentType.word()).executes(c -> mod(c.getSource(), StringArgumentType.getString(c, "modid"), true))))
            .then(Commands.literal("unblock").then(Commands.argument("modid", StringArgumentType.word()).executes(c -> mod(c.getSource(), StringArgumentType.getString(c, "modid"), false)))));
        root.then(Commands.literal("list").executes(c -> { c.getSource().sendSuccess(() -> Component.literal("Items: " + RestrictionManager.data(c.getSource().getLevel()).itemRules.size()), false); return 1; }));
        d.register(root);
    }
    private static int item(CommandSourceStack s, String raw, RestrictionType type) { try { ResourceLocation id = ResourceLocation.parse(raw); if (!BuiltInRegistries.ITEM.containsKey(id)) throw new IllegalArgumentException(); RestrictionData d = RestrictionManager.data(s.getLevel()); d.itemRules.put(id.toString(), type); if (type == RestrictionType.PLAYER_ALLOWLIST) d.playerAllowlist.computeIfAbsent(id.toString(), k -> new java.util.HashSet<>()); d.setDirty(); s.sendSuccess(() -> Component.literal("Rule saved for " + id), true); return 1; } catch (Exception e) { s.sendFailure(Component.literal("Invalid item: " + raw)); return 0; } }
    private static int allow(CommandSourceStack s, String raw, String name) { ServerPlayer target = s.getServer().getPlayerList().getPlayerByName(name); if (target == null) { s.sendFailure(Component.literal("Player must be online for this command.")); return 0; } ResourceLocation id = ResourceLocation.parse(raw); RestrictionData d = RestrictionManager.data(s.getLevel()); d.itemRules.put(id.toString(), RestrictionType.PLAYER_ALLOWLIST); d.playerAllowlist.computeIfAbsent(id.toString(), k -> new java.util.HashSet<>()).add(target.getUUID()); d.setDirty(); s.sendSuccess(() -> Component.literal("Player authorized."), true); return 1; }
    private static int mod(CommandSourceStack s, String mod, boolean block) { RestrictionData d = RestrictionManager.data(s.getLevel()); if (block) d.restrictedMods.add(mod.toLowerCase(java.util.Locale.ROOT)); else d.restrictedMods.remove(mod.toLowerCase(java.util.Locale.ROOT)); d.setDirty(); s.sendSuccess(() -> Component.literal("Mod rule saved."), true); return 1; }
}
