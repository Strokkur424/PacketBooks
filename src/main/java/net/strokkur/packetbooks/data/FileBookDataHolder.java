package net.strokkur.packetbooks.data;

import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileBookDataHolder extends AbstractBookDataHolder {

    private final JavaPlugin plugin;
    private final Path currentIdPath;

    public FileBookDataHolder(final JavaPlugin plugin) {
        this.plugin = plugin;
        this.currentIdPath = this.plugin.getDataPath().resolve("books/_curr.bin");
    }

    @Override
    @Nullable
    protected BookData loadBookData(final int id) {
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
    }

    @Override
    protected void saveBookData(final int id, final BookData bookData) {
        final String json = bookData.serializeToJson();
        final Path path = getPathForId(id);

        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, json, StandardCharsets.UTF_8);
        } catch (IOException e) {
            plugin.getSLF4JLogger().error("Failed to save book data for id {}", id, e);
        }
    }

    @Override
    public void loadCurrentId() {
        if (!Files.exists(currentIdPath)) {
            super.currentId = 0;
            return;
        }

        try {
            final ByteBuffer buf = ByteBuffer.wrap(Files.readAllBytes(currentIdPath));
            super.currentId = buf.getInt();
        } catch (IOException e) {
            plugin.getSLF4JLogger().error("Failed to load current book id path.", e);
        }
    }

    @Override
    protected void incrementCurrentId() {
        super.currentId++;

        try {
            final ByteBuffer buf = ByteBuffer.allocate(4);
            buf.putInt(super.currentId);
            Files.write(currentIdPath, buf.array());
        } catch (IOException e) {
            plugin.getSLF4JLogger().error("Failed to save current book id path.", e);
        }
    }

    private Path getPathForId(final int id) {
        return plugin.getDataFolder().toPath().resolve("books/" + id + ".txt");
    }
}
