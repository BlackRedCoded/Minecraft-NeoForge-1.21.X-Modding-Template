package net.blackredcoded.brassmanmod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.blackredcoded.brassmanmod.items.BrassManChestplateItem;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class SetStatsCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("set")
                // /set air <amount>
                .then(Commands.literal("air")
                        .then(Commands.argument("amount", IntegerArgumentType.integer(0, BrassManChestplateItem.MAX_AIR))
                                .executes(context -> {
                                    int amount = IntegerArgumentType.getInteger(context, "amount");
                                    return fillAir(context.getSource(), amount);
                                })
                        )
                )
                // /set power <amount>
                .then(Commands.literal("power")
                        .then(Commands.argument("amount", IntegerArgumentType.integer(0, BrassManChestplateItem.MAX_POWER))
                                .executes(context -> {
                                    int amount = IntegerArgumentType.getInteger(context, "amount");
                                    return fillPower(context.getSource(), amount);
                                })
                        )
                )
        );
    }

    private static int fillAir(CommandSourceStack source, int amount) {
        if (source.getEntity() instanceof Player player) {
            ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
            if (chestplate.getItem() instanceof BrassManChestplateItem brassChestplate) {
                brassChestplate.setAir(chestplate, amount);
                source.sendSuccess(() -> Component.literal("Set air to " + amount + "!"), false);
                return 1;
            } else {
                source.sendFailure(Component.literal("You must be wearing a Brass Chestplate!"));
                return 0;
            }
        }
        source.sendFailure(Component.literal("This command can only be used by players!"));
        return 0;
    }

    private static int fillPower(CommandSourceStack source, int amount) {
        if (source.getEntity() instanceof Player player) {
            ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
            if (chestplate.getItem() instanceof BrassManChestplateItem brassChestplate) {
                brassChestplate.setPower(chestplate, amount);
                source.sendSuccess(() -> Component.literal("Set power to " + amount + "!"), false);
                return 1;
            } else {
                source.sendFailure(Component.literal("You must be wearing a Brass Chestplate!"));
                return 0;
            }
        }
        source.sendFailure(Component.literal("This command can only be used by players!"));
        return 0;
    }
}
