package tubeTransportSystem;

import java.util.List;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import tubeTransportSystem.block.BlockStation;
import tubeTransportSystem.block.BlockStationHorizontal;
import tubeTransportSystem.block.BlockTube;
import tubeTransportSystem.item.ItemStation;
import tubeTransportSystem.item.ItemTube;

/**
 * Replaces the 1.12.2 {@code RegistryEvent.Register} handlers. Recipes are data now
 * ({@code data/tts/recipes}), drops are loot tables ({@code data/tts/loot_tables/blocks}).
 *
 * <p>1.12.2 item metadata is gone, so the seven tube stacks (undirected plus the six forced
 * directions, metas 0 and 10..15) are seven items sharing one block. They are named after the
 * direction the tube carries you, which is what the original tooltip showed.
 */
public final class Registration {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, TubeTransportSystem.MOD_ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, TubeTransportSystem.MOD_ID);
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TubeTransportSystem.MOD_ID);

    /**
     * 1.12.2 Material.ROCK: a pickaxe mines it at tool speed and is needed for the drop
     * (the mineable/pickaxe tag carries the first half).
     */
    private static BlockBehaviour.Properties stone() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .sound(SoundType.STONE)
                .strength(5.0F)
                .requiresCorrectToolForDrops()
                .noOcclusion()
                .dynamicShape();
    }

    public static final RegistryObject<BlockTube> TUBE = BLOCKS.register("tube", () -> new BlockTube(stone()));
    public static final RegistryObject<BlockStation> STATION = BLOCKS.register("station", () -> new BlockStation(stone()));
    // The horizontal station has no item of its own, so it has no loot table either.
    public static final RegistryObject<BlockStationHorizontal> STATION_HORIZONTAL =
            BLOCKS.register("station_horizontal", () -> new BlockStationHorizontal(stone().noLootTable()));

    /** Undirected tube (1.12.2 meta 0): takes its direction from the face you place it on. */
    public static final RegistryObject<ItemTube> TUBE_ITEM =
            ITEMS.register("tube", () -> new ItemTube(TUBE.get(), new Item.Properties(), ItemTube.UNDIRECTED));

    public static final RegistryObject<ItemTube> TUBE_DOWN = directed("tube_down", 0);
    public static final RegistryObject<ItemTube> TUBE_UP = directed("tube_up", 1);
    public static final RegistryObject<ItemTube> TUBE_NORTH = directed("tube_north", 2);
    public static final RegistryObject<ItemTube> TUBE_SOUTH = directed("tube_south", 3);
    public static final RegistryObject<ItemTube> TUBE_EAST = directed("tube_east", 4);
    public static final RegistryObject<ItemTube> TUBE_WEST = directed("tube_west", 5);

    public static final List<RegistryObject<ItemTube>> DIRECTED_TUBES =
            List.of(TUBE_DOWN, TUBE_UP, TUBE_NORTH, TUBE_SOUTH, TUBE_EAST, TUBE_WEST);

    public static final RegistryObject<ItemStation> STATION_ITEM =
            ITEMS.register("station", () -> new ItemStation(STATION.get(), new Item.Properties()));

    public static final RegistryObject<CreativeModeTab> TAB = TABS.register("tts",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemgroup.tts"))
                    .icon(() -> new ItemStack(TUBE_ITEM.get()))
                    .displayItems((params, output) -> {
                        output.accept(TUBE_ITEM.get());
                        for (RegistryObject<ItemTube> tube : DIRECTED_TUBES) {
                            output.accept(tube.get());
                        }
                        output.accept(STATION_ITEM.get());
                    })
                    .build());

    private static RegistryObject<ItemTube> directed(String name, int direction) {
        return ITEMS.register(name, () -> new ItemTube(TUBE.get(), new Item.Properties(), direction));
    }

    private Registration() {
    }

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        TABS.register(modBus);
    }
}
