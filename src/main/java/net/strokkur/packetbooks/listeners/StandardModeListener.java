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

import io.papermc.paper.event.player.PlayerInventorySlotChangeEvent;
import net.strokkur.packetbooks.PacketBooks;
import net.strokkur.packetbooks.data.BookData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Objects;

public class StandardModeListener extends AbstractModeListener {

  public StandardModeListener(final PacketBooks plugin) {
    super(plugin);

    for (final Player player : plugin.getServer().getOnlinePlayers()) {
      if (player.getOpenInventory().getType() == InventoryType.CRAFTING) {

        final @Nullable ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0, contentsLength = contents.length; i < contentsLength; i++) {
          final ItemStack itemStack = contents[i];
          if (itemStack == null || itemStack.isEmpty()) {
            continue;
          }

          if (isDirectlyAccessibleSlot(i)) {
            tryPopulateBookContents(itemStack);
          } else {
            tryClearBookContents(itemStack);
          }
        }
      }
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  void onBookSave(final PlayerEditBookEvent event) {
    final BookMeta newBookMeta = event.getNewBookMeta();

    final PersistentDataContainer pdc = newBookMeta.getPersistentDataContainer();
    if (!pdc.has(plugin.getBookIdKey(), PersistentDataType.INTEGER)) {
      final int newId = this.plugin.getHolder().saveNewBookData(new BookData(new ArrayList<>(newBookMeta.pages())));
      pdc.set(plugin.getBookIdKey(), PersistentDataType.INTEGER, newId);
    } else {
      final int id = Objects.requireNonNull(pdc.get(plugin.getBookIdKey(), PersistentDataType.INTEGER));
      plugin.getHolder().updateBookData(id, new BookData(new ArrayList<>(newBookMeta.pages())));
    }

    event.setNewBookMeta(newBookMeta);
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  void onClose(final InventoryCloseEvent event) {
    plugin.getServer().getScheduler().runTask(plugin, () -> {
      for (int slot = 0; slot < 9; slot++) {
        tryPopulateBookContents(event.getPlayer().getInventory().getItem(slot));
      }
    });
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  void onOpen(final InventoryOpenEvent event) {
    plugin.getServer().getScheduler().runTask(plugin, () -> {
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

    plugin.getServer().getScheduler().runTask(plugin, () -> {
      final ItemStack item = event.getPlayer().getInventory().getItem(event.getSlot());
      if (isDirectlyAccessibleSlot(event.getSlot())) {
        tryPopulateBookContents(item);
      } else {
        tryClearBookContents(item);
      }
    });
  }
}
