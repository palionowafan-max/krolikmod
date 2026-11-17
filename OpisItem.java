package pl.ziomsigiemka.krolik.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/** Item z opisem w dymku (tooltip). */
public class OpisItem extends Item {
    private final String desc;

    public OpisItem(Properties props, String desc) {
        super(props);
        this.desc = desc;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        for (String line : desc.split("\n")) tooltip.add(Component.literal(line).withStyle(ChatFormatting.GRAY));
    }
}
