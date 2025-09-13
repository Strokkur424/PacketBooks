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
package net.strokkur.packetbooks;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.strokkur.packetbooks.commands.PacketBooksCommandBrigadier;
import net.strokkur.packetbooks.config.PacketBooksConfig;
import net.strokkur.packetbooks.config.PacketBooksConfigImpl;
import net.strokkur.packetbooks.data.AbstractBookDataHolder;
import net.strokkur.packetbooks.data.FileBookDataHolder;
import net.strokkur.packetbooks.listeners.AbstractModeListener;
import net.strokkur.packetbooks.listeners.StandardModeListener;
import net.strokkur.packetbooks.listeners.UndoModeListener;
import org.bukkit.NamespacedKey;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public final class PacketBooks extends JavaPlugin implements Listener {

  private final AbstractBookDataHolder holder = new FileBookDataHolder(this);
  private final NamespacedKey bookIdKey = new NamespacedKey(this, "book_id");
  private final PacketBooksConfig config = new PacketBooksConfigImpl();

  private @Nullable AbstractModeListener modeListener = null;
  private boolean hasSendDefaultFallback = false;

  @Override
  @SuppressWarnings("UnstableApiUsage")
  public void onLoad() {
    this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS.newHandler(
        event -> PacketBooksCommandBrigadier.register(event.registrar())
    ));
  }

  @Override
  public void onEnable() {
    this.holder.loadCurrentId();

    try {
      reloadPlugin();
    } catch (IOException e) {
      getSLF4JLogger().error("A fatal exception occurred while trying to enable PacketBooks, it might not work as expected.", e);
    }
  }

  public void reloadPlugin() throws IOException {
    hasSendDefaultFallback = false;
    config.reload(this);

    if (modeListener != null) {
      HandlerList.unregisterAll(modeListener);
    }

    modeListener = switch (config.mode()) {
      case STANDARD -> new StandardModeListener(this);
      case UNDO -> new UndoModeListener(this);
    };
    getServer().getPluginManager().registerEvents(modeListener, this);
  }

  public AbstractBookDataHolder getHolder() {
    return holder;
  }

  public NamespacedKey getBookIdKey() {
    return bookIdKey;
  }

  public boolean hasSendDefaultFallback() {
    return hasSendDefaultFallback;
  }

  public void setHasSendDefaultFallback(final boolean hasSendDefaultFallback) {
    this.hasSendDefaultFallback = hasSendDefaultFallback;
  }
}