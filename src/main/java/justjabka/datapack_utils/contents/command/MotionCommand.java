package justjabka.datapack_utils.contents.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class MotionCommand {
    private enum MotionOperation {
        ADD,
        SET
    }

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("motion")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("add")
                        .then(Commands.argument("target", EntityArgument.entity())
                                .then(Commands.argument("momentum", Vec3Argument.vec3(false))
                                        .executes(context ->
                                                applyMomentum(context, MotionOperation.ADD))
                                )
                ))
                .then(Commands.literal("set")
                                .then(Commands.argument("target", EntityArgument.entity())
                                        .then(Commands.argument("momentum", Vec3Argument.vec3(false))
                                                .executes(context ->
                                                        applyMomentum(context, MotionOperation.SET))
                                        )
                ));
    }

    private static int applyMomentum(CommandContext<CommandSourceStack> context, MotionOperation operation) throws CommandSyntaxException {
        Entity entity = EntityArgument.getEntity(context, "target");
        Vec3 momentum = getMomentum(context);

        switch (operation) {
            case MotionOperation.ADD -> entity.addDeltaMovement(momentum);
            case SET -> entity.setDeltaMovement(momentum);
        }
        entity.hurtMarked = true;

        context.getSource().sendSuccess(() -> Component.literal("Successfully applied motion for %s".formatted(entity.getName().getString())), true);
        return Command.SINGLE_SUCCESS;
    }

    private static Vec3 getMomentum(CommandContext<CommandSourceStack> context) {
        Vec3 absolutePos = Vec3Argument.getVec3(context, "momentum");
        return absolutePos.subtract(context.getSource().getPosition());
    }
}
