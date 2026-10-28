package tubeTransportSystem.client;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import tubeTransportSystem.Registration;
import tubeTransportSystem.TubeTransportSystem;

@Mod.EventBusSubscriber(modid = TubeTransportSystem.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ClientRegistration {

    private ClientRegistration() {
    }

    /** {@code tts:tube} and {@code tts:station}; the namespace is the registering mod's id. */
    @SubscribeEvent
    public static void onRegisterGeometryLoaders(ModelEvent.RegisterGeometryLoaders event) {
        event.register("tube", TubeModelLoader.TUBE);
        event.register("station", TubeModelLoader.STATION);
    }

    /**
     * The original rendered in the alpha-tested pass. Every tts texture is fully opaque or fully
     * transparent (no partial alpha anywhere in the 123 sprites), so CUTOUT_MIPPED reproduces it
     * exactly, and it keeps the blocks in the terrain pass with depth writes instead of the
     * translucent pass which draws after and on top of sky-level geometry such as another mod's
     * clouds. The block models report the same layer; this map is what item rendering reads.
     */
    @SuppressWarnings("deprecation")
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        ItemBlockRenderTypes.setRenderLayer(Registration.TUBE.get(), RenderType.cutoutMipped());
        ItemBlockRenderTypes.setRenderLayer(Registration.STATION.get(), RenderType.cutoutMipped());
        ItemBlockRenderTypes.setRenderLayer(Registration.STATION_HORIZONTAL.get(), RenderType.cutoutMipped());
    }
}
