package tubeTransportSystem.util;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public interface IConnectable {
    boolean canConnectTo(IBlockAccess blockAccess, BlockPos pos, EnumFacing d);

    boolean canConnectToStrict(IBlockAccess blockAccess, BlockPos pos, EnumFacing d);
}
