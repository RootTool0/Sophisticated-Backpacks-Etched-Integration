package com.absolutebuddies.sophisticatedbackpacksetchedintegration;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.p3pp3rf1y.sophisticatedcore.upgrades.jukebox.DiscHandlerRegistry;

@Mod("sophisticatedbackpacksetchedintegration")
public class SophisticatedBackpacksEtchedIntegration
{
    public SophisticatedBackpacksEtchedIntegration() { FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup); }

    private void setup(final FMLCommonSetupEvent event)
    {
        event.enqueueWork(() -> { DiscHandlerRegistry.registerHandler(new EtchedDiscHandler()); });
    }
}
