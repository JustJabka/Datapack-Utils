package justjabka.datapack_utils.contents.command;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.parser.ParseException;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class EvalCommand {
    private static final DynamicCommandExceptionType ERROR_INVALID_EXPRESSION = new DynamicCommandExceptionType(
            obj -> Component.literal("Invalid expression: " + obj)
    );

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("eval")
                .then(Commands.argument("expression", StringArgumentType.string())
                        .executes(context -> {
                            String expressionString = StringArgumentType.getString(context, "expression");
                            Expression expression = new Expression(expressionString);

                            return calcExpression(context.getSource(), expression, 1);
                        })
                        .then(Commands.argument("scale", DoubleArgumentType.doubleArg()).executes(context -> {
                            String expressionString = StringArgumentType.getString(context, "expression");
                            Expression expression = new Expression(expressionString);
                            double scale = DoubleArgumentType.getDouble(context, "scale");

                            return calcExpression(context.getSource(), expression, scale);
                        }))
                );
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
}
