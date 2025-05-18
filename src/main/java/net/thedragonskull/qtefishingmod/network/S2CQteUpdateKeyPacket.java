package net.thedragonskull.qtefishingmod.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.network.NetworkEvent;
import net.thedragonskull.qtefishingmod.screen.QteScreen;

import java.util.function.Supplier;

public class S2CQteUpdateKeyPacket {
    private final String key;

    public S2CQteUpdateKeyPacket(String key) {
        this.key = key;
    }

    public S2CQteUpdateKeyPacket(FriendlyByteBuf buf) {
        this.key = buf.readUtf();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(key);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();

            if (minecraft.screen instanceof QteScreen screen) {
                screen.setCurrentKey(this.key);

                minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_PLING.get(), 1.0F, 1.5F));
            }

        });
        contextSupplier.get().setPacketHandled(true);
    }

}
