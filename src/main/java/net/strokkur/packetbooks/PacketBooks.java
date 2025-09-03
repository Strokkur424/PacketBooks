package net.strokkur.packetbooks;

import com.google.common.base.Preconditions;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.WritableBookContent;
import io.papermc.paper.datacomponent.item.WrittenBookContent;
import io.papermc.paper.event.player.PlayerInventorySlotChangeEvent;
import io.papermc.paper.text.Filtered;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.strokkur.packetbooks.data.AbstractBookDataHolder;
import net.strokkur.packetbooks.data.BookData;
import net.strokkur.packetbooks.data.FileBookDataHolder;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("UnstableApiUsage")
public final class PacketBooks extends JavaPlugin implements Listener {

    private final AbstractBookDataHolder holder = new FileBookDataHolder(this);
    private final NamespacedKey bookIdKey = new NamespacedKey(this, "book_id");

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(this, this);
        this.holder.loadCurrentId();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    void onBookSave(final PlayerEditBookEvent event) {
        final BookMeta newBookMeta = event.getNewBookMeta();

        final PersistentDataContainer pdc = newBookMeta.getPersistentDataContainer();
        if (!pdc.has(bookIdKey, PersistentDataType.INTEGER)) {
            final int newId = this.holder.saveNewBookData(new BookData(new ArrayList<>(newBookMeta.pages())));
            pdc.set(bookIdKey, PersistentDataType.INTEGER, newId);
        } else {
            final int id = Objects.requireNonNull(pdc.get(bookIdKey, PersistentDataType.INTEGER));
            this.holder.updateBookData(id, new BookData(new ArrayList<>(newBookMeta.pages())));
        }

        event.setNewBookMeta(newBookMeta);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    void onClose(final InventoryCloseEvent event) {
        getServer().getScheduler().runTask(this, () -> {
            for (int slot = 0; slot < 9; slot++) {
                tryPopulateBookContents(event.getPlayer().getInventory().getItem(slot));
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    void onOpen(final InventoryOpenEvent event) {
        getServer().getScheduler().runTask(this, () -> {
            for (final ItemStack is : event.getView().getTopInventory().getContents()) {
                tryClearBookContents(is);
            }

            for (final ItemStack is : event.getView().getBottomInventory()) {
                tryClearBookContents(is);
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    void onBookMoveInPlayerInventory(final PlayerInventorySlotChangeEvent event) {
        if (event.getPlayer().getOpenInventory().getTopInventory().getType() != InventoryType.CRAFTING) {
            return;
        }

        final boolean hotbarSlot = 0 <= event.getSlot() && event.getSlot() < 9;
        final boolean offhandSlot = event.getSlot() == 40;

        getServer().getScheduler().runTask(this, () -> {
            final ItemStack item = event.getPlayer().getInventory().getItem(event.getSlot());
            if (hotbarSlot || offhandSlot) {
                tryPopulateBookContents(item);
            } else {
                tryClearBookContents(item);
            }
        });
    }

    private void tryPopulateBookContents(final @Nullable ItemStack item) {
        if (item != null && (item.getType() == Material.WRITABLE_BOOK || item.getType() == Material.WRITTEN_BOOK)) {
            populateBookContents(item);
        }
    }

    private void tryClearBookContents(final @Nullable ItemStack item) {
        if (item != null && (item.getType() == Material.WRITABLE_BOOK || item.getType() == Material.WRITTEN_BOOK)) {
            clearBookContents(item);
        }
    }

    private void clearBookContents(final ItemStack book) {
        book.editPersistentDataContainer(pdc -> {
            final List<Component> components = new ArrayList<>();

            if (book.hasData(DataComponentTypes.WRITTEN_BOOK_CONTENT)) {
                final WrittenBookContent content = book.getData(DataComponentTypes.WRITTEN_BOOK_CONTENT);
                Preconditions.checkNotNull(content);

                for (final Filtered<Component> componentFiltered : content.pages()) {
                    components.add(componentFiltered.raw());
                }

                final WrittenBookContent empty = WrittenBookContent.writtenBookContent(content.title(), content.author())
                    .generation(content.generation())
                    .resolved(content.resolved())
                    .build();

                book.setData(DataComponentTypes.WRITTEN_BOOK_CONTENT, empty);
            } else if (book.hasData(DataComponentTypes.WRITABLE_BOOK_CONTENT)) {
                final WritableBookContent content = book.getData(DataComponentTypes.WRITABLE_BOOK_CONTENT);
                Preconditions.checkNotNull(content);

                for (final Filtered<String> componentFiltered : content.pages()) {
                    components.add(PlainTextComponentSerializer.plainText().deserialize(componentFiltered.raw()));
                }

                final WritableBookContent empty = WritableBookContent.writeableBookContent().build();
                book.setData(DataComponentTypes.WRITABLE_BOOK_CONTENT, empty);
            }

            if (!pdc.has(bookIdKey, PersistentDataType.INTEGER)) {
                // No ID set, meaning first save the book
                final int id = this.holder.saveNewBookData(new BookData(components));
                pdc.set(bookIdKey, PersistentDataType.INTEGER, id);
            }
        });

        getSLF4JLogger().debug("[clear] book contents for {}", book);
    }

    private void populateBookContents(final ItemStack book) {
        final Integer id = book.getPersistentDataContainer().get(bookIdKey, PersistentDataType.INTEGER);
        if (id == null) {
            getSLF4JLogger().debug("[populate] no id set for book {}", book);
            return;
        }

        final BookData data = this.holder.getBookData(id);
        if (data == null) {
            getSLF4JLogger().debug("[populate] no data found for book {} with id {}", book, id);
            return;
        }

        if (book.hasData(DataComponentTypes.WRITTEN_BOOK_CONTENT)) {
            final WrittenBookContent empty = book.getData(DataComponentTypes.WRITTEN_BOOK_CONTENT);
            Preconditions.checkNotNull(empty);

            final WrittenBookContent original = WrittenBookContent.writtenBookContent(empty.title(), empty.author())
                .addPages(data.components())
                .generation(empty.generation())
                .resolved(empty.resolved())
                .build();
            book.setData(DataComponentTypes.WRITTEN_BOOK_CONTENT, original);
        } else if (book.hasData(DataComponentTypes.WRITABLE_BOOK_CONTENT)) {
            final WritableBookContent empty = book.getData(DataComponentTypes.WRITABLE_BOOK_CONTENT);
            Preconditions.checkNotNull(empty);

            final List<String> plainText = data.components().stream()
                .map(PlainTextComponentSerializer.plainText()::serialize)
                .toList();

            final WritableBookContent original = WritableBookContent.writeableBookContent()
                .addPages(plainText)
                .build();
            book.setData(DataComponentTypes.WRITABLE_BOOK_CONTENT, original);
        }

        getSLF4JLogger().debug("[populate] book contents for {}", book);
    }
}