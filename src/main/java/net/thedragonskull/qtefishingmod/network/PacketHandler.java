package net.thedragonskull.qtefishingmod.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.thedragonskull.qtefishingmod.QteFishingMod;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    private static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(QteFishingMod.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    private static int id = 0;

    public static void register() {

        INSTANCE.messageBuilder(C2SQTEPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(C2SQTEPacket::encode)
                .decoder(C2SQTEPacket::new)
                .consumerMainThread(C2SQTEPacket::handle)
                .add();

        INSTANCE.messageBuilder(S2CQTEStartPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(S2CQTEStartPacket::encode)
                .decoder(S2CQTEStartPacket::new)
                .consumerMainThread(S2CQTEStartPacket::handle)
                .add();

        INSTANCE.messageBuilder(S2CQTEScreenClosePacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(S2CQTEScreenClosePacket::encode)
                .decoder(S2CQTEScreenClosePacket::new)
                .consumerMainThread(S2CQTEScreenClosePacket::handle)
                .add();

        INSTANCE.messageBuilder(S2CQteUpdateKeyPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(S2CQteUpdateKeyPacket::encode)
                .decoder(S2CQteUpdateKeyPacket::new)
                .consumerMainThread(S2CQteUpdateKeyPacket::handle)
                .add();

        INSTANCE.messageBuilder(S2CPlayFailSoundPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(S2CPlayFailSoundPacket::encode)
                .decoder(S2CPlayFailSoundPacket::new)
                .consumerMainThread(S2CPlayFailSoundPacket::handle)
                .add();

        INSTANCE.messageBuilder(C2SQTEWordPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(C2SQTEWordPacket::encode)
                .decoder(C2SQTEWordPacket::new)
                .consumerMainThread(C2SQTEWordPacket::handle)
                .add();

        INSTANCE.messageBuilder(S2CQteUpdateWordPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(S2CQteUpdateWordPacket::encode)
                .decoder(S2CQteUpdateWordPacket::new)
                .consumerMainThread(S2CQteUpdateWordPacket::handle)
                .add();

        INSTANCE.messageBuilder(S2CQteProgressPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(S2CQteProgressPacket::encode)
                .decoder(S2CQteProgressPacket::new)
                .consumerMainThread(S2CQteProgressPacket::handle)
                .add();

        INSTANCE.messageBuilder(S2CPlaySuccessSoundPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(S2CPlaySuccessSoundPacket::encode)
                .decoder(S2CPlaySuccessSoundPacket::new)
                .consumerMainThread(S2CPlaySuccessSoundPacket::handle)
                .add();
    }


    public static void sendToServer(Object msg) {
        INSTANCE.send(PacketDistributor.SERVER.noArg(), msg);
    }

    public static void sendToPlayer(Object msg, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), msg);
    }

}
