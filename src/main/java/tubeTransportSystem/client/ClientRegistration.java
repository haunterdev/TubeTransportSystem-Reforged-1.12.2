package tubeTransportSystem.client;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import tubeTransportSystem.TubeTransportSystem;
import tubeTransportSystem.block.BlockStation;
import tubeTransportSystem.block.BlockStationHorizontal;
import tubeTransportSystem.block.BlockTube;
import tubeTransportSystem.item.ItemTube;

@Mod.EventBusSubscriber(modid = TubeTransportSystem.MOD_ID, value = Side.CLIENT)
public class ClientRegistration {

    public static final ModelResourceLocation TUBE_MRL = new ModelResourceLocation("tts:tube", "normal");
    public static final ModelResourceLocation STATION_MRL = new ModelResourceLocation("tts:station", "normal");
    public static final ModelResourceLocation STATIONH_MRL = new ModelResourceLocation("tts:stationh", "normal");

    /** "tts:tube0/5" -> "tts:blocks/tube0/5" (atlas sprite location). */
    private static String toLocation(String name) {
        return name.replace(":", ":blocks/");
    }

    private static List<String> allSpriteNames() {
        List<String> names = new ArrayList<String>();
        for (int i = 0; i < 16; i++) {
            names.add("tts:tube/" + i);
        }
        for (int c = 0; c < 6; c++) {
            for (int i = 0; i < 16; i++) {
                names.add("tts:tube" + c + "/" + i);
            }
        }
        names.add("tts:station_misc");
        for (int i = 1; i <= 4; i++) {
            names.add("tts:station_side" + i);
            names.add("tts:station_entr" + i);
        }
        return names;
    }

    @SubscribeEvent
    public static void onModelRegistry(ModelRegistryEvent event) {
        ModelLoader.setCustomStateMapper(BlockTube.instance, new FixedMapper(TUBE_MRL));
        ModelLoader.setCustomStateMapper(BlockStation.instance, new FixedMapper(STATION_MRL));
        ModelLoader.setCustomStateMapper(BlockStationHorizontal.instance, new FixedMapper(STATIONH_MRL));

        // tube item: metas 10..15 map to the colour models tube_0..tube_5. Meta 0 is the
        // undirected tube, which cannot be placed, so it gets a desaturated skin of its own
        // to read as different from the six directed ones at a glance.
        ModelLoader.setCustomModelResourceLocation(ItemTube.instance, 0,
                new ModelResourceLocation("tts:tube_nodir", "inventory"));
        for (int i = 0; i < 6; i++) {
            ModelLoader.setCustomModelResourceLocation(ItemTube.instance, 10 + i,
                    new ModelResourceLocation("tts:tube_" + i, "inventory"));
        }
        // station item
        ModelLoader.setCustomModelResourceLocation(
                net.minecraft.item.Item.getItemFromBlock(BlockStation.instance), 0,
                new ModelResourceLocation("tts:station_item", "inventory"));
    }

    @SubscribeEvent
    public static void onTextureStitch(TextureStitchEvent.Pre event) {
        TextureMap map = event.getMap();
        for (String name : allSpriteNames()) {
            map.registerSprite(new ResourceLocation(toLocation(name)));
        }
    }

    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent event) {
        final TextureMap map = Minecraft.getMinecraft().getTextureMapBlocks();
        Function<String, TextureAtlasSprite> sprites = new Function<String, TextureAtlasSprite>() {
            @Override
            public TextureAtlasSprite apply(String name) {
                return map.getAtlasSprite(toLocation(name));
            }
        };
        event.getModelRegistry().putObject(TUBE_MRL, new TubeBakedModel(sprites));
        event.getModelRegistry().putObject(STATION_MRL, new StationBakedModel(sprites));
        event.getModelRegistry().putObject(STATIONH_MRL, new StationBakedModel(sprites));
    }


    private static final class FixedMapper extends StateMapperBase {
        private final ModelResourceLocation mrl;

        FixedMapper(ModelResourceLocation mrl) {
            this.mrl = mrl;
        }

        @Override
        protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
            return mrl;
        }
    }
}
