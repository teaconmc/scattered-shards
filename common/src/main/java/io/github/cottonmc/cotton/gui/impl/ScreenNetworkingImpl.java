package io.github.cottonmc.cotton.gui.impl;

import com.mojang.serialization.Decoder;
import com.mojang.serialization.Encoder;
import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryOps;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.Identifier;

import io.github.cottonmc.cotton.gui.SyncedGuiDescription;
import io.github.cottonmc.cotton.gui.networking.NetworkSide;
import io.github.cottonmc.cotton.gui.networking.ScreenNetworking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.concurrent.Executor;

public class ScreenNetworkingImpl implements ScreenNetworking {
	// Matches the one used in PacketCodecs.codec() etc
	private static final long MAX_NBT_SIZE = 0x200000L;

	public static class ScreenMessageBase implements CustomPayload {

		private final int syncId;
		private final Identifier message;
		private final NbtElement nbt;

		public ScreenMessageBase(int syncId, Identifier message, NbtElement nbt) {
			this.syncId = syncId;
			this.message = message;
			this.nbt = nbt;
		}

		public int syncId() {
			return syncId;
		}

		public Identifier message() {
			return message;
		}

		public NbtElement nbt() {
			return nbt;
		}

		@Override
		public Id<? extends CustomPayload> getId() {
			return null;
		}
	}

	public static class ScreenMessageC2S extends ScreenMessageBase {
		public static final Id<ScreenMessageC2S> ID = new Id<>(LibGuiCommon.id("screen_message_c2s"));

		public ScreenMessageC2S(int syncId, Identifier message, NbtElement nbt) {
			super(syncId, message, nbt);
		}

		public static final PacketCodec<RegistryByteBuf, ScreenMessageC2S> CODEC = PacketCodec.tuple(
				PacketCodecs.INTEGER, ScreenMessageC2S::syncId,
				Identifier.PACKET_CODEC, ScreenMessageC2S::message,
				PacketCodecs.nbt(() -> NbtSizeTracker.of(MAX_NBT_SIZE)), ScreenMessageC2S::nbt,
				ScreenMessageC2S::new
		);

		@Override
		public Id<? extends CustomPayload> getId() {
			return ID;
		}
	}

	public static class ScreenMessageS2C extends ScreenMessageBase {
		public static final Id<ScreenMessageS2C> ID = new Id<>(LibGuiCommon.id("screen_message_s2c"));

		public ScreenMessageS2C(int syncId, Identifier message, NbtElement nbt) {
			super(syncId, message, nbt);
		}

		public static final PacketCodec<RegistryByteBuf, ScreenMessageS2C> CODEC = PacketCodec.tuple(
				PacketCodecs.INTEGER, ScreenMessageS2C::syncId,
				Identifier.PACKET_CODEC, ScreenMessageS2C::message,
				PacketCodecs.nbt(() -> NbtSizeTracker.of(MAX_NBT_SIZE)), ScreenMessageS2C::nbt,
				ScreenMessageS2C::new
		);

		@Override
		public Id<? extends CustomPayload> getId() {
			return ID;
		}
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(ScreenNetworkingImpl.class);
	private static final Map<SyncedGuiDescription, ScreenNetworkingImpl> instanceCache = new WeakHashMap<>();

	private final Map<Identifier, ReceiverData<?>> receivers = new HashMap<>();
	private final SyncedGuiDescription description;
	private final NetworkSide side;

	private ScreenNetworkingImpl(SyncedGuiDescription description, NetworkSide side) {
		this.description = description;
		this.side = side;
	}

	private static RegistryOps<NbtElement> getRegistryOps(DynamicRegistryManager registryManager) {
		return registryManager.getOps(NbtOps.INSTANCE);
	}

	@Override
	public <D> void receive(Identifier message, Decoder<D> decoder, MessageReceiver<D> receiver) {
		Objects.requireNonNull(message, "message");
		Objects.requireNonNull(decoder, "decoder");
		Objects.requireNonNull(receiver, "receiver");

		if (!receivers.containsKey(message)) {
			receivers.put(message, new ReceiverData<>(decoder, receiver));
		} else {
			throw new IllegalStateException("Message " + message + " on side " + side + " already registered");
		}
	}

