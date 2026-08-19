package tubeTransportSystem.item;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import tubeTransportSystem.block.BlockTube;

/**
 * One item per 1.12.2 tube metadata: {@link #UNDIRECTED} is the old meta 0, which takes its
 * direction from the face it is placed against, and 0..5 are the old metas 10..15.
 */
public class ItemTube extends BlockItem {
    public static final int UNDIRECTED = -1;

    private final int direction;

    public ItemTube(Block block, Properties properties, int direction) {
        super(block, properties);
        this.direction = direction;
    }

    public int getDirection() {
        return direction;
    }

    @Nullable
    @Override
    protected BlockState getPlacementState(BlockPlaceContext context) {
        // Original: meta = metadata >= 10 ? metadata - 10 : side. Direction indices match the
        // 1.7.10 ForgeDirection ones, so the clicked face carries straight over.
        Direction facing = direction == UNDIRECTED
                ? context.getClickedFace()
                : Direction.from3DDataValue(direction);
        BlockState state = getBlock().defaultBlockState().setValue(BlockTube.FACING, facing);
        return canPlace(context, state) ? state : null;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (direction == UNDIRECTED) {
            // Not in the 1.7.10 original, which showed no tooltip here, so nothing told a new player
            // that an undirected tube takes its direction from the face it is placed against.
            tooltip.add(Component.translatable("tooltip.tts.autodirection.1").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("tooltip.tts.autodirection.2").withStyle(ChatFormatting.DARK_GRAY));
            tooltip.add(Component.translatable("tooltip.tts.autodirection.3").withStyle(ChatFormatting.DARK_GRAY));
            return;
        }
        tooltip.add(Component.translatable("tooltip.tts.direction",
                Component.translatable("tooltip.tts.direction." + direction).withStyle(ChatFormatting.AQUA))
                .withStyle(ChatFormatting.GRAY));
    }
}
