package net.thedragonskull.qtefishingmod.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.thedragonskull.qtefishingmod.screen.QteScreen;

import java.util.function.Supplier;

public class S2CQTEStartPacket {
    private final String expectedKey;

    public S2CQTEStartPacket(String expectedKey) {
        this.expectedKey = expectedKey;
    }

    public S2CQTEStartPacket(FriendlyByteBuf buf) {
        this.expectedKey = buf.readUtf();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(expectedKey);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context ctx = contextSupplier.get();
        ctx.enqueueWork(() -> {
            Minecraft.getInstance().setScreen(new QteScreen(this.expectedKey));
        });

        ctx.setPacketHandled(true);
    }
}
