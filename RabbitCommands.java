package pl.ziomsigiemka.krolik.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.RegisterCommandsEvent;
import pl.ziomsigiemka.krolik.ModRegistry;
import pl.ziomsigiemka.krolik.entity.PetRabbit;

import java.util.Comparator;

/** Komendy Miłośnika: /obrot /skok /idz /gryzon /miksturowiec */
public class RabbitCommands {

    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> d = event.getDispatcher();
        for (String name : new String[]{"obrot", "skok", "idz", "gryzon", "miksturowiec"}) {
            d.register(Commands.literal(name).executes(c -> run(c, name)));
        }
    }

    private static int run(CommandContext<CommandSourceStack> c, String action) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        PetRabbit r = p.level().getEntitiesOfClass(PetRabbit.class, p.getBoundingBox().inflate(64.0D),
                        x -> x.isOwner(p) && x.hasGear(PetRabbit.MILOSNIK))
                .stream().min(Comparator.comparingDouble(x -> x.distanceToSqr(p))).orElse(null);

        if (r == null) {
            c.getSource().sendFailure(Component.literal("Nie masz w pobliżu królika z Miłośnikiem! Kup Miłośnika na tablecie i daj go królikowi."));
            return 0;
        }

        String info;
        switch (action) {
            case "obrot" -> {
                r.spinTicks = 20;
                r.say("Wiiii, kręcę się!");
                info = "Królik się obraca!";
            }
            case "skok" -> {
                Vec3 v = r.getDeltaMovement();
                r.setDeltaMovement(v.x, 0.9D, v.z);
                r.hasImpulse = true;
                r.playSound(SoundEvents.RABBIT_JUMP, 1.0F, 1.0F);
                r.say("Hop!");
                info = "Królik skacze!";
            }
            case "idz" -> {
                if (r.lockedInCage) {
                    c.getSource().sendFailure(Component.literal("Królik jest zamknięty w klatce. Kliknij klatkę, żeby go wypuścić."));
                    return 0;
                }
                HitResult hit = p.pick(64.0D, 1.0F, false);
                if (hit.getType() != HitResult.Type.BLOCK) {
                    c.getSource().sendFailure(Component.literal("Pokaż celownikiem blok, do którego królik ma pójść."));
                    return 0;
                }
                BlockHitResult bh = (BlockHitResult) hit;
                r.commandTarget = bh.getBlockPos().relative(bh.getDirection());
                r.say("Już idę!");
                info = "Królik idzie tam, gdzie pokazujesz!";
            }
            case "gryzon" -> {
                if (r.gryzonTicks > 0) {
                    r.gryzonTicks = 0;
                    info = "Królik przestał gryźć bloki.";
                } else {
                    r.gryzonTicks = 400;
                    r.say("Chrum chrum, zjem wszystko!");
                    info = "Królik gryzie bloki przez 20 sekund! (/gryzon jeszcze raz = stop)";
                }
            }
            default -> { // miksturowiec
                if (r.potionCooldown > 0) {
                    c.getSource().sendFailure(Component.literal("Królik robi nową miksturę... poczekaj jeszcze " + (r.potionCooldown / 20 + 1) + " s."));
                    return 0;
                }
                r.potionCooldown = 1200;
                ItemStack potion = new ItemStack(ModRegistry.MIKSTURA.get());
                if (!p.getInventory().add(potion)) p.drop(potion, false);
                r.say("Proszę, mikstura pełnego HP dla ciebie!");
                info = "Królik dał ci Miksturę pełnego HP!";
            }
        }
        final String msg = info;
        c.getSource().sendSuccess(() -> Component.literal("§d" + msg), false);
        return 1;
    }
}
