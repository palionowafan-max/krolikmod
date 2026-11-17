package pl.ziomsigiemka.krolik.item;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import pl.ziomsigiemka.krolik.entity.PetRabbit;

import java.util.Comparator;

/** Zły zapach: uderz kogoś albo użyj na sobie - królik gryzie, mały widok, spowolnienie, zatrucie. */
public class ZlyZapachItem extends OpisItem {

    public ZlyZapachItem(Properties props) {
        super(props, "Uderz kogoś = twój królik go gryzie!\nPPM = psikasz na siebie (królik gryzie ciebie).\nEfekty: mały widok, spowolnienie, zatrucie.");
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof Player p) {
            bite(target, p);
            if (!p.getAbilities().instabuild) stack.shrink(1);
        }
        return true;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            bite(player, player);
            if (!player.getAbilities().instabuild) stack.shrink(1);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    public static void bite(LivingEntity victim, Player user) {
        Level level = victim.level();
        PetRabbit rabbit = level.getEntitiesOfClass(PetRabbit.class, user.getBoundingBox().inflate(32.0D), r -> r.isOwner(user) && !r.lockedInCage)
                .stream().min(Comparator.comparingDouble(r -> r.distanceToSqr(victim))).orElse(null);

        if (rabbit != null) {
            if (rabbit.distanceToSqr(victim) > 9.0D) rabbit.teleportTo(victim.getX() + 1.0D, victim.getY(), victim.getZ());
            rabbit.getLookControl().setLookAt(victim);
            victim.invulnerableTime = 0;
            victim.hurt(rabbit.damageSources().mobAttack(rabbit), 2.0F);
            rabbit.playSound(SoundEvents.RABBIT_ATTACK, 1.0F, 1.0F);
            rabbit.say(victim == user ? "Fuuu, ale śmierdzisz! *gryz*" : "Co za smród! *gryz gryz*");
        } else {
            victim.hurt(victim.damageSources().magic(), 1.0F);
        }
        victim.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0));
        victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1));
        victim.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 0));
        if (level instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.SNEEZE, victim.getX(), victim.getEyeY(), victim.getZ(), 20, 0.4D, 0.4D, 0.4D, 0.02D);
        }
    }
}
