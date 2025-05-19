package net.thedragonskull.qtefishingmod.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.thedragonskull.qtefishingmod.network.PacketHandler;
import net.thedragonskull.qtefishingmod.network.S2CQTEScreenClosePacket;
import net.thedragonskull.qtefishingmod.network.S2CQTEStartPacket;
import net.thedragonskull.qtefishingmod.util.IFishingHookQte;
import net.thedragonskull.qtefishingmod.util.QteManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Random;

@Mixin(FishingHook.class)
public class FishingHookMixin implements IFishingHookQte {

    @Unique
    private static final String VALID_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    @Unique
    private final int MAX_QTE_SUCCESS = 4; //todo CONFIG

    @Unique
    private final long QTE_TIMEOUT_TICKS = 30L; //todo CONFIG

    @Unique
    private static final Random RANDOM = new Random();

    @Inject(method = "retrieve", at = @At("HEAD"), cancellable = true)
    private void cancelIfQteActive(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        FishingHook hook = (FishingHook)(Object)this;
        IFishingHookQte qte = (IFishingHookQte) hook;

        if (qte.isQteActive()) {
            qte.cancelQte();
            hook.discard();
            cir.setReturnValue(0);
            return;
        }

        if (qte.isQteHandled()) {
            hook.discard();
            cir.setReturnValue(0);
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        FishingHook hook = (FishingHook)(Object)this;
        Minecraft mc = Minecraft.getInstance();

        if (!hook.level().isClientSide()) {
            int nibble = ((FishingHookAccessor) hook).getNibble();
            IFishingHookQte qte = (IFishingHookQte) hook;

            if (nibble > 0) {
                if (!qte.isQteActive()) {
                    Entity owner = hook.getOwner();
                    if (owner instanceof ServerPlayer player && hook.level() instanceof ServerLevel serverLevel) {

                        // Generate loot
                        ItemStack firstLoot = QteManager.generateFishingLoot(player, hook);

                        // Start Qte
                        String randomChar = QteManager.getRandomQteChar();
                        qte.startQte(player, randomChar, firstLoot);
                        PacketHandler.sendToPlayer(new S2CQTEStartPacket(randomChar), player);
                    }
                }
            }

            QteManager.handleQteTimeout(hook, qte);
        }
    }

    @Unique private boolean qteActive = false;
    @Unique private boolean qteHandled = false;
    @Unique private int qteSuccessCount = 0;
    @Unique private String expectedKey;
    @Unique private ItemStack qteLoot = ItemStack.EMPTY;
    @Unique private ServerPlayer qtePlayer;
    @Unique private long qteStartTime = 0L;

    @Override
    public int getQteSuccessCount() {
        return this.qteSuccessCount;
    }

    @Override
    public void incrementQteSuccessCount() {
        this.qteSuccessCount++;
    }

    @Override
    public int getMaxQteSuccess() {
        return this.MAX_QTE_SUCCESS;
    }

    @Override
    public String getExpectedKey() {
        return expectedKey;
    }

    @Override
    public void setExpectedKey(String key) {
        this.expectedKey = key;
    }

    @Override
    public long getQteStartTime() {
        return qteStartTime;
    }

    @Override
    public boolean isQteHandled() {
        return qteHandled;
    }

    @Override
    public void setQteHandled(boolean handled) {
        this.qteHandled = handled;
    }

    @Override
    public void startQte(ServerPlayer player, String key, ItemStack loot) {
        this.qtePlayer = player;
        this.qteLoot = loot;
        this.qteStartTime = player.serverLevel().getGameTime();
        this.expectedKey = key;

        if (!this.qteActive) {
            this.qteActive = true;
            this.qteSuccessCount = 0;
        }
    }

    @Override
    public void cancelQte() {
        this.qteActive = false;
        this.expectedKey = null;
        this.qteLoot = ItemStack.EMPTY;
        this.qtePlayer = null;
    }

    @Override
    public void refreshQteTimer() {
        if (this.qtePlayer != null) {
            this.qteStartTime = this.qtePlayer.serverLevel().getGameTime();
        }
    }

    @Override
    public boolean isQteActive() {
        return qteActive;
    }

    @Override
    public ItemStack getQteLoot() {
        return qteLoot;
    }

}
