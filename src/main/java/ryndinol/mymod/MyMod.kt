package ryndinol.mymod

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricDefaultAttributeRegistry
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricEntityTypeBuilder
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry
import net.minecraft.client.render.entity.model.EntityModelLayer
import net.minecraft.entity.EntityDimensions
import net.minecraft.entity.EntityType
import net.minecraft.entity.SpawnGroup
import net.minecraft.entity.mob.PhantomEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.item.SpawnEggItem
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger


object MyMod: ModInitializer {
    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    @JvmField
    val LOGGER: Logger = LogManager.getLogger("mymod")
    private val MY_PHANTOM_ENTITY_TYPE: EntityType<MyPhantomEntity> = FabricEntityTypeBuilder
        .create(SpawnGroup.CREATURE, ::MyPhantomEntity)
        .dimensions(EntityDimensions.fixed(0.75f, 0.75f))
        .build()
    private val MY_PHANTOM_SPAWN_EGG: Item =
        SpawnEggItem(MY_PHANTOM_ENTITY_TYPE, 12895428, 11382189, Item.Settings().group(ItemGroup.MISC))
    private val MY_PHANTOM_MODEL = EntityModelLayer(Identifier("mymod", "myphantom"), "main")

    private const val MODID = "mymod"

    public fun id(path: String): Identifier {
        return Identifier(MODID, path)
    }


    override fun onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.
        LOGGER.info("Hello Fabric world!")
        Registry.register(
            Registry.ENTITY_TYPE,
            Identifier(MODID, "myphantom"),
            MY_PHANTOM_ENTITY_TYPE
        )
        Registry.register(Registry.ITEM, Identifier(MODID, "my_phantom_spawn_egg"), MY_PHANTOM_SPAWN_EGG)
        FabricDefaultAttributeRegistry.register(MY_PHANTOM_ENTITY_TYPE, PhantomEntity.createMobAttributes())
        EntityRendererRegistry.register(MY_PHANTOM_ENTITY_TYPE, ::MyPhantomEntityRenderer)
        EntityModelLayerRegistry.registerModelLayer(MY_PHANTOM_MODEL, MyPhantomEntityModel::getTexturedModelData )
    }
}