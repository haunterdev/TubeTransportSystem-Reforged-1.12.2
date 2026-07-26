package tubeTransportSystem.client;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import tubeTransportSystem.TubeTransportSystem;
import tubeTransportSystem.block.BlockTube;

/**
 * Suppresses the walk head-bob while a player is inside a tube.
 *
 * The step-distance side of it is handled in BlockTube.onEntityCollision, but the bob
 * amplitude comes from EntityPlayer.cameraYaw, which onLivingUpdate recomputes AFTER
 * Entity.move has run, so clearing it during the block collision would be overwritten
 * in the same tick. Doing it at the end of the player tick lands after that update.
 *
 * Not in the 1.7.10 original: intentional change in this port.
 */
@Mod.EventBusSubscriber(modid = TubeTransportSystem.MOD_ID, value = Side.CLIENT)
public class TubeRideHandler {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.side != Side.CLIENT) {
            return;
        }
        EntityPlayer player = event.player;
        if (player == null || player.world == null) {
            return;
        }
        BlockPos pos = new BlockPos(player.posX, player.posY + player.getEyeHeight(), player.posZ);
        if (player.world.getBlockState(pos).getBlock() != BlockTube.instance
                && player.world.getBlockState(new BlockPos(player)).getBlock() != BlockTube.instance) {
            return;
        }
        player.cameraYaw = 0.0f;
        player.prevCameraYaw = 0.0f;
        player.cameraPitch = 0.0f;
        player.prevCameraPitch = 0.0f;
        player.distanceWalkedModified = player.prevDistanceWalkedModified;
    }
}
