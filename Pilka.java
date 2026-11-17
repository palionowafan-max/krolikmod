package pl.ziomsigiemka.krolik.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.sounds.SoundEvents;
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

public class Pilka extends Entity implements ItemSupplier {

    public Pilka(EntityType<? extends Pilka> type, Level level) {
        super(type, level);
    }

    public void kick(double x, double y, double z) {
        setDeltaMovement(x, y, z);
        this.hasImpulse = true;
        playSound(SoundEvents.SLIME_SQUISH_SMALL, 0.6F, 1.4F);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) return; // ruch liczy serwer
        Vec3 before = getDeltaMovement();
        double vy = before.y - 0.04D;
        if (isInWater()) vy = Math.min(vy + 0.07D, 0.1D);
        setDeltaMovement(before.x, vy, before.z);
        before = getDeltaMovement();

        move(MoverType.SELF, getDeltaMovement());

        Vec3 after = getDeltaMovement();
        double nx = after.x, ny = after.y, nz = after.z;
        if (this.horizontalCollision) {
            if (after.x == 0.0D && before.x != 0.0D) nx = -before.x * 0.7D;
            if (after.z == 0.0D && before.z != 0.0D) nz = -before.z * 0.7D;
        }
        if (onGround() && before.y < -0.12D) {
            ny = -before.y * 0.6D; // odbicie od ziemi
            playSound(SoundEvents.SLIME_SQUISH_SMALL, 0.3F, 1.6F);
        }
        double friction = onGround() ? 0.88D : 0.98D;
        setDeltaMovement(nx * friction, ny * 0.98D, nz * friction);
    }

    @Override
    public boolean isPickable() { return true; }

    @Override
    public boolean isPushable() { return true; }

    @Override
    public void push(Entity other) {
        if (level().isClientSide || !(other instanceof LivingEntity)) return;
        Vec3 d = position().subtract(other.position()).multiply(1.0D, 0.0D, 1.0D);
        if (d.lengthSqr() > 1.0E-4D) {
            d = d.normalize().scale(0.25D);
            setDeltaMovement(getDeltaMovement().add(d.x, 0.08D, d.z));
            this.hasImpulse = true;
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isInvulnerableTo(source)) return false;
        if (level().isClientSide) return true;
        Entity attacker = source.getEntity();
        if (attacker != null) {
            Vec3 look = attacker.getLookAngle();
            kick(look.x * 0.9D, 0.45D, look.z * 0.9D);
        }
        return true;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (!level().isClientSide) {
            ItemStack stack = getItem();
            if (!player.getInventory().add(stack)) spawnAtLocation(stack);
            discard();
        }
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    @Override
    public ItemStack getItem() { return new ItemStack(ModRegistry.PILKA.get()); }

    @Override protected void defineSynchedData() {}
    @Override protected void readAdditionalSaveData(CompoundTag tag) {}
    @Override protected void addAdditionalSaveData(CompoundTag tag) {}

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
