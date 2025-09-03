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

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import org.jspecify.annotations.Nullable;

import java.time.Duration;

public abstract class AbstractBookDataHolder {

    private final Cache<Integer, BookData> cache = Caffeine.newBuilder()
        .expireAfter(Expiry.accessing((key, value) -> Duration.ofMinutes(10)))
        .build();

    protected int currentId = 0;

    @Nullable
    protected abstract BookData loadBookData(int id);

    protected abstract void saveBookData(int id, BookData bookData);

    public abstract void loadCurrentId();

    protected abstract void incrementCurrentId();

    @Nullable
    public BookData getBookData(int id) {
        //noinspection DataFlowIssue - it is perfectly fine for the mappingFunction to return null
        return cache.get(id, this::loadBookData);
    }

    public void updateBookData(int id, BookData bookData) {
        cache.put(id, bookData);
        saveBookData(id, bookData);
    }

    public int saveNewBookData(BookData bookData) {
        final int id = currentId;
        cache.put(currentId, bookData);
        saveBookData(currentId, bookData);
        incrementCurrentId();
        return id;
    }
}
