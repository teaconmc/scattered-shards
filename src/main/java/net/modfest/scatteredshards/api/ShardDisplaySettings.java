package net.modfest.scatteredshards.api;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.modfest.scatteredshards.api.impl.ColorCodec;

public class ShardDisplaySettings {
	private boolean drawMiniIcons;
	private int libraryColor;
	private int viewerTopColor;
	private int viewerBottomColor;

	public static final Codec<ShardDisplaySettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.BOOL.fieldOf("draw_mini_icons").forGetter(ShardDisplaySettings::drawMiniIcons),
		ColorCodec.CODEC.fieldOf("library_color").forGetter(ShardDisplaySettings::libraryColor),
		ColorCodec.CODEC.fieldOf("viewer_top_color").forGetter(ShardDisplaySettings::viewerTopColor),
		ColorCodec.CODEC.fieldOf("viewer_bottom_color").forGetter(ShardDisplaySettings::viewerBottomColor)
	).apply(instance, ShardDisplaySettings::new));

	public static final PacketCodec<RegistryByteBuf, ShardDisplaySettings> PACKET_CODEC = PacketCodec.tuple(
		PacketCodecs.BOOL, ShardDisplaySettings::drawMiniIcons,
		PacketCodecs.INTEGER, ShardDisplaySettings::libraryColor,
		PacketCodecs.INTEGER, ShardDisplaySettings::viewerTopColor,
		PacketCodecs.INTEGER, ShardDisplaySettings::viewerBottomColor,
		ShardDisplaySettings::new
	);

	public ShardDisplaySettings() {
		this(true, 0xFF_3e2d58, 0xFF_441209, 0xFF_1c0906);
	}

	public ShardDisplaySettings(boolean drawMiniIcons, int libraryColor, int viewerTopColor, int viewerBottomColor) {
		this.drawMiniIcons = drawMiniIcons;
		this.libraryColor = libraryColor;
		this.viewerTopColor = viewerTopColor;
		this.viewerBottomColor = viewerBottomColor;
	}

	public static ShardDisplaySettings fromJson(JsonObject obj) {
		return CODEC.parse(JsonOps.INSTANCE, obj).result().orElseThrow();
	}

	public void copyFrom(ShardDisplaySettings other) {
		this.drawMiniIcons = other.drawMiniIcons;
		this.libraryColor = other.libraryColor;
		this.viewerTopColor = other.viewerTopColor;
		this.viewerBottomColor = other.viewerBottomColor;
	}

	public boolean drawMiniIcons() {
		return drawMiniIcons;
	}

	public int libraryColor() {
		return libraryColor;
	}

	public int viewerTopColor() {
		return viewerTopColor;
	}

	public int viewerBottomColor() {
		return viewerBottomColor;
	}
}
