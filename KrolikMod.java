package pl.ziomsigiemka.krolik;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import pl.ziomsigiemka.krolik.command.RabbitCommands;
import pl.ziomsigiemka.krolik.entity.PetRabbit;
import pl.ziomsigiemka.krolik.network.ModNetwork;

@Mod(KrolikMod.MODID)
public class KrolikMod {
    public static final String MODID = "krolikmod";

    public KrolikMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModRegistry.register(modBus);
        modBus.addListener(this::commonSetup);
        modBus.addListener(this::attributes);
        MinecraftForge.EVENT_BUS.addListener(RabbitCommands::register);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(ModNetwork::register);
    }

    private void attributes(EntityAttributeCreationEvent event) {
        event.put(ModRegistry.PET_RABBIT.get(), PetRabbit.createPetAttributes().build());
    }
}
