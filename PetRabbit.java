package pl.ziomsigiemka.krolik.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import pl.ziomsigiemka.krolik.ModRegistry;
import pl.ziomsigiemka.krolik.item.EquipItem;

import javax.annotation.Nullable;
import java.util.UUID;

public class PetRabbit extends Rabbit implements PlayerRideableJumping {
    private static final EntityDataAccessor<Integer> GEAR = SynchedEntityData.defineId(PetRabbit.class, EntityDataSerializers.INT);

    public static final int OKULARY = 1;
    public static final int BUTY = 2;
    public static final int OCHRONA = 4;
    public static final int DZWIEK = 8;
    public static final int MILOSNIK = 16;
    private static final int[] ALL_GEAR = {OKULARY, BUTY, OCHRONA, DZWIEK, MILOSNIK};

    private static final String[] GADKI = {
            "Kic kic!",
            "Mam ochotę na marchewkę...",
            "Pobawimy się piłką?",
            "Gdzie jest moje drewienko do gryzienia?",
            "Hop hop hop!",
            "Nudzi mi się...",
            "Jestem najszybszym królikiem na świecie!",
            "Chcę pojeździć na deskorolce!",
            "Lubię cię, wiesz?",
            "*rusza noskiem*",
            "Kup mi coś na tablecie!"
    };

    @Nullable private UUID ownerId;
    public long chewCooldownUntil = 0;
    @Nullable public BlockPos cagePos;
    public boolean lockedInCage = false;
    @Nullable public BlockPos commandTarget;
    public int commandTicks = 0;
    public int gryzonTicks = 0;
    public int spinTicks = 0;
    public int potionCooldown = 0;
    private int chatterTimer = 400;
    private int pendingJump = 0;

