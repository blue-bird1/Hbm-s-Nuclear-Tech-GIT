package com.hbm.items.food;

import java.util.List;

import com.hbm.items.machine.ItemChemicalDye.EnumChemDye;
import com.hbm.lib.RefStrings;
import com.hbm.util.EnumUtil;

import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;

public class ItemCrayon extends ItemFoodBase  {


    public ItemCrayon(int amount, float saturation, boolean isWolfFood, String s) {
        super(amount, saturation, isWolfFood, s);
        this.setMaxDamage(0);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack>  list) {
        for(int i = 0; i < EnumChemDye.values().length; i++) {
            list.add(new ItemStack(this, 1, i));
        }
    }


    @Override
    public String getUnlocalizedName(ItemStack stack) {
        // todo
        Enum num = EnumUtil.grabEnumSafely(EnumChemDye.class, stack.getMetadata());
        return super.getUnlocalizedName() + "." + num.name().toLowerCase();

    }


}