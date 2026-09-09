package com.noveris.itemrestrictor.network;

import com.mojang.logging.LogUtils;
import com.noveris.itemrestrictor.NoverisItemRestrictor;
import com.noveris.itemrestrictor.restriction.RestrictionManager;
import com.noveris.itemrestrictor.restriction.RestrictionType;
import net.minecraft.core.registries.BuiltInRegistries;
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
import org.slf4j.Logger;
import java.util.Locale;
import java.util.function.Consumer;

/** Typed payloads. Common code contains no client class references. */
public final class NetworkHandler {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_TEXT = 256;
    private static Consumer<OpenScreen> clientOpen;
    private NetworkHandler() { }
    public static void register(IEventBus bus) { bus.addListener(NetworkHandler::registerPayloads); }
    public static void registerClientHandler(Consumer<OpenScreen> handler) { clientOpen = handler; }
    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar r = event.registrar("2");
        r.playToClient(OpenScreen.TYPE, OpenScreen.CODEC, (payload, ctx) -> { if (payload.valid()) ctx.enqueueWork(() -> { if (clientOpen != null) clientOpen.accept(payload); }); });
        r.playToServer(Action.TYPE, Action.CODEC, (payload, ctx) -> ctx.enqueueWork(() -> { if (ctx.player() instanceof ServerPlayer p) handle(p, payload); }));
    }
    public static void sendOpen(ServerPlayer p) { if (p.hasPermissions(RestrictionManager.data(p).permissionLevel)) PacketDistributor.sendToPlayer(p, new OpenScreen(RestrictionManager.snapshot(p), "")); }
    public static void sendAction(Action action) { if (action.valid()) PacketDistributor.sendToServer(action); }
    private static void handle(ServerPlayer p, Action a) {
        var d = RestrictionManager.data(p); if (!p.hasPermissions(d.permissionLevel) || !a.valid()) return;
        try {
            switch (a.action()) {
                case "block_item", "allow_item" -> { ResourceLocation id = ResourceLocation.parse(a.value()); if (!BuiltInRegistries.ITEM.containsKey(id)) return; RestrictionManager.setItemRule(p, id, a.action().equals("block_item") ? RestrictionType.BLOCKED : RestrictionType.PLAYER_ALLOWLIST); RestrictionManager.audit(p, a.action(), id.toString(), null); }
                case "remove_item" -> { ResourceLocation id = ResourceLocation.parse(a.value()); if (RestrictionManager.removeItemRule(p, id)) RestrictionManager.audit(p, a.action(), id.toString(), null); }
                case "block_mod", "unblock_mod" -> { if (!RestrictionManager.validNamespace(a.value())) return; RestrictionManager.setModRule(p, a.value().toLowerCase(Locale.ROOT), a.action().equals("block_mod")); RestrictionManager.audit(p, a.action(), a.value(), null); }
                default -> { return; }
            }
            sendOpen(p);
        } catch (RuntimeException ex) { LOGGER.debug("Rejected malformed Noveris payload", ex); }
    }
    public record OpenScreen(String snapshot, String feedback) implements CustomPacketPayload {
        public static final Type<OpenScreen> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(NoverisItemRestrictor.MOD_ID, "open_screen"));
        public static final StreamCodec<RegistryFriendlyByteBuf, OpenScreen> CODEC = StreamCodec.composite(ByteBufCodecs.STRING_UTF8, OpenScreen::snapshot, ByteBufCodecs.STRING_UTF8, OpenScreen::feedback, OpenScreen::new);
        public OpenScreen(com.noveris.itemrestrictor.restriction.RestrictionSnapshot s, String feedback) { this(s.items().keySet().stream().sorted().reduce("", (a,b) -> a.isEmpty() ? b : a + "," + b), feedback); }
        boolean valid() { return snapshot.length() <= 65536 && feedback.length() <= MAX_TEXT; }
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }
    public record Action(String action, String value) implements CustomPacketPayload {
        public static final Type<Action> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(NoverisItemRestrictor.MOD_ID, "action"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Action> CODEC = StreamCodec.composite(ByteBufCodecs.STRING_UTF8, Action::action, ByteBufCodecs.STRING_UTF8, Action::value, Action::new);
        boolean valid() { return action.length() <= 32 && value.length() <= MAX_TEXT && action.matches("[a-z_]+") && !value.contains("\u0000"); }
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }
}
