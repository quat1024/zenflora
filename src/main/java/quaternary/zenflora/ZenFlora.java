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
		Class<? extends SubTileEntity> flowerClass = ClassGenerator.doIt(template, name, false);
		BotaniaAPI.registerSubTile(name, flowerClass);
		BotaniaAPI.addSubTileToCreativeMenu(name);
		
		if(alsoMiniFlower) {
			String miniName = name + "Chibi"; //Yes botania actually does this LOL
			Class<? extends SubTileEntity> miniFlowerClass = ClassGenerator.doIt(template, miniName, true);
			BotaniaAPI.registerMiniSubTile(miniName, miniFlowerClass, name);
			BotaniaAPI.addSubTileToCreativeMenu(miniName);
		}
	}
}
