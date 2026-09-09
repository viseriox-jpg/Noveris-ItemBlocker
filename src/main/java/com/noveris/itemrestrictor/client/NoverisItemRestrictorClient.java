package com.noveris.itemrestrictor.client;

import com.noveris.itemrestrictor.NoverisItemRestrictor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import com.noveris.itemrestrictor.network.NetworkHandler;
import com.noveris.itemrestrictor.client.screen.RestrictionAdminScreen;

@Mod(value = NoverisItemRestrictor.MOD_ID, dist = Dist.CLIENT)
public final class NoverisItemRestrictorClient {
    public NoverisItemRestrictorClient() {
        NetworkHandler.registerClientHandler(payload -> RestrictionAdminScreen.open("", payload.feedback(), payload.snapshot()));
    }
}
