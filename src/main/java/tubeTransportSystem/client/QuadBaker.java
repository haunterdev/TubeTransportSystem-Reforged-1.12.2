package tubeTransportSystem.client;

import java.util.List;

import org.joml.Vector3f;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.core.Direction;

/**
 * Builds axis-aligned box-face quads for the custom baked models, replacing the
 * 1.7.10 RenderBlocks face calls.
 *
 * Geometry and UVs come from vanilla's own FaceBakery with the same default UV
 * derivation JSON cubes use, so an outward face is byte-identical to a JSON
 * cube face (shade, winding, UV origin).
 *
 * The two 1.7.10 RenderBlocks flags the mod used are modelled explicitly:
 *
 * - renderFromInside (RenderBlocks.setRenderFromInside): the original swaps the
 *   two position extremes along the face's in-plane horizontal axis while leaving
 *   the UVs alone. Net effect is reversed winding (visible from inside the box)
 *   PLUS a mirrored texture on that axis.
 * - flipTexture (RenderBlocks.flipTexture): swaps the two U extremes, and only
 *   the four X/Z face renderers read it. RenderTube sets it together with
 *   renderFromInside, which cancels the implied mirror on the lateral faces and
 *   leaves it on the top/bottom caps. RenderStation does not set it, so the
 *   station inner box stays mirrored on all six faces.
 */
public final class QuadBaker {
    private static final FaceBakery BAKERY = new FaceBakery();

    private QuadBaker() {
    }

    public static void addBoxFace(List<BakedQuad> out, Direction face, TextureAtlasSprite sprite,
            double x1, double y1, double z1, double x2, double y2, double z2) {
        addBoxFace(out, face, sprite, x1, y1, z1, x2, y2, z2, false, false, 0);
    }

    /**
     * @param renderFromInside reverse the winding so the face is visible from inside the box
     * @param flipTexture      1.7.10 RenderBlocks.flipTexture (X/Z faces only)
     * @param uvRotation       0 or 180; 1.7.10 uvRotate* value 3 is a 180 degree texture turn
     */
    public static void addBoxFace(List<BakedQuad> out, Direction face, TextureAtlasSprite sprite,
            double x1, double y1, double z1, double x2, double y2, double z2,
            boolean renderFromInside, boolean flipTexture, int uvRotation) {
        if (sprite == null) {
            return;
        }
        Vector3f from = new Vector3f((float) (x1 * 16.0), (float) (y1 * 16.0), (float) (z1 * 16.0));
        Vector3f to = new Vector3f((float) (x2 * 16.0), (float) (y2 * 16.0), (float) (z2 * 16.0));

        float[] uvs = defaultUvs(face, from, to);
        // flipTexture cancels the mirror the position swap implies, but only on X/Z faces
        boolean mirrorU = renderFromInside && !(flipTexture && face.getAxis() != Direction.Axis.Y);
        if (mirrorU) {
            float u = uvs[0];
            uvs[0] = uvs[2];
            uvs[2] = u;
        }

        BlockElementFace part = new BlockElementFace(null, -1, "", new BlockFaceUV(uvs, uvRotation));
        BakedQuad quad = BAKERY.bakeQuad(from, to, part, sprite, face, BlockModelRotation.X0_Y0, null, true);
        if (renderFromInside) {
            quad = reverseWinding(quad, sprite);
        }
        out.add(quad);
    }

    /**
     * Reverses the vertex order so the quad is front-facing from inside the box.
     * Each vertex keeps its own UV, so only the winding changes; the face, sprite
     * and shade flag are preserved, which keeps the light sample and the vanilla
     * face shade identical to the outward face (what 1.7.10 did).
     */
    private static BakedQuad reverseWinding(BakedQuad quad, TextureAtlasSprite sprite) {
        int[] src = quad.getVertices();
        int stride = src.length / 4;
        int[] dst = new int[src.length];
        for (int v = 0; v < 4; v++) {
            System.arraycopy(src, (3 - v) * stride, dst, v * stride, stride);
        }
        return new BakedQuad(dst, quad.getTintIndex(), quad.getDirection(), sprite,
                quad.isShade(), quad.hasAmbientOcclusion());
    }

    /** Same defaults BlockElement applies when a JSON face omits "uv" (positions in 0..16 space). */
    private static float[] defaultUvs(Direction face, Vector3f from, Vector3f to) {
        switch (face) {
            case DOWN:
                return new float[]{from.x(), 16.0f - to.z(), to.x(), 16.0f - from.z()};
            case UP:
                return new float[]{from.x(), from.z(), to.x(), to.z()};
            case SOUTH:
                return new float[]{from.x(), 16.0f - to.y(), to.x(), 16.0f - from.y()};
            case WEST:
                return new float[]{from.z(), 16.0f - to.y(), to.z(), 16.0f - from.y()};
            case EAST:
                return new float[]{16.0f - to.z(), 16.0f - to.y(), 16.0f - from.z(), 16.0f - from.y()};
            case NORTH:
            default:
                return new float[]{16.0f - to.x(), 16.0f - to.y(), 16.0f - from.x(), 16.0f - from.y()};
        }
    }
}
