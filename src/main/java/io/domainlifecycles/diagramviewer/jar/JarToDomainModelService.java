package io.domainlifecycles.diagramviewer.jar;

import io.domainlifecycles.diagramviewer.util.DomainModelUtils;
import io.domainlifecycles.mirror.api.DomainModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.nio.file.Path;

@Service
public class JarToDomainModelService {

    private static final Logger log = LoggerFactory.getLogger(JarToDomainModelService.class);

    public DomainModel createDomainModelFromJar(Path jarPath, String... boundedContextPackages){
        log.info("Creating diagram from jar {}", jarPath);
        var dm = DomainModelUtils.initializeDomainModelFromJar(jarPath, boundedContextPackages);
        log.info("Diagram created successfully");
        return dm;
    }
}
