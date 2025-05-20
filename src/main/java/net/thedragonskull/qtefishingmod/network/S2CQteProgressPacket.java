package net.thedragonskull.qtefishingmod.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.thedragonskull.qtefishingmod.screen.QteWordScreen;

import java.util.function.Supplier;

public class S2CQteProgressPacket {
    private final int index;

    public S2CQteProgressPacket(int index) {
        this.index = index;
    }

    public S2CQteProgressPacket(FriendlyByteBuf buf) {
        this.index = buf.readInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(index);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (Minecraft.getInstance().screen instanceof QteWordScreen screen) {
                screen.setCurrentIndex(index);
            }
        });
        context.setPacketHandled(true);
    }
}

