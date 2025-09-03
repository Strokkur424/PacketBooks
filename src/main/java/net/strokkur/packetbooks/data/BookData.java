/*
 * PacketBooks - A simple plugin for fixing various book-based data overflow exploits.
 * Copyright (C) 2025  Strokkur24
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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
