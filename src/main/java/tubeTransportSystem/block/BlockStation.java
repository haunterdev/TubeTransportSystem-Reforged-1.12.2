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
import net.minecraft.init.Blocks;
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
import tubeTransportSystem.TubeTransportSystem;
import tubeTransportSystem.client.StationRenderData;
import tubeTransportSystem.util.IConnectable;
import tubeTransportSystem.util.UnlistedProperty;
import tubeTransportSystem.util.Utilities;

public class BlockStation extends Block implements IConnectable {
    public static BlockStation instance;
    public static final int SHIFT = 8;

    public static final PropertyInteger META = PropertyInteger.create("meta", 0, 15);
    public static final IUnlistedProperty<StationRenderData> RENDER =
            new UnlistedProperty<StationRenderData>("station_render", StationRenderData.class);

    public BlockStation() {
        super(Material.ROCK);
        setTranslationKey("station");
        instance = this;
        setLightOpacity(1);
        setHardness(5.0f);
        setCreativeTab(TubeTransportSystem.creativeTab);
        setDefaultState(blockState.getBaseState().withProperty(META, 0));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new ExtendedBlockState(this, new IProperty[]{META}, new IUnlistedProperty[]{RENDER});
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

    /**
     * The entrance sprites are cut out (alpha 0 or 255, nothing between). 1.7.10 rendered the
     * station in pass 0, which is alpha tested, so those texels were see-through; the 1.12.2
     * SOLID layer has no alpha test and drew them white instead.
     */
    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }

