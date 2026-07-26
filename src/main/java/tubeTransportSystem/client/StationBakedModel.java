package tubeTransportSystem.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.property.IExtendedBlockState;
import tubeTransportSystem.block.BlockStation;

/**
 * Port of RenderStation.renderWorldBlock, shared by the vertical and horizontal
 * station. The original draws two boxes through renderStandardBlock:
 *
 *   1. the inset inner box, with setRenderFromInside(true) and (for the horizontal
 *      station) uvRotate 3 on the bottom and the four lateral faces
 *   2. the full 0..1 outer cube, drawn normally
 *
 * Both go through Block.shouldSideBeRendered, so quads are emitted per side and
 * vanilla applies the cull.
 */
public class StationBakedModel implements IBakedModel {
    private final Function<String, TextureAtlasSprite> sprites;

    public StationBakedModel(Function<String, TextureAtlasSprite> sprites) {
        this.sprites = sprites;
    }

    private TextureAtlasSprite get(String name) {
        return sprites.apply(name);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        StationRenderData d = null;
        if (state instanceof IExtendedBlockState) {
            d = ((IExtendedBlockState) state).getValue(BlockStation.RENDER);
        }
        if (side == null) {
            if (d != null) {
                return Collections.emptyList();
            }
            // item / fallback: plain cube with the side skin
            List<BakedQuad> fallback = new ArrayList<BakedQuad>();
            TextureAtlasSprite base = get("tts:station_side2");
            for (int s = 0; s < 6; s++) {
                QuadBaker.addBoxFace(fallback, EnumFacing.byIndex(s), base, 0, 0, 0, 1, 1, 1);
            }
            return fallback;
        }
        int s = side.getIndex();
        List<BakedQuad> q = new ArrayList<BakedQuad>();
        String name = d != null ? d.sprite[s] : "tts:station_side2";
        TextureAtlasSprite sprite = get(name);

        // outer cube
        QuadBaker.addBoxFace(q, side, sprite, 0, 0, 0, 1, 1, 1);

        // inner box, drawn from the inside; RenderStation leaves flipTexture off
        if (d != null) {
            QuadBaker.addBoxFace(q, side, sprite, d.minX, d.minY, d.minZ, d.maxX, d.maxY, d.maxZ,
                    true, false, d.uvRotation[s]);
        }
        return q;
    }

    @Override
    public boolean isAmbientOcclusion() {
        // 1.7.10 took the AO path here, but 1.12.2 smooth lighting on an inset box sitting on
        // solid ground occludes the lower band heavily and darkens the station's base. The
        // per-face shade that actually carried the original look comes from the quads'
        // diffuse-lighting flag, which QuadBaker sets, so the flat path keeps it.
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
        return get("tts:station_side2");
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
