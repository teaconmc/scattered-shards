package cn.zbx1425.scatteredshards;

import com.google.common.base.CaseFormat;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ServerConfig {

    public ConfigItem redisUrl;
    public ConfigItem syncRole;

    public void load(Path configPath) throws IOException {
        JsonObject json = Files.exists(configPath)
                ? JsonParser.parseString(Files.readString(configPath)).getAsJsonObject()
                : new JsonObject();
        redisUrl = new ConfigItem(json, "redisUrl", "");
        syncRole = new ConfigItem(json, "syncRole", "host");

        if (!Files.exists(configPath)) save(configPath);
    }

    public void save(Path configPath) throws IOException {
        JsonObject json = new JsonObject();
        redisUrl.writeJson(json);
        syncRole.writeJson(json);

        Files.writeString(configPath, new GsonBuilder().setPrettyPrinting().create().toJson(json));
    }

    public static class ConfigItem {

        private final String camelKey;
        public String value;
        public boolean isFromJson;

        public ConfigItem(JsonObject jsonObject, String camelKey, String defaultValue) {
            String snakeKey = CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, camelKey);
            this.camelKey = camelKey;
            if (System.getenv("SCATSHARDS_" + snakeKey) != null) {
                this.value = System.getenv("SCATSHARDS_" + snakeKey);
                this.isFromJson = false;
            } else if (jsonObject.has(camelKey)) {
                if (jsonObject.get(camelKey).isJsonArray()) {
                    StringBuilder configValue = new StringBuilder();
                    for (int i = 0; i < jsonObject.get(camelKey).getAsJsonArray().size(); i++) {
                        configValue.append(jsonObject.get(camelKey).getAsJsonArray().get(i).getAsString());
                    }
                    this.value = configValue.toString();
                } else {
                    this.value = jsonObject.get(camelKey).getAsString();
                }
                this.isFromJson = true;
            } else {
                this.value = defaultValue;
                this.isFromJson = false;
            }
        }

        public void writeJson(JsonObject jsonObject) {
            if (isFromJson) {
                jsonObject.addProperty(camelKey, value);
            }
        }
    }
}
