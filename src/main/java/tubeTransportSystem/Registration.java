package tubeTransportSystem;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;
import tubeTransportSystem.block.BlockStation;
import tubeTransportSystem.block.BlockStationHorizontal;
import tubeTransportSystem.block.BlockTube;
import tubeTransportSystem.item.ItemStation;
import tubeTransportSystem.item.ItemTube;

@Mod.EventBusSubscriber(modid = TubeTransportSystem.MOD_ID)
public class Registration {

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        IForgeRegistry<Block> r = event.getRegistry();
        r.register(new BlockTube().setRegistryName("tts", "tube"));
        r.register(new BlockStation().setRegistryName("tts", "station"));
        r.register(new BlockStationHorizontal().setRegistryName("tts", "stationH"));
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> r = event.getRegistry();
        r.register(new ItemTube(BlockTube.instance).setRegistryName("tts", "tube"));
        r.register(new ItemStation(BlockStation.instance).setRegistryName("tts", "station"));
        // BlockStationHorizontal intentionally has no ItemBlock (placed via ItemStation).
    }

    @SubscribeEvent
    public static void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        IForgeRegistry<IRecipe> r = event.getRegistry();

        // 16 tube: S G S / G E G / S G S
        Ingredient s = Ingredient.fromStacks(new ItemStack(Blocks.STONE));
        Ingredient g = Ingredient.fromStacks(new ItemStack(Blocks.GLASS));
        Ingredient e = Ingredient.fromStacks(new ItemStack(Items.ENDER_PEARL));
        NonNullList<Ingredient> tubeIn = NonNullList.create();
        tubeIn.add(s); tubeIn.add(g); tubeIn.add(s);
        tubeIn.add(g); tubeIn.add(e); tubeIn.add(g);
        tubeIn.add(s); tubeIn.add(g); tubeIn.add(s);
        reg(r, "tube", new ShapedRecipes("tts", 3, 3, tubeIn, new ItemStack(BlockTube.instance, 16, 0)));

        // 1 station: S L S / S _ S / S L S  (L = stone slab)
        Ingredient l = Ingredient.fromStacks(new ItemStack(Blocks.STONE_SLAB, 1, 0));
        NonNullList<Ingredient> staIn = NonNullList.create();
        staIn.add(s); staIn.add(l); staIn.add(s);
        staIn.add(s); staIn.add(Ingredient.EMPTY); staIn.add(s);
        staIn.add(s); staIn.add(l); staIn.add(s);
        reg(r, "station", new ShapedRecipes("tts", 3, 3, staIn, new ItemStack(BlockStation.instance, 1)));

        // colour-cycle shapeless recipes (meta i -> forced 10+i, and reverse 15 -> 0)
        for (int i = 0; i < 6; i++) {
            int from = i > 0 ? 10 + i - 1 : i;
            int to = 10 + i;
            cycle(r, "tube_cycle_" + i + "_1", from, 1, to, 1);
            cycle(r, "tube_cycle_" + i + "_4", from, 4, to, 4);
            cycle(r, "tube_cycle_" + i + "_9", from, 9, to, 9);
        }
        cycle(r, "tube_reset_1", 15, 1, 0, 1);
        cycle(r, "tube_reset_4", 15, 4, 0, 4);
        cycle(r, "tube_reset_9", 15, 9, 0, 9);
    }

    private static void cycle(IForgeRegistry<IRecipe> r, String name, int fromMeta, int count, int toMeta, int outCount) {
        NonNullList<Ingredient> in = NonNullList.create();
        for (int k = 0; k < count; k++) {
            in.add(Ingredient.fromStacks(new ItemStack(ItemTube.instance, 1, fromMeta)));
        }
        reg(r, name, new ShapelessRecipes("tts", new ItemStack(ItemTube.instance, outCount, toMeta), in));
    }

    private static void reg(IForgeRegistry<IRecipe> r, String name, IRecipe recipe) {
        r.register(recipe.setRegistryName(new ResourceLocation("tts", name)));
    }
}
