package pl.ziomsigiemka.krolik;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import pl.ziomsigiemka.krolik.block.*;
import pl.ziomsigiemka.krolik.entity.Deskorolka;
import pl.ziomsigiemka.krolik.entity.PetRabbit;
import pl.ziomsigiemka.krolik.entity.Pilka;
import pl.ziomsigiemka.krolik.item.*;

public class ModRegistry {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, KrolikMod.MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, KrolikMod.MODID);
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, KrolikMod.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, KrolikMod.MODID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, KrolikMod.MODID);

    // ===== BYTY =====
    public static final RegistryObject<EntityType<PetRabbit>> PET_RABBIT = ENTITIES.register("krolik",
            () -> EntityType.Builder.<PetRabbit>of(PetRabbit::new, MobCategory.CREATURE).sized(0.6F, 0.8F).clientTrackingRange(10).build("krolik"));
    public static final RegistryObject<EntityType<Pilka>> PILKA_ENTITY = ENTITIES.register("pilka",
            () -> EntityType.Builder.<Pilka>of(Pilka::new, MobCategory.MISC).sized(0.45F, 0.45F).clientTrackingRange(10).updateInterval(1).build("pilka"));
    public static final RegistryObject<EntityType<Deskorolka>> DESKOROLKA_ENTITY = ENTITIES.register("deskorolka",
            () -> EntityType.Builder.<Deskorolka>of(Deskorolka::new, MobCategory.MISC).sized(0.8F, 0.2F).clientTrackingRange(10).updateInterval(1).build("deskorolka"));

    // ===== BLOKI =====
    public static final RegistryObject<Block> KLATKA = BLOCKS.register("klatka", () -> new KlatkaBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.METAL).strength(2.0F).sound(SoundType.METAL).noOcclusion().noCollission()));
    public static final RegistryObject<Block> DREWNO = BLOCKS.register("drewno", () -> new DrewnoBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.WOOD).strength(1.0F).sound(SoundType.WOOD).noOcclusion()));
    public static final RegistryObject<Block> TUNEL_BLOK = BLOCKS.register("tunel_blok", () -> new TunelBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_LIGHT_BLUE).strength(0.3F).sound(SoundType.GLASS).noOcclusion()
            .isViewBlocking((s, l, p) -> false).isSuffocating((s, l, p) -> false).isRedstoneConductor((s, l, p) -> false)));
    public static final RegistryObject<Block> PACZKA = BLOCKS.register("paczka", () -> new PaczkaBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.WOOD).strength(0.5F).sound(SoundType.WOOL).noOcclusion()));

    public static final RegistryObject<BlockEntityType<PaczkaBlockEntity>> PACZKA_BE = BLOCK_ENTITIES.register("paczka",
            () -> BlockEntityType.Builder.of(PaczkaBlockEntity::new, PACZKA.get()).build(null));

    // ===== ITEMY =====
    public static final RegistryObject<Item> TABLET = ITEMS.register("tablet", () -> new TabletItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> KROLIK = ITEMS.register("krolik", () -> new KrolikBoxItem(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> KLATKA_ITEM = ITEMS.register("klatka", () -> new BlockItem(KLATKA.get(), new Item.Properties()));
    public static final RegistryObject<Item> DREWNO_ITEM = ITEMS.register("drewno", () -> new BlockItem(DREWNO.get(), new Item.Properties()));
    public static final RegistryObject<Item> PRZYSMAK = ITEMS.register("przysmak", () -> new OpisItem(new Item.Properties(),
            "Trzymaj w ręce - królik do ciebie przybiegnie!\nPPM na królika = nakarm go (leczy).\nPPM na królika bez właściciela = staje się twój."));
    public static final RegistryObject<Item> PILKA = ITEMS.register("pilka", () -> new EntityPlaceItem(new Item.Properties().stacksTo(16), PILKA_ENTITY,
            "Postaw na ziemi - królik będzie ją odbijał!\nUderz piłkę żeby ją kopnąć, PPM = podnieś."));
    public static final RegistryObject<Item> OKULARY = ITEMS.register("okulary", () -> new EquipItem(new Item.Properties().stacksTo(1), PetRabbit.OKULARY,
            "Królik nie topi się w wodzie i pływa na górze."));
    public static final RegistryObject<Item> BUTY = ITEMS.register("buty", () -> new EquipItem(new Item.Properties().stacksTo(1), PetRabbit.BUTY,
            "Powolne opadanie - królik nie dostaje obrażeń od upadku."));
    public static final RegistryObject<Item> STEROWNIK = ITEMS.register("sterownik", () -> new OpisItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE),
            "PPM na swojego królika = wchodzisz w jego ciało!\nWASD - ruch, Spacja - skok, LPM - bijesz\nShift - wychodzisz z królika."));
    public static final RegistryObject<Item> OCHRONA = ITEMS.register("ochrona", () -> new EquipItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON), PetRabbit.OCHRONA,
            "Przeciwnicy obok królika są ogłuszeni i tracą życie\n(gracze i moby, ale nie ty)."));
    public static final RegistryObject<Item> DZWIEKOINATOR = ITEMS.register("dzwiekoinator", () -> new EquipItem(new Item.Properties().stacksTo(1), PetRabbit.DZWIEK,
            "Królik zaczyna gadać na czacie!"));
    public static final RegistryObject<Item> DESKOROLKA = ITEMS.register("deskorolka", () -> new EntityPlaceItem(new Item.Properties().stacksTo(1), DESKOROLKA_ENTITY,
            "Postaw na ziemi - królik sam na nią wskoczy i będzie jeździł szybko!\nUderz albo PPM = podnieś."));
    public static final RegistryObject<Item> TUNEL = ITEMS.register("tunel", () -> new TunelItem(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> ZLY_ZAPACH = ITEMS.register("zly_zapach", () -> new ZlyZapachItem(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> MILOSNIK = ITEMS.register("milosnik", () -> new EquipItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC), PetRabbit.MILOSNIK,
            "Królik cię kocha i słucha komend na czacie:\n/obrot  /skok  /idz  /gryzon  /miksturowiec"));
    public static final RegistryObject<Item> MIKSTURA = ITEMS.register("mikstura", () -> new MiksturaItem(new Item.Properties().stacksTo(4).rarity(Rarity.RARE)));

    public static final RegistryObject<CreativeModeTab> TAB = TABS.register("krolik_tab", () -> CreativeModeTab.builder()
            .title(Component.literal("Królik Mod"))
            .icon(() -> new ItemStack(TABLET.get()))
            .displayItems((params, out) -> {
                for (RegistryObject<Item> item : ITEMS.getEntries()) out.accept(item.get());
            })
            .build());

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        ENTITIES.register(bus);
        BLOCK_ENTITIES.register(bus);
        TABS.register(bus);
    }
}
