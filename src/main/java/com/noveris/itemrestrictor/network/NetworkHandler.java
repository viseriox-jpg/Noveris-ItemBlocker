package com.noveris.itemrestrictor.network;

import com.noveris.itemrestrictor.NoverisItemRestrictor;
import com.noveris.itemrestrictor.restriction.RestrictionData;
import com.noveris.itemrestrictor.restriction.RestrictionManager;
import com.noveris.itemrestrictor.client.screen.RestrictionAdminScreen;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class NetworkHandler {
    private static final ResourceLocation VERSION = ResourceLocation.fromNamespaceAndPath(NoverisItemRestrictor.MOD_ID, "main");
    private NetworkHandler() { }
    public static void register(IEventBus bus) { bus.addListener(NetworkHandler::registerPayloads); }
    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar r = event.registrar("1");
        r.playToClient(OpenScreen.TYPE, OpenScreen.CODEC, (payload, ctx) -> ctx.enqueueWork(() -> RestrictionAdminScreen.open(payload.adminName())));
        r.playToServer(Action.TYPE, Action.CODEC, (payload, ctx) -> ctx.enqueueWork(() -> handle(ctx, payload)));
    }
    public static void sendOpen(ServerPlayer player) { PacketDistributor.sendToPlayer(player, new OpenScreen(player.getGameProfile().getName())); }
    public static void send(Action action) { PacketDistributor.sendToServer(action); }
    private static void handle(net.neoforged.neoforge.network.handling.IPayloadContext ctx, Action payload) { if (ctx.player() instanceof ServerPlayer player && player.hasPermissions(2)) apply(player, payload); }
    private static void apply(ServerPlayer player, Action action) {
        RestrictionData data = RestrictionManager.data(player);
        try {
            if (action.action().equals("add_block")) data.itemRules.put(ResourceLocation.parse(action.value()).toString(), com.noveris.itemrestrictor.restriction.RestrictionType.BLOCKED);
            else if (action.action().equals("add_allowlist")) { String id = ResourceLocation.parse(action.value()).toString(); data.itemRules.put(id, com.noveris.itemrestrictor.restriction.RestrictionType.PLAYER_ALLOWLIST); data.playerAllowlist.computeIfAbsent(id, k -> new java.util.HashSet<>()); }
            else if (action.action().equals("remove_item")) { String id = ResourceLocation.parse(action.value()).toString(); data.itemRules.remove(id); data.playerAllowlist.remove(id); }
            else if (action.action().equals("block_mod")) data.restrictedMods.add(action.value().toLowerCase(java.util.Locale.ROOT));
            else if (action.action().equals("unblock_mod")) data.restrictedMods.remove(action.value().toLowerCase(java.util.Locale.ROOT));
            else return;
            data.setDirty(); RestrictionManager.audit(player, "changed restriction " + action.action() + " " + action.value()); sendOpen(player);
        } catch (Exception ignored) { }
    }

    public record OpenScreen(String adminName) implements CustomPacketPayload {
        public static final Type<OpenScreen> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(NoverisItemRestrictor.MOD_ID, "open_screen"));
        public static final StreamCodec<RegistryFriendlyByteBuf, OpenScreen> CODEC = StreamCodec.composite(ByteBufCodecs.STRING_UTF8, OpenScreen::adminName, OpenScreen::new);
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }
    public record Action(String action, String value) implements CustomPacketPayload {
        public static final Type<Action> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(NoverisItemRestrictor.MOD_ID, "action"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Action> CODEC = StreamCodec.composite(ByteBufCodecs.STRING_UTF8, Action::action, ByteBufCodecs.STRING_UTF8, Action::value, Action::new);
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }
}
