package dev.doublekekse.area_tools.mixin;

import com.mojang.authlib.GameProfile;
import dev.doublekekse.area_lib.Area;
import dev.doublekekse.area_lib.AreaLib;
import dev.doublekekse.area_lib.data.AreaSavedData;
import dev.doublekekse.area_tools.AreaTools;
import dev.doublekekse.area_tools.duck.ServerPlayerDuck;
import dev.doublekekse.area_tools.registry.AreaComponents;
import dev.doublekekse.area_tools.registry.AreaRules;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player implements ServerPlayerDuck {
    @Shadow
    @Final
    public MinecraftServer server;

    public ServerPlayerMixin(Level level, BlockPos blockPos, float f, GameProfile gameProfile) {
        super(level, blockPos, f, gameProfile);
    }

    @Shadow
    protected abstract boolean isPvpAllowed();

    @Unique
    List<Area> oldTrackedAreas;
    @Unique
    AreaSavedData data;


    @Inject(method = "<init>", at = @At("RETURN"))
    void init(MinecraftServer minecraftServer, ServerLevel serverLevel, GameProfile gameProfile, ClientInformation clientInformation, CallbackInfo ci) {
        data = AreaSavedData.getServerData(minecraftServer);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    void tick(CallbackInfo ci) {
        var trackItems = data.findTrackedAreasContaining(this);

        var newItems = trackItems.stream().filter(a -> !oldTrackedAreas.contains(a));
        var oldItems = oldTrackedAreas.stream().filter(a -> !trackItems.contains(a));

        newItems.forEach(area -> {
            var component = area.get(AreaComponents.EVENTS_COMPONENT);

            if (component != null) {
                AreaTools.runCommands(server, this, component.onEnter);
            }
        });

        oldItems.forEach(area -> {
            var component = area.get(AreaComponents.EVENTS_COMPONENT);

            if (component != null) {
                AreaTools.runCommands(server, this, component.onExit);
            }
        });

        oldTrackedAreas = trackItems;
    }

    @Override
    public List<Area> area_tools$getAreas() {
        return oldTrackedAreas;
    }

    @Override
    public void area_tools$setAreas(List<Area> areas) {
        this.oldTrackedAreas = areas;
    }

    @Inject(method = "canHarmPlayer", at = @At("HEAD"), cancellable = true)
    void canHarmPlayer(Player player, CallbackInfoReturnable<Boolean> cir) {
        var savedData = AreaLib.getSavedData(player.level());

        var area = savedData.findTrackedAreasContaining(this.level(), this.position()).stream()
                .filter(a -> a.get(AreaComponents.RULES_COMPONENT) != null && a.get(AreaComponents.RULES_COMPONENT).contains(AreaRules.PVP))
                .findFirst();

        area.ifPresent(value -> cir.setReturnValue(value.get(AreaComponents.RULES_COMPONENT).get(AreaRules.PVP)));
    }

    @Inject(method = "restoreFrom", at = @At("HEAD"))
    void restoreFrom(ServerPlayer serverPlayer, boolean bl, CallbackInfo ci) {
        oldTrackedAreas = ((ServerPlayerDuck) serverPlayer).area_tools$getAreas();
    }
}
