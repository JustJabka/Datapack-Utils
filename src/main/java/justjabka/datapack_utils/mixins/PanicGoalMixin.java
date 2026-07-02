package justjabka.datapack_utils.mixins;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PanicGoal.class)
public class PanicGoalMixin {
    @Shadow @Final protected PathfinderMob mob;

    @Inject(method = "shouldPanic", at = @At("HEAD"), cancellable = true)
    private void allowCommandProvocation(CallbackInfoReturnable<Boolean> cir) {
        if (this.mob.getLastHurtByMob() != null && (this.mob.tickCount - this.mob.getLastHurtByMobTimestamp()) < 100) {
            cir.setReturnValue(true);
        }
    }
}
