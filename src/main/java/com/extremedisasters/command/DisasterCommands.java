package com.extremedisasters.command;

import com.extremedisasters.config.DisasterConfig;
import com.extremedisasters.entity.ModEntities;
import com.extremedisasters.entity.TornadoEntity;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;

public final class DisasterCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("disaster")
                .then(Commands.literal("help").executes(context -> help(context.getSource())))
                .then(Commands.literal("info").executes(context -> info(context.getSource())))
                .then(Commands.literal("tornado")
                        .then(Commands.literal("spawn")
                                .then(Commands.literal("normal").requires(source -> source.hasPermission(2))
                                        .executes(context -> spawn(context.getSource(), 1.0F))
                                        .then(Commands.literal("intensity")
                                                .then(Commands.argument("value", FloatArgumentType.floatArg(0.25F, 5.0F))
                                                        .executes(context -> spawn(context.getSource(), FloatArgumentType.getFloat(context, "value")))))))
                        .then(Commands.literal("clear").requires(source -> source.hasPermission(2)).executes(context -> clear(context.getSource())))
                        .then(Commands.literal("list").executes(context -> list(context.getSource()))))
        );
    }

    private static int help(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("Extreme Disasters — comandos: /disaster tornado spawn normal [intensity <0.25–5>] | /disaster tornado list | /disaster tornado clear | /disaster info"), false);
        return 1;
    }

    private static int info(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("Extreme Disasters 1.0.0 — Fase 1: tornados móviles, fuerzas de viento y escombros de bloques."), false);
        return 1;
    }

    private static int spawn(CommandSourceStack source, float intensity) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        if (!DisasterConfig.ENABLED.get()) {
            source.sendFailure(Component.literal("Extreme Disasters está desactivado en la configuración del servidor."));
            return 0;
        }
        ServerLevel level = source.getLevel();
        TornadoEntity tornado = new TornadoEntity(ModEntities.TORNADO.get(), level);
        tornado.configure(DisasterConfig.TORNADO_DURATION_TICKS.get(), DisasterConfig.TORNADO_RADIUS.get().floatValue(), intensity);
        tornado.setPos(source.getPosition().x, source.getPosition().y, source.getPosition().z);
        if (!level.addFreshEntity(tornado)) {
            source.sendFailure(Component.literal("No se pudo crear el tornado en esta posición."));
            return 0;
        }
        source.sendSuccess(() -> Component.literal("Tornado normal creado (intensidad " + intensity + ")."), true);
        return 1;
    }

    private static int clear(CommandSourceStack source) {
        ServerLevel level = source.getLevel();
        int[] removed = {0};
        AABB dimensionBounds = new AABB(-30_000_000, level.getMinBuildHeight(), -30_000_000, 30_000_000, level.getMaxBuildHeight(), 30_000_000);
        level.getEntitiesOfClass(TornadoEntity.class, dimensionBounds, tornado -> true).forEach(tornado -> { tornado.discard(); removed[0]++; });
        source.sendSuccess(() -> Component.literal("Tornados eliminados en esta dimensión: " + removed[0] + "."), true);
        return removed[0];
    }

    private static int list(CommandSourceStack source) {
        ServerLevel level = source.getLevel();
        AABB dimensionBounds = new AABB(-30_000_000, level.getMinBuildHeight(), -30_000_000, 30_000_000, level.getMaxBuildHeight(), 30_000_000);
        int count = level.getEntitiesOfClass(TornadoEntity.class, dimensionBounds, tornado -> true).size();
        source.sendSuccess(() -> Component.literal("Tornados activos en esta dimensión: " + count + "."), false);
        return count;
    }

    private DisasterCommands() {}
}
