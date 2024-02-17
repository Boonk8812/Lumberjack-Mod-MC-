package com.declanminer2005.minecraft.entities;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class LumberjackEntity extends BaseEntity {

    public LumberjackEntity(World world) {
        super(world);
        taskList.clear();
        taskList.add(new EntityAISwimming(this));
        taskList.add(new EntityAIAttackOnCollide(this, EntityVillager.class, 1.0D, true));
        taskList.add(new EntityAIFollowOwner(this, 1.0D, 10.0F, 2.0F));
        taskList.add(new EntityAIWander(this, 1.0D));
        taskList.add(new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        taskList.add(new EntityAILookIdle(this));
    }

    @Override
    public void onUpdate(World world, BlockPos pos, float tickDelta) {
        super.onUpdate(world, pos, tickDelta);
        findAndCutLogsOrPlanks();
    }

    private void findAndCutLogsOrPlanks() {
        for (int i = -5; i <= 5; ++i) {
            for (int j = -5; j <= 5; ++j) {
                for (int k = -1; k <= 1; ++k) {
                    BlockPos offsetPos = new BlockPos(pos.getX() + i, pos.getY() + k, pos.getZ() + j);
                    IBlockState state = world.getBlockState(offsetPos);
                    Block block = state.getBlock();

                    if (block.equals(Blocks.LOG) || block.equals(Blocks.PLANKS)) {
                        cutBlockAtPosition(offsetPos);
                    }
                }
            }
        }
    }

    private void cutBlockAtPosition(BlockPos position) {
        world.destroyBlock(position, true);
        dropItemAtPosition(position, getEquippedAxe());
    }

    private void dropItemAtPosition(BlockPos position, ItemStack itemStack) {
        EntityItem droppedItem = new EntityItem(world, position.getX() + 0.5D, position.getY() + 1.0D, position.getZ() + 0.5D, itemStack);
        droppedItem.setDefaultPickupDelay();
        world.spawnEntity(droppedItem);
    }
}