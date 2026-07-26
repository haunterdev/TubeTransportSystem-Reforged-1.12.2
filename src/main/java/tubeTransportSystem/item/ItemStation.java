package tubeTransportSystem.item;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import tubeTransportSystem.block.BlockStation;
import tubeTransportSystem.block.BlockStationHorizontal;
import tubeTransportSystem.util.Utilities;

public class ItemStation extends ItemBlock {
    public ItemStation(Block b) {
        super(b);
    }

    @Override
    public boolean canPlaceBlockOnSide(World world, BlockPos pos, EnumFacing side, EntityPlayer player, ItemStack stack) {
        return super.canPlaceBlockOnSide(world, pos, side, player, stack) && world.isAirBlock(pos.up());
    }

    @Override
    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side,
            float hitX, float hitY, float hitZ, IBlockState newState) {
        int s = side.getIndex();
        if (s == 0 || s == 1) {
            if (!world.isAirBlock(pos.up())) {
                return false;
            }
            int metadata = Utilities.entityGetDirection(player).getIndex();
            if (world.setBlockState(pos, BlockStation.instance.getStateFromMeta(metadata), 3)
                    && world.setBlockState(pos.up(), BlockStation.instance.getStateFromMeta(metadata + 8), 3)) {
                IBlockState b1 = world.getBlockState(pos);
                if (b1.getBlock() == BlockStation.instance) {
                    BlockStation.instance.onBlockPlacedBy(world, pos, b1, player, stack);
                }
                IBlockState b2 = world.getBlockState(pos.up());
                if (b2.getBlock() == BlockStation.instance) {
                    BlockStation.instance.onBlockPlacedBy(world, pos.up(), b2, player, stack);
                }
                return true;
            }
        } else {
            EnumFacing d = Utilities.entityGetDirection(player);
            BlockPos pos2 = pos.offset(d);
            int metadata = d.getIndex();
            if (!world.isAirBlock(pos2)) {
                return false;
            }
            if (world.setBlockState(pos, BlockStationHorizontal.instance.getStateFromMeta(metadata + 8), 3)
                    && world.setBlockState(pos2, BlockStationHorizontal.instance.getStateFromMeta(metadata), 3)) {
                IBlockState b1 = world.getBlockState(pos);
                if (b1.getBlock() == BlockStationHorizontal.instance) {
                    BlockStationHorizontal.instance.onBlockPlacedBy(world, pos, b1, player, stack);
                }
                IBlockState b2 = world.getBlockState(pos2);
                if (b2.getBlock() == BlockStationHorizontal.instance) {
                    BlockStationHorizontal.instance.onBlockPlacedBy(world, pos2, b2, player, stack);
                }
                return true;
            }
        }
        return false;
    }
}
