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
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import tubeTransportSystem.TubeTransportSystem;
import tubeTransportSystem.client.TubeRenderData;
import tubeTransportSystem.util.ConnectedTextures;
import tubeTransportSystem.util.IConnectable;
import tubeTransportSystem.util.UnlistedProperty;
import tubeTransportSystem.util.Utilities;

public class BlockTube extends Block implements IConnectable {
    public static BlockTube instance;

    public static final PropertyInteger META = PropertyInteger.create("meta", 0, 15);
    public static final IUnlistedProperty<TubeRenderData> RENDER = new UnlistedProperty<TubeRenderData>("render", TubeRenderData.class);

    private final ConnectedTextures[] textures = new ConnectedTextures[12];

    public BlockTube() {
        super(Material.ROCK);
        setTranslationKey("tube");
        instance = this;
        setLightOpacity(0);
        setHardness(5.0f);
        setCreativeTab(TubeTransportSystem.creativeTab);
        setDefaultState(blockState.getBaseState().withProperty(META, 0));
        for (int i = 0; i < 6; i++) {
            this.textures[i] = new ConnectedTextures("tts:tube" + i + "/%s", this, i);
        }
        for (int i = 0; i < 6; i++) {
            this.textures[i + 6] = new ConnectedTextures("tts:tube/%s", this, i);
        }
    }

    public ConnectedTextures[] getTextures() {
        return textures;
    }

    // --- state / meta (identity 0..15) ---
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

