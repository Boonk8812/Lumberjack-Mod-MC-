package com.declanminer2005.minecraft.entities;

import net.minecraft.entity.monster.IMob;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;

public abstract class BaseEntity implements IMob {

    protected ItemStack equippedAxe = new ItemStack(Items.IRON_AXE);

    @Override
    public boolean attackEntityFrom(DamageSource damageSrc, float damageAmount) {
        // Optional health management or attack handling.
        return false;
    }

    public ItemStack getEquippedAxe() {
        return equippedAxe;
    }
}