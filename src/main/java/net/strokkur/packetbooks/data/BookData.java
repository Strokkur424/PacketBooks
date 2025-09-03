package net.strokkur.packetbooks.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

import java.util.ArrayList;
import java.util.List;

public record BookData(List<Component> components) {

    public String serializeToJson() {
        final GsonComponentSerializer serializer = GsonComponentSerializer.gson();

        final JsonArray pages = new JsonArray(components.size());
        for (final Component component : components) {
            pages.add(serializer.serializeToTree(component));
        }

        return pages.toString();
    }

    public static BookData deserializeFromJson(final String json) {
        final GsonComponentSerializer serializer = GsonComponentSerializer.gson();
        final JsonArray pagesRaw = JsonParser.parseString(json).getAsJsonArray();

        final List<Component> pages = new ArrayList<>(pagesRaw.size());
        for (final JsonElement jsonElement : pagesRaw) {
            pages.add(serializer.deserializeFromTree(jsonElement));
        }

        return new BookData(pages);
    }

    public static BookData empty() {
        return new BookData(List.of());
    }
}
