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
package net.strokkur.packetbooks.paper;

import io.papermc.paper.ServerBuildInfo;
import net.strokkur.packetbooks.paper.util.SemanticVersion;
import org.slf4j.Logger;

import java.lang.reflect.Constructor;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SequencedMap;
import java.util.SequencedSet;

public abstract class AbstractPaperProvider {
  public static final SequencedMap<SemanticVersion, String> VERSION_CLASS_NAMES = new LinkedHashMap<>(Map.of(
    SemanticVersion.getVersion("1.21.6"), "net.strokkur.packetbooks.paper_1_21_6.Paper_1_21_6"
  ));

  public static AbstractPaperProvider getProvider(final Logger logger) throws IllegalStateException, ReflectiveOperationException {
    if (VERSION_CLASS_NAMES.isEmpty()) {
      logger.error("A fatal exception has occurred: VERSION_CLASS_NAMES is empty.");
      throw new IllegalStateException("No version implementations found!");
    }

    final SequencedSet<Map.Entry<SemanticVersion, String>> entrySet = VERSION_CLASS_NAMES.sequencedEntrySet();

    final SemanticVersion serverVersion = SemanticVersion.getVersionSafe(ServerBuildInfo.buildInfo().minecraftVersionId());
    if (serverVersion == null) {
      logger.warn("Version {} could not be identified correctly. Defaulting to latest version ({}).",
        ServerBuildInfo.buildInfo().minecraftVersionName(),
        entrySet.getFirst().getKey()
      );
      return construct(entrySet.getFirst().getValue());
    }

    if (serverVersion.compareTo(entrySet.getFirst().getKey()) > 0) {
      logger.warn("Current server version is above the latest NMS implementation. Unless the plugin starts throwing errors, this is NOT a problem.");
      logger.warn("Server version: {}. Running with implementation for: {}.",
        ServerBuildInfo.buildInfo().minecraftVersionName(),
        entrySet.getFirst().getKey()
      );
      return construct(entrySet.getFirst().getValue());
    }

    for (final Map.Entry<SemanticVersion, String> entry : entrySet) {
      int compared = serverVersion.compareTo(entry.getKey());
      if (compared >= 0) {
        logger.info("Successfully found implementation for version {} ({}).",
          ServerBuildInfo.buildInfo().minecraftVersionName(),
          entry.getKey()
        );
        return construct(entrySet.getFirst().getValue());
      }
    }

    logger.error("Server version {} is below the required version {}. This plugin will not load.",
      ServerBuildInfo.buildInfo().minecraftVersionName(),
      entrySet.getFirst().getKey()
    );
    throw new IllegalStateException("Invalid server version.");
  }

  private static AbstractPaperProvider construct(final String className) throws ReflectiveOperationException {
    final Class<?> clazz = Class.forName(className);
    final Constructor<?> ctor = clazz.getConstructor();
    return (AbstractPaperProvider) ctor.newInstance();
  }
}
