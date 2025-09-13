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
package net.strokkur.packetbooks.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.strokkur.commands.annotations.Command;
import net.strokkur.commands.annotations.Executes;
import net.strokkur.commands.annotations.Permission;
import net.strokkur.packetbooks.PacketBooks;
import net.strokkur.packetbooks.config.PacketBooksConfig;
import org.bukkit.command.CommandSender;

import java.io.IOException;

@Command("packetbooks")
@SuppressWarnings("UnstableApiUsage")
class PacketBooksCommand {

  private final PacketBooks plugin = PacketBooks.getPlugin(PacketBooks.class);
  private final Component prefix = MiniMessage.miniMessage().deserialize("<gradient:aqua:light_purple><b>PacketBooks</gradient>");

  @Executes
  @Permission("packetbooks.command.use")
  void printInformation(CommandSender sender) {
    sender.sendRichMessage("""
            <br><gradient:aqua:light_purple><strikethrough>                             </strikethrough> PacketBooks <strikethrough>                              </gradient>
            
              <transition:aqua:light_purple:0.5>/packetbooks <dark_gray>—</dark_gray> <white>Print this message.</transition>
              <transition:aqua:light_purple:0.5>/packetbooks reload <dark_gray>—</dark_gray> <white>Reload the config.</transition>
              <transition:aqua:light_purple:0.5>/packetbooks version <dark_gray>—</dark_gray> <white>Display version information.</transition>
            
            <gradient:aqua:light_purple><strikethrough>                     </strikethrough> Made with ❤ by Strokkur24 <strikethrough>                     </gradient><br>""",
        Placeholder.parsed("version", plugin.getPluginMeta().getVersion())
    );
  }

  @Executes("reload")
  @Permission("packetbooks.command.reload")
  void reload(CommandSender sender) {
    try {
      plugin.reloadPlugin();
      sender.sendRichMessage("<prefix> <green>Successfully reloaded PacketBooks!", Placeholder.component("prefix", prefix));
    } catch (IOException e) {
      sender.sendRichMessage("<prefix> <red>Something went wrong. See the console for more details.", Placeholder.component("prefix", prefix));
      plugin.getSLF4JLogger().error("An error occurred reloading config file '{}'", PacketBooksConfig.FILE_PATH, e);
    }
  }

  @Executes("version")
  @Permission("packetbooks.command.version")
  void version(CommandSender sender) {
    sender.sendRichMessage("<prefix> <transition:aqua:light_purple:0.5>Version <white><version>",
        Placeholder.component("prefix", prefix),
        Placeholder.parsed("version", plugin.getPluginMeta().getVersion())
    );
  }
}
