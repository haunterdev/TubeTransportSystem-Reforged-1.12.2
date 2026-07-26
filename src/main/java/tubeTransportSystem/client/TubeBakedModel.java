package tubeTransportSystem.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.property.IExtendedBlockState;
import tubeTransportSystem.block.BlockTube;

/**
 * Port of RenderTube. The original draws two boxes per tube through
 * renderStandardBlock, so both are culled by BlockTube.shouldSideBeRendered:
 *
 *   1. inner connector box, inset 0.01 on every side NOT connected to a tube,
 *      drawn with setRenderFromInside(true) and flipTexture = true
 *   2. outer skin, the full 0..1 cube, drawn normally
 *
 * Quads are therefore emitted per side and vanilla applies the cull, which is
 * the same rule the 1.7.10 renderer used.
 */
public class TubeBakedModel implements net.minecraft.client.renderer.block.model.IBakedModel {
    private final Function<String, TextureAtlasSprite> sprites;

    public TubeBakedModel(Function<String, TextureAtlasSprite> sprites) {
        this.sprites = sprites;
    }

    private TextureAtlasSprite get(String name) {
        return sprites.apply(name);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        TubeRenderData d = null;
        if (state instanceof IExtendedBlockState) {
            d = ((IExtendedBlockState) state).getValue(BlockTube.RENDER);
        }
        if (d == null) {
            // item / fallback: plain cube with the base skin
            if (side != null) {
                return Collections.emptyList();
            }
            List<BakedQuad> q = new ArrayList<BakedQuad>();
            TextureAtlasSprite base = get("tts:tube0/0");
            for (int s = 0; s < 6; s++) {
                QuadBaker.addBoxFace(q, EnumFacing.byIndex(s), base, 0, 0, 0, 1, 1, 1);
            }
            return q;
        }
        if (side == null) {
            return Collections.emptyList();
        }
        int s = side.getIndex();
        List<BakedQuad> q = new ArrayList<BakedQuad>();

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
    public boolean isAmbientOcclusion() {
        // Off for the same reason as StationBakedModel: 1.12.2 smooth lighting darkens the
        // inset inner box against solid neighbours. The per-face shade comes from the quads'
        // diffuse-lighting flag instead.
        return false;
    }

    @Override
    public boolean isGui3d() {
        return true;
    }

    @Override
    public boolean isBuiltInRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return get("tts:tube0/0");
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return ItemCameraTransforms.DEFAULT;
    }

    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.NONE;
    }
}