	@Override
	public <D> void send(Identifier message, Encoder<D> encoder, D data) {
		Objects.requireNonNull(message, "message");
		Objects.requireNonNull(encoder, "encoder");

		var ops = getRegistryOps(description.getWorld().getRegistryManager());
		NbtElement encoded = encoder.encodeStart(ops, data).getOrThrow();
		if (description.getNetworkSide() == NetworkSide.SERVER) {
			ScreenMessageS2C packet = new ScreenMessageS2C(description.syncId, message, encoded);
			description.getPacketSender().sendPacket(packet);
		} else {
			ScreenMessageC2S packet = new ScreenMessageC2S(description.syncId, message, encoded);
			description.getPacketSender().sendPacket(packet);
		}
	}

	public static void init() {
		System.out.println("ScreenMessageS2C PayloadType: " + ScreenMessageS2C.ID);
		if (Platform.getEnvironment() == Env.SERVER) {
			NetworkManager.registerS2CPayloadType(ScreenMessageS2C.ID, ScreenMessageS2C.CODEC);
		}
//		PayloadTypeRegistry.playC2S().register(ScreenMessage.ID, ScreenMessage.CODEC);
		NetworkManager.registerReceiver(NetworkManager.Side.C2S, ScreenMessageC2S.ID, ScreenMessageC2S.CODEC, (payload, context) -> {
			handle(context.getPlayer().getServer(), context.getPlayer(), payload);
		});
	}

	public static void handle(Executor executor, PlayerEntity player, ScreenMessageBase packet) {
		ScreenHandler screenHandler = player.currentScreenHandler;

		if (!(screenHandler instanceof SyncedGuiDescription)) {
			LOGGER.error("Received message packet for screen handler {} which is not a SyncedGuiDescription", screenHandler);
			return;
		} else if (packet.syncId() != screenHandler.syncId) {
			LOGGER.error("Received message for sync ID {}, current sync ID: {}", packet.syncId(), screenHandler.syncId);
			return;
		}

		ScreenNetworkingImpl networking = instanceCache.get(screenHandler);

		if (networking != null) {
			ReceiverData<?> receiverData = networking.receivers.get(packet.message());
			if (receiverData != null) {
				processMessage(executor, player, packet, screenHandler, receiverData);
			} else {
				LOGGER.error("Message {} not registered for {} on side {}", packet.message(), screenHandler, networking.side);
			}
		} else {
			LOGGER.warn("GUI description {} does not use networking", screenHandler);
		}
	}

	private static <D> void processMessage(Executor executor, PlayerEntity player, ScreenMessageBase packet, ScreenHandler description, ReceiverData<D> receiverData) {
		var ops = getRegistryOps(player.getRegistryManager());
		var result = receiverData.decoder().parse(ops, packet.nbt());

		if (result.isSuccess()) {
			executor.execute(() -> {
				try {
					receiverData.receiver().onMessage(result.getOrThrow());
				} catch (Exception e) {
					LOGGER.error("Error handling screen message {} for {}", packet.message(), description, e);
				}
			});
		} else {
			LOGGER.error(
					"Could not parse screen message {}: {}",
					packet.message(),
					result.error().get().message()
			);
		}
	}

	public static ScreenNetworking of(SyncedGuiDescription description, NetworkSide networkSide) {
		Objects.requireNonNull(description, "description");
		Objects.requireNonNull(networkSide, "networkSide");

		if (description.getNetworkSide() == networkSide) {
			return instanceCache.computeIfAbsent(description, it -> new ScreenNetworkingImpl(description, networkSide));
		} else {
			return DummyNetworking.INSTANCE;
		}
	}

	private record ReceiverData<D>(Decoder<D> decoder, MessageReceiver<D> receiver) {
	}

	private static final class DummyNetworking extends ScreenNetworkingImpl {
		static final DummyNetworking INSTANCE = new DummyNetworking();

		private DummyNetworking() {
			super(null, null);
		}

		@Override
		public <D> void receive(Identifier message, Decoder<D> decoder, MessageReceiver<D> receiver) {
			// NO-OP
		}

		@Override
		public <D> void send(Identifier message, Encoder<D> encoder, D data) {
			// NO-OP
		}
	}
}
