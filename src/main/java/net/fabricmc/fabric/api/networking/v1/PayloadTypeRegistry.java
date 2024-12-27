package net.fabricmc.fabric.api.networking.v1;


import dev.architectury.impl.NetworkAggregator;
import dev.architectury.networking.NetworkManager;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.neoforged.fml.loading.FMLEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface PayloadTypeRegistry<B extends PacketByteBuf> {

	static Logger LOGGER = LoggerFactory.getLogger("PayloadTypeRegistry");

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
			if (FMLEnvironment.dist.isDedicatedServer()) {
				NetworkManager.registerS2CPayloadType(id, codec);
				LOGGER.info("Registered S2C payload type: {}", id);
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
