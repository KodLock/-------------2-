package ru.kod.client.mixin;

import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.kod.client.modules.ModuleManager;
import ru.kod.client.modules.utilities.NoPush;

/** NoPush: cancels entity push when the NoPush module is enabled. */
@Mixin(Entity.class)
public abstract class EntityPushMixin {
    @Inject(method = "pushAwayFrom", at = @At("HEAD"), cancellable = true)
    private void kod$noPush(Entity other, CallbackInfo ci) {
        var m = ModuleManager.get().find("NoPush");
        if (m.isPresent() && m.get().isEnabled()) ci.cancel();
    }
}
