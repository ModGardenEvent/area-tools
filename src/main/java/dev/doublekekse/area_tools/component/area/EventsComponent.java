package dev.doublekekse.area_tools.component.area;

import dev.doublekekse.area_lib.component.AreaDataComponent;
import dev.doublekekse.area_lib.data.AreaSavedData;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringTag;

import java.util.*;

public class EventsComponent implements AreaDataComponent {
    public List<String> onEnter = new ArrayList<>();
    public List<String> onExit = new ArrayList<>();
    public Set<UUID> offlinePlayers = new HashSet<>();

    @Override
    public void load(AreaSavedData areaSavedData, CompoundTag tag) {
        onEnter = toStringList(tag.getList("on_enter").orElse(new ListTag()));
        onExit = toStringList(tag.getList("on_exit").orElse(new ListTag()));
        offlinePlayers = UUIDUtil.CODEC_SET.parse(NbtOps.INSTANCE, tag.getList("offline_players").orElse(new ListTag())).getOrThrow();
    }

    @Override
    public CompoundTag save() {
        var tag = new CompoundTag();

        tag.put("on_enter", toTag(onEnter));
        tag.put("on_exit", toTag(onExit));
        tag.put("offline_players", UUIDUtil.CODEC_SET.encodeStart(NbtOps.INSTANCE, offlinePlayers).getOrThrow());

        return tag;
    }

    public ListTag toTag(Collection<?> list) {
        var listTag = new ListTag();

        for (var value : list) {
            listTag.add(StringTag.valueOf(String.valueOf(value)));
        }

        return listTag;
    }

    public List<String> toStringList(ListTag listTag) {
        var list = new ArrayList<String>();

        for (var value : listTag) {
            list.add(value.asString().get());
        }

        return list;
    }

    public boolean isEmpty() {
        return onEnter.isEmpty() && onExit.isEmpty();
    }
}
