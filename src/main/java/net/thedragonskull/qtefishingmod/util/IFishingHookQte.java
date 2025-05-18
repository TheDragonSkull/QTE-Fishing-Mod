package net.thedragonskull.qtefishingmod.util;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public interface IFishingHookQte {
    void startQte(ServerPlayer player, String expectedKey, ItemStack loot);

    boolean isQteActive();

    void cancelQte();

    boolean isQteHandled();

    void setQteHandled(boolean handled);

    String getExpectedKey();

    int getQteSuccessCount();

    int getMaxQteSuccess();

    void incrementQteSuccessCount();

    ItemStack getQteLoot();

    void setExpectedKey(String key);
}

