package pl.ziomsigiemka.krolik.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import pl.ziomsigiemka.krolik.ModRegistry;
import pl.ziomsigiemka.krolik.block.TunelBlock;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

public class PetRabbitGoals {

    private static Vec3 center(BlockPos p) { return Vec3.atBottomCenterOf(p); }

    // ------------------------------------------------------------------ KLATKA
    /** Królik sam chodzi do klatki (albo siedzi w niej zamknięty): biega, pije z poidełka i je. */
    public static class CageGoal extends Goal {
        private final PetRabbit r;
        private BlockPos cage;
        private int stay, timeout;

        public CageGoal(PetRabbit r) { this.r = r; setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP)); }

        @Override
        public boolean canUse() {
            if (r.lockedInCage && r.cagePos != null) { cage = r.cagePos; return true; }
            if (r.isBusy() || r.getRandom().nextInt(900) != 0) return false;
            Optional<BlockPos> f = BlockPos.findClosestMatch(r.blockPosition(), 16, 4,
                    p -> r.level().getBlockState(p).is(ModRegistry.KLATKA.get()));
            if (f.isEmpty()) return false;
            cage = f.get().immutable();
            return true;
        }

        @Override
        public void start() {
            stay = 500;
            timeout = 300;
            if (!r.lockedInCage) r.say("Idę do mojej klatki!");
        }

        @Override
        public boolean canContinueToUse() {
            if (!r.level().getBlockState(cage).is(ModRegistry.KLATKA.get())) return false;
            if (r.lockedInCage) return true;
            return stay > 0 && timeout > 0 && !r.isPassenger() && !r.isVehicle();
        }

        @Override
        public void tick() {
            if (!r.blockPosition().equals(cage)) {
                timeout--;
                if (r.tickCount % 15 == 0) r.getNavigation().moveTo(cage.getX() + 0.5D, cage.getY(), cage.getZ() + 0.5D, 1.2D);
                return;
            }
            stay--;
            if (!(r.level() instanceof ServerLevel sl)) return;
            int t = r.tickCount % 120;
            if (t == 0) { // idzie do poidełka
                r.getMoveControl().setWantedPosition(cage.getX() + 0.75D, cage.getY(), cage.getZ() + 0.25D, 0.7D);
            } else if (t == 20) { // pije
                sl.sendParticles(ParticleTypes.SPLASH, cage.getX() + 0.8D, cage.getY() + 0.2D, cage.getZ() + 0.2D, 8, 0.1D, 0.05D, 0.1D, 0.0D);
                r.playSound(SoundEvents.GENERIC_DRINK, 0.5F, 1.5F);
                r.heal(1.0F);
            } else if (t == 50) { // biega
                r.getMoveControl().setWantedPosition(cage.getX() + 0.2D + r.getRandom().nextDouble() * 0.6D, cage.getY(),
                        cage.getZ() + 0.2D + r.getRandom().nextDouble() * 0.6D, 1.0D);
            } else if (t == 70) { // idzie do jedzenia
                r.getMoveControl().setWantedPosition(cage.getX() + 0.25D, cage.getY(), cage.getZ() + 0.75D, 0.7D);
            } else if (t == 90) { // je
                sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, cage.getX() + 0.2D, cage.getY() + 0.3D, cage.getZ() + 0.8D, 5, 0.1D, 0.1D, 0.1D, 0.0D);
                r.playSound(SoundEvents.GENERIC_EAT, 0.5F, 1.4F);
                r.heal(1.0F);
            } else if (t == 105) {
                r.getJumpControl().jump();
            }
        }

        @Override
        public void stop() {
            if (!r.lockedInCage) r.say("Wychodzę z klatki, papa klatko!");
        }
    }

    // ------------------------------------------------------------------ /idz
    public static class CommandMoveGoal extends Goal {
        private final PetRabbit r;

        public CommandMoveGoal(PetRabbit r) { this.r = r; setFlags(EnumSet.of(Flag.MOVE)); }

        @Override public boolean canUse() { return r.commandTarget != null && !r.lockedInCage; }

        @Override public void start() { r.commandTicks = 400; }

        @Override public boolean canContinueToUse() { return r.commandTarget != null && r.commandTicks > 0; }

        @Override
        public void tick() {
            r.commandTicks--;
            BlockPos t = r.commandTarget;
            if (t == null) return;
            if (r.tickCount % 10 == 0) r.getNavigation().moveTo(t.getX() + 0.5D, t.getY(), t.getZ() + 0.5D, 1.8D);
            if (r.position().distanceToSqr(center(t)) < 2.0D) {
                r.commandTarget = null;
                r.say("Jestem na miejscu!");
            }
        }

        @Override
        public void stop() {
            r.commandTarget = null;
            r.getNavigation().stop();
        }
    }

    // ------------------------------------------------------------------ CHODZENIE ZA WŁAŚCICIELEM
    public static class FollowOwnerGoal extends Goal {
        private final PetRabbit r;
        private Player owner;
        private int recalc;

        public FollowOwnerGoal(PetRabbit r) { this.r = r; setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK)); }

        @Override
        public boolean canUse() {
            if (r.isBusy()) return false;
            owner = r.getOwnerPlayer();
            return owner != null && !owner.isSpectator() && r.distanceToSqr(owner) > 10 * 10;
        }

        @Override
        public boolean canContinueToUse() {
            return owner != null && owner.isAlive() && !r.isBusy() && r.distanceToSqr(owner) > 3 * 3;
        }

        @Override public void start() { recalc = 0; }

        @Override
        public void tick() {
            r.getLookControl().setLookAt(owner, 10.0F, r.getMaxHeadXRot());
            if (--recalc > 0) return;
            recalc = 10;
            if (r.distanceToSqr(owner) > 32 * 32) teleportNearOwner();
            else r.getNavigation().moveTo(owner, 1.7D);
        }

        private void teleportNearOwner() {
            Level level = r.level();
            for (int i = 0; i < 12; i++) {
                BlockPos p = owner.blockPosition().offset(r.getRandom().nextInt(7) - 3, 0, r.getRandom().nextInt(7) - 3);
                if (level.isEmptyBlock(p) && level.getBlockState(p.below()).isFaceSturdy(level, p.below(), Direction.UP)) {
                    r.moveTo(p.getX() + 0.5D, p.getY(), p.getZ() + 0.5D, r.getYRot(), r.getXRot());
                    r.getNavigation().stop();
                    return;
                }
            }
        }

        @Override public void stop() { owner = null; r.getNavigation().stop(); }
    }

    // ------------------------------------------------------------------ DREWNO
    /** Gryzie drewno 5 sekund, potem przez 10 minut nie gryzie. Drewno nigdy nie znika. */
    public static class ChewWoodGoal extends Goal {
        private final PetRabbit r;
        private BlockPos wood;
        private int chew, timeout;

        public ChewWoodGoal(PetRabbit r) { this.r = r; setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK)); }

        @Override
        public boolean canUse() {
            if (r.isBusy() || r.level().getGameTime() < r.chewCooldownUntil || r.getRandom().nextInt(40) != 0) return false;
            Optional<BlockPos> f = BlockPos.findClosestMatch(r.blockPosition(), 10, 4,
                    p -> r.level().getBlockState(p).is(ModRegistry.DREWNO.get()));
            if (f.isEmpty()) return false;
            wood = f.get().immutable();
            return true;
        }

        @Override public void start() { chew = 0; timeout = 400; }

        @Override
        public boolean canContinueToUse() {
            return chew < 100 && timeout > 0 && !r.isBusy() && r.level().getBlockState(wood).is(ModRegistry.DREWNO.get());
        }

        @Override
        public void tick() {
            timeout--;
            r.getLookControl().setLookAt(wood.getX() + 0.5D, wood.getY() + 0.2D, wood.getZ() + 0.5D);
            if (r.position().distanceToSqr(center(wood)) > 1.6D * 1.6D) {
                if (r.tickCount % 10 == 0) r.getNavigation().moveTo(wood.getX() + 0.5D, wood.getY(), wood.getZ() + 0.5D, 1.3D);
                return;
            }
            r.getNavigation().stop();
            if (chew == 0) r.say("Drewienko! Chrup chrup!");
            chew++;
            if (chew % 8 == 0 && r.level() instanceof ServerLevel sl) {
                BlockState s = r.level().getBlockState(wood);
                sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, s), wood.getX() + 0.5D, wood.getY() + 0.3D, wood.getZ() + 0.5D,
                        6, 0.2D, 0.1D, 0.2D, 0.05D);
                r.playSound(SoundEvents.GENERIC_EAT, 0.6F, 1.6F);
            }
        }

        @Override
        public void stop() {
            if (chew >= 100) {
                r.chewCooldownUntil = r.level().getGameTime() + 12000; // 10 minut
                r.say("Mniam, wystarczy na teraz.");
            }
        }
    }

    // ------------------------------------------------------------------ PIŁKA
    public static class PlayBallGoal extends Goal {
        private final PetRabbit r;
        private Pilka ball;
        private int play, kickCd;

        public PlayBallGoal(PetRabbit r) { this.r = r; setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK)); }

        @Override
        public boolean canUse() {
            if (r.isBusy() || r.getRandom().nextInt(60) != 0) return false;
            List<Pilka> balls = r.level().getEntitiesOfClass(Pilka.class, r.getBoundingBox().inflate(12.0D));
            ball = balls.stream().min(Comparator.comparingDouble(r::distanceToSqr)).orElse(null);
            return ball != null;
        }

        @Override public void start() { play = 0; kickCd = 0; r.say("Piłka! Moja piłka!"); }

        @Override
        public boolean canContinueToUse() {
            return ball != null && ball.isAlive() && play < 400 && !r.isBusy() && r.distanceToSqr(ball) < 20 * 20;
        }

        @Override
        public void tick() {
            play++;
            if (kickCd > 0) kickCd--;
            r.getLookControl().setLookAt(ball, 30.0F, 30.0F);
            if (r.tickCount % 8 == 0) r.getNavigation().moveTo(ball, 1.5D);
            if (kickCd == 0 && r.distanceToSqr(ball) < 1.3D * 1.3D) {
                Vec3 dir = ball.position().subtract(r.position()).multiply(1.0D, 0.0D, 1.0D);
                if (dir.lengthSqr() < 1.0E-4D) dir = new Vec3(r.getRandom().nextDouble() - 0.5D, 0, r.getRandom().nextDouble() - 0.5D);
                dir = dir.normalize();
                ball.kick(dir.x * 0.45D, 0.35D + r.getRandom().nextDouble() * 0.2D, dir.z * 0.45D);
                kickCd = 25;
            }
        }

        @Override public void stop() { ball = null; r.getNavigation().stop(); }
    }

    // ------------------------------------------------------------------ DESKOROLKA
    public static class RideSkateGoal extends Goal {
        private final PetRabbit r;
        private Deskorolka skate;
        private int time;

        public RideSkateGoal(PetRabbit r) { this.r = r; setFlags(EnumSet.of(Flag.MOVE)); }

        @Override
        public boolean canUse() {
            if (r.isBusy() || r.getRandom().nextInt(100) != 0) return false;
            List<Deskorolka> list = r.level().getEntitiesOfClass(Deskorolka.class, r.getBoundingBox().inflate(12.0D), d -> !d.isVehicle());
            skate = list.stream().min(Comparator.comparingDouble(r::distanceToSqr)).orElse(null);
            return skate != null;
        }

        @Override public void start() { time = 0; }

        @Override
        public boolean canContinueToUse() {
            return skate != null && skate.isAlive() && !skate.isVehicle() && !r.isBusy() && time < 300;
        }

        @Override
        public void tick() {
            time++;
            if (r.tickCount % 10 == 0) r.getNavigation().moveTo(skate, 1.4D);
            if (r.distanceToSqr(skate) < 1.5D * 1.5D) {
                r.getNavigation().stop();
                if (r.startRiding(skate)) r.say("Juhuuu! Jadę na deskorolce!");
            }
        }

        @Override public void stop() { skate = null; }
    }

    // ------------------------------------------------------------------ TUNEL
    /** Królik przechodzi przez szklany tunel z jednego końca na drugi, jak przez kanał. */
    public static class TunnelGoal extends Goal {
        private final PetRabbit r;
        private BlockPos entry, exit;
        private int phase, timeout;

        public TunnelGoal(PetRabbit r) { this.r = r; setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP)); }

        @Override
        public boolean canUse() {
            if (r.isBusy() || r.getRandom().nextInt(150) != 0) return false;
            Level level = r.level();
            Optional<BlockPos> f = BlockPos.findClosestMatch(r.blockPosition(), 12, 3,
                    p -> level.getBlockState(p).is(ModRegistry.TUNEL_BLOK.get()));
            if (f.isEmpty()) return false;
            BlockPos start = f.get().immutable();
            Direction.Axis axis = level.getBlockState(start).getValue(TunelBlock.AXIS);
            Direction pos = Direction.get(Direction.AxisDirection.POSITIVE, axis);
            Direction neg = pos.getOpposite();
            BlockPos a = start, b = start;
            for (int i = 0; i < 64 && level.getBlockState(a.relative(neg)).is(ModRegistry.TUNEL_BLOK.get()); i++) a = a.relative(neg);
            for (int i = 0; i < 64 && level.getBlockState(b.relative(pos)).is(ModRegistry.TUNEL_BLOK.get()); i++) b = b.relative(pos);
            if (r.position().distanceToSqr(center(a)) <= r.position().distanceToSqr(center(b))) {
                entry = a.relative(neg);
                exit = b.relative(pos);
            } else {
                entry = b.relative(pos);
                exit = a.relative(neg);
            }
            return true;
        }

        @Override public void start() { phase = 0; timeout = 600; }

        @Override
        public boolean canContinueToUse() { return phase < 2 && timeout > 0 && !r.isBusy(); }

        @Override
        public void tick() {
            timeout--;
            if (phase == 0) {
                if (r.tickCount % 15 == 0) r.getNavigation().moveTo(entry.getX() + 0.5D, entry.getY(), entry.getZ() + 0.5D, 1.3D);
                if (r.position().distanceToSqr(center(entry)) < 0.8D) {
                    r.getNavigation().stop();
                    phase = 1;
                    r.say("Wchodzę do kanału! Hop do tunelu!");
                }
            } else {
                r.getMoveControl().setWantedPosition(exit.getX() + 0.5D, exit.getY(), exit.getZ() + 0.5D, 1.2D);
                if (r.position().distanceToSqr(center(exit)) < 0.8D) {
                    phase = 2;
                    r.say("Wyszedłem z tunelu!");
                }
            }
        }

        @Override public void stop() { r.getNavigation().stop(); }
    }
}
