package me.lucko.fabric.api.permissions.v0;

import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Predicate;

public class Permissions {

    public static @NotNull Predicate<ServerCommandSource> require(@NotNull String permission, int defaultRequiredLevel) {
        return source -> source.hasPermissionLevel(defaultRequiredLevel);
    }

    public static boolean check(@NotNull CommandSource source, @NotNull String permission, int defaultRequiredLevel) {
        return source.hasPermissionLevel(defaultRequiredLevel);
    }

    public static boolean check(@NotNull Entity entity, @NotNull String permission, int defaultRequiredLevel) {
        Objects.requireNonNull(entity, "entity");
        return check(entity.getCommandSource(), permission, defaultRequiredLevel);
    }

}
