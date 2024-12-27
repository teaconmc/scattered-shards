package net.fabricmc.fabric.api.networking.v1;

import dev.architectury.networking.NetworkManager;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public interface PacketSender {
	/**
	 * Creates a packet from a packet payload.
	 *
	 * @param payload the packet payload
	 */
	Packet<?> createPacket(CustomPayload payload);

	/**
	 * Sends a packet.
	 *
	 * @param packet the packet
	 */
	default void sendPacket(Packet<?> packet) {
		sendPacket(packet, null);
	}

	/**
	 * Sends a packet.
	 * @param payload the payload
	 */
	default void sendPacket(CustomPayload payload) {
		sendPacket(createPacket(payload));
	}

	/**
	 * Sends a packet.
	 *
	 * @param packet the packet
	 * @param callback an optional callback to execute after the packet is sent, may be {@code null}.
	 */
	void sendPacket(Packet<?> packet, PacketCallbacks callback);

	/**
	 * Sends a packet.
	 *
	 * @param payload the payload
	 * @param callback an optional callback to execute after the packet is sent, may be {@code null}.
	 */
	default void sendPacket(CustomPayload payload, PacketCallbacks callback) {
		sendPacket(createPacket(payload), callback);
	}

	/**
	 * Disconnects the player.
	 * @param disconnectReason the reason for disconnection
	 */
	void disconnect(Text disconnectReason);

	class PacketSenderS2CImpl implements PacketSender {

		private final ServerPlayerEntity target;

		public PacketSenderS2CImpl(ServerPlayerEntity target) {
			this.target = target;
		}

		@Override
		public Packet<?> createPacket(CustomPayload payload) {
			return new CustomPayloadS2CPacket(payload);
		}

		@Override
		public void sendPacket(CustomPayload payload) {
			NetworkManager.sendToPlayer(target, payload);
		}

		@Override
		public void sendPacket(Packet<?> packet, PacketCallbacks callback) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void disconnect(Text disconnectReason) {
			throw new UnsupportedOperationException();
		}
	}

	class PacketSenderC2SImpl implements PacketSender {

		@Override
		public Packet<?> createPacket(CustomPayload payload) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void sendPacket(CustomPayload payload) {
			NetworkManager.sendToServer(payload);
		}

		@Override
		public void sendPacket(Packet<?> packet, PacketCallbacks callback) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void disconnect(Text disconnectReason) {
			throw new UnsupportedOperationException();
		}
	}
}
