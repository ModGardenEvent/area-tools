package dev.doublekekse.area_tools.rule;

import dev.doublekekse.area_lib.Area;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public class SpawningAreaCache {
    private static final Map<Area, Boolean> animalAreas = new HashMap<>();
    private static final Map<Area, Boolean> monsterAreas = new HashMap<>();

    public static void addToAnimalCache(Area area, boolean value) {
        animalAreas.put(area, value);
    }

    public static void addToMonsterCache(Area area, boolean value) {
        monsterAreas.put(area, value);
    }

    public static boolean shouldPreventSpawning(MobCategory category, ServerLevel level, Vec3 pos) {
        boolean doMobSpawning = level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING);

        if (category.isFriendly()) {
            if (!doMobSpawning && animalAreas.keySet().stream().noneMatch(area -> area.contains(level, pos))) {
                return true;
            }
            return animalAreas.entrySet().stream().anyMatch(entry -> entry.getKey().contains(level, pos) && !entry.getValue());
        }

        if (!doMobSpawning && monsterAreas.keySet().stream().noneMatch(area -> area.contains(level, pos))) {
            return true;
        }
        return monsterAreas.entrySet().stream().anyMatch(entry -> entry.getKey().contains(level, pos) && !entry.getValue());
    }

    public static void clearCache() {
        animalAreas.clear();
        monsterAreas.clear();
    }
}
