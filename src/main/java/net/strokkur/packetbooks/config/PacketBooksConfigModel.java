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
package net.strokkur.packetbooks.config;

import net.strokkur.config.Format;
import net.strokkur.config.annotations.ConfigFilePath;
import net.strokkur.config.annotations.ConfigFormat;
import net.strokkur.config.annotations.CustomDeserializer;
import net.strokkur.config.annotations.CustomParse;
import net.strokkur.config.annotations.CustomSerializer;
import net.strokkur.config.annotations.GenerateConfig;
import net.strokkur.packetbooks.PacketBooks;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.StringReader;

@GenerateConfig
@ConfigFilePath("config.yml")
@ConfigFormat(Format.CUSTOM)
class PacketBooksConfigModel {

  @CustomParse("parseMode")
  String mode = "STANDARD";

  PluginMode parseMode(String mode) {
    if (mode.equalsIgnoreCase("undo")) {
      return PluginMode.UNDO;
    }

    if (mode.equalsIgnoreCase("disable")) {
      return PluginMode.DISABLE;
    }

    final PacketBooks plugin = PacketBooks.getPlugin(PacketBooks.class);
    if (!mode.equalsIgnoreCase("standard") && !plugin.hasSendDefaultFallback()) {
      plugin.getSLF4JLogger().warn("({}) The mode has been set to {}, which is not a valid mode. Defaulting to STANDARD.", PacketBooksConfig.FILE_PATH, mode);
      plugin.setHasSendDefaultFallback(true);
    }

    return PluginMode.STANDARD;
  }

  @CustomSerializer
  static String serialize(final PacketBooksConfigModel model) {
    return "mode: " + model.mode;
  }

  @CustomDeserializer
  static PacketBooksConfigModel deserialize(final String serialized) {
    final PacketBooksConfigModel out = new PacketBooksConfigModel();
    out.mode = YamlConfiguration.loadConfiguration(new StringReader(serialized)).getString("mode", out.mode);
    return out;
  }
}
