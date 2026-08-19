package tubeTransportSystem.client;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import tubeTransportSystem.Registration;
import tubeTransportSystem.TubeTransportSystem;

@EventBusSubscriber(modid = TubeTransportSystem.MOD_ID, value = Dist.CLIENT)
public final class ClientRegistration {

    private ClientRegistration() {
    }

    @SubscribeEvent
    public static void onRegisterGeometryLoaders(ModelEvent.RegisterGeometryLoaders event) {
        event.register(ResourceLocation.fromNamespaceAndPath(TubeTransportSystem.MOD_ID, "tube"),
                TubeModelLoader.TUBE);
        event.register(ResourceLocation.fromNamespaceAndPath(TubeTransportSystem.MOD_ID, "station"),
                TubeModelLoader.STATION);
    }

    /**
     * The original rendered in the alpha-tested pass. Every tts texture is fully opaque or fully
     * transparent (no partial alpha anywhere in the 123 sprites), so CUTOUT_MIPPED reproduces it
     * exactly, and it keeps the blocks in the terrain pass with depth writes instead of the
     * translucent pass which draws after and on top of sky-level geometry such as another mod's
     * clouds. The models report the same layer; this covers the item models too.
     */
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        ItemBlockRenderTypes.setRenderLayer(Registration.TUBE.get(), RenderType.cutoutMipped());
        ItemBlockRenderTypes.setRenderLayer(Registration.STATION.get(), RenderType.cutoutMipped());
        ItemBlockRenderTypes.setRenderLayer(Registration.STATION_HORIZONTAL.get(), RenderType.cutoutMipped());
    }
}