    // --- render flags ---
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
     * The original rendered in pass 1, which in 1.7.10 is the alpha-tested pass. Every tts
     * texture is fully opaque or fully transparent (no partial alpha anywhere in the 123
     * sprites), so CUTOUT_MIPPED reproduces it exactly. It also keeps the tube in the terrain
     * pass with depth writes, instead of the translucent pass which draws after and on top of
     * sky-level geometry such as another mod's clouds.
     */
    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }

    @Override
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return layer == BlockRenderLayer.CUTOUT_MIPPED;
    }

    // --- connected-texture render data (replaces func_149673_e + RenderTube) ---
    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        if (!(state instanceof IExtendedBlockState)) {
            return state;
        }
        int meta = getMetaFromState(world.getBlockState(pos));
        if (meta > 5) {
            meta = 0;
        }
        TubeRenderData data = new TubeRenderData(meta);
        EnumFacing axis = EnumFacing.byIndex(meta);
        for (int s = 0; s < 6; s++) {
            ConnectedTextures set = pickSet(meta, axis, s);
            data.outerSprite[s] = set.spriteName(set.getIndexForSide(world, pos, s));
            data.innerSprite[s] = set.spriteName(set.getIndexForSideForInternal(world, pos, s));
        }
        for (EnumFacing f : EnumFacing.values()) {
            data.connByFacing[f.getIndex()] = canConnectTo(world, pos, f);
        }
        return ((IExtendedBlockState) state).withProperty(RENDER, data);
    }

    /** Mirrors BlockTube.func_149673_e: cap faces (along axis) use tube/, lateral use tube{dir}. */
    private ConnectedTextures pickSet(int meta, EnumFacing axis, int s) {
        boolean cap;
        if (axis == EnumFacing.UP || axis == EnumFacing.DOWN) {
            cap = (s == 0 || s == 1);
        } else if (axis == EnumFacing.NORTH || axis == EnumFacing.SOUTH) {
            cap = (s == 2 || s == 3);
        } else {
            cap = (s == 4 || s == 5);
        }
        return cap ? textures[meta + 6] : textures[meta];
    }

    /**
     * Port of the 1.7.10 override. There the callback received the NEIGHBOUR cell and
     * mapped back to self with getCoordinatesFromSide; in 1.12.2 pos is already self and
     * the neighbour is pos.offset(side). Culls the outer skin and the inner connector box
     * alike, as RenderTube did.
     *
     * DEVIATION: the original tests canConnectToStrict (same block AND same direction meta),
     * this tests canConnectTo (same block). Strict leaves a corner such as UP into NORTH with
     * both tubes drawing a full face on the shared plane, and the inner boxes are already
     * flush there, so four coplanar quads z-fight. Collision already treats the pair as
     * connected through canConnectTo and lets you ride the corner, so the wall it drew was
     * one you could walk through anyway. Loose culling only changes adjacent tubes whose
     * directions differ, which is exactly the corner case.
     */
    @Override
    public boolean shouldSideBeRendered(IBlockState state, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        return !canConnectTo(blockAccess, pos, side);
    }

    // --- entity physics inside the tube ---
    @Override
    public void onEntityCollision(World world, BlockPos pos, IBlockState state, Entity entity) {
        if (entity == null) {
            return;
        }
        Utilities.entityAccelerate(entity, EnumFacing.byIndex(getMetaFromState(state)));
        Utilities.entityLimitSpeed(entity);
        Utilities.entityResetFall(entity);
        Utilities.entityResetWalk(entity);
    }

    // --- collision: wall on each side NOT connected to another tube ---
    @Override
    public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB entityBox,
            List<AxisAlignedBB> collidingBoxes, @Nullable Entity entity, boolean isActualState) {
        if (entity == null) {
            return;
        }
        EnumFacing dir = EnumFacing.byIndex(getMetaFromState(state));
        List<AxisAlignedBB> axis = new ArrayList<AxisAlignedBB>();
        for (EnumFacing d : EnumFacing.values()) {
            if (!(!canConnectTo(world, pos, d)
                    && (dir != EnumFacing.UP && dir != EnumFacing.DOWN || d != EnumFacing.UP && d != EnumFacing.DOWN)
                    && (dir != EnumFacing.NORTH && dir != EnumFacing.SOUTH || d != EnumFacing.NORTH && d != EnumFacing.SOUTH)
                    && (dir != EnumFacing.EAST && dir != EnumFacing.WEST || d != EnumFacing.EAST && d != EnumFacing.WEST))) {
                continue;
            }
            axis.add(Utilities.getCollisionBoxPart(pos, d));
        }
        for (AxisAlignedBB a : axis) {
            if (a == null || !entityBox.intersects(a)) {
                continue;
            }
            collidingBoxes.add(a);
        }
    }

    @Override
    public RayTraceResult collisionRayTrace(IBlockState state, World world, BlockPos pos, Vec3d start, Vec3d end) {
        LinkedList<AxisAlignedBB> boxes = new LinkedList<AxisAlignedBB>();
        boolean[] connectTo = new boolean[6];
        for (int i = 0; i < 6; i++) {
            connectTo[i] = canConnectTo(world, pos, EnumFacing.byIndex(i));
        }
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        if (!connectTo[0]) {
            boxes.add(new AxisAlignedBB(x, y, z, x + 1, y + 0.05, z + 1));
        }
        if (!connectTo[1]) {
            boxes.add(new AxisAlignedBB(x, y + 0.95, z, x + 1, y + 1, z + 1));
        }
        if (!connectTo[2]) {
            boxes.add(new AxisAlignedBB(x, y, z, x + 1, y + 1, z + 0.05));
        }
        if (!connectTo[3]) {
            boxes.add(new AxisAlignedBB(x, y, z + 0.95, x + 1, y + 1, z + 1));
        }
        if (!connectTo[4]) {
            boxes.add(new AxisAlignedBB(x, y, z, x + 0.05, y + 1, z + 1));
        }
        if (!connectTo[5]) {
            boxes.add(new AxisAlignedBB(x + 0.95, y, z, x + 1, y + 1, z + 1));
        }
        return Utilities.rayTraceBoxes(world, pos, this, start, end, boxes);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return FULL_BLOCK_AABB;
    }

    // --- IConnectable ---
    @Override
    public boolean canConnectTo(IBlockAccess blockAccess, BlockPos pos, EnumFacing d) {
        return blockAccess.getBlockState(pos.offset(d)).getBlock() == this;
    }

    @Override
    public boolean canConnectToStrict(IBlockAccess blockAccess, BlockPos pos, EnumFacing d) {
        BlockPos o = pos.offset(d);
        return blockAccess.getBlockState(o).getBlock() == this
                && getMetaFromState(blockAccess.getBlockState(o)) == getMetaFromState(blockAccess.getBlockState(pos));
    }
}
