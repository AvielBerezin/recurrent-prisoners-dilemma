package rpd.json.serialization;

public class JsonDeserializationException extends Exception {
    public JsonDeserializationException(String message, Throwable cause) {
        super(message, cause);
    }

    public JsonDeserializationException(String message) {
        super(message);
    }
}
