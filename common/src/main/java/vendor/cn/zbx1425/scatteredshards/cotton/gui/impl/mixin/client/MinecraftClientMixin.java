package vendor.cn.zbx1425.scatteredshards.cotton.gui.impl.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

import vendor.cn.zbx1425.scatteredshards.cotton.gui.impl.client.ItemUseChecker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Prevents LibGui screens from being opened in a dev environment
// using Item.use/useOnBlock/useOnEntity.
@Mixin(MinecraftClient.class)
abstract class MinecraftClientMixin {
	@Inject(method = "setScreen", at = @At("HEAD"))
	private void onSetScreen(Screen screen, CallbackInfo info) {
		ItemUseChecker.checkSetScreen(screen);
	}
}
