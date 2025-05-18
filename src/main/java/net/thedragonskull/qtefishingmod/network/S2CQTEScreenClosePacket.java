package net.thedragonskull.qtefishingmod.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.thedragonskull.qtefishingmod.screen.QteScreen;

import java.util.function.Supplier;

public class S2CQTEScreenClosePacket {

    public S2CQTEScreenClosePacket() {
    }

    public S2CQTEScreenClosePacket(FriendlyByteBuf buf) {
    }

    public void encode(FriendlyByteBuf buf) {
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context ctx = contextSupplier.get();
        ctx.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.screen instanceof QteScreen) {
                mc.setScreen(null);
            }
        });

        ctx.setPacketHandled(true);
    }

}
