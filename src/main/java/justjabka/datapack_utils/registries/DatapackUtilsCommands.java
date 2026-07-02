package justjabka.datapack_utils.registries;

import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import justjabka.datapack_utils.DatapackUtils;
import justjabka.datapack_utils.contents.command.EvalCommand;
import justjabka.datapack_utils.contents.command.ExecuteRaycastCommand;
import justjabka.datapack_utils.contents.command.GuiCommand;
import justjabka.datapack_utils.contents.command.MotionCommand;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;

public class DatapackUtilsCommands {
    public static void initialize() {
        DatapackUtils.LOGGER.info("Initializing Commands");
        registerCommands();
    }

    private static void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            CommandNode<CommandSourceStack> executeNode = dispatcher.getRoot().getChild("execute");

            dispatcher.register(MotionCommand.register());
            dispatcher.register(GuiCommand.register(registryAccess));
            dispatcher.register(EvalCommand.register());

            if (executeNode instanceof LiteralCommandNode<CommandSourceStack> vanillaExecute) {
                LiteralCommandNode<CommandSourceStack> raycastNode = ExecuteRaycastCommand.register(vanillaExecute);
                vanillaExecute.addChild(raycastNode);
            }
        });
    }
}
