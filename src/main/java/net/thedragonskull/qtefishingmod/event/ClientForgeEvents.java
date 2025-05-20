package net.thedragonskull.qtefishingmod.event;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.thedragonskull.qtefishingmod.QteFishingMod;
import net.thedragonskull.qtefishingmod.network.C2SQTEPacket;
import net.thedragonskull.qtefishingmod.network.C2SQTEWordPacket;
import net.thedragonskull.qtefishingmod.network.PacketHandler;
import net.thedragonskull.qtefishingmod.util.QteManager;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = QteFishingMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientForgeEvents {

    @SubscribeEvent
    public static void onInputEvent(InputEvent.Key event) {
        if (event.getAction() != GLFW.GLFW_PRESS) return;

        int keyCode = event.getKey();
        String keyString = InputConstants.getKey(keyCode, 0).getDisplayName().getString();

        if (keyString.matches("^[A-Z0-9]$")) {
            if (Minecraft.getInstance().getConnection() != null) {
                if (QteManager.isHardModeEnabled()) {
                    PacketHandler.sendToServer(new C2SQTEWordPacket(keyString));
                } else {
                    PacketHandler.sendToServer(new C2SQTEPacket(keyString));
                }
            }
        }

    }
}
