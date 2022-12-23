package optic_fusion1.common.data;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.HashMap;
import java.util.UUID;

public record User(UUID uuid, String username) {

    private static final HashMap<UUID, User> users = new HashMap<>();

    /**
     * Serializes Client to JSON string
     *
     * @return JSON string
     */
    public String serialize() {
        JsonObject object = new JsonObject();
        object.addProperty("id", this.getUuid().toString());
        object.addProperty("username", this.getUsername());

        return object.toString();
    }

    /**
     * Deserializes a Client JSON string to a Client object
     *
     * @param string raw Client JSON string
     * @return new Client instance
     */
    public static User deserialize(String string) {
        JsonParser parser = new JsonParser();
        JsonObject object = (JsonObject) parser.parse(string);

        UUID uuid = UUID.fromString(object.get("id").getAsString());
        String username = object.get("username").getAsString();

        // Tries to get existing client instance or returns a new one
        return User.getUsers().getOrDefault(uuid, new User(uuid, username));
    }

    public String getUsername() {
        return username;
    }

    public UUID getUuid() {
        return uuid;
    }

    public static HashMap<UUID, User> getUsers() {
        return users;
    }
}
