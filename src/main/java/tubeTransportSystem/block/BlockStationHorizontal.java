package tubeTransportSystem.block;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import tubeTransportSystem.Registration;
import tubeTransportSystem.util.IConnectable;
import tubeTransportSystem.util.Utilities;

/**
 * The sideways station, two blocks long. 1.12.2 stored the run direction in metadata 0..5 and set
 * bit 8 on the half nearest the player who placed it; that is now {@code facing} plus
 * {@code front}, with {@link #metaOf(BlockState)} rebuilding the old number.
 */
public class BlockStationHorizontal extends Block implements IConnectable {
    public static final MapCodec<BlockStationHorizontal> CODEC = simpleCodec(BlockStationHorizontal::new);
    public static final int SHIFT = 8;

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    /** True on the half that was placed against the clicked block (1.12.2 meta >= 8). */
    public static final BooleanProperty FRONT = BooleanProperty.create("front");

    public BlockStationHorizontal(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(FRONT, false));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, FRONT);
    }

    /** 1.12.2 metadata of this state: facing index, plus 8 on the near half. */
    public static int metaOf(BlockState state) {
        return state.getValue(FACING).get3DDataValue() + (state.getValue(FRONT) ? SHIFT : 0);
    }

    public static BlockState of(Direction facing, boolean front) {
        return Registration.STATION_HORIZONTAL.get().defaultBlockState()
                .setValue(FACING, facing)
                .setValue(FRONT, front);
    }

    /** The end of the run this half closes off: away from its partner. */
    public static Direction outward(BlockState state) {
        Direction facing = state.getValue(FACING);
        return state.getValue(FRONT) ? facing.getOpposite() : facing;
    }

    /** The other half of the pair. */
    public static BlockPos partner(BlockState state, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        return pos.relative(state.getValue(FRONT) ? facing : facing.getOpposite());
    }

    /** Port of func_149673_e per-side table. */
    public static String spriteForSide(int meta, int s) {
        if (meta == Direction.NORTH.get3DDataValue()) {
            if (s == 1) return "tts:block/station_entr2";
            if (s == 0) return "tts:block/station_side1";
            if (s == 4) return "tts:block/station_side4";
            if (s == 5) return "tts:block/station_side3";
        } else if (meta == Direction.NORTH.get3DDataValue() + SHIFT) {
            if (s == 1) return "tts:block/station_entr1";
            if (s == 0) return "tts:block/station_side2";
            if (s == 4) return "tts:block/station_side3";
            if (s == 5) return "tts:block/station_side4";
        } else if (meta == Direction.EAST.get3DDataValue()) {
            if (s == 1) return "tts:block/station_entr3";
            if (s == 0) return "tts:block/station_side3";
            if (s == 2) return "tts:block/station_side4";
            if (s == 3) return "tts:block/station_side3";
        } else if (meta == Direction.EAST.get3DDataValue() + SHIFT) {
            if (s == 1) return "tts:block/station_entr4";
            if (s == 0) return "tts:block/station_side4";
            if (s == 2) return "tts:block/station_side3";
            if (s == 3) return "tts:block/station_side4";
        } else if (meta == Direction.SOUTH.get3DDataValue()) {
            if (s == 1) return "tts:block/station_entr1";
            if (s == 0) return "tts:block/station_side2";
            if (s == 4) return "tts:block/station_side3";
            if (s == 5) return "tts:block/station_side4";
        } else if (meta == Direction.SOUTH.get3DDataValue() + SHIFT) {
            if (s == 1) return "tts:block/station_entr2";
            if (s == 0) return "tts:block/station_side1";
            if (s == 4) return "tts:block/station_side4";
            if (s == 5) return "tts:block/station_side3";
        } else if (meta == Direction.WEST.get3DDataValue()) {
            if (s == 1) return "tts:block/station_entr4";
            if (s == 0) return "tts:block/station_side4";
            if (s == 2) return "tts:block/station_side3";
            if (s == 3) return "tts:block/station_side4";
        } else if (meta == Direction.WEST.get3DDataValue() + SHIFT) {
            if (s == 1) return "tts:block/station_entr3";
            if (s == 0) return "tts:block/station_side3";
            if (s == 2) return "tts:block/station_side4";
            if (s == 3) return "tts:block/station_side3";
        }
        return "tts:block/station_misc";
    }

    /** Outline and ray trace, port of addCuboidsForRaytraceStationHorizontal. */
    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction facing = state.getValue(FACING);
        VoxelShape shape = Shapes.empty();
        for (Direction h : Direction.Plane.HORIZONTAL) {
            if (h.getAxis() != facing.getAxis()) {
                shape = Shapes.or(shape, Utilities.getThinPart(h));
            }
        }
        shape = Shapes.or(shape, Utilities.getThinPart(outward(state)));
        return Shapes.or(shape, Utilities.getThinPart(Direction.DOWN));
    }

    @Override
    protected boolean skipRendering(BlockState state, BlockState adjacentState, Direction side) {
        int s = side.get3DDataValue();
        if (s == 0) {
            return false;
        }
        Direction out = outward(state);
        // The face towards the partner is never drawn; the outward end drops out when a tube
        // carries the run on. The lateral walls and the entrance on top always draw.
        if (side == out.getOpposite()) {
            return true;
        }
        return side == out && adjacentState.is(Registration.TUBE.get());
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Entity entity = context instanceof EntityCollisionContext ctx ? ctx.getEntity() : null;
        if (entity == null) {
            return Shapes.empty();
        }
        Direction facing = state.getValue(FACING);
        VoxelShape shape = Shapes.empty();
        for (Direction h : Direction.Plane.HORIZONTAL) {
            if (h.getAxis() != facing.getAxis()) {
                shape = Shapes.or(shape, Utilities.getCollisionBoxPart(h));
            }
        }
        Direction out = outward(state);
        if (entity.isShiftKeyDown() || !Utilities.isTube(level, pos.relative(out))) {
            shape = Shapes.or(shape, Utilities.getCollisionBoxPart(out));
        }
        return Shapes.or(shape, Utilities.getCollisionBoxPart(Direction.DOWN));
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            BlockPos partner = partner(state, pos);
            if (level.getBlockState(partner).is(this)) {
                level.setBlock(partner, Blocks.AIR.defaultBlockState(),
                        Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return 1;
    }

    @Override
    public boolean canConnectTo(BlockGetter level, BlockPos pos, Direction d) {
        if (d == Direction.UP || d == Direction.DOWN) {
            return false;
        }
        BlockState other = level.getBlockState(pos.relative(d));
        int meta = other.is(this) ? metaOf(other) : -1;
        int thisMeta = metaOf(level.getBlockState(pos));
        return other.is(this) && thisMeta >= SHIFT ? meta == thisMeta - SHIFT : thisMeta + SHIFT == meta;
    }

    @Override
    public boolean canConnectToStrict(BlockGetter level, BlockPos pos, Direction d) {
        return canConnectTo(level, pos, d);
    }
}
