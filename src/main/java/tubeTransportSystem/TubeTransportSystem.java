package tubeTransportSystem;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(TubeTransportSystem.MOD_ID)
public class TubeTransportSystem {
    public static final String MOD_NAME = "Tube Transport System";
    public static final String MOD_ID = "tts";

    public TubeTransportSystem(IEventBus modBus, ModContainer container) {
        Registration.register(modBus);
        modBus.addListener(Config::onLoad);
        modBus.addListener(Config::onReload);
        container.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }
}
