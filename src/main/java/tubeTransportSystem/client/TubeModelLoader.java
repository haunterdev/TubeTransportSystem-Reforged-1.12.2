package tubeTransportSystem.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;

/**
 * The two custom models are plain {@code {"loader": "tts:tube"}} / {@code {"loader": "tts:station"}}
 * model files, so the blockstate JSONs point at them like any other model.
 *
 * <p>The 123 sprites are not referenced by any JSON, but everything under
 * {@code assets/tts/textures/block} is stitched onto the block atlas by the vanilla atlas
 * definition, so they can be resolved straight from the sprite getter at bake time. This replaces
 * the 1.12.2 TextureStitchEvent registration.
 */
public final class TubeModelLoader implements IUnbakedGeometry<TubeModelLoader> {
    public static final IGeometryLoader<TubeModelLoader> TUBE = (json, ctx) -> new TubeModelLoader(true);
    public static final IGeometryLoader<TubeModelLoader> STATION = (json, ctx) -> new TubeModelLoader(false);

    private final boolean tube;

    private TubeModelLoader(boolean tube) {
        this.tube = tube;
    }

    /** Every sprite name the ported render tables can ask for. */
    public static List<String> spriteNames() {
        List<String> names = new ArrayList<>();
        for (int i = 0; i < 16; i++) {
            names.add("tts:block/tube/" + i);
        }
        for (int c = 0; c < 6; c++) {
            for (int i = 0; i < 16; i++) {
                names.add("tts:block/tube" + c + "/" + i);
            }
        }
        names.add("tts:block/station_misc");
        for (int i = 1; i <= 4; i++) {
            names.add("tts:block/station_side" + i);
            names.add("tts:block/station_entr" + i);
        }
        return names;
    }

    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBaker baker,
            Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides) {
        Map<String, TextureAtlasSprite> resolved = new HashMap<>();
        for (String name : spriteNames()) {
            resolved.put(name, spriteGetter.apply(new Material(InventoryMenu.BLOCK_ATLAS, ResourceLocation.parse(name))));
        }
        Function<String, TextureAtlasSprite> sprites = resolved::get;
        return tube ? new TubeBakedModel(sprites) : new StationBakedModel(sprites);
    }
}
