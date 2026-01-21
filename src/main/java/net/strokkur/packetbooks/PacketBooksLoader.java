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

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;

import java.nio.file.Files;
import java.util.Objects;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

@SuppressWarnings({"UnstableApiUsage", "SameParameterValue"})
public class PacketBooksLoader implements PluginLoader {
  @Override
  public void classloader(final PluginClasspathBuilder builder) {
    final MavenLibraryResolver resolver = new MavenLibraryResolver();
    this.addRepository(resolver, MavenLibraryResolver.MAVEN_CENTRAL_DEFAULT_MIRROR);
    this.addDependency(builder, resolver, "com.github.ben-manes.caffeine:caffeine", "Caffeine");
    builder.addLibrary(resolver);
  }

  private void addRepository(final MavenLibraryResolver resolver, final String repository) {
    resolver.addRepository(new RemoteRepository.Builder(null, "default", repository).build());
  }

  private void addDependency(final PluginClasspathBuilder builder, final MavenLibraryResolver resolver, final String group, final String version) {
    resolver.addDependency(new Dependency(new DefaultArtifact(group + ":" + this.getVersion(builder, version)), null));
  }

  @SuppressWarnings("SameParameterValue")
  private String getVersion(final PluginClasspathBuilder builder, final String libraryId) {
    try {
      final JarInputStream jarStream = new JarInputStream(Files.newInputStream(builder.getContext().getPluginSource()));
      final Manifest manifest = Objects.requireNonNull(jarStream.getManifest());
      return Objects.requireNonNull(manifest.getMainAttributes().getValue(libraryId + "-Version"));
    } catch (final Exception ex) {
      throw new RuntimeException("Failed to read version for %s from manifest file".formatted(libraryId), ex);
    }
  }
}
