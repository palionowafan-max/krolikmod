package pl.ziomsigiemka.krolik.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import pl.ziomsigiemka.krolik.client.ClientHooks;

public class TabletItem extends OpisItem {
    public TabletItem(Properties props) {
        super(props, "PPM = otwórz sklep dla królika.\nDostawa od razu - paczka ląduje 5 bloków od ciebie!");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ClientHooks::openTablet);
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }
}
