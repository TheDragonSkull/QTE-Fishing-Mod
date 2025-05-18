package net.thedragonskull.qtefishingmod.util;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public interface FishingHookQteData {
    void startQte(ServerPlayer player, String key, ItemStack loot);
    void cancelQte();
    boolean isQteActive();
    String getExpectedKey();
    ItemStack getQteLoot();
    ServerPlayer getQtePlayer();
    boolean wasQteHandled();
    void setQteHandled(boolean handled);
}
