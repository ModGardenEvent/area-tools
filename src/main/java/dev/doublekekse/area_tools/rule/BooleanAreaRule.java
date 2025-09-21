package dev.doublekekse.area_tools.rule;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.serialization.Codec;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class BooleanAreaRule implements AreaRule<Boolean> {
    @Override
    public Codec<Boolean> codec() {
        return Codec.BOOL;
    }

    @Override
    public @Nullable Supplier<ArgumentType<?>> argumentType() {
        return BoolArgumentType::bool;
    }
}
