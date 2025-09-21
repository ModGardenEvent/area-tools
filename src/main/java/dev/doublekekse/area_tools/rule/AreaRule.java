package dev.doublekekse.area_tools.rule;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.serialization.Codec;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public interface AreaRule<T> {
    /**
     * A codec for serializing this area rule's value.
     */
    Codec<T> codec();

    /**
     * A argument type for this area rule's type. Used in commands.
     */
    @Nullable
    default Supplier<ArgumentType<?>> argumentType() {
        return null;
    }

    /**
     * A default value for this area rule, used when {@link AreaRule#argumentType()} is not specified.
     */
    @Nullable
    default Supplier<T> defaultValue() {
        return null;
    }
}
