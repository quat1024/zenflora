package quaternary.zenflora.generated;

import crafttweaker.api.data.IData;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crafttweaker.api.world.IWorld;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import quaternary.zenflora.IFlower;
import quaternary.zenflora.Util;
import quaternary.zenflora.templates.GenericFlowerTemplate;
import vazkii.botania.api.subtile.SubTileEntity;

//Only using this right now for ASM bytecode reference purposes.
//I will delete this later, I take a different approach.
public abstract class UnusedGenericFlower extends SubTileEntity implements IFlower {
	protected UnusedGenericFlower(String name, boolean isMini) {
		this.template = GeneratedClassSupport.retrieveTemplate(name);
		this.isMini = isMini;
	}
	
	protected final GenericFlowerTemplate template;
	protected final boolean isMini;
	
	@Override
	public void onUpdate() {
		if(template.onUpdate == null) super.onUpdate();
		else template.onUpdate.onUpdate(this, super::onUpdate);
	}
	
	@Override
	public void writeToPacketNBT(NBTTagCompound cmp) {
		if(template.writeToNBT == null) super.writeToPacketNBT(cmp);
		
		IData data = CraftTweakerMC.getIDataModifyable(cmp);
		template.writeToNBT.writeToNBT(this,
			(idata) -> {
				//incorporate changes from crafttweaker to botania
				NBTTagCompound cmp_ = CraftTweakerMC.getNBTCompound(idata);
				//call super
				super.writeToPacketNBT(cmp_);
				//incorporate changes from botania to crafttweaker
				Util.writeDataOnto(CraftTweakerMC.getIData(cmp_), idata);
			},
			data
		);
	}
	
	@Override
	public void readFromPacketNBT(NBTTagCompound cmp) {
		if(template.readFromNBT == null) super.readFromPacketNBT(cmp);
		
		IData data = CraftTweakerMC.getIDataModifyable(cmp);
		template.readFromNBT.readFromNBT(this,
			(idata) -> {
				//Botania doesn't ever write to the tag here (it's a read method, duh)
				//no need to copy the data back to ct
				super.readFromPacketNBT(CraftTweakerMC.getNBTCompound(idata));
			},
			data
		);
	}
	
	@Override
	public String getUnlocalizedName() {
		if(template.getUnlocalizedName == null) return super.getUnlocalizedName();
		else return template.getUnlocalizedName.getUnlocalizedName(this, super::getUnlocalizedName);
	}
	
	@Override
	public boolean onWanded(EntityPlayer player, ItemStack wand) {
		if(template.onWanded == null) return super.onWanded(player, wand);
		
		return template.onWanded.onWanded(this,
			(iplayer, iitemstack) -> {
				ItemStack wand_ = CraftTweakerMC.getItemStack(iitemstack);
				return super.onWanded(CraftTweakerMC.getPlayer(iplayer), wand_);
				//TODO is there anything I need to copy back to crafttweaker?
				//((ItemStack)iitemstack.getInternal()).setXxxxx
			},
			CraftTweakerMC.getIPlayer(player),
			CraftTweakerMC.getIItemStack(wand)
		);
	}
	
	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase entity, ItemStack stack) {
		if(template.onBlockPlacedBy == null) super.onBlockPlacedBy(world, pos, state, entity, stack);
		
		template.onBlockPlacedBy.onBlockPlacedBy(this,
			(iworld, iblockpos, iblockstate, iliving, istack) -> {
				World world_ = CraftTweakerMC.getWorld(iworld);
				BlockPos pos_ = CraftTweakerMC.getBlockPos(iblockpos);
				IBlockState state_ = CraftTweakerMC.getBlockState(iblockstate);
				EntityLivingBase living_ = CraftTweakerMC.getEntityLivingBase(iliving);
				ItemStack stack_ = CraftTweakerMC.getItemStack(istack);
				
				super.onBlockPlacedBy(world_, pos_, state_, living_, stack_);
			},
			CraftTweakerMC.getIWorld(world),
			CraftTweakerMC.getIBlockPos(pos),
			CraftTweakerMC.getBlockState(state),
			CraftTweakerMC.getIEntityLivingBase(entity),
			CraftTweakerMC.getIItemStack(stack)
		);
	}
	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		if(template.onBlockActivated == null) return super.onBlockActivated(world, pos, state, player, hand, side, hitX, hitY, hitZ);
		
		return template.onBlockActivated.onBlockActivated(this,
			(iworld, iblockpos, iblockstate, iplayer, boolHand, ifacing, hitX_, hitY_, hitZ_) -> {
				World world_ = CraftTweakerMC.getWorld(iworld);
				BlockPos pos_ = CraftTweakerMC.getBlockPos(iblockpos);
				IBlockState state_ = CraftTweakerMC.getBlockState(iblockstate);
				EntityPlayer player_ = CraftTweakerMC.getPlayer(iplayer);
				EnumHand hand_ = boolHand ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
				EnumFacing facing_ = (EnumFacing) ifacing.getInternal();
				
				return super.onBlockActivated(world_, pos_, state_, player_, hand_, facing_, hitX_, hitY_, hitZ_);
			},
			CraftTweakerMC.getIWorld(world),
			CraftTweakerMC.getIBlockPos(pos),
			CraftTweakerMC.getBlockState(state),
			CraftTweakerMC.getIPlayer(player),
			hand == EnumHand.MAIN_HAND,
			CraftTweakerMC.getIFacing(side),
			hitX,
			hitY,
			hitZ
		);
	}
	
	//@Override
	public void syncZen() {
		sync();
	}
	
	@Override
	public IWorld getWorldZen() {
		return CraftTweakerMC.getIWorld(getWorld());
	}
}
