package tubeTransportSystem.util;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Items;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tubeTransportSystem.block.BlockTube;

public class CreativeTab extends CreativeTabs {
    public CreativeTab() {
        super("tts");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ItemStack createIcon() {
        if (BlockTube.instance != null) {
            Item item = Item.getItemFromBlock(BlockTube.instance);
            if (item != Items.AIR) {
                return new ItemStack(item, 1, 0);
            }
        }
        return new ItemStack(Items.ENDER_PEARL);
    }
}
