package com.noveris.itemrestrictor.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.noveris.itemrestrictor.network.NetworkHandler;
import com.noveris.itemrestrictor.restriction.*;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import java.util.UUID;

public final class RestrictionCommands {
    private RestrictionCommands() { }
    public static void register(CommandDispatcher<CommandSourceStack> d) {
        var root = Commands.literal("itemrestrict").requires(s -> s.hasPermission(2));
        root.then(Commands.literal("open").executes(c -> { NetworkHandler.sendOpen(c.getSource().getPlayerOrException()); return 1; }));
        var item = Commands.literal("item");
        item.then(Commands.literal("block").then(itemArg((s,id) -> changeItem(s,id,RestrictionType.BLOCKED))));
        item.then(Commands.literal("unblock").then(itemArg(RestrictionCommands::removeItem)));
        item.then(Commands.literal("remove").then(itemArg(RestrictionCommands::removeItem)));
        item.then(Commands.literal("allow").then(Commands.argument("item", StringArgumentType.word()).suggests((c,b) -> SharedSuggestionProvider.suggestResource(BuiltInRegistries.ITEM.keySet(), b)).then(Commands.argument("player", StringArgumentType.word()).executes(c -> authorize(c.getSource(), StringArgumentType.getString(c,"item"), StringArgumentType.getString(c,"player"), true)))));
        item.then(Commands.literal("deny").then(Commands.argument("item", StringArgumentType.word()).suggests((c,b) -> SharedSuggestionProvider.suggestResource(BuiltInRegistries.ITEM.keySet(), b)).then(Commands.argument("player", StringArgumentType.word()).executes(c -> authorize(c.getSource(), StringArgumentType.getString(c,"item"), StringArgumentType.getString(c,"player"), false)))));
        root.then(item);
        root.then(Commands.literal("mod").then(Commands.literal("block").then(modArg(true))).then(Commands.literal("unblock").then(modArg(false))));
        root.then(Commands.literal("list").executes(c -> list(c.getSource())));
        root.then(Commands.literal("reload").executes(c -> { c.getSource().sendSuccess(() -> Component.translatable("noveris_item_restrictor.command.reloaded"), true); return 1; }));
        root.then(Commands.literal("audit").executes(c -> audit(c.getSource())));
        d.register(root);
    }
    private static com.mojang.brigadier.builder.RequiredArgumentBuilder<CommandSourceStack,String> itemArg(java.util.function.BiFunction<CommandSourceStack,String,Integer> fn) { return Commands.argument("item", StringArgumentType.word()).suggests((c,b) -> SharedSuggestionProvider.suggestResource(BuiltInRegistries.ITEM.keySet(), b)).executes(c -> fn.apply(c.getSource(), StringArgumentType.getString(c,"item"))); }
    private static com.mojang.brigadier.builder.RequiredArgumentBuilder<CommandSourceStack,String> modArg(boolean block) { return Commands.argument("modid", StringArgumentType.word()).executes(c -> { String m = StringArgumentType.getString(c,"modid").toLowerCase(java.util.Locale.ROOT); if (!RestrictionManager.validNamespace(m)) { c.getSource().sendFailure(Component.translatable("noveris_item_restrictor.command.invalid_mod")); return 0; } ServerPlayer p = c.getSource().getPlayer(); if (p != null) RestrictionManager.setModRule(p,m,block); c.getSource().sendSuccess(() -> Component.translatable(block ? "noveris_item_restrictor.command.mod_blocked" : "noveris_item_restrictor.command.mod_unblocked", m), true); return 1; }); }
    private static int changeItem(CommandSourceStack s, String raw, RestrictionType type) { try { ResourceLocation id = ResourceLocation.parse(raw); if (!BuiltInRegistries.ITEM.containsKey(id)) throw new IllegalArgumentException(); ServerPlayer p = s.getPlayer(); if (p != null) { RestrictionManager.setItemRule(p,id,type); RestrictionManager.audit(p,"item_rule",id.toString(),null); } s.sendSuccess(() -> Component.translatable("noveris_item_restrictor.command.saved", id), true); return 1; } catch (Exception e) { s.sendFailure(Component.translatable("noveris_item_restrictor.command.invalid_item", raw)); return 0; } }
    private static int removeItem(CommandSourceStack s, String raw) { try { ResourceLocation id = ResourceLocation.parse(raw); ServerPlayer p=s.getPlayer(); if (p != null) RestrictionManager.removeItemRule(p,id); s.sendSuccess(() -> Component.translatable("noveris_item_restrictor.command.removed",id), true); return 1; } catch (Exception e) { s.sendFailure(Component.translatable("noveris_item_restrictor.command.invalid_item",raw)); return 0; } }
    private static int authorize(CommandSourceStack s, String raw, String playerRaw, boolean allow) { try { ResourceLocation id=ResourceLocation.parse(raw); UUID uuid=UUID.fromString(playerRaw); ServerPlayer admin=s.getPlayer(); if (admin == null) return 0; RestrictionData d=RestrictionManager.data(admin); if (allow) d.playerAllowlist.computeIfAbsent(id.toString(), k -> new java.util.HashSet<>()).add(uuid); else d.playerAllowlist.getOrDefault(id.toString(), java.util.Set.of()).remove(uuid); d.itemRules.put(id.toString(),RestrictionType.PLAYER_ALLOWLIST); d.setDirty(); RestrictionManager.audit(admin,allow?"authorize":"deny",id.toString(),uuid); s.sendSuccess(() -> Component.translatable(allow?"noveris_item_restrictor.command.authorized":"noveris_item_restrictor.command.denied",uuid),true); return 1; } catch(Exception e) { s.sendFailure(Component.translatable("noveris_item_restrictor.command.invalid_uuid")); return 0; } }
    private static int list(CommandSourceStack s) { ServerPlayer p=s.getPlayer(); if(p==null)return 0; RestrictionData d=RestrictionManager.data(p); s.sendSuccess(() -> Component.translatable("noveris_item_restrictor.command.list_header",d.itemRules.size()),false); d.itemRules.forEach((id,t)->s.sendSuccess(()->Component.literal(id+" | "+t+" | "+d.playerAllowlist.getOrDefault(id,java.util.Set.of()).size()),false)); d.restrictedMods.forEach(m->s.sendSuccess(()->Component.literal("mod:"+m+" | RESTRICTED"),false)); return d.itemRules.size(); }
    private static int audit(CommandSourceStack s) { ServerPlayer p=s.getPlayer(); if(p==null)return 0; RestrictionManager.data(p).auditLog.stream().limit(20).forEach(a -> s.sendSuccess(() -> Component.literal(a.adminName()+" | "+a.action()+" | "+a.subject()+" | "+a.timestamp()), false)); return 1; }
}
