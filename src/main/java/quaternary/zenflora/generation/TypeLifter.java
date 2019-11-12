package quaternary.zenflora.generation;

import crafttweaker.api.data.IData;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crafttweaker.api.player.IPlayer;
import crafttweaker.api.world.IWorld;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;

public class TypeLifter implements Opcodes {
	private static final Type NBTTAGCOMPOUND = Type.getType(NBTTagCompound.class);
	private static final Type NBTBASE = Type.getType(NBTBase.class);
	private static final Type ENTITYPLAYER = Type.getType(EntityPlayer.class);
	private static final Type ITEMSTACK = Type.getType(ItemStack.class);
	private static final Type WORLD = Type.getType(World.class);
	
	private static final Type IDATA = Type.getType(IData.class);
	private static final Type IPLAYER = Type.getType(IPlayer.class); //Brought to you by BBC
	private static final Type IITEMSTACK = Type.getType(IItemStack.class);
	private static final Type IWORLD = Type.getType(IWorld.class);
	
	
	public InsnList crafttweakizeType(Type type) {
		InsnList list = new InsnList();
		
		if(type.equals(NBTTAGCOMPOUND)) {
			return callCTMC(list, "getIDataModifyable", NBTBASE, IDATA);
		} else if(type.equals(ENTITYPLAYER)) {
			return callCTMC(list, "getIPlayer", ENTITYPLAYER, IPLAYER);
		} else if(type.equals(ITEMSTACK)) {
			return callCTMC(list, "getIItemStack", ITEMSTACK, IITEMSTACK);
		} else if(type.equals(WORLD)) {
			return callCTMC(list, "getIWorld", WORLD, IWORLD);
		}
		
		return list; //assume this type doesn't need any crafttweakizing
	}
	
	public InsnList unCrafttweakizeType(Type type) {
		InsnList list = new InsnList();
		
		if(type.equals(IDATA)) {
			return callCTMC(list, "getNBTCompound", IDATA, NBTTAGCOMPOUND);
		} else if(type.equals(IPLAYER)) {
			return callCTMC(list, "getPlayer", IPLAYER, ENTITYPLAYER);
		} else if(type.equals(IITEMSTACK)) {
			return callCTMC(list, "getItemStack", IITEMSTACK, ITEMSTACK);
		} else if(type.equals(IWORLD)) {
			return callCTMC(list, "getWorld", IWORLD, WORLD);
		}
		
		return list;
	}
	
	private static final String CRAFTTWEAKERMC_INTERNAL_NAME = Type.getInternalName(CraftTweakerMC.class);
	
	private static InsnList callCTMC(InsnList list, String name, Type source, Type dest) {
		list.add(new MethodInsnNode(
			INVOKESTATIC,
			CRAFTTWEAKERMC_INTERNAL_NAME,
			name,
			Type.getMethodDescriptor(dest, source),
			false
		));
		return list;
	}
}
