package tubeTransportSystem;

import java.util.List;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import tubeTransportSystem.block.BlockStation;
import tubeTransportSystem.block.BlockStationHorizontal;
import tubeTransportSystem.block.BlockTube;
import tubeTransportSystem.item.ItemStation;
import tubeTransportSystem.item.ItemTube;

/**
 * Replaces the 1.12.2 {@code RegistryEvent.Register} handlers. Recipes are data now
 * ({@code data/tts/recipe}), drops are loot tables ({@code data/tts/loot_table/blocks}).
 *
 * <p>1.12.2 item metadata is gone, so the seven tube stacks (undirected plus the six forced
 * directions, metas 0 and 10..15) are seven items sharing one block. They are named after the
 * direction the tube carries you, which is what the original tooltip showed.
 */
public final class Registration {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(TubeTransportSystem.MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TubeTransportSystem.MOD_ID);
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TubeTransportSystem.MOD_ID);

    private static BlockBehaviour.Properties stone() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .sound(SoundType.STONE)
                .strength(5.0F)
                .noOcclusion()
                .dynamicShape();
    }

    public static final DeferredBlock<BlockTube> TUBE = BLOCKS.registerBlock("tube", BlockTube::new, stone());
    public static final DeferredBlock<BlockStation> STATION = BLOCKS.registerBlock("station", BlockStation::new, stone());
    // The horizontal station has no item of its own, so it has no loot table either.
    public static final DeferredBlock<BlockStationHorizontal> STATION_HORIZONTAL =
            BLOCKS.registerBlock("station_horizontal", BlockStationHorizontal::new, stone().noLootTable());

    /** Undirected tube (1.12.2 meta 0): takes its direction from the face you place it on. */
    public static final DeferredItem<ItemTube> TUBE_ITEM =
            ITEMS.registerItem("tube", p -> new ItemTube(TUBE.get(), p, ItemTube.UNDIRECTED), new Item.Properties());

    public static final DeferredItem<ItemTube> TUBE_DOWN = directed("tube_down", 0);
    public static final DeferredItem<ItemTube> TUBE_UP = directed("tube_up", 1);
    public static final DeferredItem<ItemTube> TUBE_NORTH = directed("tube_north", 2);
    public static final DeferredItem<ItemTube> TUBE_SOUTH = directed("tube_south", 3);
    public static final DeferredItem<ItemTube> TUBE_EAST = directed("tube_east", 4);
    public static final DeferredItem<ItemTube> TUBE_WEST = directed("tube_west", 5);

    public static final List<DeferredItem<ItemTube>> DIRECTED_TUBES =
            List.of(TUBE_DOWN, TUBE_UP, TUBE_NORTH, TUBE_SOUTH, TUBE_EAST, TUBE_WEST);

    public static final DeferredItem<ItemStation> STATION_ITEM =
            ITEMS.registerItem("station", p -> new ItemStation(STATION.get(), p), new Item.Properties());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = TABS.register("tts",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemgroup.tts"))
                    .icon(() -> new ItemStack(TUBE_ITEM.get()))
                    .displayItems((params, output) -> {
                        output.accept(TUBE_ITEM.get());
                        for (DeferredItem<ItemTube> tube : DIRECTED_TUBES) {
                            output.accept(tube.get());
                        }
                        output.accept(STATION_ITEM.get());
                    })
                    .build());

    private static DeferredItem<ItemTube> directed(String name, int direction) {
        return ITEMS.registerItem(name, p -> new ItemTube(TUBE.get(), p, direction), new Item.Properties());
    }

    private Registration() {
    }

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        TABS.register(modBus);
    }
}
