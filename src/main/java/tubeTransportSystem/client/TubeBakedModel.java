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
import tubeTransportSystem.block.BlockTube;

/**
 * Port of RenderTube. The original draws two boxes per tube through renderStandardBlock,
 * so both are culled by the block's face-cull hook:
 *
 *   1. inner connector box, inset 0.01 on every side NOT connected to a tube,
 *      drawn with setRenderFromInside(true) and flipTexture = true
 *   2. outer skin, the full 0..1 cube, drawn normally
 *
 * Quads are therefore emitted per side and vanilla applies the cull, which is
 * the same rule the 1.7.10 renderer used.
 */
public class TubeBakedModel implements IDynamicBakedModel {
    private static final ChunkRenderTypeSet LAYERS = ChunkRenderTypeSet.of(RenderType.cutoutMipped());

    private final Function<String, TextureAtlasSprite> sprites;

    public TubeBakedModel(Function<String, TextureAtlasSprite> sprites) {
        this.sprites = sprites;
    }

    private TextureAtlasSprite get(String name) {
        return sprites.apply(name);
    }

    /** The 1.12.2 getExtendedState: all neighbour sampling happens here, where the level is known. */
    @Override
    public ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData modelData) {
        if (!(state.getBlock() instanceof BlockTube)) {
            return modelData;
        }
        return modelData.derive().with(TubeRenderData.PROPERTY, TubeRenderData.compute(level, pos, state)).build();
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData data) {
        return LAYERS;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand,
            ModelData extraData, @Nullable RenderType renderType) {
        TubeRenderData d = extraData.get(TubeRenderData.PROPERTY);
        if (d == null) {
            // item / fallback: plain cube with the base skin
            if (side != null) {
                return Collections.emptyList();
            }
            List<BakedQuad> q = new ArrayList<>();
            TextureAtlasSprite base = get("tts:block/tube0/0");
            for (int s = 0; s < 6; s++) {
                QuadBaker.addBoxFace(q, Direction.from3DDataValue(s), base, 0, 0, 0, 1, 1, 1);
            }
            return q;
        }
        if (side == null) {
            return Collections.emptyList();
        }
        int s = side.get3DDataValue();
        List<BakedQuad> q = new ArrayList<>();

        // outer skin, full cube
        QuadBaker.addBoxFace(q, side, get(d.outerSprite[s]), 0, 0, 0, 1, 1, 1);

        // inner connector box: flush 0/1 where connected, inset 0.01 where not
        double minX = d.connByFacing[4] ? 0.0 : 0.01;
        double minY = d.connByFacing[0] ? 0.0 : 0.01;
        double minZ = d.connByFacing[2] ? 0.0 : 0.01;
        double maxX = d.connByFacing[5] ? 1.0 : 0.99;
        double maxY = d.connByFacing[1] ? 1.0 : 0.99;
        double maxZ = d.connByFacing[3] ? 1.0 : 0.99;
        QuadBaker.addBoxFace(q, side, get(d.innerSprite[s]), minX, minY, minZ, maxX, maxY, maxZ, true, true, 0);
        return q;
    }

    @Override
    public boolean useAmbientOcclusion() {
        // 1.7.10 took the AO path here, but modern smooth lighting on an inset box sitting on
        // solid ground occludes the lower band heavily and darkens the tube. The per-face shade
        // that actually carried the original look comes from the quads' shade flag, which
        // QuadBaker sets, so the flat path keeps it.
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
        return get("tts:block/tube0/0");
    }

    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }
}
