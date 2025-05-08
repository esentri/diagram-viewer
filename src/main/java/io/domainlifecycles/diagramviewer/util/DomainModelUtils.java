package io.domainlifecycles.diagramviewer.util;

import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import io.domainlifecycles.mirror.api.DomainMirror;
import io.domainlifecycles.mirror.reflect.ReflectiveDomainMirrorFactory;
import io.domainlifecycles.mirror.resolver.TypeMetaResolver;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DomainModelUtils {

    private final static Logger LOGGER = LoggerFactory.getLogger(DomainModelUtils.class);

    public static DomainMirror initializeDomainMirrorFromJar(
            Path path,
            Set<String> domainModelPackages){
        if(domainModelPackages == null || domainModelPackages.isEmpty()) {
            throw DiagramViewerException.fail("Domain model packages is null or empty!");
        }
        URL url = null;
        try{
            url = path.toUri().toURL();
        } catch (MalformedURLException e) {
            LOGGER.error(e.getMessage(), e);
        }
        if(url != null) {
            var cl = subClassLoader(List.of(url));
            if (cl.isPresent()) {
                LOGGER.info("Classes loaded - Initializing domain model");
                final ReflectiveDomainMirrorFactory domainModelFactory = new ReflectiveDomainMirrorFactory(domainModelPackages.toArray(String[]::new));
                domainModelFactory.setGenericTypeResolver(new TypeMetaResolver());
                domainModelFactory.setExternalClassLoader(cl.get());

                var dm = domainModelFactory.initializeDomainMirror();
                LOGGER.info("Domain model initialized");
                LOGGER.debug("Mirrored types count = " + dm.getAllDomainTypeMirrors().size());
                return dm;
            }
        }
        throw DiagramViewerException.fail("Domain model could not be initialized!");
    }

    private static Optional<ClassLoader> subClassLoader(final List<URL> filesToAddToClasspath) {
        if(filesToAddToClasspath == null) return Optional.empty();
        try {
            URLClassLoader childClassLoader = new URLClassLoader(filesToAddToClasspath.toArray(URL[]::new), DomainModelUtils.class.getClassLoader());
            return Optional.of(childClassLoader);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return Optional.empty();
        }
    }
}