package dev.doublekekse.area_tools.mixin;

import com.mojang.authlib.GameProfile;
import dev.doublekekse.area_lib.Area;
import dev.doublekekse.area_lib.data.AreaClientData;
import dev.doublekekse.area_tools.compat.FiguraCompat;
import dev.doublekekse.area_tools.duck.LocalPlayerDuck;
import dev.doublekekse.area_tools.registry.AreaComponents;
import dev.doublekekse.area_tools.registry.AreaRules;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends AbstractClientPlayer implements LocalPlayerDuck {
    @Unique
    boolean wasInPanicArea = false;
    @Unique
    boolean previousPanicValue = false;

    public LocalPlayerMixin(ClientLevel clientLevel, GameProfile gameProfile) {
        super(clientLevel, gameProfile);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    void tick(CallbackInfo ci) {
        if (!FabricLoader.getInstance().isModLoaded("figura")) {
            return;
        }

        var savedData = AreaClientData.getClientLevelData();

        if (savedData == null) {
            return;
        }

        var areas = savedData.findTrackedAreasContaining(this);

        if (areas.isEmpty()) {
            return;
        }

        for (Area area : areas) {
            var shouldPanic = area.contains(this) && area.has(AreaComponents.RULES_COMPONENT) && area.get(AreaComponents.RULES_COMPONENT).contains(AreaRules.FIGURA_PANIC);

            if (shouldPanic && !wasInPanicArea) {
                previousPanicValue = FiguraCompat.isPanic();
                FiguraCompat.setPanic(true);
                wasInPanicArea = true;
            }
            if (!shouldPanic && wasInPanicArea) {
                FiguraCompat.setPanic(previousPanicValue);
                wasInPanicArea = false;
            }
        }
    }

    @Override
    public void area_tools$resetFiguraPanic() {
        if (!FabricLoader.getInstance().isModLoaded("figura")) {
            return;
        }

        if (wasInPanicArea) {
            FiguraCompat.setPanic(previousPanicValue);
            wasInPanicArea = false;
        }
    }
}
