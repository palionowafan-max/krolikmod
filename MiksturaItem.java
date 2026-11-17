package pl.ziomsigiemka.krolik.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

/** Mikstura pełnego HP - od królika po komendzie /miksturowiec. */
public class MiksturaItem extends OpisItem {

    public MiksturaItem(Properties props) {
        super(props, "Wypij = całe życie wraca do pełna!\nDostajesz ją od królika: /miksturowiec");
    }

    @Override public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.DRINK; }
    @Override public int getUseDuration(ItemStack stack) { return 32; }
    @Override public boolean isFoil(ItemStack stack) { return true; }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide) entity.setHealth(entity.getMaxHealth());
        boolean creative = entity instanceof Player p && p.getAbilities().instabuild;
        if (!creative) {
            stack.shrink(1);
            if (stack.isEmpty()) return new ItemStack(Items.GLASS_BOTTLE);
            if (entity instanceof Player p && !p.getInventory().add(new ItemStack(Items.GLASS_BOTTLE))) {
                p.drop(new ItemStack(Items.GLASS_BOTTLE), false);
            }
        }
        return stack;
    }
}
