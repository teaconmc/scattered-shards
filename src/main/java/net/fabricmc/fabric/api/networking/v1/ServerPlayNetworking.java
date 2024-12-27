package net.fabricmc.fabric.api.networking.v1;

import dev.architectury.impl.NetworkAggregator;
import dev.architectury.networking.NetworkManager;
import io.github.cottonmc.cotton.gui.impl.ScreenNetworkingImpl;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public class ServerPlayNetworking {

	public static void send(ServerPlayerEntity target, CustomPayload customPayload) {
		NetworkManager.sendToPlayer(target, customPayload);
	}

	@SuppressWarnings("unchecked")
	public static <T extends CustomPayload> void registerGlobalReceiver(CustomPayload.Id<T> id, PlayPayloadHandler<T> o) {
		NetworkManager.registerReceiver(NetworkManager.Side.C2S, id, (PacketCodec)NetworkAggregator.C2S_CODECS.get(id.id()), (packet, context) -> {
			o.receive(packet, new ContextImpl(context));
		});
	}

	public static PacketSender getSender(ServerPlayerEntity player) {
		return new PacketSender.PacketSenderS2CImpl(player);
	}

	/**
	 * A thread-safe packet handler utilizing {@link CustomPayload}.
	 * @param <T> the type of the packet
	 */
	@FunctionalInterface
	public interface PlayPayloadHandler<T extends CustomPayload> {
		/**
		 * Handles the incoming packet. This is called on the server thread, and can safely
		 * manipulate the world.
		 *
		 * <p>An example usage of this is to create an explosion where the player is looking:
		 * <pre>{@code
		 * // use PayloadTypeRegistry for registering the payload
		 * ServerPlayNetworking.registerReceiver(BoomPayload.ID, (payload, context) -> {
		 * 	ModPacketHandler.createExplosion(context.player(), payload.fire());
		 * });
		 * }</pre>
		 *
		 * <p>The network handler can be accessed via {@link ServerPlayer#connection}.
		 *
		 * @param payload the packet payload
		 * @param context the play networking context
		 * @see CustomPayload
		 */
		void receive(T payload, Context context);
	}

	public interface Context {
		/**
		 * @return The MinecraftServer instance
		 */
		MinecraftServer server();

		/**
		 * @return The player that received the packet
		 */
		ServerPlayerEntity player();

		/**
		 * @return The packet sender
		 */
		PacketSender responseSender();
	}

	private record ContextImpl(MinecraftServer server, ServerPlayerEntity player, PacketSender responseSender) implements Context {

		ContextImpl(NetworkManager.PacketContext archContext) {
			this(archContext.getPlayer().getServer(), (ServerPlayerEntity) archContext.getPlayer(), new PacketSender.PacketSenderS2CImpl((ServerPlayerEntity) archContext.getPlayer()));
		}
	}
}
