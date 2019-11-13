package quaternary.zenflora;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.world.IWorld;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;

@ZenClass("mods.zenflora.IFlower")
@ZenRegister
public interface IFlower {
	@ZenGetter("world")
	IWorld getWorldZen();
}
