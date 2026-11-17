package pl.ziomsigiemka.krolik.block;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import pl.ziomsigiemka.krolik.entity.PetRabbit;

import java.util.Comparator;
import java.util.List;

/** Klatka: królik sam do niej przychodzi, albo wsadzasz go Shift+PPM. Środek bez kolizji, więc królik może tam biegać. */
public class KlatkaBlock extends Block {

    public KlatkaBlock(Properties props) { super(props); }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (level.isClientSide) return InteractionResult.SUCCESS;

        List<PetRabbit> mine = level.getEntitiesOfClass(PetRabbit.class, new AABB(pos).inflate(32.0D), r -> r.isOwner(player));

        for (PetRabbit r : mine) {
            if (r.lockedInCage && pos.equals(r.cagePos)) {
                r.lockedInCage = false;
                r.cagePos = null;
                player.displayClientMessage(Component.literal("§aWypuściłeś królika z klatki."), true);
                r.say("Hurra, wolność!");
                return InteractionResult.CONSUME;
            }
        }

        if (!player.isShiftKeyDown()) {
            player.displayClientMessage(Component.literal("§eShift + PPM pustą ręką = wsadź swojego królika do klatki. Królik sam też tu czasem przychodzi!"), true);
            return InteractionResult.CONSUME;
        }

        PetRabbit nearest = mine.stream().filter(r -> !r.lockedInCage)
                .min(Comparator.comparingDouble(r -> r.distanceToSqr(player))).orElse(null);
        if (nearest == null) {
            player.displayClientMessage(Component.literal("§cNie masz w pobliżu wolnego królika."), true);
        } else {
            nearest.stopRiding();
            nearest.ejectPassengers();
            nearest.commandTarget = null;
            nearest.lockedInCage = true;
            nearest.cagePos = pos.immutable();
            nearest.teleportTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
            nearest.say("Moja klatka! Tu jest woda i jedzonko!");
            player.displayClientMessage(Component.literal("§aKrólik jest w klatce! Kliknij klatkę, żeby go wypuścić."), true);
        }
        return InteractionResult.CONSUME;
    }
}
