package net.thedragonskull.qtefishingmod.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.thedragonskull.qtefishingmod.screen.QteScreen;
import net.thedragonskull.qtefishingmod.screen.QteWordScreen;

import java.util.function.Supplier;

public class S2CQTEStartPacket {
    private final String expectedKey;
    private final boolean isWord;

    public S2CQTEStartPacket(String expectedKey, boolean isWord) {
        this.expectedKey = expectedKey;
        this.isWord = isWord;
    }

    public S2CQTEStartPacket(FriendlyByteBuf buf) {
        this.expectedKey = buf.readUtf();
        this.isWord = buf.readBoolean();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(expectedKey);
        buf.writeBoolean(isWord);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (isWord) {
                mc.setScreen(new QteWordScreen(expectedKey));
            } else {
                mc.setScreen(new QteScreen(expectedKey));
            }
        });

        contextSupplier.get().setPacketHandled(true);
    }
}
