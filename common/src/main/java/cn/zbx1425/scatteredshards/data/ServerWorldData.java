package cn.zbx1425.scatteredshards.data;

import cn.zbx1425.scatteredshards.data.persist.FileSerializer;
import cn.zbx1425.scatteredshards.data.sync.Synchronizer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.WorldSavePath;
import net.modfest.scatteredshards.api.ShardCollection;

import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

public class ServerWorldData {

    public static final Snowflake SNOWFLAKE = new Snowflake();

    public final MinecraftServer server;
    public final Path basePath;

    public boolean isHost;

    public final PlayerShardCollections comments = new PlayerShardCollections();

    public final FileSerializer fileSerializer;
//  public final UplinkDispatcher uplinkDispatcher;
    public Synchronizer peerChannel;

    public ServerWorldData(MinecraftServer server, boolean isHost) {
        this.server = server;
        this.basePath = Path.of(server.getSavePath(WorldSavePath.ROOT).toString(), "scattered_shards");
        fileSerializer = new FileSerializer(basePath);
        this.isHost = isHost;
        this.peerChannel = Synchronizer.NOOP;
//        uplinkDispatcher = new UplinkDispatcher(Main.SERVER_CONFIG.uplinkUrl.value);
    }

    public void load() throws IOException {
        if (isHost) {
            fileSerializer.loadInto(comments);
            peerChannel.kvWriteAll(comments);
        } else {
            peerChannel.kvReadAllInto(comments);
        }
    }

    public void insert(UUID bearer, ShardCollection newEntry, boolean fromPeer) throws IOException {
//        if (CommentCommand.isCommand(newEntry)) {
//            if (isHost) {
//                CommentCommand.executeCommandServer(newEntry, this);
//            }
//            return;
//        }
        comments.insert(bearer, newEntry);
        if (isHost) {
            fileSerializer.insert(bearer, newEntry);
//          uplinkDispatcher.insert(newEntry);
            peerChannel.kvWriteEntry(bearer, newEntry);
        }
        if (!fromPeer) {
            peerChannel.notifyInsert(bearer, newEntry);
        }
//        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
//            PacketEntryUpdateS2C.send(player, newEntry, false);
//        }
    }

    public void update(UUID bearer, ShardCollection newEntry, boolean fromPeer) throws IOException {
        ShardCollection trustedEntry = comments.update(bearer, newEntry);
        if (isHost) {
            fileSerializer.update(bearer, trustedEntry);
//            uplinkDispatcher.update(trustedEntry);
            peerChannel.kvWriteEntry(bearer, trustedEntry);
        }
        if (!fromPeer) {
            peerChannel.notifyUpdate(bearer, trustedEntry);
        }
//        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
//            PacketEntryUpdateS2C.send(player, trustedEntry, true);
//        }
    }
}
