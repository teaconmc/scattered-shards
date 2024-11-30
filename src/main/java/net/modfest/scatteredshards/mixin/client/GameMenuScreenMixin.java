package net.modfest.scatteredshards.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.text.Text;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.client.ScatteredShardsClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameMenuScreen.class)
public class GameMenuScreenMixin {
	@WrapOperation(method = "initWidgets", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/GridWidget$Adder;add(Lnet/minecraft/client/gui/widget/Widget;)Lnet/minecraft/client/gui/widget/Widget;", ordinal = 0))
	private Widget replaceAdvancements(GridWidget.Adder instance, Widget widget, Operation<Widget> original) {
		if (!ScatteredShards.CONFIG.replace_advancements.value()) return original.call(instance, widget);
		return instance.add(ButtonWidget.builder(Text.translatable("menu.scattered_shards.collection"), b -> ScatteredShardsClient.openShardTablet()).width(98).build());
	}
}
