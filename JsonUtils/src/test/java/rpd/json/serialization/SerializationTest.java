package rpd.json.serialization;

import org.junit.jupiter.api.Test;
import rpd.json.values.*;
import rpd.json.values.writer.JSONWriter;

import java.util.Map;

class SerializationTest {
    public record A(int a, boolean b, String c) {
    }

    public static class B {
        int a;
        boolean b;
        String c;

        public void setA(int a) {
            this.a = a;
        }

        public void setB(boolean b) {
            this.b = b;
        }

        public void setC(String c) {
            this.c = c;
        }

        @Override
        public String toString() {
            return "B{" +
                   "a=" + a +
                   ", b=" + b +
                   ", c='" + c + '\'' +
                   '}';
        }
    }

    @Test
    void serialization() throws JsonSerializationException {
        Serializer<Object, JSONValue> ajsonObjectSerializer = Serializers.generalSerializer();
        JSONWriter.compactWrite(ajsonObjectSerializer.serialize(new A(1, true, "hello"))).accept(System.out);
    }

    @Test
    void deserialize() throws JsonDeserializationException {
        B deserialized = Deserializers.generalDeserializer(B.class).deserialize(JSONObject.of(Map.of("a", new JSONNumber(1), "b", new JSONBoolean(true), "c", new JSONString("hello"))));
        System.out.println(deserialized);
    }
}