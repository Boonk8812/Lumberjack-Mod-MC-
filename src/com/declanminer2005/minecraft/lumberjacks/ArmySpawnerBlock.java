package com.declanminer2005.minecraft.blocks;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class ArmySpawnerBlock extends BlockContainer {

    public ArmySpawnerBlock() {
        super(Properties.create(Material.ROCK).hardnessAndResistance(1.5F, 6.0F));
        setRegistryName("army_spawner");
    }

    @Override
    public boolean activate(World worldIn, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        TileEntity te = worldIn.getTileEntity(pos);

        if (!(te instanceof ArmySpawnerTileEntity)) {
            return false;
        }

        ArmySpawnerTileEntity spawnerTE = (ArmySpawnerTileEntity) te;

        switch (hit.sideHit) {
            case UP:
                spawnerTE.toggleActive();
                break;
            default:
                break;
        }

        return true;
    }

    @Override
    public TileEntity createNewTileEntity(World p_196283_1_) {
        return new ArmySpawnerTileEntity();
    }
}