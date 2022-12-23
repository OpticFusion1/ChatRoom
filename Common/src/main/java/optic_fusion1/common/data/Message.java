package optic_fusion1.common.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public record Message(User user, String content) {

    /**
     * Converts a JSON string to a Message
     *
     * @param string JSON string
     * @return Message
     */
    public static Message deserialize(String string) {
        JsonParser parser = new JsonParser();
        JsonObject object = (JsonObject) parser.parse(string);

        JsonElement clientElement = object.get("user");

        User user = clientElement.isJsonNull() ? null : User.deserialize(clientElement.getAsString());
        String content = object.get("content").getAsString();
        return new Message(user, content);
    }

    /**
     * Converts this class to a JSON string
     *
     * @return JSON string
     */
    public String serialize() {
        JsonObject object = new JsonObject();
        object.addProperty("user", this.getUser() == null ? null : this.getUser().serialize());
        object.addProperty("content", this.getContent());

        return object.toString();
    }

    public User getUser() {
        return user;
    }

    public String getContent() {
        return content;
    }
}
