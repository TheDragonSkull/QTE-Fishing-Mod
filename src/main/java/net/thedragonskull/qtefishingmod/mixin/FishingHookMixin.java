package net.thedragonskull.qtefishingmod.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import net.thedragonskull.qtefishingmod.util.FishingHookQteData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Random;

@Mixin(FishingHook.class)
public class FishingHookMixin implements FishingHookQteData {

    @Unique
    private static final String VALID_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    @Unique
    private static final Random RANDOM = new Random();

    @Inject(method = "retrieve", at = @At("HEAD"), cancellable = true)
    private void cancelIfQteActive(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        FishingHook hook = (FishingHook)(Object)this;
        FishingHookQteData qte = (FishingHookQteData) hook;

        if (qte.isQteActive()) {
            qte.cancelQte(); // Cancelamos el QTE
            hook.discard();  // Recogemos el anzuelo
            cir.setReturnValue(0); // Sin loot
            return;
        }

        // Si el QTE ya fue procesado (acertado o fallado), no dar loot vanilla
        if (qte.wasQteHandled()) {
            hook.discard(); // Eliminar anzuelo normalmente
            cir.setReturnValue(0); // Cancelar loot vanilla
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))  //TODO: solo teclado americano y números 0-9
    private void onTick(CallbackInfo ci) {
        FishingHook hook = (FishingHook)(Object)this;
        Minecraft mc = Minecraft.getInstance();

        if (!hook.level().isClientSide()) {
            int nibble = ((FishingHookAccessor) hook).getNibble();
            FishingHookQteData qte = (FishingHookQteData) hook;

            if (nibble > 0) {
                if (!qte.isQteActive()) {
                    Entity owner = hook.getOwner();
                    if (owner instanceof ServerPlayer player && hook.level() instanceof ServerLevel serverLevel) {

                        // Generar loot
                        LootParams lootparams = new LootParams.Builder(serverLevel)
                                .withParameter(LootContextParams.ORIGIN, hook.position())
                                .withParameter(LootContextParams.TOOL, player.getMainHandItem())  // TODO: ambas manos
                                .withParameter(LootContextParams.THIS_ENTITY, hook)
                                .withParameter(LootContextParams.KILLER_ENTITY, owner)
                                .withLuck((float)((FishingHookAccessor)hook).getLuck() + player.getLuck())
                                .create(LootContextParamSets.FISHING);

                        LootTable loottable = serverLevel.getServer().getLootData().getLootTable(BuiltInLootTables.FISHING);
                        List<ItemStack> loot = loottable.getRandomItems(lootparams);

                        ItemStack firstLoot = loot.isEmpty() ? ItemStack.EMPTY : loot.get(0);

                        // Iniciar QTE con letra random
                        String randomChar = getRandomQteChar();
                        qte.startQte(player, randomChar, firstLoot);
                        PacketHandler.sendToPlayer(new S2CQTEStartPacket(randomChar), player);
                    }
                }
            }

            if (qte.isQteActive() && hook.getOwner() instanceof ServerPlayer player && hook.level() instanceof ServerLevel level) {
                long elapsed = level.getGameTime() - this.qteStartTime;
                if (elapsed >= QTE_TIMEOUT_TICKS) {
                    qte.cancelQte();
                    qte.setQteHandled(true);
                    hook.retrieve(player.getMainHandItem());  // TODO: ambas manos y restar durability?
                    player.displayClientMessage(Component.literal("¡Fallaste el QTE por no reaccionar a tiempo!"), false);
                    PacketHandler.sendToPlayer(new S2CQTEScreenClosePacket(), player);
                }
            }
        }
    }

    private static String getRandomQteChar() {
        int index = RANDOM.nextInt(VALID_CHARS.length());
        return String.valueOf(VALID_CHARS.charAt(index));
    }

    @Unique
    private long qteStartTime = 0L;

    @Unique
    private final long QTE_TIMEOUT_TICKS = 200L; //todo cambiar

    @Unique
    private boolean qteHandled = false;

    @Override
    public boolean wasQteHandled() {
        return qteHandled;
    }

    @Override
    public void setQteHandled(boolean handled) {
        this.qteHandled = handled;
    }

    @Unique
    private boolean qteActive = false;

    @Unique
    private String expectedKey;

    @Unique
    private ItemStack qteLoot = ItemStack.EMPTY;

    @Unique
    private ServerPlayer qtePlayer;

    @Override
    public void startQte(ServerPlayer player, String key, ItemStack loot) {
        this.qtePlayer = player;
        this.expectedKey = key;
        this.qteLoot = loot;
        this.qteActive = true;
        this.qteStartTime = player.serverLevel().getGameTime();
    }

    @Override
    public void cancelQte() {
        this.qteActive = false;
        this.expectedKey = null;
        this.qteLoot = ItemStack.EMPTY;
        this.qtePlayer = null;
    }

    @Override
    public boolean isQteActive() {
        return qteActive;
    }

    @Override
    public String getExpectedKey() {
        return expectedKey;
    }

    @Override
    public ItemStack getQteLoot() {
        return qteLoot;
    }

    @Override
    public ServerPlayer getQtePlayer() {
        return qtePlayer;
    }
}
