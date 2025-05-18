package net.thedragonskull.qtefishingmod.util;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.FishingHook;
import net.thedragonskull.qtefishingmod.network.PacketHandler;
import net.thedragonskull.qtefishingmod.network.S2CQteUpdateKeyPacket;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class QteManager {
    private static final String VALID_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final Random RANDOM = new Random();

    private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor();


    public static String getRandomQteChar() {
        int index = RANDOM.nextInt(VALID_CHARS.length());
        return String.valueOf(VALID_CHARS.charAt(index));
    }
}

