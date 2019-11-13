package quaternary.zenflora.generation;

import crafttweaker.api.data.IData;
import crafttweaker.api.entity.IEntityLivingBase;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crafttweaker.api.player.IPlayer;
import crafttweaker.api.world.IBlockPos;
import crafttweaker.api.world.IFacing;
import crafttweaker.api.world.IWorld;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * Some classes are represented differently in the CraftTweaker world and the Minecraft world.
 * This class contains static utilities that attempt to bridge the gap.
 */
public class TypeLifter implements Opcodes {
	//Minecraft types
	private static final Type NBTTAGCOMPOUND = Type.getType(NBTTagCompound.class);
	private static final Type NBTBASE = Type.getType(NBTBase.class);
	private static final Type ENTITYPLAYER = Type.getType(EntityPlayer.class);
	private static final Type ITEMSTACK = Type.getType(ItemStack.class);
	private static final Type WORLD = Type.getType(World.class);
	private static final Type BLOCKPOS = Type.getType(BlockPos.class);
	private static final Type IBLOCKSTATE_MC = Type.getType(IBlockState.class);
	private static final Type ENTITYLIVINGBASE = Type.getType(EntityLivingBase.class);
	private static final Type ENUMHAND = Type.getType(EnumHand.class);
	private static final Type ENUMFACING = Type.getType(EnumFacing.class);
	
	private static final String ENUMHAND_INTERNAL_NAME = Type.getInternalName(EnumHand.class);
	
	//Crafttweaker types
	private static final Type IDATA = Type.getType(IData.class);
	private static final Type IPLAYER = Type.getType(IPlayer.class); //Brought to you by the BBC
	private static final Type IITEMSTACK = Type.getType(IItemStack.class);
	private static final Type IWORLD = Type.getType(IWorld.class);
	private static final Type IBLOCKPOS = Type.getType(IBlockPos.class);
	private static final Type IBLOCKSTATE_CT = Type.getType(crafttweaker.api.block.IBlockState.class);
	private static final Type IENTITYLIVINGBASE = Type.getType(IEntityLivingBase.class);
	private static final Type IFACING = Type.getType(IFacing.class);
	
	private static final String CRAFTTWEAKERMC_INTERNAL_NAME = Type.getInternalName(CraftTweakerMC.class);
	private static final String IFACING_INTERNAL_NAME = Type.getInternalName(IFacing.class);
	
	public static Type genLifting(Type type, MethodVisitor method) {
		int typeType = type.getSort(); //They call it a "sort" because well, are you gonna call it a "type"?
		//TODO: Missing support for array and method types
		if(typeType != Type.OBJECT) return type;
		
		if(type.equals(NBTTAGCOMPOUND)) {
			callCTMC(method, "getIDataModifyable", NBTBASE, IDATA);
			return IDATA;
		} else if(type.equals(ENTITYPLAYER)) {
			callCTMC(method, "getIPlayer", ENTITYPLAYER, IPLAYER);
			return IPLAYER;
		} else if(type.equals(ITEMSTACK)) {
			callCTMC(method, "getIItemStack", ITEMSTACK, IITEMSTACK);
			return IITEMSTACK;
		} else if(type.equals(WORLD)) {
			callCTMC(method, "getIWorld", WORLD, IWORLD);
			return IWORLD;
		} else if(type.equals(BLOCKPOS)) {
			callCTMC(method, "getIBlockPos", BLOCKPOS, IBLOCKPOS);
			return IBLOCKPOS;
		} else if(type.equals(IBLOCKSTATE_MC)) {
			callCTMC(method, "getBlockState", IBLOCKSTATE_MC, IBLOCKSTATE_CT);
			return IBLOCKSTATE_CT;
		} else if(type.equals(ENTITYLIVINGBASE)) {
			callCTMC(method, "getIEntityLivingBase", ENTITYLIVINGBASE, IENTITYLIVINGBASE);
			return IENTITYLIVINGBASE;
		} else if(type.equals(ENUMFACING)) {
			callCTMC(method, "getIFacing", ENUMFACING, IFACING);
			return IFACING;
		} else if(type.equals(ENUMHAND)) {
			//Grab the ordinal of the hand
			method.visitMethodInsn(
				INVOKEVIRTUAL,
				ENUMHAND_INTERNAL_NAME,
				"ordinal",
				Type.getMethodDescriptor(Type.INT_TYPE),
				false
			);
			//0 is main hand 1 is off hand... I want the other way
			//Push a 1 underneath
			method.visitInsn(ICONST_1);
			method.visitInsn(SWAP);
			//Subtract
			method.visitInsn(ISUB);
			//Now it (should be) correct
			return Type.BOOLEAN_TYPE;
		}
		
		return type; //assume this type doesn't need any crafttweakizing (like a float or whatever)
	}
	
