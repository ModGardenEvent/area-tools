package dev.doublekekse.area_tools.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.doublekekse.area_lib.Area;
import dev.doublekekse.area_lib.data.AreaSavedData;
import dev.doublekekse.area_tools.registry.AreaComponents;
import dev.doublekekse.area_tools.registry.AreaRules;
import dev.doublekekse.area_tools.rule.SpawningAreaCache;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(ServerChunkCache.class)
public class ServerChunkCacheMixin {
    @Shadow
    @Final
    private ServerLevel level;

    @ModifyVariable(method = "tickSpawningChunk", at = @At("HEAD"), argsOnly = true)
    private List<MobCategory> addSpawningOptionsToChunks(List<MobCategory> list,
                                                         @Local(argsOnly = true) LevelChunk levelChunk,
                                                         @Local(argsOnly = true) NaturalSpawner.SpawnState spawnState) {
        List<MobCategory> categories = new ArrayList<>(list);
        ChunkPos chunkPos = levelChunk.getPos();

        boolean onlySpawnPersistent = level.getGameTime() % 400L == 0L;

        for (int y = levelChunk.getMinY() + 8; y < levelChunk.getMaxY(); y += 16) {
            var areas = AreaSavedData.getServerData(level.getServer()).findAllAreasContaining(level, chunkPos.getMiddleBlockPosition(y).getCenter());
            if (!areas.isEmpty()) {
                for (Area area : areas) {
                    var rules = area.get(AreaComponents.RULES_COMPONENT);
                    if (rules != null && rules.contains(AreaRules.SPAWN_ANIMALS)) {
                        if (rules.get(AreaRules.SPAWN_ANIMALS)) {
                            var filteredCategories = NaturalSpawner.getFilteredSpawningCategories(spawnState, true, false, onlySpawnPersistent);
                            categories.addAll(filteredCategories);
                            SpawningAreaCache.addToAnimalCache(area, true);
                        } else {
                            SpawningAreaCache.addToAnimalCache(area, false);
                        }
                    }

                    if (rules != null && rules.contains(AreaRules.SPAWN_MONSTERS)) {
                        if (rules.get(AreaRules.SPAWN_MONSTERS)) {
                            var filteredCategories = NaturalSpawner.getFilteredSpawningCategories(spawnState, false, true, onlySpawnPersistent);
                            categories.addAll(filteredCategories);
                            SpawningAreaCache.addToMonsterCache(area, true);
                        } else {
                            SpawningAreaCache.addToMonsterCache(area, false);
                        }
                    }
                }
            }
        }
        return List.copyOf(categories);
    }

    @Inject(method = "tickSpawningChunk", at = @At("TAIL"))
    private void clearMobAreaCache(LevelChunk levelChunk, long l, List<MobCategory> list, NaturalSpawner.SpawnState spawnState, CallbackInfo ci) {
        SpawningAreaCache.clearCache();
    }
}
