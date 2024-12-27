package net.fabricmc.fabric.api.networking.v1;


import dev.architectury.impl.NetworkAggregator;
import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public interface PayloadTypeRegistry<B extends PacketByteBuf> {

	<T extends CustomPayload> CustomPayload.Type<? super B, T> register(CustomPayload.Id<T> id, PacketCodec<? super B, T> codec);

	static PayloadTypeRegistry<RegistryByteBuf> playS2C() {
		return PayloadTypeS2CRegistryImpl.INSTANCE;
	}

	static PayloadTypeRegistry<RegistryByteBuf> playC2S() {
		return PayloadTypeC2SRegistryImpl.INSTANCE;
	}

	class PayloadTypeS2CRegistryImpl implements PayloadTypeRegistry<RegistryByteBuf> {

		protected static PayloadTypeS2CRegistryImpl INSTANCE = new PayloadTypeS2CRegistryImpl();

		@Override
		public <T extends CustomPayload> CustomPayload.Type<? super RegistryByteBuf, T> register(CustomPayload.Id<T> id, PacketCodec<? super RegistryByteBuf, T> codec) {
			if (Platform.getEnvironment() == Env.SERVER) {
				NetworkManager.registerS2CPayloadType(id, codec);
			} else {
				NetworkAggregator.S2C_CODECS.put(id.id(), (PacketCodec)codec);
			}
			return new CustomPayload.Type(id, codec);
		}
	}

	class PayloadTypeC2SRegistryImpl implements PayloadTypeRegistry<RegistryByteBuf> {

		protected static PayloadTypeC2SRegistryImpl INSTANCE = new PayloadTypeC2SRegistryImpl();

		@Override
		public <T extends CustomPayload> CustomPayload.Type<? super RegistryByteBuf, T> register(CustomPayload.Id<T> id, PacketCodec<? super RegistryByteBuf, T> codec) {
			NetworkAggregator.C2S_CODECS.put(id.id(), (PacketCodec)codec);
			return new CustomPayload.Type(id, codec);
		}
	}
}
