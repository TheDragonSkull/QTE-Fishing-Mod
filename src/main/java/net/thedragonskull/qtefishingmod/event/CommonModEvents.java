package net.thedragonskull.qtefishingmod.event;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.thedragonskull.qtefishingmod.QteFishingMod;
import net.thedragonskull.qtefishingmod.network.PacketHandler;

@Mod.EventBusSubscriber(modid = QteFishingMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CommonModEvents {

    @SubscribeEvent
    public static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            event.enqueueWork(PacketHandler::register);
        });
    }

}
