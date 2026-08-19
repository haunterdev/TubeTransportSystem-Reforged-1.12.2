package tubeTransportSystem.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;

public interface IConnectable {
    boolean canConnectTo(BlockGetter level, BlockPos pos, Direction d);

    boolean canConnectToStrict(BlockGetter level, BlockPos pos, Direction d);
}
