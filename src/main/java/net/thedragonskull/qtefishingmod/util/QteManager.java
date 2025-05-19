package net.thedragonskull.qtefishingmod.util;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.thedragonskull.qtefishingmod.config.QteCommonConfigs;
import net.thedragonskull.qtefishingmod.mixin.FishingHookAccessor;
import net.thedragonskull.qtefishingmod.network.PacketHandler;
import net.thedragonskull.qtefishingmod.network.S2CPlayFailSoundPacket;
import net.thedragonskull.qtefishingmod.network.S2CQTEScreenClosePacket;

import java.util.List;
import java.util.Random;

public class QteManager {
    private static final String VALID_CHARS = QteCommonConfigs.VALID_CHARS.get();
    private static final Random RANDOM = new Random();

    public static String getRandomQteChar() {
        int index = RANDOM.nextInt(VALID_CHARS.length());
        return String.valueOf(VALID_CHARS.charAt(index));
    }

    public static void retrieveAndDamageRod(ServerPlayer player, FishingHook hook, int damage) {
        ItemStack main = player.getMainHandItem();
        ItemStack off = player.getOffhandItem();

        InteractionHand hand;
        ItemStack rod;

        if (main.getItem() instanceof FishingRodItem) {
            hand = InteractionHand.MAIN_HAND;
            rod = main;
        } else if (off.getItem() instanceof FishingRodItem) {
            hand = InteractionHand.OFF_HAND;
            rod = off;
        } else {
            return;
        }

        rod.hurtAndBreak(damage, player, p -> p.broadcastBreakEvent(hand));
        hook.remove(Entity.RemovalReason.KILLED);
    }

    public static ItemStack generateFishingLoot(ServerPlayer player, FishingHook hook) {
        ServerLevel level = player.serverLevel();
        LootParams lootparams = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, hook.position())
                .withParameter(LootContextParams.TOOL, player.getMainHandItem())
                .withParameter(LootContextParams.THIS_ENTITY, hook)
                .withParameter(LootContextParams.KILLER_ENTITY, player)
                .withLuck(((FishingHookAccessor)hook).getLuck() + player.getLuck())
                .create(LootContextParamSets.FISHING);

        LootTable table = level.getServer().getLootData().getLootTable(BuiltInLootTables.FISHING);
        List<ItemStack> loot = table.getRandomItems(lootparams);

        return loot.isEmpty() ? ItemStack.EMPTY : loot.get(0);
    }

    public static void handleQteTimeout(FishingHook hook, IFishingHookQte qte) {
        if (!qte.isQteActive()) return;
        if (!(hook.getOwner() instanceof ServerPlayer player) || !(hook.level() instanceof ServerLevel level)) return;

        long elapsed = level.getGameTime() - qte.getQteStartTime();
        if (elapsed >= QteCommonConfigs.QTE_TIMEOUT_TICKS.get()) {
            qte.cancelQte();
            qte.setQteHandled(true);
            retrieveAndDamageRod(player, hook, 1);
            PacketHandler.sendToPlayer(new S2CQTEScreenClosePacket(), player);
            PacketHandler.sendToPlayer(new S2CPlayFailSoundPacket(), player);
            player.level().playSound(null, hook.getX(), hook.getY(), hook.getZ(),
                    SoundEvents.FISH_SWIM, player.getSoundSource(), 1.0F, 1.5F);
        }
    }



}

