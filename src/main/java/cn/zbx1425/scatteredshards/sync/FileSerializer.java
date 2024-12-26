package cn.zbx1425.scatteredshards.sync;

import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.api.ShardCollection;
import net.modfest.scatteredshards.api.impl.ShardCollectionImpl;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

public class FileSerializer {

    private final Path basePath;

    public FileSerializer(Path basePath) {
        this.basePath = basePath;
    }

    public void loadInto(Map<UUID, ShardCollection> playerShardCollections) throws IOException {
        playerShardCollections.clear();
        try {
            Files.createDirectories(basePath.resolve("collections"));
        } catch (Exception ignored) { }
        try (Stream<Path> userFiles = Files.list(basePath.resolve("collections"))) {
            for (Path userFile : userFiles.toList()) {
				try {
					String[] fileNameParts = userFile.getFileName().toString().split("\\.");
					if (fileNameParts.length != 2 || !fileNameParts[1].equals("txt")) continue;
					ShardCollection collection = new ShardCollectionImpl();
					for (String line : Files.readAllLines(userFile)) {
						if (line.isEmpty()) continue;
						Identifier shardId = Identifier.tryParse(line);
						if (shardId != null) collection.add(shardId);
					}
					playerShardCollections.put(UUID.fromString(fileNameParts[0]), collection);
				} catch (IOException ex) {
					ScatteredShards.LOGGER.error("Failed to load shard collections from disk for file " + userFile.getFileName(), ex);
				}
            }
        }
    }

    private Path getUserPath(UUID bearer) {
        return basePath.resolve("collections")
                .resolve(bearer.toString() + ".txt");
    }

    public void write(UUID bearer, ShardCollection existingEntry) throws IOException {
        Path targetFile = getUserPath(bearer);
		try (FileOutputStream fos = new FileOutputStream(targetFile.toFile())) {
			for (Identifier shardId : existingEntry) {
				fos.write((shardId.toString() + "\n").getBytes());
			}
		}
    }
}
