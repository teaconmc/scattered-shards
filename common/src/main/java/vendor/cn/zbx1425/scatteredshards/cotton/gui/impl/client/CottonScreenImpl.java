package vendor.cn.zbx1425.scatteredshards.cotton.gui.impl.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import vendor.cn.zbx1425.scatteredshards.cotton.gui.GuiDescription;
import vendor.cn.zbx1425.scatteredshards.cotton.gui.widget.WWidget;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public interface CottonScreenImpl {
	GuiDescription getDescription();

	@Nullable
	WWidget getLastResponder();

	void setLastResponder(@Nullable WWidget lastResponder);
}
