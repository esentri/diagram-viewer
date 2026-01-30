package io.domainlifecycles.diagramviewer.util;

import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.values.UploadFileType;
import io.domainlifecycles.mirror.api.DomainMirror;
import io.domainlifecycles.mirror.exception.MirrorException;
import io.domainlifecycles.mirror.reflect.ReflectiveDomainMirrorFactory;
import io.domainlifecycles.mirror.resolver.TypeMetaResolver;
import io.domainlifecycles.mirror.serialize.DomainSerializer;
import io.domainlifecycles.mirror.serialize.Jackson3DomainSerializer;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DomainModelUtils {

    private final static Logger LOGGER = LoggerFactory.getLogger(DomainModelUtils.class);

    public static DomainMirror initializeDomainMirrorFromFile(Path pathToFile, Set<String> domainModelPackages, UploadFileType uploadFileType) {
        if (pathToFile == null || !Files.exists(pathToFile)) {
            throw DiagramViewerException.fail("Could not initialize domain model. Path to file is null or invalid.");
        }

        switch (uploadFileType) {
            case JAR -> {
                return initializeDomainMirrorFromJar(pathToFile, domainModelPackages);
            }
            case JSON -> {
                return initializeDomainMirrorFromJson(pathToFile);
            }
            default -> throw DiagramViewerException.fail("Could not initialize domain model. Unsupported file type.");
        }
    }

    private static DomainMirror initializeDomainMirrorFromJson(Path pathToFile) {
        try {
            return serializeDomainMirror(Files.readString(pathToFile));
        } catch (IOException e) {
            throw DiagramViewerException.fail(
                String.format("Could not read file at '%s'.", pathToFile.toAbsolutePath()), e);
        }
    }

    private static DomainMirror initializeDomainMirrorFromJar(Path pathToJarFile, Set<String> domainModelPackages) {
        checkDomainModelPackagesEmpty(domainModelPackages);
        URL url = null;

        try {
            url = pathToJarFile.toUri().toURL();
        } catch (MalformedURLException e) {
            LOGGER.error(e.getMessage(), e);
        }

        if (url == null) {
            throw DiagramViewerException.fail(
                "Could not initialize domain model. Path to jar file is null or invalid.");
        }

        Optional<ClassLoader> cl = subClassLoader(List.of(url));

        if (cl.isPresent()) {
            LOGGER.info("Classes loaded - Initializing domain model");
            final ReflectiveDomainMirrorFactory domainModelFactory = new ReflectiveDomainMirrorFactory(domainModelPackages.toArray(new String[0]));
            domainModelFactory.setGenericTypeResolver(new TypeMetaResolver());
            domainModelFactory.setExternalClassLoader(cl.get());

            try {
                var dm = domainModelFactory.initializeDomainMirror();
                LOGGER.info("Domain model initialized");
                LOGGER.debug("Mirrored types count = {}", dm.getAllDomainTypeMirrors().size());
                return dm;
            } catch (MirrorException e) {
                throw DiagramViewerException.fail("Domain model could not be initialized. Please check whether you " +
                    "specified correct and all packages needed to read the Domain model. It is necessary to specify " +
                    "referenced packages as well.");
            }
        }
        throw DiagramViewerException.fail("Domain model could not be initialized!");
    }

    private static DomainMirror serializeDomainMirror(String domainMirrorJson) {
        DomainSerializer domainSerializer = new Jackson3DomainSerializer(false);
        return domainSerializer.deserialize(domainMirrorJson);
    }

    private static Optional<ClassLoader> subClassLoader(final List<URL> filesToAddToClasspath) {
        if (filesToAddToClasspath == null) return Optional.empty();
        try {
            URLClassLoader childClassLoader = new URLClassLoader(filesToAddToClasspath.toArray(URL[]::new),
                DomainModelUtils.class.getClassLoader());
            return Optional.of(childClassLoader);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return Optional.empty();
        }
    }

    private static void checkDomainModelPackagesEmpty(Set<String> domainModelPackages) {
        if(domainModelPackages == null || domainModelPackages.isEmpty()) {
            throw DiagramViewerException.fail("Domain model packages is null or empty!");
        }
    }
}