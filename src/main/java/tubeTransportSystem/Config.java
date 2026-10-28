package tubeTransportSystem;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;

/**
 * Port of the 1.12.2 {@code config/TubeTransportSystem.cfg}. The single option survives with the
 * same name, default and range; the file is now {@code config/tts-common.toml}.
 */
public final class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.DoubleValue MAX_TUBE_SPEED = BUILDER
            .comment("The maximum speed an entity can travel through the Transport Tubes")
            .translation("tts.configuration.max_tube_speed")
            .defineInRange("MaxTubeSpeed", 0.5, 0.0, 10.0);

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    /** Cached so the physics path does not hit the config map every tick, as in the original. */
    public static double maxSpeed = 0.5;
    public static double maxSpeedInverse = -0.5;

    private Config() {
    }

    public static void onLoad(ModConfigEvent.Loading event) {
        cache(event.getConfig());
    }

    public static void onReload(ModConfigEvent.Reloading event) {
        cache(event.getConfig());
    }

    private static void cache(ModConfig config) {
        if (config.getSpec() != SPEC) {
            return;
        }
        maxSpeed = MAX_TUBE_SPEED.get();
        maxSpeedInverse = -maxSpeed;
    }
}
