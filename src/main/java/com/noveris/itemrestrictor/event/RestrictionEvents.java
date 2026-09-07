package com.noveris.itemrestrictor.event;

import com.noveris.itemrestrictor.NoverisItemRestrictor;
import com.noveris.itemrestrictor.command.RestrictionCommands;
import com.noveris.itemrestrictor.network.NetworkHandler;
import com.noveris.itemrestrictor.restriction.RestrictionManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;

public final class RestrictionEvents {
    public static void onRegisterCommands(RegisterCommandsEvent event) { RestrictionCommands.register(event.getDispatcher()); }

    @SubscribeEvent public void login(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) RestrictionManager.touch(player);
    }

    @SubscribeEvent public void tick(ServerTickEvent.Post event) {
        if (event.getServer().getTickCount() % 20 != 0) return;
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            RestrictionManager.touch(player);
            clean(player, player.getMainHandItem()); clean(player, player.getOffhandItem());
            for (ItemStack stack : player.getInventory().items) clean(player, stack);
            for (ItemStack stack : player.getInventory().armor) clean(player, stack);
        }
    }

    private static void clean(ServerPlayer player, ItemStack stack) {
        if (!stack.isEmpty() && !RestrictionManager.canPossessItem(player, stack)) {
            String reason = RestrictionManager.reason(player, stack);
            stack.setCount(0);
            player.displayClientMessage(Component.literal(reason.replace('\n', ' ')), true);
        }
    }

    @SubscribeEvent public void rightItem(PlayerInteractEvent.RightClickItem event) { cancel(event, event.getItemStack()); }
    @SubscribeEvent public void rightBlock(PlayerInteractEvent.RightClickBlock event) { cancel(event, event.getItemStack()); }
    @SubscribeEvent public void leftBlock(PlayerInteractEvent.LeftClickBlock event) { cancel(event, event.getItemStack()); }
    @SubscribeEvent public void entityInteract(PlayerInteractEvent.EntityInteract event) { cancel(event, event.getItemStack()); }
    @SubscribeEvent public void attack(AttackEntityEvent event) { cancel(event, event.getEntity().getMainHandItem()); }
    @SubscribeEvent public void use(LivingEntityUseItemEvent.Start event) {
        if (event.getEntity() instanceof ServerPlayer player && !RestrictionManager.canUseItem(player, event.getItem())) { event.setCanceled(true); player.displayClientMessage(Component.literal(RestrictionManager.reason(player, event.getItem()).replace('\n', ' ')), true); }
    }

    private static void cancel(PlayerInteractEvent.RightClickItem event, ItemStack stack) {
        if (event.getEntity() instanceof ServerPlayer player && !RestrictionManager.canUseItem(player, stack)) { event.setCanceled(true); player.displayClientMessage(Component.literal(RestrictionManager.reason(player, stack).replace('\n', ' ')), true); }
    }
    private static void cancel(PlayerInteractEvent.RightClickBlock event, ItemStack stack) {
        if (event.getEntity() instanceof ServerPlayer player && !RestrictionManager.canUseItem(player, stack)) { event.setCanceled(true); player.displayClientMessage(Component.literal(RestrictionManager.reason(player, stack).replace('\n', ' ')), true); }
    }
    private static void cancel(PlayerInteractEvent.LeftClickBlock event, ItemStack stack) {
        if (event.getEntity() instanceof ServerPlayer player && !RestrictionManager.canUseItem(player, stack)) { event.setCanceled(true); player.displayClientMessage(Component.literal(RestrictionManager.reason(player, stack).replace('\n', ' ')), true); }
    }
    private static void cancel(PlayerInteractEvent.EntityInteract event, ItemStack stack) {
        if (event.getEntity() instanceof ServerPlayer player && !RestrictionManager.canUseItem(player, stack)) { event.setCanceled(true); player.displayClientMessage(Component.literal(RestrictionManager.reason(player, stack).replace('\n', ' ')), true); }
    }
    private static void cancel(AttackEntityEvent event, ItemStack stack) {
        if (event.getEntity() instanceof ServerPlayer player && !RestrictionManager.canUseItem(player, stack)) { event.setCanceled(true); player.displayClientMessage(Component.literal(RestrictionManager.reason(player, stack).replace('\n', ' ')), true); }
    }

    @SubscribeEvent public void crafted(PlayerEvent.ItemCraftedEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && !RestrictionManager.canCraftItem(player, event.getCrafting())) { event.getCrafting().setCount(0); player.displayClientMessage(Component.literal(RestrictionManager.reason(player, event.getCrafting()).replace('\n', ' ')), true); }
    }
}
