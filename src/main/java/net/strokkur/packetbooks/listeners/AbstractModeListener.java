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
package net.strokkur.packetbooks.listeners;

import com.google.common.base.Preconditions;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.WritableBookContent;
import io.papermc.paper.datacomponent.item.WrittenBookContent;
import io.papermc.paper.text.Filtered;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.strokkur.packetbooks.PacketBooks;
import net.strokkur.packetbooks.data.BookData;
import org.bukkit.Material;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public abstract class AbstractModeListener implements Listener {

  protected final PacketBooks plugin;

  public AbstractModeListener(final PacketBooks plugin) {
    this.plugin = plugin;
  }

  protected boolean isDirectlyAccessibleSlot(int slot) {
    return 0 <= slot && slot < 9 || slot == 40;
  }

  protected void tryPopulateBookContents(final @Nullable ItemStack item) {
    if (item != null && (item.getType() == Material.WRITABLE_BOOK || item.getType() == Material.WRITTEN_BOOK)) {
      populateBookContents(item);
    }
  }

  protected void tryClearBookContents(final @Nullable ItemStack item) {
    if (item != null && (item.getType() == Material.WRITABLE_BOOK || item.getType() == Material.WRITTEN_BOOK)) {
      clearBookContents(item);
    }
  }

  protected void clearBookContents(final ItemStack book) {
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

      if (!pdc.has(plugin.getBookIdKey(), PersistentDataType.INTEGER)) {
        // No ID set, meaning first save the book
        final int id = plugin.getHolder().saveNewBookData(new BookData(components));
        pdc.set(plugin.getBookIdKey(), PersistentDataType.INTEGER, id);
      }
    });

    plugin.getSLF4JLogger().debug("[clear] book contents for {}", book);
  }

  protected void populateBookContents(final ItemStack book) {
    final Integer id = book.getPersistentDataContainer().get(plugin.getBookIdKey(), PersistentDataType.INTEGER);
    if (id == null) {
      plugin.getSLF4JLogger().debug("[populate] no id set for book {}", book);
      return;
    }

    final BookData data = plugin.getHolder().getBookData(id);
    if (data == null) {
      plugin.getSLF4JLogger().debug("[populate] no data found for book {} with id {}", book, id);
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

    plugin.getSLF4JLogger().debug("[populate] book contents for {}", book);
  }

}
