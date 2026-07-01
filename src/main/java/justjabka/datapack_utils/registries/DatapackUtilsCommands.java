package justjabka.datapack_utils.registries;

import justjabka.datapack_utils.DatapackUtils;
import justjabka.datapack_utils.contents.command.EvalCommand;
import justjabka.datapack_utils.contents.command.GuiCommand;
import justjabka.datapack_utils.contents.command.MotionCommand;
import justjabka.datapack_utils.contents.command.RaycastCommand;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class DatapackUtilsCommands {
    public static void initialize() {
        DatapackUtils.LOGGER.info("Initializing Commands");
        registerCommands();
    }

    private static void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(MotionCommand.register());
            dispatcher.register(GuiCommand.register(registryAccess));
            dispatcher.register(EvalCommand.register());
            RaycastCommand.register(dispatcher, registryAccess);
        });
    }
}
