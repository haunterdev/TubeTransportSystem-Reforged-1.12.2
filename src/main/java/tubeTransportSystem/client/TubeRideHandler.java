package tubeTransportSystem.client;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import tubeTransportSystem.TubeTransportSystem;
import tubeTransportSystem.util.Utilities;

/**
 * Suppresses the walk head-bob while a player is inside a tube.
 *
 * The step-distance side of it is handled in BlockTube.entityInside, but the bob amplitude comes
 * from Player.bob, which the movement update recomputes AFTER Entity.move has run, so clearing it
 * during the block collision would be overwritten in the same tick. Doing it at the end of the
 * player tick lands after that update.
 *
 * Not in the 1.7.10 original: intentional change in this port.
 */
@EventBusSubscriber(modid = TubeTransportSystem.MOD_ID, value = Dist.CLIENT)
public final class TubeRideHandler {

    private TubeRideHandler() {
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player == null || player.level() == null || !player.level().isClientSide) {
            return;
        }
        BlockPos eyes = BlockPos.containing(player.getX(), player.getEyeY(), player.getZ());
        if (!Utilities.isTube(player.level(), eyes) && !Utilities.isTube(player.level(), player.blockPosition())) {
            return;
        }
        player.bob = 0.0f;
        player.oBob = 0.0f;
        player.walkDist = player.walkDistO;
    }
}
