package com.noveris.itemrestrictor;

import com.noveris.itemrestrictor.command.RestrictionCommands;
import com.noveris.itemrestrictor.event.RestrictionEvents;
import com.noveris.itemrestrictor.network.NetworkHandler;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

@Mod(NoverisItemRestrictor.MOD_ID)
public final class NoverisItemRestrictor {
    public static final String MOD_ID = "noveris_item_restrictor";

    public NoverisItemRestrictor(IEventBus modBus) {
        NetworkHandler.register(modBus);
        NeoForge.EVENT_BUS.addListener(RestrictionEvents::onRegisterCommands);
        NeoForge.EVENT_BUS.register(new RestrictionEvents());
    }
}

