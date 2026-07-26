package tubeTransportSystem.util;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import tubeTransportSystem.block.BlockTube;
import tubeTransportSystem.network.ProxyCommon;

public class Utilities {
    static double AXIS_MIN_MIN = 0.0;
    static double AXIS_MIN_MAX = 0.1;
    static double AXIS_MAX_MIN = 0.9;
    static double AXIS_MAX_MAX = 1.0;
    static double AXIS_FLOOR_MIN = -0.01;
    static double AXIS_FLOOR_MAX = 0.0;

    public static void entityAccelerate(Entity entity, EnumFacing direction) {
        if (direction == EnumFacing.DOWN) {
            entity.addVelocity(0.0, -0.1, 0.0);
        } else if (direction == EnumFacing.UP) {
            entity.addVelocity(0.0, 0.1, 0.0);
        } else if (direction == EnumFacing.NORTH) {
            entity.addVelocity(0.0, 0.0, -0.1);
        } else if (direction == EnumFacing.SOUTH) {
            entity.addVelocity(0.0, 0.0, 0.1);
        } else if (direction == EnumFacing.EAST) {
            entity.addVelocity(-0.1, 0.0, 0.0);
        } else if (direction == EnumFacing.WEST) {
            entity.addVelocity(0.1, 0.0, 0.0);
        }
    }

    public static void entityLimitSpeed(Entity entity) {
        entity.motionX = MathHelper.clamp(entity.motionX, ProxyCommon.CONFIG_MAX_SPEED_INVERSE, ProxyCommon.CONFIG_MAX_SPEED);
        entity.motionY = MathHelper.clamp(entity.motionY, ProxyCommon.CONFIG_MAX_SPEED_INVERSE, ProxyCommon.CONFIG_MAX_SPEED);
        entity.motionZ = MathHelper.clamp(entity.motionZ, ProxyCommon.CONFIG_MAX_SPEED_INVERSE, ProxyCommon.CONFIG_MAX_SPEED);
    }

    public static void entityResetFall(Entity entity) {
        entity.fallDistance = 0.0f;
    }

    /**
     * The tube carries the entity, so vanilla's walk bookkeeping should not run inside one.
     * Entity.move accumulates distanceWalkedOnStepModified and fires playStepSound (and its
     * subtitle) once it passes nextStepDistance; zeroing the accumulator every tick keeps it
     * under the threshold, since the tube speed cap of 0.5 adds at most ~0.3 per tick.
     * distanceWalkedModified feeds the view bob, so it is pinned to its previous value.
     * Not in the 1.7.10 original: intentional change in this port.
     */
    public static void entityResetWalk(Entity entity) {
        entity.distanceWalkedOnStepModified = 0.0f;
        entity.distanceWalkedModified = entity.prevDistanceWalkedModified;
    }

    public static EnumFacing entityGetDirection(EntityLivingBase entityLiving) {
        int facing = MathHelper.floor((double) (entityLiving.rotationYaw * 4.0f / 360.0f) + 0.5) & 3;
        if (facing == 0) {
            return EnumFacing.NORTH;
        }
        if (facing == 1) {
            return EnumFacing.EAST;
        }
        if (facing == 2) {
            return EnumFacing.SOUTH;
        }
        if (facing == 3) {
            return EnumFacing.WEST;
        }
        return null;
    }

    public static AxisAlignedBB getCollisionBoxPart(BlockPos p, EnumFacing direction) {
        int x = p.getX();
        int y = p.getY();
        int z = p.getZ();
        if (direction == EnumFacing.EAST) {
            return new AxisAlignedBB(x + AXIS_MAX_MIN, y, z, x + AXIS_MAX_MAX, y + 1, z + 1);
        }
        if (direction == EnumFacing.WEST) {
            return new AxisAlignedBB(x + AXIS_MIN_MIN, y, z, x + AXIS_MIN_MAX, y + 1, z + 1);
        }
        if (direction == EnumFacing.SOUTH) {
            return new AxisAlignedBB(x, y, z + AXIS_MAX_MIN, x + 1, y + 1, z + AXIS_MAX_MAX);
        }
        if (direction == EnumFacing.NORTH) {
            return new AxisAlignedBB(x, y, z + AXIS_MIN_MIN, x + 1, y + 1, z + AXIS_MIN_MAX);
        }
        if (direction == EnumFacing.UP) {
            return new AxisAlignedBB(x, y + AXIS_MAX_MIN, z, x + 1, y + AXIS_MAX_MAX, z + 1);
        }
        if (direction == EnumFacing.DOWN) {
            return new AxisAlignedBB(x, y + AXIS_MIN_MIN, z, x + 1, y + AXIS_MIN_MAX, z + 1);
        }
        return null;
    }

    public static AxisAlignedBB getCollisionBoxPartFloor(BlockPos p) {
        int x = p.getX();
        int y = p.getY();
        int z = p.getZ();
        return new AxisAlignedBB(x, y + AXIS_FLOOR_MIN, z, x + 1, y + AXIS_FLOOR_MAX, z + 1);
    }

    /** Side index s (0-5) maps to the neighbour pos, matching the original getCoordinatesFromSide. */
    public static BlockPos getCoordinatesFromSide(BlockPos p, int s) {
        int x = p.getX();
        int y = p.getY();
        int z = p.getZ();
        if (s == 0) {
            ++y;
        } else if (s == 1) {
            --y;
        } else if (s == 2) {
            ++z;
        } else if (s == 3) {
            --z;
        } else if (s == 4) {
            ++x;
        } else if (s == 5) {
            --x;
        }
        return new BlockPos(x, y, z);
    }

