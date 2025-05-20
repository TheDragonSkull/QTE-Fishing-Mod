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
import net.thedragonskull.qtefishingmod.config.QteCommonConfigs;
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
                        String qteKey;
                        if (QteManager.isHardModeEnabled()) {
                            qteKey = QteManager.getRandomQteWord();
                        } else {
                            qteKey = QteManager.getRandomQteChar();
                        }

                        boolean isHard = QteManager.isHardModeEnabled();
                        qteKey = isHard ? QteManager.getRandomQteWord() : QteManager.getRandomQteChar();
                        qte.startQte(player, qteKey, firstLoot);
                        PacketHandler.sendToPlayer(new S2CQTEStartPacket(qteKey, isHard), player);
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

    @Unique private String expectedWord;
    @Unique private int currentWordIndex;

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
        return QteCommonConfigs.MAX_QTE_SUCCESS.get();
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

        this.expectedWord = key;
        this.currentWordIndex = 0;

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


    // words

    @Override
    public void generateNewWord() {
        this.expectedWord = QteManager.getRandomQteWord();
        this.currentWordIndex = 0;
        this.refreshQteTimer();
    }

    @Override
    public String getExpectedWord() {
        return expectedWord;
    }

    @Override
    public void setExpectedWord(String word) {
        this.expectedWord = word;
    }

    @Override
    public int getCurrentWordIndex() {
        return currentWordIndex;
    }

    @Override
    public void setCurrentWordIndex(int index) {
        this.currentWordIndex = index;
    }

    @Override
    public void incrementCurrentWordIndex() {
        this.currentWordIndex++;
    }

}
