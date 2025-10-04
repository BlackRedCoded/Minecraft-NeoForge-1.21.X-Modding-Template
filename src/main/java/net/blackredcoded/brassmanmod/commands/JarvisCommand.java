package net.blackredcoded.brassmanmod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.blackredcoded.brassmanmod.config.FlightConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class JarvisCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("jarvis")
                .then(Commands.literal("flight")
                        .then(Commands.argument("enabled", BoolArgumentType.bool())
                                .executes(JarvisCommand::toggleFlight)
                        )
                )
                .then(Commands.literal("hover")
                        .then(Commands.argument("enabled", BoolArgumentType.bool())
                                .executes(JarvisCommand::toggleHover)
                        )
                )
                .then(Commands.literal("nightvision")
                        .then(Commands.argument("enabled", BoolArgumentType.bool())
                                .executes(JarvisCommand::toggleNightvision)
                        )
                )
                .then(Commands.literal("flightspeed")
                        .then(Commands.argument("percent", IntegerArgumentType.integer(0, 100))
                                .executes(JarvisCommand::setFlightSpeed)
                        )
                )
                .then(Commands.literal("speedboost")
                        .then(Commands.argument("percent", IntegerArgumentType.integer(0, 100))
                                .executes(JarvisCommand::setSpeedBoost)
                        )
                )
                .then(Commands.literal("jumpboost")
                        .then(Commands.argument("percent", IntegerArgumentType.integer(0, 100))
                                .executes(JarvisCommand::setJumpBoost)
                        )
                )
                .then(Commands.literal("fallsave")
                        .then(Commands.literal("hover")
                                .then(Commands.argument("enabled", BoolArgumentType.bool())
                                        .executes(JarvisCommand::setFallsaveHover)
                                )
                        )
                        .then(Commands.literal("flight")
                                .then(Commands.argument("enabled", BoolArgumentType.bool())
                                        .executes(JarvisCommand::setFallsaveFlight)
                                )
                        )
                        .then(Commands.literal("powertoair")
                                .then(Commands.argument("amount", IntegerArgumentType.integer(0, 1000))
                                        .executes(JarvisCommand::setFallsavePowerToAir)
                                )
                        )
                )
                .then(Commands.literal("status")
                        .executes(JarvisCommand::showStatus)
                )
        );
    }

    private static int toggleFlight(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) return 0;

        boolean enabled = BoolArgumentType.getBool(context, "enabled");
        FlightConfig.PlayerFlightData data = FlightConfig.get(player);
        data.flightEnabled = enabled;
        FlightConfig.save(player, data);

        player.sendSystemMessage(Component.literal("JARVIS: Flight mode " + (enabled ? "enabled" : "disabled"))
                .withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.RED));
        return 1;
    }

    private static int toggleHover(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) return 0;

        boolean enabled = BoolArgumentType.getBool(context, "enabled");
        FlightConfig.PlayerFlightData data = FlightConfig.get(player);
        data.hoverEnabled = enabled;
        FlightConfig.save(player, data);

        player.sendSystemMessage(Component.literal("JARVIS: Hover mode " + (enabled ? "enabled" : "disabled"))
                .withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.RED));
        return 1;
    }

    private static int toggleNightvision(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) return 0;

        boolean enabled = BoolArgumentType.getBool(context, "enabled");
        FlightConfig.PlayerFlightData data = FlightConfig.get(player);
        data.nightvisionEnabled = enabled;
        FlightConfig.save(player, data);

        player.sendSystemMessage(Component.literal("JARVIS: Night vision " + (enabled ? "enabled" : "disabled"))
                .withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.RED));
        return 1;
    }

    private static int setFlightSpeed(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) return 0;

        int percent = IntegerArgumentType.getInteger(context, "percent");
        FlightConfig.PlayerFlightData data = FlightConfig.get(player);
        data.flightSpeed = percent;
        FlightConfig.save(player, data);

        player.sendSystemMessage(Component.literal("JARVIS: Flight speed set to " + percent + "%")
                .withStyle(ChatFormatting.AQUA));
        return 1;
    }

    private static int setSpeedBoost(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) return 0;

        int percent = IntegerArgumentType.getInteger(context, "percent");
        FlightConfig.PlayerFlightData data = FlightConfig.get(player);
        data.speedBoost = percent;
        FlightConfig.save(player, data);

        player.sendSystemMessage(Component.literal("JARVIS: Speed boost set to " + percent + "%")
                .withStyle(ChatFormatting.YELLOW));
        return 1;
    }

    private static int setJumpBoost(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) return 0;

        int percent = IntegerArgumentType.getInteger(context, "percent");
        FlightConfig.PlayerFlightData data = FlightConfig.get(player);
        data.jumpBoost = percent;
        FlightConfig.save(player, data);

        player.sendSystemMessage(Component.literal("JARVIS: Jump boost set to " + percent + "%")
                .withStyle(ChatFormatting.GREEN));
        return 1;
    }

    // NEW: Fallsave hover toggle
    private static int setFallsaveHover(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) return 0;

        boolean enabled = BoolArgumentType.getBool(context, "enabled");
        FlightConfig.PlayerFlightData data = FlightConfig.get(player);
        data.fallsaveHover = enabled;
        FlightConfig.save(player, data);

        player.sendSystemMessage(Component.literal("JARVIS: Fallsave hover " + (enabled ? "enabled" : "disabled"))
                .withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.RED));
        return 1;
    }

    // NEW: Fallsave flight toggle
    private static int setFallsaveFlight(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) return 0;

        boolean enabled = BoolArgumentType.getBool(context, "enabled");
        FlightConfig.PlayerFlightData data = FlightConfig.get(player);
        data.fallsaveFlight = enabled;
        FlightConfig.save(player, data);

        player.sendSystemMessage(Component.literal("JARVIS: Fallsave flight " + (enabled ? "enabled" : "disabled"))
                .withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.RED));
        return 1;
    }

    // NEW: Fallsave power-to-air conversion
    private static int setFallsavePowerToAir(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) return 0;

        int amount = IntegerArgumentType.getInteger(context, "amount");
        FlightConfig.PlayerFlightData data = FlightConfig.get(player);
        data.fallsavePowerToAir = amount;
        FlightConfig.save(player, data);

        int airGenerated = amount / 10;
        player.sendSystemMessage(Component.literal("JARVIS: Fallsave power-to-air set to " + amount + " power (" + airGenerated + " air)")
                .withStyle(ChatFormatting.GOLD));
        return 1;
    }

    private static int showStatus(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) return 0;

        FlightConfig.PlayerFlightData data = FlightConfig.get(player);

        player.sendSystemMessage(Component.literal("━━━ JARVIS Status ━━━").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        player.sendSystemMessage(Component.literal("Flight: " + (data.flightEnabled ? "✓" : "✗"))
                .withStyle(data.flightEnabled ? ChatFormatting.GREEN : ChatFormatting.RED));
        player.sendSystemMessage(Component.literal("Hover: " + (data.hoverEnabled ? "✓" : "✗"))
                .withStyle(data.hoverEnabled ? ChatFormatting.GREEN : ChatFormatting.RED));
        player.sendSystemMessage(Component.literal("Night Vision: " + (data.nightvisionEnabled ? "✓" : "✗"))
                .withStyle(data.nightvisionEnabled ? ChatFormatting.GREEN : ChatFormatting.RED));
        player.sendSystemMessage(Component.literal("Flight Speed: " + data.flightSpeed + "%").withStyle(ChatFormatting.AQUA));
        player.sendSystemMessage(Component.literal("Speed Boost: " + data.speedBoost + "%").withStyle(ChatFormatting.YELLOW));
        player.sendSystemMessage(Component.literal("Jump Boost: " + data.jumpBoost + "%").withStyle(ChatFormatting.GREEN));
        player.sendSystemMessage(Component.literal("━━━ Fallsave Settings ━━━").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        player.sendSystemMessage(Component.literal("Fallsave Hover: " + (data.fallsaveHover ? "✓" : "✗"))
                .withStyle(data.fallsaveHover ? ChatFormatting.GREEN : ChatFormatting.RED));
        player.sendSystemMessage(Component.literal("Fallsave Flight: " + (data.fallsaveFlight ? "✓" : "✗"))
                .withStyle(data.fallsaveFlight ? ChatFormatting.GREEN : ChatFormatting.RED));
        player.sendSystemMessage(Component.literal("Fallsave Power→Air: " + data.fallsavePowerToAir + " power (" + (data.fallsavePowerToAir / 10) + " air)")
                .withStyle(ChatFormatting.GOLD));

        return 1;
    }

}
