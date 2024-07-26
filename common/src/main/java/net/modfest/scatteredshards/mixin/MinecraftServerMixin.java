package net.modfest.scatteredshards.mixin;

import net.minecraft.server.MinecraftServer;
import net.modfest.scatteredshards.load.ShardTypeLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {

    @Inject(method = "reloadResources", at = @At("TAIL"))
    private void endResourceReload(Collection<String> collection, CallbackInfoReturnable<CompletableFuture<Void>> cir) {
        cir.getReturnValue().handleAsync((value, throwable) -> {
            // Hook into fail
//            ShardTypeLoader.END_DATA_PACK_RELOAD.invoker().endDataPackReload((MinecraftServer) (Object) this, null, throwable == null);
            return value;
        }, (MinecraftServer) (Object) this);
    }
}
