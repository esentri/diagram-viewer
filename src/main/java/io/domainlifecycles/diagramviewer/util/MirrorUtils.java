package io.domainlifecycles.diagramviewer.util;

import io.domainlifecycles.mirror.api.Domain;
import io.domainlifecycles.mirror.reflect.ReflectiveDomainMirrorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class MirrorUtils {

    private final static Logger log = LoggerFactory.getLogger(MirrorUtils.class);

    public static void initializeMirrorFromJar(Path path, String... boundedContextPackages){
        URL url = null;
        try{
            url = path.toUri().toURL();
        } catch (MalformedURLException e) {
            log.error(e.getMessage(), e);
        }
        if(url != null) {
            var cl = subClassLoader(List.of(url));
            if (cl.isPresent()) {
                log.info("Classes loaded - Initializing domain mirror");
                final ReflectiveDomainMirrorFactory domainMirrorFactory = new ReflectiveDomainMirrorFactory(cl.get(), boundedContextPackages);
                Domain.initialize(domainMirrorFactory);
                log.info("Domain mirror initialized");
                log.info("Mirrored types count = " + Domain.getInitializedDomain().allTypeMirrors().size());
            }
        }
    }

    private static Optional<ClassLoader> subClassLoader(final List<URL> filesToAddToClasspath) {
        if(filesToAddToClasspath == null) return Optional.empty();
        try {
            URLClassLoader childClassLoader = new URLClassLoader(filesToAddToClasspath.toArray(URL[]::new), MirrorUtils.class.getClassLoader());
            return Optional.of(childClassLoader);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Optional.empty();
        }
    }

}
