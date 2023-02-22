package com.hbm.items.food;

import com.hbm.capability.HbmCapability;

import com.hbm.interfaces.IHasCustomModel;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemChemicalDye;
import com.hbm.util.EnumUtil;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemFood;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemFlask extends ItemFoodBase {

    public ItemFlask(int amount, float saturation, boolean isWolfFood, String s) {
        super(amount, saturation, isWolfFood, s);
    }



    public static enum EnumInfusion {
        SHIELD
    }



    @Override
    public void onFoodEaten(ItemStack stack, World world, EntityPlayer player) {

        if(world.isRemote)
            return;

        if(this == ModItems.shield_flask) {

            float infusion = 5F;
            HbmCapability.HBMData props = (HbmCapability.HBMData) HbmCapability.getData(player);

            props.maxShield = Math.min(HbmCapability.HBMData.shieldCap, props.shield + infusion);
            props.shield += infusion;
        }

        return;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack p_77626_1_) {
        return 32;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack p_77661_1_) {
        return EnumAction.DRINK;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand handIn){
        ItemStack stack = player.getHeldItem(handIn);
        HbmCapability.HBMData props = (HbmCapability.HBMData) HbmCapability.getData(player);
        if(this == ModItems.empty_flask)
            return ActionResult.<ItemStack> newResult(EnumActionResult.PASS, player.getHeldItem(handIn));
        if(this == ModItems.shield_flask && props.maxShield >= HbmCapability.HBMData.shieldCap)
            return ActionResult.<ItemStack> newResult(EnumActionResult.PASS, player.getHeldItem(handIn));
        player.setActiveHand(handIn);
        return ActionResult.<ItemStack> newResult(EnumActionResult.SUCCESS, player.getHeldItem(handIn));
    }

    @Override
    public String getUnlocalizedName()
    {
        return this.getUnlocalizedName();
    }
    @Override
    public String getUnlocalizedName(ItemStack stack) {
        // todo
        Enum num = EnumUtil.grabEnumSafely(EnumInfusion.class, stack.getItemDamage());
        return super.getUnlocalizedName() + "." + num.name().toLowerCase();
    }


}