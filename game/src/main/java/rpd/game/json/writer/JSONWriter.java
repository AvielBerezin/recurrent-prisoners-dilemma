package rpd.game.json.writer;

import rpd.game.json.*;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JSONWriter {
    public static Consumer<OutputStream> compactWrite(JSONValue jsonValue) {
        return outputStream -> {
            PrintWriter printWriter = new PrintWriter(outputStream);
            compactWriteToPrintWriter(jsonValue).accept(printWriter);
            printWriter.flush();
        };
    }

    private static Consumer<PrintWriter> compactWriteToPrintWriter(JSONValue jsonValue) {
        return jsonValue.dispatch(new JSONValue.Dispatcher<>() {
            @Override
            public Consumer<PrintWriter> apply(JSONObject jsonObject) {
                return printWriter -> {
                    printWriter.write("{");
                    writeObjectElements(jsonObject, printWriter);
                    printWriter.write("}");
                };
            }

            private void writeObjectElements(JSONObject jsonObject, PrintWriter printWriter) {
                ArrayList<String> keys = new ArrayList<>(jsonObject.keySet());
                if (keys.size() == 0) {
                    return;
                }
                writeJsonString(printWriter, keys.get(0));
                printWriter.write(":");
                compactWriteToPrintWriter(jsonObject.get(keys.get(0))).accept(printWriter);
                for (int i = 1; i < keys.size(); i++) {
                    printWriter.write(",");
                    String key = keys.get(i);
                    JSONValue value = jsonObject.get(key);
                    writeJsonString(printWriter, key);
                    printWriter.write(":");
                    compactWriteToPrintWriter(value).accept(printWriter);
                }
            }

            private void writeJsonString(PrintWriter printWriter, String string) {
                if (Pattern.compile("\\p{Cc}").matcher(string).find()) {
                    throw new RuntimeException("writing json strings where a key of contains control characters is not supported");
                }
                printWriter.write("\"");
                printWriter.write(string.replaceAll(Pattern.quote("\\"),
                                                 Matcher.quoteReplacement("\\\\"))
                                     .replaceAll(Pattern.quote("\""),
                                                 Matcher.quoteReplacement("\\\""))
                                     .replaceAll(Pattern.quote("\n"),
                                                 Matcher.quoteReplacement("\\n"))
                                     .replaceAll(Pattern.quote("\b"),
                                                 Matcher.quoteReplacement("\\b"))
                                     .replaceAll(Pattern.quote("\f"),
                                                 Matcher.quoteReplacement("\\f"))
                                     .replaceAll(Pattern.quote("\r"),
                                                 Matcher.quoteReplacement("\\r"))
                                     .replaceAll(Pattern.quote("\t"),
                                                 Matcher.quoteReplacement("\\t")));
                printWriter.write("\"");
            }

            @Override
            public Consumer<PrintWriter> apply(JSONArray jsonArray) {
                return printWriter -> {
                    printWriter.write("[");
                    writeArrayElements(jsonArray, printWriter);
                    printWriter.write("]");
                };
            }

            private void writeArrayElements(JSONArray jsonArray, PrintWriter printWriter) {
                if (jsonArray.size() == 0) {
                    return;
                }
                compactWriteToPrintWriter(jsonArray.get(0)).accept(printWriter);
                for (int i = 1; i < jsonArray.size(); i++) {
                    printWriter.write(",");
                    compactWriteToPrintWriter(jsonArray.get(i)).accept(printWriter);
                }
            }

            @Override
            public Consumer<PrintWriter> apply(JSONNumber jsonNumber) {
                return printWriter -> printWriter.write(jsonNumber.number().toString());
            }

            @Override
            public Consumer<PrintWriter> apply(JSONString jsonString) {
                return printWriter -> writeJsonString(printWriter, jsonString.value());
            }

            @Override
            public Consumer<PrintWriter> apply(JSONBoolean jsonBoolean) {
                return printWriter -> printWriter.write(String.valueOf(jsonBoolean.value()));
            }

            @Override
            public Consumer<PrintWriter> apply(JSONNull jsonNull) {
                return printWriter -> printWriter.write("null");
            }
        });
    }
}
