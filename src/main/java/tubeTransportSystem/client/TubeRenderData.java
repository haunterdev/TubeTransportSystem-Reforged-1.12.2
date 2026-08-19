package tubeTransportSystem.client;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import tubeTransportSystem.block.BlockTube;
import tubeTransportSystem.util.ConnectedTextures;

/**
 * Per-tube render description, the 1.12.2 unlisted property in its 1.21.1 shape. It is built in
 * {@link TubeBakedModel#getModelData} (which has the level and position) and read back in
 * getQuads, so no neighbour sampling happens inside the model itself.
 *
 * <p>Face index s uses the vanilla numbering: 0=DOWN 1=UP 2=NORTH 3=SOUTH 4=WEST 5=EAST.
 *
 * <p>Face culling is NOT held here: the original culls both the outer skin and the inner
 * connector box through the block's own face-cull hook, which is now skipRendering.
 */
public class TubeRenderData {
    public static final ModelProperty<TubeRenderData> PROPERTY = new ModelProperty<>();

    public final int direction;
    /** Outer skin sprite name per face s. */
    public final String[] outerSprite = new String[6];
    /** Inner connector sprite name per face s. */
    public final String[] innerSprite = new String[6];
    /** canConnectTo by direction index (D0,U1,N2,S3,W4,E5); drives the inner-box inset. */
    public final boolean[] connByFacing = new boolean[6];

    public TubeRenderData(int direction) {
        this.direction = direction;
    }

    public static TubeRenderData compute(BlockGetter level, BlockPos pos, BlockState state) {
        BlockTube block = (BlockTube) state.getBlock();
        int meta = BlockTube.metaOf(state);
        Direction axis = Direction.from3DDataValue(meta);
        TubeRenderData data = new TubeRenderData(meta);
        for (int s = 0; s < 6; s++) {
            ConnectedTextures set = block.pickSet(meta, axis, s);
            data.outerSprite[s] = set.spriteName(set.getIndexForSide(level, pos, s));
            data.innerSprite[s] = set.spriteName(set.getIndexForSideForInternal(level, pos, s));
        }
        for (Direction f : Direction.values()) {
            data.connByFacing[f.get3DDataValue()] = block.canConnectTo(level, pos, f);
        }
        return data;
    }
}
