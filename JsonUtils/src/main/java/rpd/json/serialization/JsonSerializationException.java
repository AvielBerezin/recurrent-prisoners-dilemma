package rpd.json.serialization;

public class JsonSerializationException extends Exception {
    public JsonSerializationException(String message, Throwable cause) {
        super(message, cause);
    }

    public JsonSerializationException(String message) {
        super(message);
    }
}
