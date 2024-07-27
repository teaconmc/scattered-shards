package vendor.cn.zbx1425.scatteredshards.cotton.gui.impl;

import net.minecraft.util.Identifier;

public final class LibGuiCommon {
	public static final String MOD_ID = "libgui";

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}

	public void onInitialize() {
		ScreenNetworkingImpl.init();
	}
}
