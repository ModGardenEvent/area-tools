package dev.doublekekse.area_tools.registry;

import dev.doublekekse.area_tools.AreaTools;
import dev.doublekekse.area_tools.rule.AreaRule;
import dev.doublekekse.area_tools.rule.BooleanAreaRule;
import dev.doublekekse.area_tools.rule.UnitAreaRule;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public class AreaRules {
    public static final ResourceKey<Registry<AreaRule<?>>> REGISTRY_KEY = ResourceKey.createRegistryKey(AreaTools.id("area_rule"));
    public static final Registry<AreaRule<?>> REGISTRY = FabricRegistryBuilder.createSimple(REGISTRY_KEY).buildAndRegister();

    public static final BooleanAreaRule PVP = Registry.register(REGISTRY, AreaTools.id("pvp"), new BooleanAreaRule());
    public static final BooleanAreaRule SPAWN_ANIMALS = Registry.register(REGISTRY, AreaTools.id("spawn_animals"), new BooleanAreaRule());
    public static final BooleanAreaRule SPAWN_MONSTERS = Registry.register(REGISTRY, AreaTools.id("spawn_monsters"), new BooleanAreaRule());

    public static final UnitAreaRule FIGURA_PANIC = Registry.register(REGISTRY, AreaTools.id("figura_panic"), new UnitAreaRule());

    public static void register() {
    }
}
