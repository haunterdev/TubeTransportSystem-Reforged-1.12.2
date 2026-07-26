package tubeTransportSystem.item;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import tubeTransportSystem.block.BlockTube;

public class ItemTube extends ItemBlock {
    public static ItemTube instance;

    public ItemTube(Block b) {
        super(b);
        instance = this;
        setHasSubtypes(true);
        setMaxDamage(0);
    }

    @Override
    public int getMetadata(int damage) {
        return damage;
    }

    @Override
    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side,
            float hitX, float hitY, float hitZ, IBlockState newState) {
        int damage = stack.getMetadata();
        // Original: meta = metadata >= 10 ? metadata - 10 : side. An undirected tube takes its
        // direction from the clicked face. EnumFacing indices match 1.7.10 ForgeDirection 0-5.
        int meta = damage >= 10 ? damage - 10 : side.getIndex();
        if (!world.setBlockState(pos, BlockTube.instance.getStateFromMeta(meta), 3)) {
            return false;
        }
        IBlockState placed = world.getBlockState(pos);
        if (placed.getBlock() == BlockTube.instance) {
            BlockTube.instance.onBlockPlacedBy(world, pos, placed, player, stack);
        }
        return true;
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (!isInCreativeTab(tab)) {
            return;
        }
        items.add(new ItemStack(this, 1, 0));
        items.add(new ItemStack(this, 1, 10));
        items.add(new ItemStack(this, 1, 11));
        items.add(new ItemStack(this, 1, 12));
        items.add(new ItemStack(this, 1, 13));
        items.add(new ItemStack(this, 1, 14));
        items.add(new ItemStack(this, 1, 15));
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        int meta = stack.getMetadata();
        if (meta < 10) {
            // Not in the 1.7.10 original, which showed no tooltip here, so nothing told a new player that an
            // undirected tube takes its direction from the face it is placed against.
            tooltip.add(TextFormatting.GRAY + I18n.format("item.tube.autodirection.1"));
            tooltip.add(TextFormatting.DARK_GRAY + I18n.format("item.tube.autodirection.2"));
            tooltip.add(TextFormatting.DARK_GRAY + I18n.format("item.tube.autodirection.3"));
            return;
        }
        if (meta >= 10) {
            // I18n.format already runs String.format on the result, so formatting the value a
            // second time is what produced "Format error: Direction: %s". Pass the argument in.
            tooltip.add(TextFormatting.GRAY + I18n.format("item.tube.forced",
                    TextFormatting.AQUA + I18n.format("item.tube.direction." + (meta - 10))));
        }
    }
}
