package quaternary.zenflora.templates;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.data.IData;
import crafttweaker.api.entity.IEntityLivingBase;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.player.IPlayer;
import crafttweaker.api.world.IBlockPos;
import crafttweaker.api.world.IFacing;
import crafttweaker.api.world.IWorld;
import quaternary.zenflora.IFlower;
import quaternary.zenflora.ZenFlora;
import quaternary.zenflora.annotation.Extends;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenConstructor;
import stanhebben.zenscript.annotations.ZenMethod;
import stanhebben.zenscript.annotations.ZenProperty;
import vazkii.botania.api.subtile.SubTileEntity;

import java.util.function.Consumer;

@ZenClass("mods.zenflora.templates.GenericFlowerTemplate")
@ZenRegister
@SuppressWarnings({"unused", "WeakerAccess"})
@Extends(SubTileEntity.class)
public class GenericFlowerTemplate {
	@ZenConstructor
	public GenericFlowerTemplate() {}
	
	@ZenMethod
	public static GenericFlowerTemplate create() {
		return new GenericFlowerTemplate();
	}
	
	@ZenMethod
	public void register(String name, @Optional(valueBoolean = true) boolean alsoMini) {
		ZenFlora.registerFlower(this, name, alsoMini);
	}
	
	//////////////////////////////////////////////////
	
	@ZenProperty
	public OnUpdate onUpdate;
	
	@ZenRegister
	@ZenClass("mods.zenflora.functions.OnUpdate")
	public interface OnUpdate {
		@ZenMethod
		void onUpdate(IFlower me, OnUpdateSuper sup);
	}
	
	@ZenRegister
	@ZenClass("mods.zenflora.functions.OnUpdateSuper")
	public interface OnUpdateSuper {
		@ZenMethod
		void onUpdate();
	}
	
	//////////////////////////////////////////////////
	
	@ZenProperty
	public WriteToNBT writeToNBT;
	
	@ZenRegister
	@ZenClass("mods.zenflora.functions.WriteToNBT")
	public interface WriteToNBT {
		@ZenMethod
		void writeToNBT(IFlower me, WriteToNBTSuper sup, IData data);
	}
	
	@ZenRegister
	@ZenClass("mods.zenflora.functions.WriteToNBTSuper")
	public interface WriteToNBTSuper {
		@ZenMethod
		void writeToNBT(IData data);
	}
	
	//////////////////////////////////////////////////
	
	@ZenProperty
	public ReadFromNBT readFromNBT;
	
	@ZenRegister
	@ZenClass("mods.zenflora.functions.ReadFromNBT")
	public interface ReadFromNBT {
		@ZenMethod
		void readFromNBT(IFlower me, Consumer<IData> sup, IData data);
	}
	
	@ZenRegister
	@ZenClass("mods.zenflora.functions.ReadFromNBTSuper")
	public interface ReadFromNBTSuper {
		@ZenMethod
		void readFromNBT(IData data);
	}
	
	//////////////////////////////////////////////////
	
	@ZenProperty
	public GetUnlocalizedName getUnlocalizedName;
	
	@ZenRegister
	@ZenClass("mods.zenflora.functions.GetUnlocalizedName")
	public interface GetUnlocalizedName {
		@ZenMethod
		String getUnlocalizedName(IFlower me, GetUnlocalizedNameSuper sup);
	}
	
	@ZenRegister
	@ZenClass("mods.zenflora.functions.GetUnlocalizedNameSuper")
	public interface GetUnlocalizedNameSuper {
		@ZenMethod
		String getUnlocalizedName();
	}
	
	//////////////////////////////////////////////////
	
	@ZenProperty
	public OnWanded onWanded;
	
	@ZenRegister
	@ZenClass("mods.zenflora.functions.OnWanded")
	public interface OnWanded {
		@ZenMethod
		boolean onWanded(IFlower me, OnWandedSuper sup, IPlayer player, IItemStack wand);
	}
	
	@ZenRegister
	@ZenClass("mods.zenflora.functions.OnWandedSuper")
	public interface OnWandedSuper {
		@ZenMethod
		boolean onWanded(IPlayer player, IItemStack wand);
	}
	
	//////////////////////////////////////////////////
	
	@ZenProperty
	public OnBlockPlacedBy onBlockPlacedBy;
	
	@ZenRegister
	@ZenClass("mods.zenflora.functions.OnBlockPlacedBy")
	public interface OnBlockPlacedBy {
		@ZenMethod
		void onBlockPlacedBy(IFlower me, OnBlockPlacedBySuper sup, IWorld world, IBlockPos pos, crafttweaker.api.block.IBlockState state, IEntityLivingBase entity, IItemStack stack);
	}
	
	@ZenRegister
	@ZenClass("mods.zenflora.functions.OnBlockPlacedBySuper")
	public interface OnBlockPlacedBySuper {
		@ZenMethod
		void onBlockPlacedBy(IWorld world, IBlockPos pos, crafttweaker.api.block.IBlockState state, IEntityLivingBase entity, IItemStack stack);
	}
	
	//////////////////////////////////////////////////
	
	@ZenProperty
	public OnBlockActivated onBlockActivated;
	
	@ZenRegister
	@ZenClass("mods.zenflora.functions.OnBlockActivated")
	public interface OnBlockActivated {
		@ZenMethod
		boolean onBlockActivated(IFlower me, OnBlockActivatedSuper sup, IWorld world, IBlockPos pos, crafttweaker.api.block.IBlockState state, IPlayer player, boolean mainHand, IFacing facing, float hitX, float hitY, float hitZ);
	}
	
	@ZenRegister
	@ZenClass("mods.zenflora.functions.OnBlockActivatedSuper")
	public interface OnBlockActivatedSuper {
		@ZenMethod
		boolean onBlockActivated(IWorld world, IBlockPos pos, crafttweaker.api.block.IBlockState state, IPlayer player, boolean mainHand, IFacing facing, float hitX, float hitY, float hitZ);
	}
	
	//////////////////////////////////////////////////
	
	@ZenProperty
	public GetPowerLevel getPowerLevel;
	
	@ZenRegister
	@ZenClass("mods.zenflora.functions.GetPowerLevel")
	public interface GetPowerLevel {
		@ZenMethod
		int getPowerLevel(IFlower flower, GetPowerLevelSuper sup, IFacing facing);
	}
	
	@ZenRegister
	@ZenClass("mods.zenflora.functions.GetPowerLevelSuper")
	public interface GetPowerLevelSuper {
		@ZenMethod
		int getPowerLevel(IFacing facing);
	}
}
