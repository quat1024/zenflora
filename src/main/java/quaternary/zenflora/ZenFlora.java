package quaternary.zenflora;

import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import quaternary.zenflora.generation.ClassGenerator;
import quaternary.zenflora.templates.GenericFlowerTemplate;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.subtile.SubTileEntity;

@Mod(modid = ZenFlora.MODID, name = ZenFlora.NAME, version = ZenFlora.VERSION)
public class ZenFlora {
	public static final String MODID = "zenflora";
	public static final String NAME = "ZenFlora";
	public static final String VERSION = "GRADLE:VERSION";
	public static final Logger LOGGER = LogManager.getLogger(NAME);
	
	public static void registerFlower(GenericFlowerTemplate template, String name, boolean alsoMiniFlower) {
		Class<? extends SubTileEntity> flowerClass = new ClassGenerator(template, name, false, name).doIt();
		BotaniaAPI.registerSubTile(name, flowerClass);
		BotaniaAPI.addSubTileToCreativeMenu(name);
		
		if(alsoMiniFlower) {
			String miniName = name + "Chibi"; //Yes botania actually does this LOL
			Class<? extends SubTileEntity> miniFlowerClass = new ClassGenerator(template, miniName, true, miniName).doIt();
			BotaniaAPI.registerMiniSubTile(miniName, miniFlowerClass, name);
			BotaniaAPI.addSubTileToCreativeMenu(miniName);
		}
	}
}
