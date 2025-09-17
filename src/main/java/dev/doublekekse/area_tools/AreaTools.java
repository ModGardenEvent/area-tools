package dev.doublekekse.area_tools;

import dev.doublekekse.area_lib.Area;
import dev.doublekekse.area_lib.data.AreaSavedData;
import dev.doublekekse.area_tools.command.AreaToolsCommand;
import dev.doublekekse.area_tools.component.area.EventsComponent;
import dev.doublekekse.area_tools.duck.ServerPlayerDuck;
import dev.doublekekse.area_tools.registry.AreaComponents;
import dev.doublekekse.area_tools.registry.AreaItemComponents;
import dev.doublekekse.area_tools.registry.AreaItems;
import dev.doublekekse.area_tools.registry.AreaLootConditions;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTabs;

import java.util.Comparator;
import java.util.List;

public class AreaTools implements ModInitializer {
    @Override
    public void onInitialize() {
        AreaItems.register();
        AreaItemComponents.register();
        AreaComponents.register();
        AreaLootConditions.register();

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.OP_BLOCKS).register(content -> {
            content.accept(AreaItems.AREA_CREATOR);
            content.accept(AreaItems.SPAWNPOINT_SETTER);
        });

        CommandRegistrationCallback.EVENT.register(
            (dispatcher, registryAccess, environment) -> {
                AreaToolsCommand.register(dispatcher);
            }
        );
        ServerPlayerEvents.JOIN.register(player -> {
            MinecraftServer server = player.getServer();
            if (server == null)
                return;
            ((ServerPlayerDuck)player).area_tools$setAreas(AreaSavedData.getServerData(server
            ).findTrackedAreasContaining(player).stream().filter(area -> {
                EventsComponent component = area.get(AreaComponents.EVENTS_COMPONENT);
                return component != null && !component.isEmpty() && component.offlinePlayers.remove(player.getUUID());
            }).toList());
        });
        ServerPlayerEvents.LEAVE.register(player -> {
            MinecraftServer server = player.getServer();
            if (server == null)
                return;
            AreaSavedData.getServerData(server).findTrackedAreasContaining(player).forEach(area -> {
                EventsComponent component = area.get(AreaComponents.EVENTS_COMPONENT);
                if (component != null && !component.isEmpty()) {
                    component.offlinePlayers.add(player.getUUID());
                }
            });
        });
    }

    public static void runCommands(MinecraftServer server, Player player, List<String> commands) {
        var stack = player.createCommandSourceStackForNameResolution((ServerLevel) player.level()).withSuppressedOutput().withMaximumPermission(2);

        for (var command : commands) {
            server.getCommands().performPrefixedCommand(stack, command);
        }
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("area_tools", path);
    }

    public static Comparator<Area> smallestArea() {
        return Comparator.comparingDouble((area) -> {
            var boundingBox = area.getBoundingBox();

            if (boundingBox == null) {
                return Double.MAX_VALUE;
            }

            return boundingBox.getSize();
        });
    }
}
