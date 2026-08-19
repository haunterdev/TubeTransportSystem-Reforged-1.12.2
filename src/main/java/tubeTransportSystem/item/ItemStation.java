package tubeTransportSystem.item;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import tubeTransportSystem.Registration;
import tubeTransportSystem.block.BlockStation;
import tubeTransportSystem.block.BlockStationHorizontal;
import tubeTransportSystem.util.Utilities;

/**
 * Places both halves of a station at once. Clicking a floor or ceiling gives the two-block-tall
 * station, any other face gives the horizontal one, laid out along the player's facing.
 *
 * <p>The original's canPlaceBlockOnSide also demanded air directly above the placement spot for
 * either variant, and that is kept.
 */
public class ItemStation extends BlockItem {
    public ItemStation(Block block, Properties properties) {
        super(block, properties);
    }

    @Nullable
    @Override
    protected BlockState getPlacementState(BlockPlaceContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return null;
        }
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (!level.isEmptyBlock(pos.above())) {
            return null;
        }
        Direction facing = Utilities.entityGetDirection(player);
        Direction clicked = context.getClickedFace();
        BlockState state;
        if (clicked == Direction.UP || clicked == Direction.DOWN) {
            state = BlockStation.of(facing, false);
        } else {
            if (!level.isEmptyBlock(pos.relative(facing))) {
                return null;
            }
            state = BlockStationHorizontal.of(facing, true);
        }
        return canPlace(context, state) ? state : null;
    }

    @Override
    protected boolean placeBlock(BlockPlaceContext context, BlockState state) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (state.is(Registration.STATION.get())) {
            BlockPos above = pos.above();
            if (!level.isEmptyBlock(above)) {
                return false;
            }
            return level.setBlock(pos, state, Block.UPDATE_ALL_IMMEDIATE)
                    && level.setBlock(above, state.setValue(BlockStation.TOP, true), Block.UPDATE_ALL_IMMEDIATE);
        }
        Direction facing = state.getValue(BlockStationHorizontal.FACING);
        BlockPos far = pos.relative(facing);
        if (!level.isEmptyBlock(far)) {
            return false;
        }
        return level.setBlock(pos, state, Block.UPDATE_ALL_IMMEDIATE)
                && level.setBlock(far, state.setValue(BlockStationHorizontal.FRONT, false), Block.UPDATE_ALL_IMMEDIATE);
    }
}
