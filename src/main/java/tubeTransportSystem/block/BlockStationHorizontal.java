package tubeTransportSystem.block;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import tubeTransportSystem.client.StationRenderData;
import tubeTransportSystem.util.IConnectable;
import tubeTransportSystem.util.Utilities;

public class BlockStationHorizontal extends Block implements IConnectable {
    public static BlockStationHorizontal instance;
    public static final int SHIFT = 8;

    public static final PropertyInteger META = PropertyInteger.create("meta", 0, 15);

    public BlockStationHorizontal() {
        super(Material.ROCK);
        setTranslationKey("station");
        instance = this;
        setLightOpacity(1);
        setHardness(5.0f);
        setDefaultState(blockState.getBaseState().withProperty(META, 0));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new ExtendedBlockState(this, new IProperty[]{META}, new IUnlistedProperty[]{BlockStation.RENDER});
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(META, meta & 15);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(META);
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    /** Cut-out entrance sprites, same reason as BlockStation. */
    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }

    @Override
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return layer == BlockRenderLayer.CUTOUT_MIPPED;
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        // Original drops nothing (stationH has no registered ItemBlock).
    }

    /** Port of func_149673_e per-side table. */
    public static String spriteForSide(int meta, int s) {
        if (meta == EnumFacing.NORTH.getIndex()) {
            if (s == 1) return "tts:station_entr2";
            if (s == 0) return "tts:station_side1";
            if (s == 4) return "tts:station_side4";
            if (s == 5) return "tts:station_side3";
        } else if (meta == EnumFacing.NORTH.getIndex() + 8) {
            if (s == 1) return "tts:station_entr1";
            if (s == 0) return "tts:station_side2";
            if (s == 4) return "tts:station_side3";
            if (s == 5) return "tts:station_side4";
        } else if (meta == EnumFacing.EAST.getIndex()) {
            if (s == 1) return "tts:station_entr3";
            if (s == 0) return "tts:station_side3";
            if (s == 2) return "tts:station_side4";
            if (s == 3) return "tts:station_side3";
        } else if (meta == EnumFacing.EAST.getIndex() + 8) {
            if (s == 1) return "tts:station_entr4";
            if (s == 0) return "tts:station_side4";
            if (s == 2) return "tts:station_side3";
            if (s == 3) return "tts:station_side4";
        } else if (meta == EnumFacing.SOUTH.getIndex()) {
            if (s == 1) return "tts:station_entr1";
            if (s == 0) return "tts:station_side2";
            if (s == 4) return "tts:station_side3";
            if (s == 5) return "tts:station_side4";
        } else if (meta == EnumFacing.SOUTH.getIndex() + 8) {
            if (s == 1) return "tts:station_entr2";
            if (s == 0) return "tts:station_side1";
            if (s == 4) return "tts:station_side4";
            if (s == 5) return "tts:station_side3";
        } else if (meta == EnumFacing.WEST.getIndex()) {
            if (s == 1) return "tts:station_entr4";
            if (s == 0) return "tts:station_side4";
            if (s == 2) return "tts:station_side3";
            if (s == 3) return "tts:station_side4";
        } else if (meta == EnumFacing.WEST.getIndex() + 8) {
            if (s == 1) return "tts:station_entr3";
            if (s == 0) return "tts:station_side3";
            if (s == 2) return "tts:station_side4";
            if (s == 3) return "tts:station_side3";
        }
        return "tts:station_misc";
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        if (!(state instanceof IExtendedBlockState)) {
            return state;
        }
        int meta = getMetaFromState(world.getBlockState(pos));
        StationRenderData data = new StationRenderData();
        for (int s = 0; s < 6; s++) {
            data.sprite[s] = spriteForSide(meta, s);
        }
        // RenderStation: nudge the inner box 0.01 outwards where the run continues, so the
        // two halves and any attached tube join without a seam.
        EnumFacing d = EnumFacing.byIndex(meta < SHIFT ? meta : meta - SHIFT);
        EnumFacing o = d.getOpposite();
        boolean joinDown = joins(world, pos.offset(d));
        boolean joinUp = joins(world, pos.offset(o));
        double negX = 0.0;
        double posX = 0.0;
        double negZ = 0.0;
        double posZ = 0.0;
        if (d == EnumFacing.NORTH) {
            if (joinDown) negZ = -0.01;
            if (joinUp) posZ = 0.01;
        } else if (d == EnumFacing.SOUTH) {
            if (joinUp) negZ = -0.01;
            if (joinDown) posZ = 0.01;
        } else if (d == EnumFacing.EAST) {
            if (joinUp) negX = -0.01;
            if (joinDown) posX = 0.01;
        } else if (d == EnumFacing.WEST) {
            if (joinDown) negX = -0.01;
            if (joinUp) posX = 0.01;
        }
        data.minX = 0.01 + negX;
        data.minZ = 0.01 + negZ;
        data.maxX = 0.99 + posX;
        data.maxZ = 0.99 + posZ;
        // uvRotateBottom, then East/West/South/North; 3 means a 180 degree turn
        data.uvRotation[0] = (d == EnumFacing.NORTH || d == EnumFacing.SOUTH) ? 0 : 180;
        data.uvRotation[2] = 180;
        data.uvRotation[3] = 180;
        data.uvRotation[4] = 180;
        data.uvRotation[5] = 180;
        return ((IExtendedBlockState) state).withProperty(BlockStation.RENDER, data);
    }

    @Override
    public RayTraceResult collisionRayTrace(IBlockState state, World world, BlockPos pos, Vec3d start, Vec3d end) {
        LinkedList<AxisAlignedBB> boxes = new LinkedList<AxisAlignedBB>();
        Utilities.addCuboidsForRaytraceStationHorizontal(boxes, world, pos);
        return Utilities.rayTraceBoxes(world, pos, this, start, end, boxes);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return FULL_BLOCK_AABB;
    }

    /** RenderStation treats another horizontal station or a tube as a continuation of the run. */
    private boolean joins(IBlockAccess world, BlockPos pos) {
        Block b = world.getBlockState(pos).getBlock();
        return b == this || b == BlockTube.instance;
    }

    private BlockPos getPartner(World world, BlockPos pos) {
        return getPartner(pos, metaAt(world, pos));
    }

    private BlockPos getPartner(BlockPos pos, int m) {
        EnumFacing d = m < 8 ? EnumFacing.byIndex(m).getOpposite() : EnumFacing.byIndex(m - 8);
        return pos.offset(d);
    }

    @Override
    public boolean shouldSideBeRendered(IBlockState state, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        int s = side.getIndex();
        if (s == 0) {
            return true;
        }
        // 1.7.10 handed this callback the NEIGHBOUR cell and stepped back to self; in 1.12.2
        // pos is already self, so read the meta here and the neighbour at pos.offset(side).
        int meta = metaAt(blockAccess, pos);
        BlockPos n = pos.offset(side);
        if (meta == EnumFacing.SOUTH.getIndex()) {
            if (s == 3 && blockAccess.getBlockState(n).getBlock() == BlockTube.instance) {
                return false;
            }
            return s != 2;
        }
        if (meta == EnumFacing.SOUTH.getIndex() + 8) {
            if (s == 2 && blockAccess.getBlockState(n).getBlock() == BlockTube.instance) {
                return false;
            }
            return s != 3;
        }
        if (meta == EnumFacing.NORTH.getIndex()) {
            if (s == 2 && blockAccess.getBlockState(n).getBlock() == BlockTube.instance) {
                return false;
            }
            return s != 3;
        }
        if (meta == EnumFacing.NORTH.getIndex() + 8) {
            if (s == 3 && blockAccess.getBlockState(n).getBlock() == BlockTube.instance) {
                return false;
            }
            return s != 2;
        }
        if (meta == EnumFacing.EAST.getIndex()) {
            if (s == 5 && blockAccess.getBlockState(n).getBlock() == BlockTube.instance) {
                return false;
            }
            return s != 4;
        }
        if (meta == EnumFacing.EAST.getIndex() + 8) {
            if (s == 4 && blockAccess.getBlockState(n).getBlock() == BlockTube.instance) {
                return false;
            }
            return s != 5;
        }
        if (meta == EnumFacing.WEST.getIndex()) {
            if (s == 4 && blockAccess.getBlockState(n).getBlock() == BlockTube.instance) {
                return false;
            }
            return s != 5;
        }
        if (meta == EnumFacing.WEST.getIndex() + 8) {
            if (s == 5 && blockAccess.getBlockState(n).getBlock() == BlockTube.instance) {
                return false;
            }
            return s != 4;
        }
        return true;
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB entityBox,
            List<AxisAlignedBB> collidingBoxes, @Nullable Entity entity, boolean isActualState) {
        if (entity == null) {
            return;
        }
        List<AxisAlignedBB> axis = new ArrayList<AxisAlignedBB>();
        int meta = getMetaFromState(state);
        if (meta == EnumFacing.NORTH.getIndex() || meta == EnumFacing.NORTH.getIndex() + 8
                || meta == EnumFacing.SOUTH.getIndex() || meta == EnumFacing.SOUTH.getIndex() + 8) {
            axis.add(Utilities.getCollisionBoxPart(pos, EnumFacing.EAST));
            axis.add(Utilities.getCollisionBoxPart(pos, EnumFacing.WEST));
        } else if (meta == EnumFacing.EAST.getIndex() || meta == EnumFacing.EAST.getIndex() + 8
                || meta == EnumFacing.WEST.getIndex() || meta == EnumFacing.WEST.getIndex() + 8) {
            axis.add(Utilities.getCollisionBoxPart(pos, EnumFacing.NORTH));
            axis.add(Utilities.getCollisionBoxPart(pos, EnumFacing.SOUTH));
        }
        EnumFacing d = meta >= 8 ? EnumFacing.byIndex(meta - 8).getOpposite() : EnumFacing.byIndex(meta);
        if (entity.isSneaking() || world.getBlockState(pos.offset(d)).getBlock() != BlockTube.instance) {
            axis.add(Utilities.getCollisionBoxPart(pos, d));
        }
        axis.add(Utilities.getCollisionBoxPart(pos, EnumFacing.DOWN));
        for (AxisAlignedBB a : axis) {
            if (a == null || !entityBox.intersects(a)) {
                continue;
            }
            collidingBoxes.add(a);
        }
    }

    @Override
    public void onBlockExploded(World world, BlockPos pos, Explosion explosion) {
        BlockPos partner = getPartner(world, pos);
        if (world.getBlockState(partner).getBlock() == this) {
            world.setBlockToAir(partner);
        }
        super.onBlockExploded(world, pos, explosion);
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        BlockPos partner = getPartner(pos, getMetaFromState(state));
        if (world.getBlockState(partner).getBlock() == this) {
            world.setBlockToAir(partner);
        }
        super.breakBlock(world, pos, state);
    }

    @Override
    public boolean canConnectTo(IBlockAccess blockAccess, BlockPos pos, EnumFacing d) {
        if (d == EnumFacing.UP || d == EnumFacing.DOWN) {
            return false;
        }
        BlockPos o = pos.offset(d);
        Block block = blockAccess.getBlockState(o).getBlock();
        int meta = metaAt(blockAccess, o);
        int thisMeta = metaAt(blockAccess, pos);
        return block == this && thisMeta >= 8 ? meta == thisMeta - 8 : thisMeta + 8 == meta;
    }

    @Override
    public boolean canConnectToStrict(IBlockAccess blockAccess, BlockPos pos, EnumFacing d) {
        return canConnectTo(blockAccess, pos, d);
    }

    private int metaAt(IBlockAccess world, BlockPos pos) {
        IBlockState st = world.getBlockState(pos);
        return st.getBlock().getMetaFromState(st);
    }
}
