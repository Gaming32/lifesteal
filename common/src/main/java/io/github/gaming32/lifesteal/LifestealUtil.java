package io.github.gaming32.lifesteal;

import com.google.gson.JsonElement;
import com.google.gson.internal.Streams;
import org.quiltmc.qup.json.JsonReader;
import org.quiltmc.qup.json.JsonWriter;
import org.quiltmc.qup.json.gson.GsonReader;
import org.quiltmc.qup.json.gson.GsonWriter;

import java.io.IOException;

public class LifestealUtil {
    public static JsonElement parseQup(JsonReader reader) {
        return Streams.parse(new GsonReader(reader));
    }

    public static void writeQup(JsonWriter writer, JsonElement element) throws IOException {
        Streams.write(element, new GsonWriter(writer));
    }
}