    public static EnumFacing getDirectionFromSide(int s) {
        if (s == 0) {
            return EnumFacing.DOWN;
        }
        if (s == 1) {
            return EnumFacing.UP;
        }
        if (s == 2) {
            return EnumFacing.NORTH;
        }
        if (s == 3) {
            return EnumFacing.SOUTH;
        }
        if (s == 4) {
            return EnumFacing.WEST;
        }
        if (s == 5) {
            return EnumFacing.EAST;
        }
        return null;
    }

    private static int metaAt(World world, BlockPos pos) {
        net.minecraft.block.state.IBlockState st = world.getBlockState(pos);
        return st.getBlock().getMetaFromState(st);
    }

    public static void addCuboidsForRaytraceStation(List<AxisAlignedBB> list, World world, BlockPos pos) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        int meta = metaAt(world, pos);
        EnumFacing d = EnumFacing.byIndex(meta >= 8 ? meta - 8 : meta);
        if (d == EnumFacing.NORTH || d == EnumFacing.SOUTH) {
            list.add(new AxisAlignedBB(x, y, z, x + 0.05, y + 1, z + 1));
            list.add(new AxisAlignedBB(x + 0.95, y, z, x + 1, y + 1, z + 1));
            if (d == EnumFacing.NORTH) {
                list.add(new AxisAlignedBB(x, y, z + 0.95, x + 1, y + 1, z + 1));
            } else {
                list.add(new AxisAlignedBB(x, y, z, x + 1, y + 1, z + 0.05));
            }
        } else {
            list.add(new AxisAlignedBB(x, y, z, x + 1, y + 1, z + 0.05));
            list.add(new AxisAlignedBB(x, y, z + 0.95, x + 1, y + 1, z + 1));
            if (d == EnumFacing.WEST) {
                list.add(new AxisAlignedBB(x + 0.95, y, z, x + 1, y + 1, z + 1));
            } else {
                list.add(new AxisAlignedBB(x, y, z, x + 0.05, y + 1, z + 1));
            }
        }
        if (meta >= 8) {
            if (world.getBlockState(pos.up()).getBlock() != BlockTube.instance) {
                list.add(new AxisAlignedBB(x, y + 0.95, z, x + 1, y + 1, z + 1));
            }
        } else if (world.getBlockState(pos.down()).getBlock() != BlockTube.instance) {
            list.add(new AxisAlignedBB(x, y, z, x + 1, y + 0.05, z + 1));
        }
    }

    public static void addCuboidsForRaytraceStationHorizontal(List<AxisAlignedBB> list, World world, BlockPos pos) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        int meta = metaAt(world, pos);
        EnumFacing d = EnumFacing.byIndex(meta >= 8 ? meta - 8 : meta);
        if (d == EnumFacing.NORTH || d == EnumFacing.SOUTH) {
            list.add(new AxisAlignedBB(x, y, z, x + 0.05, y + 1, z + 1));
            list.add(new AxisAlignedBB(x + 0.95, y, z, x + 1, y + 1, z + 1));
            if (d == EnumFacing.NORTH) {
                if (meta >= 8) {
                    list.add(new AxisAlignedBB(x, y, z + 0.95, x + 1, y + 1, z + 1));
                } else {
                    list.add(new AxisAlignedBB(x, y, z, x + 1, y + 1, z + 0.05));
                }
            } else if (meta >= 8) {
                list.add(new AxisAlignedBB(x, y, z, x + 1, y + 1, z + 0.05));
            } else {
                list.add(new AxisAlignedBB(x, y, z + 0.95, x + 1, y + 1, z + 1));
            }
        } else {
            list.add(new AxisAlignedBB(x, y, z, x + 1, y + 1, z + 0.05));
            list.add(new AxisAlignedBB(x, y, z + 0.95, x + 1, y + 1, z + 1));
            if (d == EnumFacing.WEST) {
                if (meta >= 8) {
                    list.add(new AxisAlignedBB(x + 0.95, y, z, x + 1, y + 1, z + 1));
                } else {
                    list.add(new AxisAlignedBB(x, y, z, x + 0.05, y + 1, z + 1));
                }
            } else if (meta >= 8) {
                list.add(new AxisAlignedBB(x, y, z, x + 0.05, y + 1, z + 1));
            } else {
                list.add(new AxisAlignedBB(x + 0.95, y, z, x + 1, y + 1, z + 1));
            }
        }
        list.add(new AxisAlignedBB(x, y, z, x + 1, y + 0.05, z + 1));
    }

    /** Replaces the repacked CodeChickenLib RayTracer: nearest-hit over a set of world-space boxes. */
    public static RayTraceResult rayTraceBoxes(World world, BlockPos pos, Block block, Vec3d start, Vec3d end, List<AxisAlignedBB> boxes) {
        RayTraceResult best = null;
        double bestDist = Double.POSITIVE_INFINITY;
        for (AxisAlignedBB box : boxes) {
            if (box == null) {
                continue;
            }
            RayTraceResult hit = box.calculateIntercept(start, end);
            if (hit == null) {
                continue;
            }
            double d = start.squareDistanceTo(hit.hitVec);
            if (d < bestDist) {
                bestDist = d;
                best = hit;
            }
        }
        if (best == null) {
            return null;
        }
        return new RayTraceResult(best.hitVec, best.sideHit, pos);
    }
}
