package tubeTransportSystem.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import tubeTransportSystem.client.LangInjector;

public class ProxyClient extends ProxyCommon {
    @Override
    public void miscSetup() {
        super.miscSetup();
        Minecraft mc = Minecraft.getMinecraft();
        if (mc != null && mc.getResourceManager() instanceof IReloadableResourceManager) {
            ((IReloadableResourceManager) mc.getResourceManager())
                    .registerReloadListener(new LangInjector());
        }
        // Inject once now too: the initial resource load already ran before mod init,
        // so the listener alone would not cover the current Locale state.
        LangInjector.inject();
    }
}
