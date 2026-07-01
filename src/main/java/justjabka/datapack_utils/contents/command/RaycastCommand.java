package justjabka.datapack_utils.contents.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jspecify.annotations.Nullable;

public class RaycastCommand {
    public static void register(final CommandDispatcher<CommandSourceStack> dispatcher, final CommandBuildContext buildContext) {
        dispatcher.register(
                Commands.literal("raycast")
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.argument("target", EntityArgument.entity())
                                .then(Commands.argument("range", DoubleArgumentType.doubleArg(0, 128))
                                        .then(Commands.argument("with_liquids", BoolArgumentType.bool())
                                                .then(Commands.argument("block", BlockStateArgument.block(buildContext))
                                                        .executes(context -> {
                                                            Entity entity = EntityArgument.getEntity(context, "target");
                                                            double range = DoubleArgumentType.getDouble(context, "range");
                                                            boolean withLiquids = BoolArgumentType.getBool(context, "with_liquids");

                                                            BlockInput block = BlockStateArgument.getBlock(context, "block");

                                                            return raycast(entity, range, withLiquids, block, null);
                                                        })
                                                        .then(Commands.literal("run").redirect(dispatcher.getRoot()))
                                                )
                                                .then(Commands.argument("entity", EntityArgument.entity())
                                                        .executes(context -> {
                                                            Entity entity = EntityArgument.getEntity(context, "target");
                                                            double range = DoubleArgumentType.getDouble(context, "range");
                                                            boolean withLiquids = BoolArgumentType.getBool(context, "with_liquids");

                                                            Entity target = EntityArgument.getEntity(context, "entity");

                                                            return raycast(entity, range, withLiquids, null, target);
                                                        })
                                                        .then(Commands.literal("run").redirect(dispatcher.getRoot()))
                                                )
                                        )
                                )
                        )
        );
    }

    private static int raycast(Entity entity, double range, boolean withLiquids, @Nullable BlockInput blockInput, @Nullable Entity target) {
        HitResult hitResult = entity.pick(range, 1.0f, withLiquids);

        switch (hitResult.getType()) {
            case BLOCK -> {
                if (blockInput == null) return 0;
                BlockHitResult blockHit = (BlockHitResult) hitResult;

                BlockState blockState = entity.level().getBlockState(blockHit.getBlockPos());
                Block block = blockState.getBlock();

                BlockState inputedBlockState = blockInput.getState();
                Block inputedBlock = blockInput.getState().getBlock();

                if (!block.equals(inputedBlock)) return 0;
                if (!blockState.equals(inputedBlockState)) return 0;
            }
            case ENTITY -> {
                if (target == null) return 0;
                EntityHitResult entityHit = (EntityHitResult) hitResult;

                Entity pickedEntity = entityHit.getEntity();
                if (!pickedEntity.equals(target)) return 0;
            }
            default -> {
                return 0;
            }
        }

        return Command.SINGLE_SUCCESS;
    }
}
