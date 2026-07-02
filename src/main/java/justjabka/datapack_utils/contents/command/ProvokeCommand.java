package justjabka.datapack_utils.contents.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.*;

public class ProvokeCommand {
    private static final SimpleCommandExceptionType ERROR_VICTIM_IS_NOT_MOB = new SimpleCommandExceptionType(Component.literal("Victim is not an mob"));
    private static final SimpleCommandExceptionType ERROR_PROVOKER_IS_NOT_LIVING = new SimpleCommandExceptionType(Component.literal("Provoker is not living entity"));

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("provoke")
                .then(Commands.argument("victim", EntityArgument.entity())
                        .then(Commands.argument("provoker", EntityArgument.entity())
                                .executes(context -> {
                                    Entity victim = EntityArgument.getEntity(context, "victim");
                                    Entity provoker = EntityArgument.getEntity(context, "provoker");

                                    return tryProvoke(victim, provoker);
                                })
                        )
                );
    }

    private static int tryProvoke(Entity victim, Entity provoker) throws CommandSyntaxException {
        if (!(victim instanceof Mob mob)) throw ERROR_VICTIM_IS_NOT_MOB.create();
        if (!(provoker instanceof LivingEntity livingProvoker)) throw ERROR_PROVOKER_IS_NOT_LIVING.create();

        // Neutral
        if (mob instanceof NeutralMob neutral) {
            neutral.setPersistentAngerTarget(EntityReference.of(livingProvoker));
            neutral.startPersistentAngerTimer();
            neutral.setTarget(livingProvoker);
            return Command.SINGLE_SUCCESS;
        }

        // Hostile
        if (!mob.getType().getCategory().isFriendly()) {
            mob.setTarget(livingProvoker);
            return Command.SINGLE_SUCCESS;
        }

        // Passive
        mob.setLastHurtByMob(livingProvoker);

        return 0;
    }
}
