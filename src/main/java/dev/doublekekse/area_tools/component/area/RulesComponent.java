package dev.doublekekse.area_tools.component.area;

import com.mojang.serialization.Codec;
import dev.doublekekse.area_lib.component.AreaDataComponent;
import dev.doublekekse.area_lib.data.AreaSavedData;
import dev.doublekekse.area_tools.registry.AreaRules;
import dev.doublekekse.area_tools.rule.AreaRule;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;

import java.util.*;

public class RulesComponent implements AreaDataComponent {
    private final Map<AreaRule<?>, Object> ruleMap = new HashMap<>();
    private static final Codec<Map<AreaRule<?>, Object>> CODEC =  Codec.dispatchedMap(AreaRules.REGISTRY.byNameCodec(), AreaRule::codec);

    @Override
    public void load(AreaSavedData areaSavedData, CompoundTag tag) {
        ruleMap.clear();
        ruleMap.putAll(CODEC.parse(NbtOps.INSTANCE, tag).getOrThrow());
    }

    @Override
    public CompoundTag save() {
        return CODEC.encodeStart(NbtOps.INSTANCE, ruleMap).getOrThrow().asCompound().orElseThrow();
    }

    public boolean contains(AreaRule<?> rule) {
        return ruleMap.containsKey(rule);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(AreaRule<T> rule) {
        return (T) ruleMap.get(rule);
    }

    @SuppressWarnings("unchecked")
    public <T> T getOrDefault(AreaRule<T> rule, T defaultValue) {
        return (T) ruleMap.getOrDefault(rule, defaultValue);
    }

    public <T>void set(AreaRule<T> rule, T value) {
        ruleMap.put(rule, value);
    }

    public void clear(AreaRule<?> rule) {
        ruleMap.remove(rule);
    }

    public boolean hasNoRules() {
        return ruleMap.isEmpty();
    }
}
