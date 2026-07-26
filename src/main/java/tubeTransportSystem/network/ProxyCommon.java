package tubeTransportSystem.network;

import java.io.File;

import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ProxyCommon {
    Configuration config;
    public static double CONFIG_MAX_SPEED = 0.5;
    public static double CONFIG_MAX_SPEED_INVERSE = -0.5;
    public int lastSideHit = 0;
    public static Logger logger = LogManager.getLogger("TubeTransportSystem");

    public void setupConfig(File file) {
        this.config = new Configuration(file);
        CONFIG_MAX_SPEED = MathHelper.clamp(this.config.get("General", "MaxTubeSpeed", 0.5,
                "The maximum speed an entity can travel through the Transport Tubes").getDouble(), 0.0, 10.0);
        CONFIG_MAX_SPEED_INVERSE = -CONFIG_MAX_SPEED;
        if (this.config.hasChanged()) {
            this.config.save();
        }
    }

    public void miscSetup() {
    }
}
