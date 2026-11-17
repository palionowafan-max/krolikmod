package pl.ziomsigiemka.krolik.item;

/** Rzecz, którą zakłada się królikowi (PPM na królika). */
public class EquipItem extends OpisItem {
    public final int flag;

    public EquipItem(Properties props, int flag, String desc) {
        super(props, desc + "\n§ePPM na swojego królika = załóż mu to.\n§8Shift+PPM pustą ręką na królika = zdejmij wszystko.");
        this.flag = flag;
    }
}
