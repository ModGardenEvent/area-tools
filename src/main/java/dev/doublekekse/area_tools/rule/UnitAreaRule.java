package dev.doublekekse.area_tools.rule;

import com.mojang.serialization.Codec;
import net.minecraft.util.Unit;

/**
 * Used for area rules that only need to be checked for presence.
 */
public class UnitAreaRule implements AreaRule<Unit> {
    @Override
    public Codec<Unit> codec() {
        return Unit.CODEC;
    }
}
