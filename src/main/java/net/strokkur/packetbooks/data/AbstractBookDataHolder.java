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