	/*
		Call this with the *Minecraft* type, not the CT one (i.e. call this with the type you want it to return)
		Reason being not all Minecraft types map to a CT type (like EnumHand doesn't map to anything).
		In those cases I need to use a different type. I used boolean, true if you're in the main hand
		But since boolean is such a nonspecific type there might be more than one type that needs to map that way.
	 */
	public static void genUnlifting(Type type, MethodVisitor method) {
		//It's a primitive type! No munging needed
		if(type.getDescriptor().length() == 1) return;
		
		if(type.equals(NBTTAGCOMPOUND)) {
			callCTMC(method, "getNBTCompound", IDATA, NBTTAGCOMPOUND);
		} else if(type.equals(ENTITYPLAYER)) {
			callCTMC(method, "getPlayer", IPLAYER, ENTITYPLAYER);
		} else if(type.equals(ITEMSTACK)) {
			callCTMC(method, "getItemStack", IITEMSTACK, ITEMSTACK);
		} else if(type.equals(WORLD)) {
			callCTMC(method, "getWorld", IWORLD, WORLD);
		} else if(type.equals(BLOCKPOS)) {
			callCTMC(method, "getBlockPos", IBLOCKPOS, BLOCKPOS);
		} else if(type.equals(IBLOCKSTATE_MC)) {
			callCTMC(method, "getBlockState", IBLOCKSTATE_CT, IBLOCKSTATE_MC);
		} else if(type.equals(ENTITYLIVINGBASE)) {
			callCTMC(method, "getEntityLivingBase", IENTITYLIVINGBASE, ENTITYLIVINGBASE);
		} else if(type.equals(ENUMFACING)) {
			//Probably due to a mistake, there's no CTMC methods to turn an IFacing back into an EnumFacing.
			//Luckily we can just grab the Object from the interface's methods:
			method.visitMethodInsn(
				INVOKEINTERFACE,
				IFACING_INTERNAL_NAME,
				"getInternal",
				Type.getMethodDescriptor(Type.getType(Object.class)),
				true
			);
			//...and cast it to EnumFacing
			method.visitTypeInsn(
				CHECKCAST,
				ENUMFACING.getInternalName()
			);
		} else if(type.equals(ENUMHAND)) {
			//What's actually on the stack rn is a boolean 0 or 1, 1 if it's the main hand
			//Push a 1 underneath
			method.visitInsn(ICONST_1);
			method.visitInsn(SWAP);
			//Subtract
			method.visitInsn(ISUB);
			//Grab values()
			method.visitMethodInsn(
				INVOKESTATIC,
				ENUMHAND_INTERNAL_NAME,
				"values",
				"()[" + ENUMHAND.getDescriptor(), //Type doesn't seem to let you arrayize a type?! Ok I'll do it myself then
				false
			);
			//Index the array
			method.visitInsn(SWAP);
			method.visitInsn(AALOAD);
			//Now an enumhand is on the stack... we did it reddit
		}
	}
	
	private static void callCTMC(MethodVisitor list, String name, Type source, Type dest) {
		list.visitMethodInsn(
			INVOKESTATIC,
			CRAFTTWEAKERMC_INTERNAL_NAME,
			name,
			Type.getMethodDescriptor(dest, source),
			false
		);
	}
}
