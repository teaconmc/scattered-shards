package cn.zbx1425.scatteredshards.data.persist;

import cn.zbx1425.scatteredshards.data.PlayerShardCollections;
import net.modfest.scatteredshards.api.ShardCollection;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.stream.Stream;

public class FileSerializer {

    private final Path basePath;

    public FileSerializer(Path basePath) {
        this.basePath = basePath;
    }

    public void loadInto(PlayerShardCollections playerShardCollections) throws IOException {
        playerShardCollections.clear();
        try {
            Files.createDirectories(basePath.resolve("collections"));
        } catch (FileAlreadyExistsException ignored) { }
        try (Stream<Path> userFiles = Files.list(basePath.resolve("collections"))) {
            for (Path userFile : userFiles.toList()) {
                String[] fileNameParts = userFile.getFileName().toString().split("\\.");
                if (fileNameParts.length != 2 || !fileNameParts[1].equals("txt")) continue;
                String fileContent = Files.readString(userFile);
                playerShardCollections.loadBearer(UUID.fromString(fileNameParts[0]), fileContent, true);
            }
        }
    }

    private Path getUserPath(UUID bearer) {
        return basePath.resolve("collections")
                .resolve(bearer.toString() + ".txt");
    }

    public void insert(UUID bearer, ShardCollection newEntry) throws IOException {
        Path targetFile = getUserPath(bearer);
        Files.writeString(targetFile, PlayerShardCollections.serialize(newEntry));
    }

    public void cover(UUID bearer, ShardCollection newEntry, boolean append) throws IOException {
        Path targetFile = getUserPath(bearer);
        Files.writeString(targetFile, PlayerShardCollections.serialize(newEntry));
    }

    public void update(UUID bearer, ShardCollection existingEntry) throws IOException {
        Path targetFile = getUserPath(bearer);
        Files.writeString(targetFile, PlayerShardCollections.serialize(existingEntry));
    }
}
