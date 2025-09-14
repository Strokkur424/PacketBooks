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

import net.strokkur.packetbooks.PacketBooks;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

public class UndoModeListener extends AbstractModeListener {

  public UndoModeListener(final PacketBooks plugin) {
    super(plugin);

    for (final Player player : plugin.getServer().getOnlinePlayers()) {
      populateInventory(player.getInventory());
    }
  }

  @Override
  public String getName() {
    return "undo";
  }

  @EventHandler
  void onPickupItem(final EntityPickupItemEvent event) {
    final ItemStack item = event.getItem().getItemStack();
    tryPopulateBookContents(item);
    event.getItem().setItemStack(item);
  }

  @EventHandler
  void onPlayerJoin(final PlayerJoinEvent event) {
    populateInventory(event.getPlayer().getInventory());
  }

  @EventHandler
  void onInventoryOpen(final InventoryOpenEvent event) {
    populateInventory(event.getInventory());
  }
}
