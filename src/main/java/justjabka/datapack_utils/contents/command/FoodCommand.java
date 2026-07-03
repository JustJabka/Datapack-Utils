package justjabka.datapack_utils.contents.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import justjabka.datapack_utils.types.Operation;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;

public class FoodCommand {
    private static final int MAX_FOOD_LEVEL = 20;
    private static final int MAX_SATURATION_LEVEL = 20;

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("food")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.argument("target", EntityArgument.player())
                        .then(Commands.literal("hunger")
                                .then(Commands.literal("add")
                                        .then(Commands.argument("value", IntegerArgumentType.integer(1, MAX_FOOD_LEVEL))
                                                .executes(context -> modifyHunger(context, Operation.ADD))))
                                .then(Commands.literal("remove")
                                        .then(Commands.argument("value", IntegerArgumentType.integer(1, MAX_FOOD_LEVEL))
                                                .executes(context -> modifyHunger(context, Operation.REMOVE))))
                                .then(Commands.literal("set")
                                        .then(Commands.argument("value", IntegerArgumentType.integer(0, MAX_FOOD_LEVEL))
                                                .executes(context -> modifyHunger(context, Operation.SET))))
                        )
                        .then(Commands.literal("saturation")
                                .then(Commands.literal("add")
                                        .then(Commands.argument("value", FloatArgumentType.floatArg(1, MAX_SATURATION_LEVEL))
                                                .executes(context -> modifySaturation(context, Operation.ADD))))
                                .then(Commands.literal("remove")
                                        .then(Commands.argument("value", FloatArgumentType.floatArg(1, MAX_SATURATION_LEVEL))
                                                .executes(context -> modifySaturation(context, Operation.REMOVE))))
                                .then(Commands.literal("set")
                                        .then(Commands.argument("value", FloatArgumentType.floatArg(0, MAX_SATURATION_LEVEL))
                                                .executes(context -> modifySaturation(context, Operation.SET))))
                        )
                );
    }

    private static int modifyHunger(CommandContext<CommandSourceStack> context, Operation operation) throws CommandSyntaxException {
        Player player = EntityArgument.getPlayer(context, "target");
        int value = IntegerArgumentType.getInteger(context, "value");

        FoodData foodData = player.getFoodData();
        int finalHunger = calcHunger(operation, foodData, value);
        foodData.setFoodLevel(finalHunger);

        context.getSource().sendSuccess(() -> Component.literal("Set hunger of %s to %s/%s"
                        .formatted(player.getDisplayName().getString(), finalHunger, MAX_FOOD_LEVEL)
        ), true);

        return Command.SINGLE_SUCCESS;
    }

    private static int calcHunger(Operation operation, FoodData data, int value) {
        int currentHunger = data.getFoodLevel();
        int newHunger = currentHunger;

        switch (operation) {
            case ADD -> newHunger = Math.min(MAX_FOOD_LEVEL, currentHunger + value);
            case REMOVE -> newHunger = Math.max(0, currentHunger - value);
            case SET -> newHunger = value;
        }

        return newHunger;
    }

    private static int modifySaturation(CommandContext<CommandSourceStack> context, Operation operation) throws CommandSyntaxException {
        Player player = EntityArgument.getPlayer(context, "target");
        float value = FloatArgumentType.getFloat(context, "value");

        FoodData foodData = player.getFoodData();
        float finalSaturation = calcSaturation(operation, foodData, value);
        foodData.setSaturation(finalSaturation);

        context.getSource().sendSuccess(() -> Component.literal("Set saturation of %s to %s/%s"
                .formatted(player.getDisplayName().getString(), finalSaturation, MAX_SATURATION_LEVEL)
        ), true);

        return Command.SINGLE_SUCCESS;
    }

    private static float calcSaturation(Operation operation, FoodData data, float value) {
        float currentSaturation = data.getSaturationLevel();
        float maxAllowedSaturation = data.getFoodLevel();
        float newSaturation = currentSaturation;

        switch (operation) {
            case ADD -> newSaturation = Math.min(maxAllowedSaturation, currentSaturation + value);
            case REMOVE -> newSaturation = Math.max(0, currentSaturation - value);
            case SET -> newSaturation = Math.min(maxAllowedSaturation, value);
        }

        return newSaturation;
    }
}
