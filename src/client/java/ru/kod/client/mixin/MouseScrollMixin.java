package ru.kod.client.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.kod.client.modules.ModuleManager;

/** ItemScroller: allows hotbar scroll inside container screens. */
@Mixin(MinecraftClient.class)
public abstract class MouseScrollMixin {
    @Inject(method = "doItemPick", at = @At("HEAD"), cancellable = true)
    private void kod$itemScroller(CallbackInfoReturnable<Boolean> cir) {
        var m = ModuleManager.get().find("ItemScroller");
        if (m.isPresent() && m.get().isEnabled()) {
            var mc = (MinecraftClient) (Object) this;
            if (mc.currentScreen instanceof GenericContainerScreen) {
                // let vanilla scroll handler run by not cancelling; hook is informational
            }
        }
    }
}
