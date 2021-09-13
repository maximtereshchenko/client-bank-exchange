package ru.standard1c.reader.source;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * @author Maxim Tereshchenko
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Attribute {

    private final String key;
    private final String value;

    public static Attribute from(String line) {
        if (!line.contains("=")) {
            return new Attribute(line, null);
        }
        var attributeArray = line.split("=");

        return new Attribute(
                attributeArray[0],
                attributeArray.length == 1 ? "" : attributeArray[1]
        );
    }

    public String key() {
        return key;
    }

    public String value() {
        return value;
    }

    public boolean hasKey(String key) {
        return this.key.equals(key);
    }

    @Override
    public String toString() {
        if (value == null) {
            return key;
        }

        return String.format("%s=%s", key, value);
    }
}
