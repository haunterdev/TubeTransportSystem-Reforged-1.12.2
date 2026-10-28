package tubeTransportSystem;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(TubeTransportSystem.MOD_ID)
public class TubeTransportSystem {
    public static final String MOD_NAME = "Tube Transport System";
    public static final String MOD_ID = "tts";

    public TubeTransportSystem() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        Registration.register(modBus);
        modBus.addListener(Config::onLoad);
        modBus.addListener(Config::onReload);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        // DROPPED: TubeTransportSystemClient registered NeoForge's ConfigurationScreen as the mod
        // list Config button. Forge 1.20.1 has no generic config screen; the TOML file remains.
    }
}
