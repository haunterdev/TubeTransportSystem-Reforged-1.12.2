package tubeTransportSystem;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import tubeTransportSystem.network.ProxyCommon;
import tubeTransportSystem.util.CreativeTab;

@Mod(modid = TubeTransportSystem.MOD_ID, name = TubeTransportSystem.MOD_NAME, version = TubeTransportSystem.MOD_VERSION,
        acceptedMinecraftVersions = "[1.12.2]")
public class TubeTransportSystem {
    public static final String MOD_NAME = "Tube Transport System";
    public static final String MOD_ID = "tts";
    public static final String MOD_VERSION = "0.6";

    public static final CreativeTabs creativeTab = new CreativeTab();

    @Mod.Instance(value = MOD_ID)
    public static TubeTransportSystem instance;

    @SidedProxy(clientSide = "tubeTransportSystem.network.ProxyClient", serverSide = "tubeTransportSystem.network.ProxyCommon")
    public static ProxyCommon proxy;

    @Mod.EventHandler
    public void preinit(FMLPreInitializationEvent event) {
        proxy.setupConfig(event.getSuggestedConfigurationFile());
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.miscSetup();
    }

    @Mod.EventHandler
    public void postinit(FMLPostInitializationEvent event) {
    }
}