    @Override
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return layer == BlockRenderLayer.CUTOUT_MIPPED;
    }

    /** Per-side sprite table, port of func_149673_e. */
    public static String spriteForSide(int meta, int s) {
        if (meta == EnumFacing.NORTH.getIndex() && s == 2) {
            return "tts:station_entr1";
        }
        if (meta == EnumFacing.NORTH.getIndex() + 8 && s == 2) {
            return "tts:station_entr2";
        }
        if (meta == EnumFacing.EAST.getIndex() && s == 5) {
            return "tts:station_entr1";
        }
        if (meta == EnumFacing.EAST.getIndex() + 8 && s == 5) {
            return "tts:station_entr2";
        }
        if (meta == EnumFacing.SOUTH.getIndex() && s == 3) {
            return "tts:station_entr1";
        }
        if (meta == EnumFacing.SOUTH.getIndex() + 8 && s == 3) {
            return "tts:station_entr2";
        }
        if (meta == EnumFacing.WEST.getIndex() && s == 4) {
            return "tts:station_entr1";
        }
        if (meta == EnumFacing.WEST.getIndex() + 8 && s == 4) {
            return "tts:station_entr2";
        }
        return (s == 0 || s == 1) ? "tts:station_misc" : (meta >= 8 ? "tts:station_side1" : "tts:station_side2");
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
        // RenderStation: the inner box goes flush on the Y side that joins a tube or the partner half
        Block below = world.getBlockState(pos.down()).getBlock();
        Block above = world.getBlockState(pos.up()).getBlock();
        data.minY = (below != BlockTube.instance && below != this) ? 0.01 : 0.0;
        data.maxY = (above != BlockTube.instance && above != this) ? 0.99 : 1.0;
        return ((IExtendedBlockState) state).withProperty(RENDER, data);
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        if (getMetaFromState(state) < 8) {
            super.getDrops(drops, world, pos, state, fortune);
        }
    }

    @Override
    public RayTraceResult collisionRayTrace(IBlockState state, World world, BlockPos pos, Vec3d start, Vec3d end) {
        LinkedList<AxisAlignedBB> boxes = new LinkedList<AxisAlignedBB>();
        Utilities.addCuboidsForRaytraceStation(boxes, world, pos);
        return Utilities.rayTraceBoxes(world, pos, this, start, end, boxes);
    }

    @Override
    public boolean shouldSideBeRendered(IBlockState state, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        int s = side.getIndex();
        if (s > 1 && s <= 5) {
            return true;
        }
        // 1.7.10 handed this callback the NEIGHBOUR cell; 1.12.2 hands it self, so offset here.
        IBlockState neighbour = blockAccess.getBlockState(pos.offset(side));
        if (neighbour.getBlock() == this) {
            return getMetaFromState(neighbour) < SHIFT && s == 1;
        }
        return neighbour.getBlock() != BlockTube.instance;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return FULL_BLOCK_AABB;
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB entityBox,
            List<AxisAlignedBB> collidingBoxes, @Nullable Entity entity, boolean isActualState) {
        if (entity == null) {
            return;
        }
        int meta = getMetaFromState(state);
        List<AxisAlignedBB> axis = new ArrayList<AxisAlignedBB>();
        if (meta == EnumFacing.NORTH.getIndex() || meta == EnumFacing.NORTH.getIndex() + 8) {
            axis.add(Utilities.getCollisionBoxPart(pos, EnumFacing.SOUTH));
            axis.add(Utilities.getCollisionBoxPart(pos, EnumFacing.EAST));
            axis.add(Utilities.getCollisionBoxPart(pos, EnumFacing.WEST));
        } else if (meta == EnumFacing.SOUTH.getIndex() || meta == EnumFacing.SOUTH.getIndex() + 8) {
            axis.add(Utilities.getCollisionBoxPart(pos, EnumFacing.NORTH));
            axis.add(Utilities.getCollisionBoxPart(pos, EnumFacing.EAST));
            axis.add(Utilities.getCollisionBoxPart(pos, EnumFacing.WEST));
        } else if (meta == EnumFacing.EAST.getIndex() || meta == EnumFacing.EAST.getIndex() + 8) {
            axis.add(Utilities.getCollisionBoxPart(pos, EnumFacing.WEST));
            axis.add(Utilities.getCollisionBoxPart(pos, EnumFacing.NORTH));
            axis.add(Utilities.getCollisionBoxPart(pos, EnumFacing.SOUTH));
        } else if (meta == EnumFacing.WEST.getIndex() || meta == EnumFacing.WEST.getIndex() + 8) {
            axis.add(Utilities.getCollisionBoxPart(pos, EnumFacing.EAST));
            axis.add(Utilities.getCollisionBoxPart(pos, EnumFacing.NORTH));
            axis.add(Utilities.getCollisionBoxPart(pos, EnumFacing.SOUTH));
        }
        if (meta >= 8) {
            if (entity.isSneaking() && metaAt(world, pos.up()) == EnumFacing.UP.getIndex()) {
                axis.add(Utilities.getCollisionBoxPart(pos, EnumFacing.UP));
            } else if (world.getBlockState(pos.up()).getBlock() != BlockTube.instance) {
                axis.add(Utilities.getCollisionBoxPart(pos, EnumFacing.UP));
            }
        } else if (entity.posY >= pos.getY()) {
            if (entity.isSneaking() && metaAt(world, pos.down()) == EnumFacing.DOWN.getIndex()) {
                axis.add(Utilities.getCollisionBoxPartFloor(pos));
            } else if (world.getBlockState(pos.down()).getBlock() != BlockTube.instance) {
                axis.add(Utilities.getCollisionBoxPartFloor(pos));
            } else if (metaAt(world, pos.down()) != EnumFacing.DOWN.getIndex()) {
                axis.add(Utilities.getCollisionBoxPartFloor(pos));
            }
        }
        for (AxisAlignedBB a : axis) {
            if (a == null || !entityBox.intersects(a)) {
                continue;
            }
            collidingBoxes.add(a);
        }
    }

    private int metaAt(World world, BlockPos pos) {
        IBlockState st = world.getBlockState(pos);
        return st.getBlock().getMetaFromState(st);
    }

    @Override
    public void onBlockExploded(World world, BlockPos pos, Explosion explosion) {
        int meta = getMetaFromState(world.getBlockState(pos));
        if (meta >= 8 && world.getBlockState(pos.down()).getBlock() == this) {
            world.setBlockToAir(pos.down());
        } else if (meta < 8 && world.getBlockState(pos.up()).getBlock() == this) {
            world.setBlockToAir(pos.up());
        }
        super.onBlockExploded(world, pos, explosion);
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        int meta = getMetaFromState(state);
        if (meta >= 8 && world.getBlockState(pos.down()).getBlock() == this) {
            world.setBlockToAir(pos.down());
        } else if (meta < 8 && world.getBlockState(pos.up()).getBlock() == this) {
            world.setBlockToAir(pos.up());
        }
        super.breakBlock(world, pos, state);
    }

    @Override
    public void onEntityCollision(World world, BlockPos pos, IBlockState state, Entity entity) {
        if (entity == null) {
            return;
        }
        int meta = getMetaFromState(state);
        if (!entity.isSneaking() && meta >= 8 && world.getBlockState(pos.up()).getBlock() == BlockTube.instance
                && metaAt(world, pos.up()) == EnumFacing.UP.getIndex()) {
            Utilities.entityAccelerate(entity, EnumFacing.UP);
            Utilities.entityAccelerate(entity, EnumFacing.UP);
        }
        Utilities.entityLimitSpeed(entity);
    }

    @Override
    public boolean canConnectTo(IBlockAccess blockAccess, BlockPos pos, EnumFacing d) {
        if (d != EnumFacing.UP && d != EnumFacing.DOWN) {
            return false;
        }
        BlockPos o = pos.offset(d);
        Block block = blockAccess.getBlockState(o).getBlock();
        int meta = metaAt2(blockAccess, o);
        int thisMeta = metaAt2(blockAccess, pos);
        return block == this && thisMeta >= 8 ? meta == thisMeta - 8 : thisMeta + 8 == meta;
    }

    @Override
    public boolean canConnectToStrict(IBlockAccess blockAccess, BlockPos pos, EnumFacing d) {
        return canConnectTo(blockAccess, pos, d);
    }

    private int metaAt2(IBlockAccess world, BlockPos pos) {
        IBlockState st = world.getBlockState(pos);
        return st.getBlock().getMetaFromState(st);
    }
}
