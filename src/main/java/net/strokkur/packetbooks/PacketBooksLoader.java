package net.strokkur.packetbooks;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;

@SuppressWarnings("UnstableApiUsage")
public class PacketBooksLoader implements PluginLoader, Versions {

    @Override
    public void classloader(final PluginClasspathBuilder classpathBuilder) {
        final MavenLibraryResolver centralResolver = new MavenLibraryResolver();
        centralResolver.addRepository(new RemoteRepository.Builder("Central Repository", "default", MavenLibraryResolver.MAVEN_CENTRAL_DEFAULT_MIRROR).build());
        centralResolver.addDependency(new Dependency(new DefaultArtifact("com.github.ben-manes.caffeine:caffeine:" + VER_CAFFEINE), null));
        classpathBuilder.addLibrary(centralResolver);
    }
}
