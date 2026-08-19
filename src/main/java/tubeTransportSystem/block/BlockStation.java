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
 * The two-block-tall station. 1.12.2 kept the entrance direction in metadata 0..5 and set bit 8
 * on the upper half; that is now {@code facing} plus {@code top}, and {@link #metaOf(BlockState)}
 * rebuilds the old number for the ported tables.
 */
public class BlockStation extends Block implements IConnectable {
    public static final MapCodec<BlockStation> CODEC = simpleCodec(BlockStation::new);
    public static final int SHIFT = 8;

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty TOP = BooleanProperty.create("top");

    public BlockStation(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(TOP, false));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, TOP);
    }

    /** 1.12.2 metadata of this state: facing index, plus 8 on the upper half. */
    public static int metaOf(BlockState state) {
        return state.getValue(FACING).get3DDataValue() + (state.getValue(TOP) ? SHIFT : 0);
    }

    public static BlockState of(Direction facing, boolean top) {
        return Registration.STATION.get().defaultBlockState()
                .setValue(FACING, facing)
                .setValue(TOP, top);
    }

    /** Per-side sprite table, port of func_149673_e. */
    public static String spriteForSide(int meta, int s) {
        if (meta == Direction.NORTH.get3DDataValue() && s == 2) {
            return "tts:block/station_entr1";
        }
        if (meta == Direction.NORTH.get3DDataValue() + SHIFT && s == 2) {
            return "tts:block/station_entr2";
        }
        if (meta == Direction.EAST.get3DDataValue() && s == 5) {
            return "tts:block/station_entr1";
        }
        if (meta == Direction.EAST.get3DDataValue() + SHIFT && s == 5) {
            return "tts:block/station_entr2";
        }
        if (meta == Direction.SOUTH.get3DDataValue() && s == 3) {
            return "tts:block/station_entr1";
        }
        if (meta == Direction.SOUTH.get3DDataValue() + SHIFT && s == 3) {
            return "tts:block/station_entr2";
        }
        if (meta == Direction.WEST.get3DDataValue() && s == 4) {
            return "tts:block/station_entr1";
        }
        if (meta == Direction.WEST.get3DDataValue() + SHIFT && s == 4) {
            return "tts:block/station_entr2";
        }
        return (s == 0 || s == 1)
                ? "tts:block/station_misc"
                : (meta >= SHIFT ? "tts:block/station_side1" : "tts:block/station_side2");
    }

    /** Outline and ray trace, port of addCuboidsForRaytraceStation: every wall but the entrance. */
    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction facing = state.getValue(FACING);
        VoxelShape shape = Shapes.empty();
        for (Direction h : Direction.Plane.HORIZONTAL) {
            if (h != facing) {
                shape = Shapes.or(shape, Utilities.getThinPart(h));
            }
        }
        if (state.getValue(TOP)) {
            if (!Utilities.isTube(level, pos.above())) {
                shape = Shapes.or(shape, Utilities.getThinPart(Direction.UP));
            }
        } else if (!Utilities.isTube(level, pos.below())) {
            shape = Shapes.or(shape, Utilities.getThinPart(Direction.DOWN));
        }
        return shape;
    }

    @Override
    protected boolean skipRendering(BlockState state, BlockState adjacentState, Direction side) {
        int s = side.get3DDataValue();
        if (s > 1) {
            return false;
        }
        if (adjacentState.is(this)) {
            return !(metaOf(adjacentState) < SHIFT && s == 1);
        }
        return adjacentState.is(Registration.TUBE.get());
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Entity entity = context instanceof EntityCollisionContext ctx ? ctx.getEntity() : null;
        if (entity == null) {
            // The original only ever added boxes for a colliding entity.
            return Shapes.empty();
        }
        Direction facing = state.getValue(FACING);
        VoxelShape shape = Shapes.empty();
        for (Direction h : Direction.Plane.HORIZONTAL) {
            if (h != facing) {
                shape = Shapes.or(shape, Utilities.getCollisionBoxPart(h));
            }
        }
        if (state.getValue(TOP)) {
            BlockPos above = pos.above();
            if (entity.isShiftKeyDown() && Utilities.tubeDirection(level, above) == Direction.UP.get3DDataValue()) {
                shape = Shapes.or(shape, Utilities.getCollisionBoxPart(Direction.UP));
            } else if (!Utilities.isTube(level, above)) {
                shape = Shapes.or(shape, Utilities.getCollisionBoxPart(Direction.UP));
            }
        } else if (entity.getY() >= pos.getY()) {
            BlockPos below = pos.below();
            int belowDir = Utilities.tubeDirection(level, below);
            if (entity.isShiftKeyDown() && belowDir == Direction.DOWN.get3DDataValue()) {
                shape = Shapes.or(shape, Utilities.COLLISION_FLOOR);
            } else if (!Utilities.isTube(level, below)) {
                shape = Shapes.or(shape, Utilities.COLLISION_FLOOR);
            } else if (belowDir != Direction.DOWN.get3DDataValue()) {
                shape = Shapes.or(shape, Utilities.COLLISION_FLOOR);
            }
        }
        return shape;
    }

    /** Breaking either half takes the other with it, as the original breakBlock did. */
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            BlockPos partner = state.getValue(TOP) ? pos.below() : pos.above();
            if (level.getBlockState(partner).is(this)) {
                level.setBlock(partner, Blocks.AIR.defaultBlockState(),
                        Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (entity == null) {
            return;
        }
        if (!entity.isShiftKeyDown() && state.getValue(TOP)
                && Utilities.tubeDirection(level, pos.above()) == Direction.UP.get3DDataValue()) {
            Utilities.entityAccelerate(entity, Direction.UP);
            Utilities.entityAccelerate(entity, Direction.UP);
        }
        Utilities.entityLimitSpeed(entity);
    }

    @Override
    protected int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return 1;
    }

    @Override
    public boolean canConnectTo(BlockGetter level, BlockPos pos, Direction d) {
        if (d != Direction.UP && d != Direction.DOWN) {
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
