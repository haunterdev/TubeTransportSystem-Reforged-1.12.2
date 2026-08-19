package tubeTransportSystem.client;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import tubeTransportSystem.Registration;
import tubeTransportSystem.block.BlockStation;
import tubeTransportSystem.block.BlockStationHorizontal;

/**
 * Per-side render description for a station block (face s in vanilla numbering). Carries what
 * RenderStation.renderWorldBlock computed before drawing the inner box: the inset bounds (which
 * shift to flush or overlap where a tube or the partner half joins) and the per-face uvRotate.
 */
public class StationRenderData {
    public static final ModelProperty<StationRenderData> PROPERTY = new ModelProperty<>();

    public final String[] sprite = new String[6];

    /** Inner-box bounds; defaults are the plain 0.01 inset RenderStation starts from. */
    public double minX = 0.01;
    public double minY = 0.01;
    public double minZ = 0.01;
    public double maxX = 0.99;
    public double maxY = 0.99;
    public double maxZ = 0.99;

    /** uvRotate per face for the inner box; 1.7.10 only ever used 0 or 3 (= 180 degrees). */
    public final int[] uvRotation = new int[6];

    /** The two-block-tall station: the inner box goes flush on the Y side that joins a tube or the partner half. */
    public static StationRenderData vertical(BlockGetter level, BlockPos pos, BlockState state) {
        int meta = BlockStation.metaOf(state);
        StationRenderData data = new StationRenderData();
        for (int s = 0; s < 6; s++) {
            data.sprite[s] = BlockStation.spriteForSide(meta, s);
        }
        Block self = state.getBlock();
        Block tube = Registration.TUBE.get();
        Block below = level.getBlockState(pos.below()).getBlock();
        Block above = level.getBlockState(pos.above()).getBlock();
        data.minY = (below != tube && below != self) ? 0.01 : 0.0;
        data.maxY = (above != tube && above != self) ? 0.99 : 1.0;
        return data;
    }

    /**
     * The sideways station: RenderStation nudges the inner box 0.01 outwards where the run
     * continues, so the two halves and any attached tube join without a seam.
     */
    public static StationRenderData horizontal(BlockGetter level, BlockPos pos, BlockState state) {
        int meta = BlockStationHorizontal.metaOf(state);
        StationRenderData data = new StationRenderData();
        for (int s = 0; s < 6; s++) {
            data.sprite[s] = BlockStationHorizontal.spriteForSide(meta, s);
        }
        Direction d = state.getValue(BlockStationHorizontal.FACING);
        Direction o = d.getOpposite();
        boolean joinDown = joins(level, pos.relative(d), state);
        boolean joinUp = joins(level, pos.relative(o), state);
        double negX = 0.0;
        double posX = 0.0;
        double negZ = 0.0;
        double posZ = 0.0;
        if (d == Direction.NORTH) {
            if (joinDown) negZ = -0.01;
            if (joinUp) posZ = 0.01;
        } else if (d == Direction.SOUTH) {
            if (joinUp) negZ = -0.01;
            if (joinDown) posZ = 0.01;
        } else if (d == Direction.EAST) {
            if (joinUp) negX = -0.01;
            if (joinDown) posX = 0.01;
        } else if (d == Direction.WEST) {
            if (joinDown) negX = -0.01;
            if (joinUp) posX = 0.01;
        }
        data.minX = 0.01 + negX;
        data.minZ = 0.01 + negZ;
        data.maxX = 0.99 + posX;
        data.maxZ = 0.99 + posZ;
        // uvRotateBottom, then East/West/South/North; 1.7.10 value 3 means a 180 degree turn
        data.uvRotation[0] = (d == Direction.NORTH || d == Direction.SOUTH) ? 0 : 180;
        data.uvRotation[2] = 180;
        data.uvRotation[3] = 180;
        data.uvRotation[4] = 180;
        data.uvRotation[5] = 180;
        return data;
    }

    /** RenderStation treats another horizontal station or a tube as a continuation of the run. */
    private static boolean joins(BlockGetter level, BlockPos pos, BlockState self) {
        Block b = level.getBlockState(pos).getBlock();
        return b == self.getBlock() || b == Registration.TUBE.get();
    }
}
