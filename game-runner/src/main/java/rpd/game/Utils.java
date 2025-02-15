package rpd.game;

import java.io.IOException;
import java.util.Scanner;
import java.util.Set;

public class Utils {
    public static String prettyExceptionDisplay(Exception exception) {
        StringBuilder stringBuilder = new StringBuilder();
        prettyExceptionDisplay(exception, 0, stringBuilder);
        return stringBuilder.toString();
    }

    private static void prettyExceptionDisplay(Throwable exception, int indent, StringBuilder stringBuilder) {
        boolean anythingPrinted = false;
        { // first section
            if (!Set.of(
                    Exception.class,
                    RuntimeException.class,
                    Error.class,
                    AssertionError.class,
                    IOException.class
            ).contains(exception.getClass())) {
                stringBuilder.append('(')
                             .append(exception.getClass().getSimpleName())
                             .append(')')
                             .append(' ');
                anythingPrinted = true;
            }
            if (exception.getMessage() != null) {
                multilineStringAppendIndented(indent + 1, exception.getMessage(), stringBuilder);
                anythingPrinted = true;
            }
        }
        if (exception.getCause() != null) {
            if (anythingPrinted) {
                stringBuilder.append(System.lineSeparator());
                appendSpaces(stringBuilder, indent);
            }
            stringBuilder.append("because: ");
            prettyExceptionDisplay(exception.getCause(), indent + 3, stringBuilder);
            anythingPrinted = true;
        }
        if (exception.getSuppressed().length > 0) {
            int padding = String.valueOf(exception.getSuppressed().length + 1).length() + 2;
            if (anythingPrinted) {
                stringBuilder.append(System.lineSeparator());
                appendSpaces(stringBuilder, indent);
            }
            stringBuilder.append("for ")
                         .append(exception.getSuppressed().length)
                         .append(" reasons:");
            for (int i = 0; i < exception.getSuppressed().length; i++) {
                Throwable reason = exception.getSuppressed()[i];
                stringBuilder.append(System.lineSeparator());
                appendSpaces(stringBuilder, indent);
                int number = i + 1;
                stringBuilder.append(number);
                appendSpaces(stringBuilder, padding - String.valueOf(number).length());
                prettyExceptionDisplay(reason, indent + padding, stringBuilder);
            }
            anythingPrinted = true;
        }
        if (!anythingPrinted) {
            stringBuilder.append(exception.getClass().getSimpleName());
        }
    }

    private static void multilineStringAppendIndented(int indent, String message, StringBuilder stringBuilder) {
        Scanner scanner = new Scanner(message);
        if (!scanner.hasNextLine()) return;
        {
            String line = scanner.nextLine();
            stringBuilder.append(line);
        }
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            stringBuilder.append(System.lineSeparator());
            appendSpaces(stringBuilder, indent);
            stringBuilder.append(line);
        }
    }

    private static void appendSpaces(StringBuilder stringBuilder, int indent) {
        for (int i = 0; i < indent; i++) {
            stringBuilder.append(' ');
        }
    }
}
