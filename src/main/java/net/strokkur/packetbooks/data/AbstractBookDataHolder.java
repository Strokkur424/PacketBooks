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

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public abstract class AbstractBookDataHolder {

  private final ExecutorService executorService = Executors.newCachedThreadPool();
  protected final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

  private final AsyncCache<Integer, BookData> cache = Caffeine.newBuilder()
      .expireAfter(Expiry.accessing((key, value) -> Duration.ofMinutes(10)))
      .buildAsync();

  private volatile int currentId = 0;

  protected void setCurrentIdValue(int value) {
    readWriteLock.writeLock().lock();
    try {
      currentId = value;
    } finally {
      readWriteLock.writeLock().unlock();
    }
  }

  protected int incrementCurrentIdValue() {
    readWriteLock.writeLock().lock();
    try {
      int id = currentId;
      id++;
      currentId = id;
      return id;
    } finally {
      readWriteLock.writeLock().unlock();
    }
  }

  protected int getCurrentIdValue() {
    readWriteLock.readLock().lock();
    try {
      return currentId;
    } finally {
      readWriteLock.readLock().unlock();
    }
  }

  protected CompletableFuture<@Nullable BookData> loadBookData(int id) {
    return loadBookData(id, executorService);
  }

  protected abstract CompletableFuture<@Nullable BookData> loadBookData(int id, Executor executor);

  protected abstract CompletableFuture<Void> saveBookData(int id, CompletableFuture<BookData> bookData);

  public abstract CompletableFuture<Void> loadCurrentId();

  protected abstract CompletableFuture<Void> incrementCurrentId();

  public CompletableFuture<@Nullable BookData> getBookData(int id) {
    return cache.get(id, (i, e) -> loadBookData(i));
  }

  public void updateBookData(int id, BookData bookData) {
    final CompletableFuture<BookData> future = CompletableFuture.completedFuture(bookData);
    cache.put(id, future);
    saveBookData(id, future);
  }

  public int saveNewBookData(CompletableFuture<BookData> bookData) {
    final int id = getCurrentIdValue();
    cache.put(getCurrentIdValue(), bookData);
    saveBookData(getCurrentIdValue(), bookData);
    incrementCurrentId();
    return id;
  }
}
