package net.thedragonskull.qtefishingmod.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.network.NetworkEvent;
import net.thedragonskull.qtefishingmod.screen.QteWordScreen;

import java.util.function.Supplier;

public class S2CQteUpdateWordPacket {
    private final String word;

    public S2CQteUpdateWordPacket(String word) {
        this.word = word;
    }

    public S2CQteUpdateWordPacket(FriendlyByteBuf buf) {
        this.word = buf.readUtf();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(word);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();

            if (minecraft.screen instanceof QteWordScreen screen) {
                screen.setFullWord(word);
                //screen.setCurrentKey(String.valueOf(word.charAt(0))); // empieza por la primera letra
                minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_PLING.get(), 1.0F, 1.5F));
            }
        });

        contextSupplier.get().setPacketHandled(true);
    }
}

