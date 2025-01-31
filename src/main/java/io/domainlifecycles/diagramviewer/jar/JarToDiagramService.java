package io.domainlifecycles.diagramviewer.jar;

import io.domainlifecycles.diagramviewer.kroki.KrokiClient;
import io.domainlifecycles.diagramviewer.util.DiagrammerUtils;
import io.domainlifecycles.diagramviewer.util.FileIOUtils;
import io.domainlifecycles.diagramviewer.util.MirrorUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.nio.file.Path;

@Service
public class JarToDiagramService {

    private final KrokiClient krokiClient;

    @Value("${diagrams.location}")
    private String diagramDirectory;

    public JarToDiagramService(KrokiClient krokiClient) {
        this.krokiClient = krokiClient;
    }

    public void createDiagramFromJar(Path jarPath, String... boundedContextPackages){
        MirrorUtils.initializeMirrorFromJar(jarPath, boundedContextPackages);
        var nomnoml = DiagrammerUtils.generateNomnoml(boundedContextPackages[0]);
        var svgBytes = krokiClient.convertNomnomlToSVG(nomnoml);
        FileIOUtils.saveFile(diagramDirectory, new ByteArrayInputStream(svgBytes), "currentJar.svg");
    }
}
