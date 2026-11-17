package pl.ziomsigiemka.krolik.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import pl.ziomsigiemka.krolik.ModRegistry;

public class Deskorolka extends Entity implements ItemSupplier {
    private int rideTicks = 0;

    public Deskorolka(EntityType<? extends Deskorolka> type, Level level) {
        super(type, level);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) return;
        Vec3 v = getDeltaMovement();
        double vy = isNoGravity() ? v.y : v.y - 0.04D;
        double vx = v.x, vz = v.z;

        if (getFirstPassenger() instanceof PetRabbit rabbit) {
            rideTicks++;
            if (onGround()) {
                Vec3 f = Vec3.directionFromRotation(0.0F, getYRot()).scale(0.32D);
                vx = f.x;
                vz = f.z;
            }
            if (this.horizontalCollision || random.nextInt(70) == 0) {
                setYRot(getYRot() + 90.0F + random.nextInt(180));
            }
            if (tickCount % 3 == 0) {
                ((ServerLevel) level()).sendParticles(ParticleTypes.CLOUD, getX(), getY() + 0.05D, getZ(), 1, 0.1D, 0.0D, 0.1D, 0.0D);
            }
            if (rideTicks > 400) {
                ejectPassengers();
                rideTicks = 0;
                rabbit.say("To było super! Jeszcze raz później!");
            }
        } else {
            rideTicks = 0;
            vx *= 0.7D;
            vz *= 0.7D;
        }
        setDeltaMovement(vx, vy, vz);
        move(MoverType.SELF, getDeltaMovement());
        if (onGround()) setDeltaMovement(getDeltaMovement().multiply(1.0D, 0.0D, 1.0D));
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return getPassengers().isEmpty() && passenger instanceof PetRabbit;
    }

    @Override
    public double getPassengersRidingOffset() { return 0.12D; }

    @Override
    protected void positionRider(Entity passenger, Entity.MoveFunction callback) {
        super.positionRider(passenger, callback);
        passenger.setYRot(getYRot());
        if (passenger instanceof LivingEntity le) {
            le.yBodyRot = getYRot();
            le.setYHeadRot(getYRot());
        }
    }

    @Override
    public boolean isPickable() { return true; }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isInvulnerableTo(source)) return false;
        if (!level().isClientSide && source.getEntity() instanceof Player) pickUp(null);
        return true;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (!level().isClientSide) pickUp(player);
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    private void pickUp(Player player) {
        ejectPassengers();
        ItemStack stack = getItem();
        if (player == null || !player.getInventory().add(stack)) spawnAtLocation(stack);
        discard();
    }

    @Override
    public ItemStack getItem() { return new ItemStack(ModRegistry.DESKOROLKA.get()); }

    @Override protected void defineSynchedData() {}
    @Override protected void readAdditionalSaveData(CompoundTag tag) {}
    @Override protected void addAdditionalSaveData(CompoundTag tag) {}

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
