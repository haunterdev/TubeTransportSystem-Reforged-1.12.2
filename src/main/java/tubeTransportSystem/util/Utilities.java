package tubeTransportSystem.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import tubeTransportSystem.Config;
import tubeTransportSystem.Registration;
import tubeTransportSystem.block.BlockTube;

public final class Utilities {
    static final double AXIS_MIN_MIN = 0.0;
    static final double AXIS_MIN_MAX = 0.1;
    static final double AXIS_MAX_MIN = 0.9;
    static final double AXIS_MAX_MAX = 1.0;
    static final double AXIS_FLOOR_MIN = -0.01;
    static final double AXIS_FLOOR_MAX = 0.0;

    /** 0.1 thick collision wall per side, indexed by {@link Direction#get3DDataValue()}. */
    private static final VoxelShape[] COLLISION_PART = new VoxelShape[6];
    /** 0.05 thick wall per side, the boxes the 1.12.2 collisionRayTrace used. */
    private static final VoxelShape[] THIN_PART = new VoxelShape[6];

    public static final VoxelShape COLLISION_FLOOR =
            Shapes.box(0.0, AXIS_FLOOR_MIN, 0.0, 1.0, AXIS_FLOOR_MAX, 1.0);

    static {
        COLLISION_PART[Direction.DOWN.get3DDataValue()] = Shapes.box(0, AXIS_MIN_MIN, 0, 1, AXIS_MIN_MAX, 1);
        COLLISION_PART[Direction.UP.get3DDataValue()] = Shapes.box(0, AXIS_MAX_MIN, 0, 1, AXIS_MAX_MAX, 1);
        COLLISION_PART[Direction.NORTH.get3DDataValue()] = Shapes.box(0, 0, AXIS_MIN_MIN, 1, 1, AXIS_MIN_MAX);
        COLLISION_PART[Direction.SOUTH.get3DDataValue()] = Shapes.box(0, 0, AXIS_MAX_MIN, 1, 1, AXIS_MAX_MAX);
        COLLISION_PART[Direction.WEST.get3DDataValue()] = Shapes.box(AXIS_MIN_MIN, 0, 0, AXIS_MIN_MAX, 1, 1);
        COLLISION_PART[Direction.EAST.get3DDataValue()] = Shapes.box(AXIS_MAX_MIN, 0, 0, AXIS_MAX_MAX, 1, 1);

        THIN_PART[Direction.DOWN.get3DDataValue()] = Shapes.box(0, 0, 0, 1, 0.05, 1);
        THIN_PART[Direction.UP.get3DDataValue()] = Shapes.box(0, 0.95, 0, 1, 1, 1);
        THIN_PART[Direction.NORTH.get3DDataValue()] = Shapes.box(0, 0, 0, 1, 1, 0.05);
        THIN_PART[Direction.SOUTH.get3DDataValue()] = Shapes.box(0, 0, 0.95, 1, 1, 1);
        THIN_PART[Direction.WEST.get3DDataValue()] = Shapes.box(0, 0, 0, 0.05, 1, 1);
        THIN_PART[Direction.EAST.get3DDataValue()] = Shapes.box(0.95, 0, 0, 1, 1, 1);
    }

    private Utilities() {
    }

    /**
     * Port of the original acceleration table. Note that EAST pushes -X and WEST pushes +X:
     * the 1.7.10 mod had the X axis the other way round, and its own tooltips agree with the
     * motion, so the quirk is kept.
     */
    public static void entityAccelerate(Entity entity, Direction direction) {
        Vec3 push = switch (direction) {
            case DOWN -> new Vec3(0.0, -0.1, 0.0);
            case UP -> new Vec3(0.0, 0.1, 0.0);
            case NORTH -> new Vec3(0.0, 0.0, -0.1);
            case SOUTH -> new Vec3(0.0, 0.0, 0.1);
            case EAST -> new Vec3(-0.1, 0.0, 0.0);
            case WEST -> new Vec3(0.1, 0.0, 0.0);
        };
        entity.addDeltaMovement(push);
        entity.hasImpulse = true;
    }

    public static void entityLimitSpeed(Entity entity) {
        Vec3 motion = entity.getDeltaMovement();
        entity.setDeltaMovement(
                Mth.clamp(motion.x, Config.maxSpeedInverse, Config.maxSpeed),
                Mth.clamp(motion.y, Config.maxSpeedInverse, Config.maxSpeed),
                Mth.clamp(motion.z, Config.maxSpeedInverse, Config.maxSpeed));
    }

    public static void entityResetFall(Entity entity) {
        entity.fallDistance = 0.0f;
    }

    /**
     * The tube carries the entity, so vanilla's walk bookkeeping should not run inside one.
     * Entity.move accumulates moveDist and fires playStepSound (and its subtitle) once it passes
     * nextStep; zeroing the accumulator every tick keeps it under the threshold, since the tube
     * speed cap of 0.5 adds at most ~0.3 per tick. walkDist feeds the view bob, so it is pinned
     * to its previous value. Not in the 1.7.10 original: intentional change in this port.
     */
    public static void entityResetWalk(Entity entity) {
        entity.moveDist = 0.0f;
        entity.walkDist = entity.walkDistO;
    }

    /**
     * The original's yaw table. It is the opposite of vanilla's {@link Direction#fromYRot},
     * so a station's entrance ends up facing the player who placed it.
     */
    public static Direction entityGetDirection(LivingEntity entityLiving) {
        int facing = Mth.floor((double) (entityLiving.getYRot() * 4.0f / 360.0f) + 0.5) & 3;
        return switch (facing) {
            case 1 -> Direction.EAST;
            case 2 -> Direction.SOUTH;
            case 3 -> Direction.WEST;
            default -> Direction.NORTH;
        };
    }

    /** The 1.12.2 getCollisionBoxPart, as a block-local shape. */
    public static VoxelShape getCollisionBoxPart(Direction direction) {
        return COLLISION_PART[direction.get3DDataValue()];
    }

    /** The thin wall the original ray trace used on the given side. */
    public static VoxelShape getThinPart(Direction direction) {
        return THIN_PART[direction.get3DDataValue()];
    }

    /** Facing index of a tube at that position, or -1 when it is not a tube. */
    public static int tubeDirection(BlockGetter level, BlockPos pos) {
        var state = level.getBlockState(pos);
        return state.is(Registration.TUBE.get()) ? BlockTube.metaOf(state) : -1;
    }

    public static boolean isTube(BlockGetter level, BlockPos pos) {
        return level.getBlockState(pos).is(Registration.TUBE.get());
    }
}
