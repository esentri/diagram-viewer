/*
 *     ________  .__
 *     \______ \ |__|____     ________________    _____
 *      |    |  \|  \__  \   / ___\_  __ \__  \  /     \
 *      |    |   \  |/ __ \_/ /_/  >  | \// __ \|  Y Y  \
 *     /_______  /__(____  /\___  /|__|  (____  /__|_|  /
 *             \/        \//_____/            \/      \/
 *     ____   ____.__
 *     \   \ /   /|__| ______  _  __ ___________
 *      \   Y   / |  |/ __ \ \/ \/ // __ \_  __ \
 *       \     /  |  \  ___/\     /\  ___/|  | \/
 *        \___/   |__|\___  >\/\_/  \___  >__|
 *                        \/            \/
 *
 *  Copyright 2025-2026 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.domainlifecycles.diagramviewer.util;

import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.values.UploadFileType;
import io.domainlifecycles.mirror.api.DomainMirror;
import io.domainlifecycles.mirror.api.DomainType;
import io.domainlifecycles.mirror.api.DomainTypeMirror;
import io.domainlifecycles.mirror.exception.MirrorException;
import io.domainlifecycles.mirror.reflect.ReflectiveDomainMirrorFactory;
import io.domainlifecycles.mirror.resolver.TypeMetaResolver;
import io.domainlifecycles.mirror.serialize.DomainSerializer;
import io.domainlifecycles.mirror.serialize.jackson2.JacksonDomainSerializer;
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

    public static final String DOMAINLIFECYCLES_PACKAGE_NAME = "io.domainlifecycles";

    public static String nameWithStereoType(DomainTypeMirror mirror) {
        return mirror.getTypeName().substring(mirror.getTypeName().lastIndexOf('.') + 1)
                + " <" + translateDomainType(mirror.getDomainType())+">";
    }

    public static String translateDomainType(DomainType domainType) {
        return switch (domainType) {
            case ENUM -> "Enum";
            case AGGREGATE_ROOT -> "AggregateRoot";
            case ENTITY -> "Entity";
            case IDENTITY -> "Identity";
            case READ_MODEL -> "ReadModel";
            case REPOSITORY -> "Repository";
            case DOMAIN_EVENT -> "DomainEvent";
            case SERVICE_KIND -> "Service";
            case VALUE_OBJECT -> "ValueObject";
            case QUERY_HANDLER -> "QueryHandler";
            case DOMAIN_COMMAND -> "DomainCommand";
            case OUTBOUND_SERVICE -> "OutboundService";
            case DOMAIN_SERVICE -> "DomainService";
            case APPLICATION_SERVICE -> "ApplicationService";
            default -> "Object";
        };
    }

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
        DomainSerializer domainSerializer = new JacksonDomainSerializer(false);
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