    public PetRabbit(EntityType<? extends PetRabbit> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createPetAttributes() {
        return Rabbit.createAttributes()
                .add(Attributes.MAX_HEALTH, 12.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.35D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(GEAR, 0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PetRabbitGoals.CageGoal(this));
        this.goalSelector.addGoal(1, new PetRabbitGoals.CommandMoveGoal(this));
        this.goalSelector.addGoal(2, new TemptGoal(this, 1.6D, Ingredient.of(ModRegistry.PRZYSMAK.get()), false));
        this.goalSelector.addGoal(3, new PetRabbitGoals.FollowOwnerGoal(this));
        this.goalSelector.addGoal(4, new PetRabbitGoals.ChewWoodGoal(this));
        this.goalSelector.addGoal(4, new PetRabbitGoals.PlayBallGoal(this));
        this.goalSelector.addGoal(4, new PetRabbitGoals.RideSkateGoal(this));
        this.goalSelector.addGoal(4, new PetRabbitGoals.TunnelGoal(this));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.6D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
    }

    // ===================== WŁAŚCICIEL I EKWIPUNEK =====================

    public void setOwnerId(@Nullable UUID id) { this.ownerId = id; }
    @Nullable public UUID getOwnerId() { return ownerId; }

    public boolean isOwner(@Nullable Entity e) {
        return e != null && ownerId != null && ownerId.equals(e.getUUID());
    }

    @Nullable
    public Player getOwnerPlayer() {
        return ownerId == null ? null : level().getPlayerByUUID(ownerId);
    }

    public boolean hasGear(int flag) { return (this.entityData.get(GEAR) & flag) != 0; }

    public void setGear(int flag, boolean on) {
        int g = this.entityData.get(GEAR);
        this.entityData.set(GEAR, on ? (g | flag) : (g & ~flag));
    }

    /** Królik jest zajęty czymś ważniejszym niż zabawa. */
    public boolean isBusy() {
        return lockedInCage || commandTarget != null || isPassenger() || isVehicle() || gryzonTicks > 0;
    }

    /** Dźwiękoinator - królik pisze na czacie. */
    public void say(String text) {
        if (level().isClientSide || !hasGear(DZWIEK)) return;
        Component msg = Component.literal("§d[" + getName().getString() + "] §f" + text);
        for (Player p : level().players()) {
            if (isOwner(p) || p.distanceToSqr(this) < 48 * 48) p.sendSystemMessage(msg);
        }
    }

    private static Item gearItem(int flag) {
        return switch (flag) {
            case OKULARY -> ModRegistry.OKULARY.get();
            case BUTY -> ModRegistry.BUTY.get();
            case OCHRONA -> ModRegistry.OCHRONA.get();
            case DZWIEK -> ModRegistry.DZWIEKOINATOR.get();
            default -> ModRegistry.MILOSNIK.get();
        };
    }

    private static String gearThanks(int flag) {
        return switch (flag) {
            case OKULARY -> "Super okulary! Teraz pływam jak kaczka!";
            case BUTY -> "Fajne buciki! Nie boję się wysokości!";
            case OCHRONA -> "Teraz nikt mi nie podskoczy!";
            case DZWIEK -> "Hej! Teraz mogę z tobą gadać!";
            default -> "Kocham cię! Napisz /obrot, /skok, /idz, /gryzon albo /miksturowiec!";
        };
    }

    private void hearts() {
        if (level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.HEART, getX(), getY() + 0.6D, getZ(), 5, 0.3D, 0.3D, 0.3D, 0.0D);
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        boolean client = level().isClientSide;

        // --- zakładanie rzeczy (okulary, buty, ochrona, dźwiękoinator, miłośnik)
        if (stack.getItem() instanceof EquipItem eq) {
            if (!client) {
                if (ownerId == null) ownerId = player.getUUID();
                if (!isOwner(player)) {
                    player.displayClientMessage(Component.literal("§cTo nie twój królik!"), true);
                } else if (hasGear(eq.flag)) {
                    player.displayClientMessage(Component.literal("§eKrólik już to ma!"), true);
                } else {
                    String name = stack.getHoverName().getString();
                    setGear(eq.flag, true);
                    if (!player.getAbilities().instabuild) stack.shrink(1);
                    player.displayClientMessage(Component.literal("§aKrólik dostał: " + name), true);
                    playSound(SoundEvents.ARMOR_EQUIP_LEATHER, 1.0F, 1.2F);
                    hearts();
                    say(gearThanks(eq.flag));
                }
            }
            return InteractionResult.sidedSuccess(client);
        }

        // --- sterownik: wejście w ciało królika
        if (stack.is(ModRegistry.STEROWNIK.get())) {
            if (!client) {
                if (ownerId == null) ownerId = player.getUUID();
                if (isOwner(player)) {
                    if (isPassenger()) stopRiding();
                    lockedInCage = false;
                    cagePos = null;
                    player.startRiding(this);
                    player.displayClientMessage(Component.literal("§bSterujesz królikiem! WASD - ruch, Spacja - skok, Shift - wyjście"), true);
                    say("Ej, co się dzieje?! Ktoś mną steruje!");
                } else {
                    player.displayClientMessage(Component.literal("§cTo nie twój królik!"), true);
                }
            }
            return InteractionResult.sidedSuccess(client);
        }

        // --- przysmak: karmienie / oswajanie
        if (stack.is(ModRegistry.PRZYSMAK.get())) {
            if (!client) {
                if (ownerId == null) {
                    ownerId = player.getUUID();
                    player.displayClientMessage(Component.literal("§aTen królik jest teraz twój!"), true);
                }
                heal(4.0F);
                if (!player.getAbilities().instabuild) stack.shrink(1);
                playSound(SoundEvents.GENERIC_EAT, 1.0F, 1.5F);
                hearts();
                say("Mniam! Dziękuję!");
            }
            return InteractionResult.sidedSuccess(client);
        }

        // --- Shift + PPM pustą ręką: zdejmij wszystkie rzeczy
        if (stack.isEmpty() && player.isShiftKeyDown()) {
            if (!client && isOwner(player)) {
                int g = this.entityData.get(GEAR);
                for (int f : ALL_GEAR) {
                    if ((g & f) != 0) {
                        ItemStack it = new ItemStack(gearItem(f));
                        if (!player.getInventory().add(it)) spawnAtLocation(it);
                    }
                }
                this.entityData.set(GEAR, 0);
                player.displayClientMessage(Component.literal("§eZdjęto wszystkie rzeczy z królika."), true);
            }
            return InteractionResult.sidedSuccess(client);
        }

        return super.mobInteract(player, hand);
    }

    // ===================== CO TICK =====================

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide) serverTick((ServerLevel) level());
    }

