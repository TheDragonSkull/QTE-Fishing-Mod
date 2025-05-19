package net.thedragonskull.qtefishingmod.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.thedragonskull.qtefishingmod.util.IFishingHookQte;
import net.thedragonskull.qtefishingmod.util.QteManager;

import java.util.function.Supplier;

public class C2SQTEPacket {
    private final String key;

    public C2SQTEPacket(String key) {
        this.key = key;
    }

    public C2SQTEPacket(FriendlyByteBuf buf) {
        this.key = buf.readUtf();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(key);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null && player.fishing instanceof FishingHook) {
                FishingHook hook = player.fishing;
                IFishingHookQte qte = (IFishingHookQte) hook;
                if (qte.isQteActive()) {
                    if (qte.getExpectedKey().equalsIgnoreCase(this.key)) {
                        qte.incrementQteSuccessCount();

                        if (qte.getQteSuccessCount() >= qte.getMaxQteSuccess()) {

                            //Finish
                            ServerLevel level = player.serverLevel();
                            ItemStack loot = qte.getQteLoot();
                            ItemEntity itemEntity = new ItemEntity(level, hook.getX(), hook.getY(), hook.getZ(), loot.copy());

                            double dx = player.getX() - hook.getX();
                            double dy = player.getY() - hook.getY();
                            double dz = player.getZ() - hook.getZ();
                            itemEntity.setDeltaMovement(dx * 0.1D, dy * 0.1D + Math.sqrt(Math.sqrt(dx * dx + dy * dy + dz * dz)) * 0.08D, dz * 0.1D);

                            level.addFreshEntity(itemEntity);
                            level.addFreshEntity(new ExperienceOrb(level, player.getX(), player.getY() + 0.5D, player.getZ() + 0.5D, level.random.nextInt(6) + 1));

                            if (loot.is(ItemTags.FISHES)) {
                                player.awardStat(Stats.FISH_CAUGHT, 1);
                            }

                            qte.setQteHandled(true);
                            QteManager.retrieveAndDamageRod(player, hook, 2);
                            qte.cancelQte();
                            PacketHandler.sendToPlayer(new S2CQTEScreenClosePacket(), player);

                        } else {

                            //Next QTE
                            String nextKey = QteManager.getRandomQteChar();
                            qte.setExpectedKey(nextKey);
                            qte.refreshQteTimer();
                            PacketHandler.sendToPlayer(new S2CQteUpdateKeyPacket(nextKey), player);
                        }
                    } else {

                        //Fail
                        qte.setQteHandled(true);
                        QteManager.retrieveAndDamageRod(player, hook, 1);
                        qte.cancelQte();
                        PacketHandler.sendToPlayer(new S2CQTEScreenClosePacket(), player);
                        PacketHandler.sendToPlayer(new S2CPlayFailSoundPacket(), player);
                        player.level().playSound(null, hook.getX(), hook.getY(), hook.getZ(),
                                SoundEvents.FISH_SWIM, player.getSoundSource(), 1.0F, 1.5F);
                    }
                }
            }
        });

        context.setPacketHandled(true);
    }

}
