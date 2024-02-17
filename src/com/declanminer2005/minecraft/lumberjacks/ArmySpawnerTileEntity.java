package com.declanminer2005.minecraft.tiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.registries.ObjectHolder;

public class ArmySpawnerTileEntity extends TileEntity implements ITickableTileEntity {

    @ObjectHolder("modid:army_spawner")
    private static ArmySpawnerTileEntity INSTANCE;

    private List<UUID> summonedLumberjacks = new ArrayList<>();
    private boolean active = false;

    public ArmySpawnerTileEntity() {
        super(INSTANCE);
    }

    public void toggleActive() {
        active = !active;

        if (!level.isClientSide()) {
            syncData();
        }
    }

    private void spawnLumberjacks() {
        Random rand = new Random();
        Vector3d center = new Vector3d(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5);

        for (int i = 0; i < 5; i++) {
            double angle = 2 * Math.PI * i / 5;
            double distance = 2;

            double dx = distance * Math.cos(angle);
            double dz = distance * Math.sin(angle);

            BlockPos spawnPoint = pos.below().relative(Direction.getValues()[rand.nextInt(6)]);
            Vec3 spawnLoc = center.add(dx, 0, dz);
            Level.playSound(null, spawnPoint, SoundEvents.AMBIENT_CAVE, SoundCategory.BLOCKS, 0.3f, 1);
            Level.broadcastEntityEvent(null, pos, (byte) 10);

            LumberjackEntity lumberjack = new LumberjackEntity(level);
            lumberjack.moveTo(spawnLoc.x, spawnLoc.y, spawnLoc.z, rand.nextFloat() * 360, 0);
            level.addFreshEntity(lumberjack);

            summonedLumberjacks.add(lumberjack.getUUID());
        }
    }

    private void deleteLumberjacks() {
        summonedLumberjacks.forEach(summoned -> {
            List<Entity> entities = level.getEntitiesOfClass(Entity.class, AxisAlignedBB.expand(pos.getX() - 50, pos.getY() - 50, pos.getZ() - 50, pos.getX() + 50, pos.getY() + 50, pos.getZ() + 50),
                    ent -> ent instanceof LumberjackEntity && ent.getUUID().equals(summoned));

            entities.forEach(ent -> {
                ent.discard();
                level.sendParticles(ParticleTypes.EXPLOSION, ent.getX(), ent.getY(), ent.getZ(), 3, 0, 0, 0, 0);
            });
        });

        summonedLumberjacks.clear();

        if (!level.isClientSide()) {
            ChatMessageComponent msg = Component.literal("We have killed the lumberjacks to clear them out.");
            ServerPlayer owner = level.getServer().getPlayerList().getPlayers().stream().filter(p -> p.getUUID().equals(owner)).findAny().orElse(null);

            if (owner != null) {
                owner.sendSystemMessage(msg);
            }
        }
    }

    private void followPlayers(List<PlayerEntity> players) {
        summonedLumberjacks.forEach(summoned -> {
            List<Entity> entities = level.getEntitiesOfClass(Entity.class, AxisAlignedBB.expand(pos.getX() - 50, pos.getY() - 50, pos.getZ() - 50, pos.getX() + 50, pos.getY() + 50, pos.getZ() + 50),
                    ent -> ent instanceof LumberjackEntity && ent.getUUID().equals(summoned));

            entities.forEach(ent -> {
                if (!players.isEmpty()) {
                    PlayerEntity nearestPlayer = players.get(0);
                    ent.getLookControl().setLookAt(nearestPlayer);
                    ent.moveTowardsTarget(nearestPlayer, 0.1);
                } else {
                    ent.stopFollowingPath();
                }
            });
        });
    }

    private void syncData() {
        markDirty();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        setChanged();
    }

    @Override
    public void load(CompoundNBT compound) {
        super.load(compound);
        summonedLumberjacks = new ArrayList<>();

        ListTag taggedSummons = compound.getList("summonedLumberjacks", Tag.TAG_COMPOUND);

        for (int i = 0; i < taggedSummons.size(); i++) {
            CompoundNBT taggedSummon = taggedSummons.getCompound(i);
            summonedLumberjacks.add(taggedSummon.getUniqueId("uuid"));
        }

        active = compound.getBoolean("active");
    }

    @Override
    public CompoundNBT save(CompoundNBT compound) {
        super.save(compound);
        ListTag taggedSummons = new ListTag();

        summonedLumberjacks.forEach(summoned -> {
            CompoundNBT taggedSummon = new CompoundNBT();
            taggedSummon.putUniqueId("uuid", summoned);
            taggedSummons.add(taggedSummon);
        });

        compound.put("summonedLumberjacks", taggedSummons);
        compound.putBoolean("active", active);

        return compound;
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return save(super.getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager networkManager, SUpdateTileEntityPacket packet) {
        handleUpdateTag(packet.getTag());
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(worldPosition, 0, getUpdateTag());
    }

    @Override
    public void tick() {
        if (level.isClientSide()) {
            return;
        }

        if (active) {
            spawnLumberjacks();
            active = false;
        }

        if (!summonedLumberjacks.isEmpty()) {
            List<PlayerEntity> players = level.getEntitiesOfClass(PlayerEntity.class, AxisAlignedBB.expand(pos.getX() - 50, pos.getY() - 50, pos.getZ() - 50, pos.getX() + 50, pos.getY() + 50, pos.getZ() + 50),
                    predicate -> predicate.getMainHandItem().getItem() == Items.WATER_BUCKET || predicate.getOffhandItem().getItem() == Items.WATER_BUCKET);

            if (!players.isEmpty()) {
                followPlayers(players);
            }
        }
    }
}