    private void serverTick(ServerLevel sl) {
        // Okulary do wody: nie topi się i pływa na górze
        if (hasGear(OKULARY)) {
            setAirSupply(getMaxAirSupply());
            if (isInWater()) {
                Vec3 d = getDeltaMovement();
                if (d.y < 0.12D) setDeltaMovement(d.x, d.y + 0.07D, d.z);
            }
        }

        // Buty: powolne opadanie
        if (hasGear(BUTY) && !onGround() && !isInWater()) {
            Vec3 d = getDeltaMovement();
            if (d.y < -0.12D) setDeltaMovement(d.x, -0.12D, d.z);
            this.fallDistance = 0.0F;
            if (tickCount % 4 == 0) sl.sendParticles(ParticleTypes.CLOUD, getX(), getY(), getZ(), 1, 0.1D, 0.0D, 0.1D, 0.0D);
        }

        // Ochrona: ogłusza i rani przeciwników obok
        if (hasGear(OCHRONA) && tickCount % 20 == 0) {
            for (LivingEntity e : level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(3.0D), this::isEnemy)) {
                e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 9));
                e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 1));
                e.hurt(damageSources().mobAttack(this), 2.0F);
                sl.sendParticles(ParticleTypes.CRIT, e.getX(), e.getEyeY() + 0.3D, e.getZ(), 8, 0.3D, 0.2D, 0.3D, 0.1D);
            }
        }

        // Dźwiękoinator: losowe gadanie
        if (hasGear(DZWIEK) && --chatterTimer <= 0) {
            chatterTimer = 600 + random.nextInt(900);
            say(GADKI[random.nextInt(GADKI.length)]);
        }

        // Zamknięty w klatce
        if (lockedInCage) {
            if (cagePos == null || !level().getBlockState(cagePos).is(ModRegistry.KLATKA.get())) {
                lockedInCage = false;
                cagePos = null;
                say("Jestem wolny!");
            } else if (distanceToSqr(Vec3.atBottomCenterOf(cagePos)) > 4.0D) {
                teleportTo(cagePos.getX() + 0.5D, cagePos.getY(), cagePos.getZ() + 0.5D);
            }
        }

        // /gryzon - gryzie bloki
        if (gryzonTicks > 0) {
            gryzonTicks--;
            if (gryzonTicks % 10 == 0) gnaw(sl);
            if (gryzonTicks == 0) say("Uff, najadłem się bloków!");
        }

        // /obrot
        if (spinTicks > 0) {
            spinTicks--;
            float y = getYRot() + 18.0F;
            setYRot(y);
            setYHeadRot(y);
            this.yBodyRot = y;
        }

        if (potionCooldown > 0) potionCooldown--;

        // Sterownik: gracz w środku jest niewidzialny
        if (getFirstPassenger() instanceof Player p && tickCount % 10 == 0) {
            p.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 30, 0, false, false));
        }
    }

    private boolean isEnemy(LivingEntity e) {
        if (e == this || !e.isAlive() || isOwner(e) || e instanceof PetRabbit) return false;
        if (e.getVehicle() == this) return false;
        if (e instanceof Player p) return !p.isCreative() && !p.isSpectator();
        return e instanceof Enemy;
    }

    private void gnaw(ServerLevel sl) {
        BlockPos front = blockPosition().relative(getDirection());
        boolean ate = tryEat(sl, front) || tryEat(sl, front.above());
        if (!ate) {
            for (Direction d : Direction.Plane.HORIZONTAL.shuffledCopy(random)) {
                BlockPos f = blockPosition().relative(d);
                if (tryEat(sl, f) || tryEat(sl, f.above())) {
                    front = f;
                    break;
                }
            }
        }
        getMoveControl().setWantedPosition(front.getX() + 0.5D, front.getY(), front.getZ() + 0.5D, 1.0D);
    }

    private boolean tryEat(ServerLevel sl, BlockPos p) {
        BlockState s = sl.getBlockState(p);
        if (s.isAir() || !s.getFluidState().isEmpty() || sl.getBlockEntity(p) != null) return false;
        float hardness = s.getDestroySpeed(sl, p);
        if (hardness < 0.0F || hardness > 3.0F) return false;
        sl.destroyBlock(p, false, this);
        playSound(SoundEvents.GENERIC_EAT, 1.0F, 1.0F);
        return true;
    }

    // ===================== OBRAŻENIA =====================

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // właściciel nie zrani królika przypadkiem (chyba że trzyma Shift)
        if (source.getEntity() instanceof Player p && isOwner(p) && !p.isShiftKeyDown()) return false;
        if (hasGear(BUTY) && source.is(DamageTypeTags.IS_FALL)) return false;
        return super.hurt(source, amount);
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        if (hasGear(BUTY)) return false;
        return super.causeFallDamage(distance, multiplier, source);
    }

    @Override
    public boolean canBreatheUnderwater() {
        return hasGear(OKULARY) || super.canBreatheUnderwater();
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    // ===================== STEROWNIK (jazda w ciele królika) =====================

    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        if (getFirstPassenger() instanceof Player p) return p;
        return super.getControllingPassenger();
    }

    @Override
    protected void tickRidden(Player player, Vec3 travel) {
        super.tickRidden(player, travel);
        this.setRot(player.getYRot(), player.getXRot() * 0.5F);
        this.yRotO = this.getYRot();
        this.yBodyRot = this.getYRot();
        this.yHeadRot = this.getYRot();
        if (pendingJump > 0 && onGround()) {
            Vec3 d = getDeltaMovement();
            setDeltaMovement(d.x, 0.42D + pendingJump / 250.0D, d.z);
            this.hasImpulse = true;
            pendingJump = 0;
        }
    }

    @Override
    protected Vec3 getRiddenInput(Player player, Vec3 travel) {
        float strafe = player.xxa * 0.6F;
        float forward = player.zza;
        if (forward <= 0.0F) forward *= 0.4F;
        return new Vec3(strafe, 0.0D, forward);
    }

    @Override
    protected float getRiddenSpeed(Player player) {
        return (float) getAttributeValue(Attributes.MOVEMENT_SPEED) * 1.1F;
    }

    @Override
    public void onPlayerJump(int power) {
        pendingJump = Math.max(power, 30);
    }

    @Override
    public boolean canJump() { return true; }

    @Override
    public void handleStartJump(int power) {}

    @Override
    public void handleStopJump() {}

    // ===================== ZAPIS =====================

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (ownerId != null) tag.putUUID("KrolikOwner", ownerId);
        tag.putInt("Gear", this.entityData.get(GEAR));
        tag.putLong("ChewCd", chewCooldownUntil);
        if (cagePos != null) tag.putLong("Cage", cagePos.asLong());
        tag.putBoolean("Locked", lockedInCage);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("KrolikOwner")) ownerId = tag.getUUID("KrolikOwner");
        this.entityData.set(GEAR, tag.getInt("Gear"));
        chewCooldownUntil = tag.getLong("ChewCd");
        cagePos = tag.contains("Cage") ? BlockPos.of(tag.getLong("Cage")) : null;
        lockedInCage = tag.getBoolean("Locked") && cagePos != null;
    }
}
