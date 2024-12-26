package cn.zbx1425.scatteredshards.sync;

import net.minecraft.server.MinecraftServer;

public class SyncDispatcher implements AutoCloseable {

    public final MinecraftServer server;

	public boolean isHost;
    public Synchronizer peerChannel;

	public static SyncDispatcher INSTANCE;

    public SyncDispatcher(MinecraftServer server, boolean isHost) {
        this.server = server;
        this.isHost = isHost;
        this.peerChannel = Synchronizer.NOOP;
    }

	@Override
	public void close() throws Exception {
		this.peerChannel.close();
	}
}
