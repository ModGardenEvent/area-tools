package dev.doublekekse.area_tools.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.doublekekse.area_lib.command.argument.AreaArgument;
import dev.doublekekse.area_tools.component.area.EventsComponent;
import dev.doublekekse.area_tools.component.area.RespawnPointComponent;
import dev.doublekekse.area_tools.component.area.RulesComponent;
import dev.doublekekse.area_tools.registry.AreaComponents;
import dev.doublekekse.area_tools.registry.AreaRules;
import dev.doublekekse.area_tools.rule.AreaRule;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import java.util.List;
import java.util.function.Function;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class AreaToolsCommand {
    public static final String AREA_NAME = "area_tools";

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        var base = literal(AREA_NAME).requires(stack -> stack.hasPermission(2));

        trackEvent("on_enter", base, (track) -> track.onEnter);
        trackEvent("on_exit", base, (track) -> track.onExit);

        var ruleCommand = literal("rule");

        for (Holder.Reference<AreaRule<?>> holder : context.lookupOrThrow(AreaRules.REGISTRY_KEY).listElements().toList()) {
            var areaRule = holder.value();

            if (areaRule.argumentType() != null) {
                ruleCommand
                        .then(literal("set")
                                .then(argument("area", AreaArgument.area())
                                                .then(literal(holder.key().location().toString())
                                                        .then(argument("value", areaRule.argumentType().get())
                                                                .executes(ctx -> {
                                                                    var server = ctx.getSource().getServer();
                                                                    var area = AreaArgument.getArea(ctx, "area");
                                                                    var value = ctx.getArgument("value", Object.class);

                                                                    if (!area.has(AreaComponents.RULES_COMPONENT)) {
                                                                        area.put(server, AreaComponents.RULES_COMPONENT, new RulesComponent());
                                                                    }

                                                                    var rulesComponent = area.get(AreaComponents.RULES_COMPONENT);
                                                                    if (rulesComponent != null) {
                                                                        //noinspection unchecked
                                                                        rulesComponent.set((AreaRule<Object>) areaRule, value);
                                                                    }

                                                                    String valueString;
                                                                    if (value instanceof Boolean bool) {
                                                                        valueString = Boolean.toString(bool);
                                                                    } else {
                                                                        valueString = value.toString();
                                                                    }

                                                                    ctx.getSource().sendSuccess(() -> Component.translatable("commands.area_tools.area_tools.rule.set.argument", holder.key().location().toString(), valueString, area.getId().toString()), false);

                                                                    return 1;
                                                                })
                                                        )
                                                )
                                        )
                        );
            } else if (areaRule.defaultValue() != null) {
                ruleCommand
                        .then(literal("set")
                                .then(argument("area", AreaArgument.area())
                                        .then(literal(holder.key().location().toString())
                                                .executes(ctx -> {
                                                    var server = ctx.getSource().getServer();
                                                    var area = AreaArgument.getArea(ctx, "area");

                                                    if (!area.has(AreaComponents.RULES_COMPONENT)) {
                                                        area.put(server, AreaComponents.RULES_COMPONENT, new RulesComponent());
                                                    }

                                                    var rulesComponent = area.get(AreaComponents.RULES_COMPONENT);
                                                    if (rulesComponent != null) {
                                                        //noinspection unchecked
                                                        rulesComponent.set((AreaRule<Object>) areaRule, areaRule.defaultValue().get());
                                                    }

                                                    ctx.getSource().sendSuccess(() -> Component.translatable("commands.area_tools.area_tools.rule.set", holder.key().location().toString(), area.getId().toString()), false);

                                                    return 1;
                                                })
                                        )
                                )
                        );
            }
            ruleCommand
                    .then(literal("clear")
                            .then(argument("area", AreaArgument.area())
                                    .then(literal(holder.key().location().toString())
                                            .executes(ctx -> {
                                                var server = ctx.getSource().getServer();
                                                var area = AreaArgument.getArea(ctx, "area");

                                                var rulesComponent = area.get(AreaComponents.RULES_COMPONENT);
                                                if (rulesComponent != null) {
                                                    rulesComponent.clear(holder.value());
                                                    if (rulesComponent.hasNoRules()) {
                                                        area.remove(server, AreaComponents.RULES_COMPONENT);
                                                    }
                                                }

                                                ctx.getSource().sendSuccess(() -> Component.translatable("commands.area_tools.area_tools.rule.clear", holder.key().location().toString(), area.getId().toString()), false);

                                                return 1;
                                            })
                                    )
                            )
                    );
        }
        base.then(ruleCommand);

        dispatcher.register(
            base.then(literal("spawnpoint").then(argument("area", AreaArgument.area()).then(argument("position", Vec3Argument.vec3()).executes(ctx -> {
                var server = ctx.getSource().getServer();
                var area = AreaArgument.getArea(ctx, "area");
                var position = Vec3Argument.getVec3(ctx, "position");

                area.put(server, AreaComponents.RESPAWN_POINT_COMPONENT, new RespawnPointComponent(position, 0));

                ctx.getSource().sendSuccess(() -> Component.translatable("commands.area_tools.area_tools.spawnpoint"), false);

                return 1;
            }).then(argument("yaw", FloatArgumentType.floatArg()).executes(ctx -> {
                var server = ctx.getSource().getServer();
                var area = AreaArgument.getArea(ctx, "area");
                var position = Vec3Argument.getVec3(ctx, "position");
                var yaw = FloatArgumentType.getFloat(ctx, "yaw");

                area.put(server, AreaComponents.RESPAWN_POINT_COMPONENT, new RespawnPointComponent(position, yaw));

                ctx.getSource().sendSuccess(() -> Component.translatable("commands.area_tools.area_tools.spawnpoint"), false);

                return 1;
            }))).then(literal("clear").executes(ctx -> {
                var server = ctx.getSource().getServer();
                var area = AreaArgument.getArea(ctx, "area");

                area.remove(server, AreaComponents.RESPAWN_POINT_COMPONENT);

                ctx.getSource().sendSuccess(() -> Component.translatable("commands.area_tools.area_tools.spawnpoint.clear"), false);

                return 1;
            }))))
        );
    }

    static void trackEvent(String trackEvent, LiteralArgumentBuilder<CommandSourceStack> base, Function<EventsComponent, List<String>> lookup) {
        base.then(literal(trackEvent).then(literal("add").then(argument("area", AreaArgument.area()).then(argument("command", StringArgumentType.greedyString()).executes(ctx -> {
            var server = ctx.getSource().getServer();
            var area = AreaArgument.getArea(ctx, "area");
            var command = StringArgumentType.getString(ctx, "command");
            var component = area.getOrDefault(AreaComponents.EVENTS_COMPONENT, new EventsComponent());

            lookup.apply(component).add(command);
            area.put(server, AreaComponents.EVENTS_COMPONENT, component);

            ctx.getSource().sendSuccess(() -> Component.translatable("commands.area_tools.area_tools." + trackEvent + ".add", command), false);

            return 1;
        })))).then(literal("remove").then(argument("area", AreaArgument.area()).then(argument("command", StringArgumentType.greedyString()).executes(ctx -> {
            var server = ctx.getSource().getServer();
            var area = AreaArgument.getArea(ctx, "area");
            var command = StringArgumentType.getString(ctx, "command");

            var component = area.get(AreaComponents.EVENTS_COMPONENT);

            if (component == null) {
                ctx.getSource().sendFailure(Component.translatable("commands.area_tools.area_tools.track_event.no_events"));
                return 0;
            }

            lookup.apply(component).remove(command);
            ctx.getSource().sendSuccess(() -> Component.translatable("commands.area_tools.area_tools." + trackEvent + ".remove", command), false);

            if (component.isEmpty()) {
                area.remove(server, AreaComponents.EVENTS_COMPONENT);
            }

            return 1;
        })))).then(literal("list").then(argument("area", AreaArgument.area()).executes(ctx -> {
            var area = AreaArgument.getArea(ctx, "area");
            var component = area.get(AreaComponents.EVENTS_COMPONENT);


            if (component == null) {
                ctx.getSource().sendFailure(Component.translatable("commands.area_tools.area_tools.track_event.no_events"));
                return 0;
            }

            var commands = lookup.apply(component);

            for (var command : commands) {
                var style = Style.EMPTY.withClickEvent(new ClickEvent.SuggestCommand(String.format("/%s %s remove %s %s", AREA_NAME, trackEvent, area.getId(), command))).withColor(ChatFormatting.AQUA);
                ctx.getSource().sendSuccess(() -> Component.literal(command).append(" ").append(Component.translatable("commands.area_tools.area_tools.track_event.list.remove").withStyle(style)), false);
            }

            return 1;
        }))));
    }
}
