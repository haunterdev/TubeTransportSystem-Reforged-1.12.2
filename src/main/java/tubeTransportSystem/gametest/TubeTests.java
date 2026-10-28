package tubeTransportSystem.gametest;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import tubeTransportSystem.Registration;
import tubeTransportSystem.TubeTransportSystem;
import tubeTransportSystem.block.BlockStation;
import tubeTransportSystem.block.BlockStationHorizontal;
import tubeTransportSystem.block.BlockTube;

/**
 * Headless checks for the parts of the mod that run on the server: tube physics, station
 * placement and removal, the block-to-item mapping, harvest rules, loot and recipes.
 * Rendering is client-only and is checked in game.
 *
 * <p>Run with {@code gradlew runGameTestServer}. The arena16 template is an empty 16 x 10 x 16 box.
 */
@GameTestHolder(TubeTransportSystem.MOD_ID)
public class TubeTests {

    private static void floor(GameTestHelper helper) {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
            }
        }
    }

    private static BlockState tube(Direction facing) {
        return Registration.TUBE.get().defaultBlockState().setValue(BlockTube.FACING, facing);
    }

    /** An item dropped into an UP tube column is carried upward. */
    @PrefixGameTestTemplate(false)
    @GameTest(template = "arena16", timeoutTicks = 100)
    public static void up_tube_lifts_entity(GameTestHelper helper) {
        floor(helper);
        for (int y = 1; y <= 7; y++) {
            helper.setBlock(new BlockPos(8, y, 8), tube(Direction.UP));
        }
        ItemEntity item = helper.spawnItem(Items.COBBLESTONE, 8.5f, 2.5f, 8.5f);
        double startY = item.getY();
        helper.succeedWhen(() -> {
            double rise = item.getY() - startY;
            if (rise < 3.0) {
                helper.fail("item rose " + String.format("%.2f", rise) + " blocks, expected at least 3");
            }
        });
    }

    /** Fall distance is cleared inside a tube, so a long tube ride never ends in fall damage. */
    @PrefixGameTestTemplate(false)
    @GameTest(template = "arena16", timeoutTicks = 40)
    public static void tube_resets_fall_distance(GameTestHelper helper) {
        floor(helper);
        for (int y = 1; y <= 7; y++) {
            helper.setBlock(new BlockPos(8, y, 8), tube(Direction.UP));
        }
        ItemEntity item = helper.spawnItem(Items.COBBLESTONE, 8.5f, 3.5f, 8.5f);
        item.fallDistance = 10.0f;
        // Checked while the item is still rising mid-tube: moving up and off the ground, vanilla
        // leaves fallDistance alone, so only the tube can have cleared it.
        helper.runAfterDelay(2, () -> {
            if (item.onGround()) {
                helper.fail("item touched ground before the check");
            }
            if (item.fallDistance != 0.0f) {
                helper.fail("fallDistance is " + item.fallDistance + ", expected 0");
            }
            helper.succeed();
        });
    }

    /** A tube placed facing a direction pushes along the 1.7.10 table: EAST moves toward -X. */
    @PrefixGameTestTemplate(false)
    @GameTest(template = "arena16", timeoutTicks = 100)
    public static void east_tube_pushes_negative_x(GameTestHelper helper) {
        floor(helper);
        for (int x = 2; x <= 13; x++) {
            helper.setBlock(new BlockPos(x, 1, 8), tube(Direction.EAST));
        }
        ItemEntity item = helper.spawnItem(Items.COBBLESTONE, 12.5f, 1.5f, 8.5f);
        double startX = item.getX();
        helper.succeedWhen(() -> {
            double moved = startX - item.getX();
            if (moved < 3.0) {
                helper.fail("item moved " + String.format("%.2f", moved) + " blocks toward -X, expected at least 3");
            }
        });
    }

    /** Using the station item on a floor places both halves of the vertical station. */
    @PrefixGameTestTemplate(false)
    @GameTest(template = "arena16")
    public static void station_item_places_vertical(GameTestHelper helper) {
        floor(helper);
        BlockPos ground = new BlockPos(4, 0, 4);
        use(helper, Registration.STATION_ITEM.get().getDefaultInstance(), ground, Direction.UP);
        helper.assertBlockPresent(Registration.STATION.get(), ground.above());
        helper.assertBlockPresent(Registration.STATION.get(), ground.above(2));
        BlockState lower = helper.getBlockState(ground.above());
        BlockState upper = helper.getBlockState(ground.above(2));
        if (lower.getValue(BlockStation.TOP) || !upper.getValue(BlockStation.TOP)) {
            helper.fail("station halves have the wrong TOP flags: lower=" + lower + " upper=" + upper);
        }
        helper.succeed();
    }

    /** Using the station item on a wall places the two-block horizontal station, entrance first. */
    @PrefixGameTestTemplate(false)
    @GameTest(template = "arena16")
    public static void station_item_places_horizontal(GameTestHelper helper) {
        floor(helper);
        BlockPos wall = new BlockPos(10, 1, 10);
        helper.setBlock(wall, Blocks.STONE);
        use(helper, Registration.STATION_ITEM.get().getDefaultInstance(), wall, Direction.NORTH);
        BlockPos front = wall.north();
        BlockPos back = front.north();
        helper.assertBlockPresent(Registration.STATION_HORIZONTAL.get(), front);
        helper.assertBlockPresent(Registration.STATION_HORIZONTAL.get(), back);
        if (!helper.getBlockState(front).getValue(BlockStationHorizontal.FRONT)
                || helper.getBlockState(back).getValue(BlockStationHorizontal.FRONT)) {
            helper.fail("horizontal station halves have the wrong FRONT flags");
        }
        helper.succeed();
    }

    /** Breaking either half of a vertical station removes the other half. */
    @PrefixGameTestTemplate(false)
    @GameTest(template = "arena16")
    public static void station_break_removes_partner(GameTestHelper helper) {
        floor(helper);
        BlockPos lowerA = new BlockPos(4, 1, 4);
        helper.setBlock(lowerA, BlockStation.of(Direction.NORTH, false));
        helper.setBlock(lowerA.above(), BlockStation.of(Direction.NORTH, true));
        BlockPos lowerB = new BlockPos(10, 1, 4);
        helper.setBlock(lowerB, BlockStation.of(Direction.NORTH, false));
        helper.setBlock(lowerB.above(), BlockStation.of(Direction.NORTH, true));

        helper.destroyBlock(lowerA);
        helper.destroyBlock(lowerB.above());
        helper.assertBlockPresent(Blocks.AIR, lowerA.above());
        helper.assertBlockPresent(Blocks.AIR, lowerB);
        helper.succeed();
    }

    /** Seven items share the tube block; the block must still resolve to tts:tube. */
    @PrefixGameTestTemplate(false)
    @GameTest(template = "arena16")
    public static void tube_block_resolves_to_undirected_item(GameTestHelper helper) {
        if (Registration.TUBE.get().asItem() != Registration.TUBE_ITEM.get()) {
            helper.fail("tube asItem() is " + Registration.TUBE.get().asItem() + ", expected tts:tube");
        }
        if (Registration.STATION.get().asItem() != Registration.STATION_ITEM.get()) {
            helper.fail("station block does not resolve to tts:station");
        }
        // As in the original, the horizontal station has no item and drops nothing.
        if (Registration.STATION_HORIZONTAL.get().asItem() != Items.AIR) {
            helper.fail("station_horizontal resolves to " + Registration.STATION_HORIZONTAL.get().asItem() + ", expected none");
        }
        helper.succeed();
    }

    /** Tubes and stations are stone: a pickaxe mines them at tool speed and is needed for the drop. */
    @PrefixGameTestTemplate(false)
    @GameTest(template = "arena16")
    public static void blocks_need_pickaxe(GameTestHelper helper) {
        ItemStack pickaxe = new ItemStack(Items.WOODEN_PICKAXE);
        ItemStack stick = new ItemStack(Items.STICK);
        for (Block block : List.of(Registration.TUBE.get(), Registration.STATION.get(), Registration.STATION_HORIZONTAL.get())) {
            BlockState state = block.defaultBlockState();
            if (!state.is(BlockTags.MINEABLE_WITH_PICKAXE)) {
                helper.fail(block + " is not in minecraft:mineable/pickaxe");
            }
            if (!state.requiresCorrectToolForDrops()) {
                helper.fail(block + " drops without the correct tool");
            }
            if (!pickaxe.isCorrectToolForDrops(state) || stick.isCorrectToolForDrops(state)) {
                helper.fail(block + " has the wrong correct-tool result");
            }
            if (pickaxe.getDestroySpeed(state) <= 1.0f) {
                helper.fail(block + " is not mined faster by a pickaxe");
            }
        }
        helper.succeed();
    }

    /** The loot tables are in the 1.20.1 data path and drop the right item. */
    @PrefixGameTestTemplate(false)
    @GameTest(template = "arena16")
    public static void loot_tables_drop_items(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
        ItemStack pickaxe = new ItemStack(Items.IRON_PICKAXE);
        List<ItemStack> tube = Block.getDrops(tube(Direction.UP), helper.getLevel(), pos, null, null, pickaxe);
        if (tube.size() != 1 || !tube.get(0).is(Registration.TUBE_ITEM.get())) {
            helper.fail("tube drops " + tube + ", expected one tts:tube");
        }
        List<ItemStack> lower = Block.getDrops(BlockStation.of(Direction.NORTH, false), helper.getLevel(), pos, null, null, pickaxe);
        List<ItemStack> upper = Block.getDrops(BlockStation.of(Direction.NORTH, true), helper.getLevel(), pos, null, null, pickaxe);
        if (lower.size() != 1 || !lower.get(0).is(Registration.STATION_ITEM.get()) || !upper.isEmpty()) {
            helper.fail("station drops lower=" + lower + " upper=" + upper + ", expected one tts:station from the lower half");
        }
        List<ItemStack> horizontal = Block.getDrops(BlockStationHorizontal.of(Direction.NORTH, true), helper.getLevel(), pos, null, null, pickaxe);
        if (!horizontal.isEmpty()) {
            helper.fail("station_horizontal drops " + horizontal + ", expected nothing");
        }
        helper.succeed();
    }

    /** Every recipe file parsed under the 1.20.1 schema. */
    @PrefixGameTestTemplate(false)
    @GameTest(template = "arena16")
    public static void recipes_load(GameTestHelper helper) {
        String[] ids = {"tube", "station", "tube_to_tube_down_1", "tube_down_to_tube_up_4", "tube_west_to_tube_9"};
        for (String id : ids) {
            if (helper.getLevel().getRecipeManager().byKey(new ResourceLocation(TubeTransportSystem.MOD_ID, id)).isEmpty()) {
                helper.fail("recipe tts:" + id + " did not load");
            }
        }
        long count = helper.getLevel().getRecipeManager().getRecipes().stream()
                .filter(r -> r.getId().getNamespace().equals(TubeTransportSystem.MOD_ID)).count();
        if (count != 23) {
            helper.fail(count + " tts recipes loaded, expected 23");
        }
        helper.succeed();
    }

    /** Uses {@code stack} on the given face of the block at {@code target}, as a player facing north. */
    private static void use(GameTestHelper helper, ItemStack stack, BlockPos target, Direction face) {
        Player player = helper.makeMockPlayer();
        player.setYRot(0.0f);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        BlockPos abs = helper.absolutePos(target);
        Vec3 hit = Vec3.atCenterOf(abs).relative(face, 0.5);
        stack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, new BlockHitResult(hit, face, abs, false)));
    }
}
