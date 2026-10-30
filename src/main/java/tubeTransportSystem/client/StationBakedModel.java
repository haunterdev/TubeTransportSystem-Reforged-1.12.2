package tubeTransportSystem.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.IDynamicBakedModel;
import net.neoforged.neoforge.client.model.data.ModelData;
import tubeTransportSystem.block.BlockStation;
import tubeTransportSystem.block.BlockStationHorizontal;

/**
 * Port of RenderStation.renderWorldBlock, shared by the vertical and horizontal
 * station. The original draws two boxes through renderStandardBlock:
 *
 *   1. the inset inner box, with setRenderFromInside(true) and (for the horizontal
 *      station) uvRotate 3 on the bottom and the four lateral faces
 *   2. the full 0..1 outer cube, drawn normally
 *
 * Both go through the block's face-cull hook. The outer cube is emitted per side, so
 * vanilla culls it; the inner box has no cull face, so its inset faces are lit from the
 * station (renderStandardBlock's rule), and StationRenderData.shown applies the cull.
 */
public class StationBakedModel implements IDynamicBakedModel {
    private static final ChunkRenderTypeSet LAYERS = ChunkRenderTypeSet.of(RenderType.cutoutMipped());

    private final Function<String, TextureAtlasSprite> sprites;

    public StationBakedModel(Function<String, TextureAtlasSprite> sprites) {
        this.sprites = sprites;
    }

    private TextureAtlasSprite get(String name) {
        return sprites.apply(name);
    }

    @Override
    public ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData modelData) {
        StationRenderData data;
        if (state.getBlock() instanceof BlockStation) {
            data = StationRenderData.vertical(level, pos, state);
        } else if (state.getBlock() instanceof BlockStationHorizontal) {
            data = StationRenderData.horizontal(level, pos, state);
        } else {
            return modelData;
        }
        return modelData.derive().with(StationRenderData.PROPERTY, data).build();
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData data) {
        return LAYERS;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand,
            ModelData extraData, @Nullable RenderType renderType) {
        StationRenderData d = extraData.get(StationRenderData.PROPERTY);
        if (d == null) {
            // item / fallback: plain cube with the side skin
            if (side != null) {
                return Collections.emptyList();
            }
            List<BakedQuad> fallback = new ArrayList<>();
            TextureAtlasSprite base = get("tts:block/station_side2");
            for (int s = 0; s < 6; s++) {
                QuadBaker.addBoxFace(fallback, Direction.from3DDataValue(s), base, 0, 0, 0, 1, 1, 1);
            }
            return fallback;
        }
        if (side == null) {
            // inner box, drawn from the inside; RenderStation leaves flipTexture off. No cull face, so
            // the inset floor is lit from the station, as renderStandardBlock lit it, and not from
            // the ground under it; d.shown applies the cull instead.
            List<BakedQuad> q = new ArrayList<>();
            for (int s = 0; s < 6; s++) {
                if (d.shown[s]) {
                    QuadBaker.addBoxFace(q, Direction.from3DDataValue(s), get(d.sprite[s]),
                            d.minX, d.minY, d.minZ, d.maxX, d.maxY, d.maxZ, true, false, d.uvRotation[s]);
                }
            }
            return q;
        }

        // outer cube
        List<BakedQuad> q = new ArrayList<>();
        QuadBaker.addBoxFace(q, side, get(d.sprite[side.get3DDataValue()]), 0, 0, 0, 1, 1, 1);
        return q;
    }

    @Override
    public boolean useAmbientOcclusion() {
        // Off for the same reason as TubeBakedModel: smooth lighting darkens the inset inner box
        // against solid neighbours. The per-face shade comes from the quads' shade flag instead.
        return false;
    }

    @Override
    public boolean isGui3d() {
        return true;
    }

    @Override
    public boolean usesBlockLight() {
        return true;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return get("tts:block/station_side2");
    }

    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }
}
