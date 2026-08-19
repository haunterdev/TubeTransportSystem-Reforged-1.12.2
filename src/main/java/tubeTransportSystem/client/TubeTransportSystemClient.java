package tubeTransportSystem.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import tubeTransportSystem.TubeTransportSystem;

/** Client-only entry point: hooks the generated config screen onto the mod list Config button. */
@Mod(value = TubeTransportSystem.MOD_ID, dist = Dist.CLIENT)
public class TubeTransportSystemClient {
    public TubeTransportSystemClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
}
