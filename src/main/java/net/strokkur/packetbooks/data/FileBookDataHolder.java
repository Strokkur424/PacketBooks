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

import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class FileBookDataHolder extends AbstractBookDataHolder {

  protected final ReadWriteLock lock = new ReentrantReadWriteLock();

  private final JavaPlugin plugin;
  private final Path currentIdPath;

  public FileBookDataHolder(final JavaPlugin plugin) {
    this.plugin = plugin;
    this.currentIdPath = this.plugin.getDataPath().resolve("books/_curr.bin");
  }

  @Override
  protected CompletableFuture<@Nullable BookData> loadBookData(final int id, final Executor executor) {
    return CompletableFuture.supplyAsync(() -> {
      final Path path = getPathForId(id);
      if (!Files.exists(path)) {
        return BookData.empty();
      }

      final String json;
      try {
        json = Files.readString(path, StandardCharsets.UTF_8);
      } catch (IOException e) {
        plugin.getSLF4JLogger().error("Failed to load book data for id {}", id, e);
        return null;
      }

      return BookData.deserializeFromJson(json);
    }, executor);
  }

  @Override
  protected CompletableFuture<Void> saveBookData(final int id, final CompletableFuture<BookData> bookData) {
    return bookData.thenAccept(data -> {
      final String json = data.serializeToJson();
      final Path path = getPathForId(id);

      try {
        Files.createDirectories(path.getParent());
        Files.writeString(path, json, StandardCharsets.UTF_8);
      } catch (IOException e) {
        plugin.getSLF4JLogger().error("Failed to save book data for id {}", id, e);
      }
    });
  }

  @Override
  public CompletableFuture<Void> loadCurrentId() {
    return CompletableFuture.runAsync(() -> {
      if (!Files.exists(currentIdPath)) {
        return;
      }

      lock.readLock().lock();
      try {
        final ByteBuffer buf = ByteBuffer.wrap(Files.readAllBytes(currentIdPath));
        super.setCurrentIdValue(buf.getInt());
      } catch (IOException e) {
        plugin.getSLF4JLogger().error("Failed to load current book id path.", e);
      } finally {
        lock.readLock().unlock();
      }
    });
  }

  @Override
  protected CompletableFuture<Void> incrementCurrentId() {
    int id = super.incrementCurrentIdValue();

    return CompletableFuture.runAsync(() -> {
      lock.writeLock().lock();
      try {
        final ByteBuffer buf = ByteBuffer.allocate(4);
        buf.putInt(id);
        Files.write(currentIdPath, buf.array());
      } catch (IOException e) {
        plugin.getSLF4JLogger().error("Failed to save current book id path.", e);
      } finally {
        lock.writeLock().unlock();
      }
    });
  }

  private Path getPathForId(final int id) {
    return plugin.getDataFolder().toPath().resolve("books/" + id + ".txt");
  }
}
