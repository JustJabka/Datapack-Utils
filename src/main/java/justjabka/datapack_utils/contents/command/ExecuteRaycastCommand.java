package justjabka.datapack_utils.contents.command;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.*;
import org.jspecify.annotations.NonNull;

import java.util.Collections;
import java.util.List;

public class ExecuteRaycastCommand {
    public static LiteralCommandNode<CommandSourceStack> register(LiteralCommandNode<CommandSourceStack> vanillaExecute) {
        return Commands.literal("raycast")
                .then(Commands.argument("range", DoubleArgumentType.doubleArg(0, 128))
                        .then(Commands.argument("with_liquids", BoolArgumentType.bool())
                                .fork(vanillaExecute, context -> {
                                    try {
                                        HitResult hitResult = getHitResult(context);
                                        return getForkedResult(context, hitResult);
                                    } catch (Exception e) {
                                        return Collections.emptyList();
                                    }
                                })
                        )
                ).build();
    }

    private static HitResult getHitResult(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Entity raycaster = context.getSource().getEntityOrException();
        double range = DoubleArgumentType.getDouble(context, "range");
        boolean withLiquids = BoolArgumentType.getBool(context, "with_liquids");

        HitResult blockHit = raycaster.pick(range, 1.0f, withLiquids);

        Vec3 startPos = raycaster.getEyePosition(1.0f);
        Vec3 lookVec = raycaster.getViewVector(1.0f);
        Vec3 endPos = blockHit.getType() != HitResult.Type.MISS ? blockHit.getLocation() : startPos.add(lookVec.scale(range));

        AABB searchBox = raycaster.getBoundingBox()
                .expandTowards(lookVec.scale(range))
                .inflate(1);

        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                raycaster.level(),
                raycaster,
                startPos,
                endPos,
                searchBox,
                target -> !target.isSpectator() && target.isAlive(),
                ProjectileUtil.computeMargin(raycaster)
        );

        return entityHit != null ? entityHit : blockHit;
    }

    private static @NonNull List<CommandSourceStack> getForkedResult(CommandContext<CommandSourceStack> context, HitResult hitResult) {
        if (hitResult.getType() == HitResult.Type.MISS) {
            return Collections.emptyList();
        }

        CommandSourceStack newSource = context.getSource().withPosition(hitResult.getLocation());

        if (hitResult instanceof BlockHitResult blockHit) {
            BlockPos blockPos = blockHit.getBlockPos();
            Vec3 pos = new Vec3(blockPos.getX(), blockPos.getY(), blockPos.getZ());

            newSource = newSource.withPosition(pos);
        }

        if (hitResult instanceof EntityHitResult entityHit) {
            newSource = newSource.withEntity(entityHit.getEntity());
        }

        return Collections.singletonList(newSource);
    }
}
