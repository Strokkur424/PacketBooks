package net.strokkur.packetbooks;

import io.papermc.paper.event.player.PlayerInventorySlotChangeEvent;
import net.strokkur.packetbooks.data.AbstractBookDataHolder;
import net.strokkur.packetbooks.data.BookData;
import net.strokkur.packetbooks.data.FileBookDataHolder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    void onBookMove(final PlayerInventorySlotChangeEvent event) {
        final boolean mainSlot = event.getPlayer().getInventory().getHeldItemSlot() == event.getSlot();
        final boolean offhandSlot = event.getSlot() == 40;

        Bukkit.getScheduler().runTask(this, () -> {
            final ItemStack item = event.getPlayer().getInventory().getItem(event.getSlot());
            if (mainSlot || offhandSlot) {
                tryPopulateBookContents(item);
            } else {
                tryClearBookContents(item);
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    void onSlotChange(final PlayerItemHeldEvent event) {
        tryClearBookContents(event.getPlayer().getInventory().getItem(event.getPreviousSlot()));
        tryPopulateBookContents(event.getPlayer().getInventory().getItem(event.getNewSlot()));
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
        //noinspection ResultOfMethodCallIgnored
        book.editMeta(BookMeta.class, meta -> meta.pages(List.of()));
        getSLF4JLogger().info("[clear] book contents for {}", book);
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

        //noinspection ResultOfMethodCallIgnored
        book.editMeta(BookMeta.class, meta -> meta.pages(data.components()));
        getSLF4JLogger().debug("[populate] book contents for {}", book);
    }
}