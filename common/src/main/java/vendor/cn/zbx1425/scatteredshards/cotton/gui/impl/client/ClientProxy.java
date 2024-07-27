package vendor.cn.zbx1425.scatteredshards.cotton.gui.impl.client;

import vendor.cn.zbx1425.scatteredshards.cotton.gui.impl.Proxy;
import vendor.cn.zbx1425.scatteredshards.cotton.gui.widget.WWidget;

public final class ClientProxy extends Proxy {
	@Override
	public void addPainters(WWidget widget) {
		widget.addPainters();
	}
}
