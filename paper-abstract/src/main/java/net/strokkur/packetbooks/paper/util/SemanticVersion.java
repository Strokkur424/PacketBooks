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
package net.strokkur.packetbooks.paper.util;

import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.IntStream;

public class SemanticVersion implements Comparable<SemanticVersion> {
  private final int[] numberComponents;

  public static SemanticVersion getVersion(final String versionString) {
    return Objects.requireNonNull(getVersionSafe(versionString));
  }

  public static @Nullable SemanticVersion getVersionSafe(final String versionString) {
    final SemanticVersion instance = new SemanticVersion(versionString);
    if (instance.numberComponents.length == 0) {
      return null;
    }
    return instance;
  }

  private SemanticVersion(final String versionString) {
    final String[] split = versionString.split("\\.");
    final IntStream.Builder builder = IntStream.builder();

    for (final String component : split) {
      try {
        builder.add(Integer.parseInt(component));
      } catch (final NumberFormatException notANumber) {
        // We've reached the non-number part
        break;
      }
    }

    this.numberComponents = builder.build().toArray();
  }

  @Override
  public String toString() {
    return String.join(".", Arrays.stream(this.numberComponents).mapToObj(Integer::toString).toList());
  }

  @Override
  public int compareTo(final SemanticVersion other) {
    final int min = Integer.min(this.numberComponents.length, other.numberComponents.length);

    for (int i = 0; i < min; i++) {
      int compared = Integer.compare(this.numberComponents[i], other.numberComponents[i]);
      if (compared != 0) {
        return compared;
      }
    }

    return Integer.compare(this.numberComponents.length, other.numberComponents.length);
  }
}
