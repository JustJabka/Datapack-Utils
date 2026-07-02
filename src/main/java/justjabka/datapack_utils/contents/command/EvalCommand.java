package justjabka.datapack_utils.contents.command;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.parser.ParseException;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class EvalCommand {
    private static final DynamicCommandExceptionType ERROR_INVALID_EXPRESSION = new DynamicCommandExceptionType(
            obj -> Component.literal("Invalid expression: " + obj)
    );

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("eval")
                .then(Commands.argument("expression", StringArgumentType.string())
                        .executes(context -> tryCalcExpression(
                                context.getSource(),
                                StringArgumentType.getString(context, "expression"),
                                1,
                                Map.of()
                        ))
                        .then(Commands.argument("scale", DoubleArgumentType.doubleArg())
                                .executes(context -> tryCalcExpression(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "expression"),
                                        DoubleArgumentType.getDouble(context, "scale"),
                                        Map.of()
                                ))
                                .then(Commands.argument("arguments", CompoundTagArgument.compoundTag())
                                        .executes(context -> tryCalcExpression(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "expression"),
                                                DoubleArgumentType.getDouble(context, "scale"),
                                                getArguments(context, "arguments")
                                        ))
                                )
                        )
                );
    }

    private static int tryCalcExpression(CommandSourceStack source, String expressionString, double scale, Map<String, Double> arguments) throws CommandSyntaxException {
        Expression expression = new Expression(expressionString).withValues(arguments);
        return calcExpression(source, expression, scale);
    }

    private static int calcExpression(CommandSourceStack source, Expression expression, double scale) throws CommandSyntaxException {
        try {
            double result = expression.evaluate().getNumberValue().doubleValue();
            double scaledResult = result * scale;
            int roundedResult = (int) Math.round(scaledResult);

            source.sendSuccess(() -> Component.literal("Expression result: %s".formatted(roundedResult)), true);
            return roundedResult;
        } catch (EvaluationException | ParseException e) {
            throw ERROR_INVALID_EXPRESSION.create(e.getMessage());
        }
    }

    private static Map<String, Double> getArguments(CommandContext<CommandSourceStack> context, String name) {
        Map<String, Double> arguments = new HashMap<>();
        CompoundTag argumentsCompound = CompoundTagArgument.getCompoundTag(context, name);

        for (String key : argumentsCompound.keySet()) {
            Tag tag = argumentsCompound.get(key);
            if (tag == null) continue;

            Optional<Number> tagAsNumber = tag.asNumber();
            tagAsNumber.ifPresent(number -> arguments.put(key, number.doubleValue()));
        }

        return arguments;
    }
}
