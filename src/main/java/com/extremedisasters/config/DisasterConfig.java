package com.extremedisasters.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class DisasterConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue ENABLED;
    public static final ForgeConfigSpec.BooleanValue BLOCK_PICKUP;
    public static final ForgeConfigSpec.DoubleValue TORNADO_RADIUS;
    public static final ForgeConfigSpec.IntValue TORNADO_DURATION_TICKS;
    public static final ForgeConfigSpec.IntValue MAX_ACTIVE_DEBRIS;
    public static final ForgeConfigSpec.DoubleValue PLAYER_PULL;
    public static final ForgeConfigSpec.DoubleValue MOB_PULL;
    public static final ForgeConfigSpec.BooleanValue DEBRIS_DAMAGE;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("general");
        ENABLED = builder.comment("Master switch for Extreme Disasters.").define("enabled", true);
        TORNADO_RADIUS = builder.comment("Horizontal tornado influence radius in blocks.").defineInRange("tornadoRadius", 12.0, 4.0, 48.0);
        TORNADO_DURATION_TICKS = builder.comment("Default tornado lifetime in ticks.").defineInRange("tornadoDurationTicks", 2400, 200, 12000);
        PLAYER_PULL = builder.comment("Base horizontal pull on players near the tornado.").defineInRange("playerPull", 0.12, 0.0, 1.0);
        MOB_PULL = builder.comment("Base horizontal pull on other living entities.").defineInRange("mobPull", 0.10, 0.0, 1.0);
        BLOCK_PICKUP = builder.comment("Allow tornadoes to remove and carry eligible world blocks.").define("blockPickup", true);
        MAX_ACTIVE_DEBRIS = builder.comment("Maximum carried block entities per tornado.").defineInRange("maxActiveDebris", 24, 0, 128);
        DEBRIS_DAMAGE = builder.comment("Allow thrown block debris to damage living entities.").define("debrisDamage", true);
        builder.pop();
        SPEC = builder.build();
    }

    private DisasterConfig() {}
}
