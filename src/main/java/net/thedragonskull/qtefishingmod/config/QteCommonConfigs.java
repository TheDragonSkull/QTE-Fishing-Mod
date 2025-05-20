package net.thedragonskull.qtefishingmod.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class QteCommonConfigs {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<String> VALID_CHARS;
    public static final ForgeConfigSpec.ConfigValue<Integer> MAX_QTE_SUCCESS;
    public static final ForgeConfigSpec.LongValue QTE_TIMEOUT_TICKS;

    public static final ForgeConfigSpec.ConfigValue<Boolean> HARD_MODE;

    static {
        BUILDER.push("Configs for Qte Fishing Mod");

        VALID_CHARS = BUILDER.comment("Specify the valid keys for the quick time events (A-Z & 0-9)")
                .define("Valid chars", "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789");

        MAX_QTE_SUCCESS = BUILDER.comment("How many quick time events")
                .define("Number of Qte", 4);

        QTE_TIMEOUT_TICKS = BUILDER.comment("How much time do you have to success at the quick time events (in ticks)")
                .defineInRange("Time to success (ticks)", 30L, 1L, 10000L);

        HARD_MODE = BUILDER.comment("Enable Hard Mode: instead of a single key, the QTE requires typing a full word")
                .define("hardMode", false);


        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
