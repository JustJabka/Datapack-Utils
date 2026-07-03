package justjabka.datapack_utils.contents.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import justjabka.datapack_utils.types.Operation;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

public class AirCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("air")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.argument("target", EntityArgument.entity())
                        .then(Commands.literal("add")
                                .then(Commands.argument("value", IntegerArgumentType.integer(1, Short.MAX_VALUE))
                                        .executes(context -> modifyAir(context, Operation.ADD))))
                        .then(Commands.literal("remove")
                                .then(Commands.argument("value", IntegerArgumentType.integer(1, Short.MAX_VALUE))
                                        .executes(context -> modifyAir(context, Operation.REMOVE))))
                        .then(Commands.literal("set")
                                .then(Commands.argument("value", IntegerArgumentType.integer(Short.MIN_VALUE, Short.MAX_VALUE))
                                        .executes(context -> modifyAir(context, Operation.SET))))
                );
    }

    private static int modifyAir(CommandContext<CommandSourceStack> context, Operation operation) throws CommandSyntaxException {
        Entity entity = EntityArgument.getEntity(context, "target");
        int value = IntegerArgumentType.getInteger(context, "value");

        int currentAir = entity.getAirSupply();
        int maxAir = entity.getMaxAirSupply();
        int newAir = currentAir;

        switch (operation) {
            case ADD -> newAir = Math.min(maxAir, currentAir + value);
            case REMOVE -> newAir = Math.max(Short.MIN_VALUE, currentAir - value);
            case SET -> newAir = value;
        }

        entity.setAirSupply(newAir);
        final int finalAir = newAir;

        context.getSource().sendSuccess(() -> Component.literal("Set air supply of %s to %s/%s"
                        .formatted(entity.getDisplayName().getString(), finalAir, maxAir)
        ), true);

        return Command.SINGLE_SUCCESS;
    }
}
