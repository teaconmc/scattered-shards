package net.fabricmc.fabric.api.client.networking.v1;

import dev.architectury.impl.NetworkAggregator;
import dev.architectury.networking.NetworkManager;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public class ClientPlayNetworking {

	public static PacketSender getSender() {
		return new PacketSender.PacketSenderC2SImpl();
	}

	public static <T extends CustomPayload> void registerGlobalReceiver(CustomPayload.Id<T> packetId, PlayPayloadHandler<T> receive) {
		NetworkManager.registerReceiver(NetworkManager.Side.S2C, packetId, (PacketCodec)NetworkAggregator.S2C_CODECS.get(packetId.id()), (packet, context) -> {
			receive.receive(packet, new ContextImpl(context));
		});
	}

	public static void send(CustomPayload packet) {
		NetworkManager.sendToServer(packet);
	}

	@FunctionalInterface
	public interface PlayPayloadHandler<T extends CustomPayload> {
		/**
		 * Handles the incoming payload. This is called on the render thread, and can safely
		 * call client methods.
		 *
		 * <p>An example usage of this is to display an overlay message:
		 * <pre>{@code
		 * // use PayloadTypeRegistry for registering the payload
		 * ClientPlayNetworking.registerReceiver(OVERLAY_PACKET_TYPE, (payload, context) -> {
		 * 	context.client().inGameHud.setOverlayMessage(payload.message(), true);
		 * });
		 * }</pre>
		 *
		 * <p>The network handler can be accessed via {@link LocalPlayer#connection}.
		 *
		 * @param payload the packet payload
		 * @param context the play networking context
		 * @see CustomPayload
		 */
		void receive(T payload, Context context);
	}

	public interface Context {
		/**
		 * @return The MinecraftClient instance
		 */
		MinecraftClient client();

		/**
		 * @return The player that received the payload
		 */
		ClientPlayerEntity player();

		/**
		 * @return The packet sender
		 */
		PacketSender responseSender();
	}

	private record ContextImpl(MinecraftClient client, ClientPlayerEntity player, PacketSender responseSender) implements Context {

		public ContextImpl(NetworkManager.PacketContext archContext) {
			this(MinecraftClient.getInstance(), MinecraftClient.getInstance().player, new PacketSender.PacketSenderC2SImpl());
		}
	}
}
