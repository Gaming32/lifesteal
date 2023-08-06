package io.github.gaming32.lifesteal;

import com.google.gson.JsonElement;
import com.google.gson.internal.Streams;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.network.chat.Component;
import org.quiltmc.qup.json.JsonReader;
import org.quiltmc.qup.json.JsonWriter;
import org.quiltmc.qup.json.gson.GsonReader;
import org.quiltmc.qup.json.gson.GsonWriter;

import java.io.IOException;
import java.util.Objects;
import java.util.function.BinaryOperator;
import java.util.stream.Collector;

public class LifestealUtil {
    public static JsonElement parseQup(JsonReader reader) {
        return Streams.parse(new GsonReader(reader));
    }

    public static void writeQup(JsonWriter writer, JsonElement element) throws IOException {
        Streams.write(element, new GsonWriter(writer));
    }

    public static Collector<Component, ?, Component> componentJoin(Component separator) {
        return Collector.of(
            Component::empty,
            (c, e) -> {
                if (!c.getSiblings().isEmpty()) {
                    c.append(separator);
                }
                c.append(e);
            },
            (a, b) -> {
                if (a.getSiblings().isEmpty()) {
                    return b;
                }
                if (b.getSiblings().isEmpty()) {
                    return a;
                }
                return Component.empty()
                    .append(a)
                    .append(separator)
                    .append(b);
            },
            c -> c,
            Collector.Characteristics.IDENTITY_FINISH
        );
    }

    public static String toString(MinMaxBounds<?> bounds) {
        if (bounds.isAny()) {
            return "..";
        }
        if (Objects.equals(bounds.getMin(), bounds.getMax())) {
            assert bounds.getMin() != null;
            return bounds.getMin().toString();
        }
        return Objects.requireNonNullElse(bounds.getMin(), "") + ".." + Objects.requireNonNullElse(bounds.getMax(), "");
    }

    public static <T extends Number> T clamp(T value, MinMaxBounds<T> bounds, BinaryOperator<T> min, BinaryOperator<T> max) {
        if (bounds.getMin() != null) {
            value = max.apply(value, bounds.getMin());
        }
        if (bounds.getMax() != null) {
            value = min.apply(value, bounds.getMax());
        }
        return value;
    }

    public static int clamp(int value, MinMaxBounds.Ints bounds) {
        return clamp(value, bounds, Math::min, Math::max);
    }
}
