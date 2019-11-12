package quaternary.zenflora;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.world.IWorld;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.zenflora.IFlower")
@ZenRegister
public interface IFlower {
	@ZenMethod("sync")
	void syncZen();
	
	@ZenGetter("world")
	IWorld getWorldZen();
}
