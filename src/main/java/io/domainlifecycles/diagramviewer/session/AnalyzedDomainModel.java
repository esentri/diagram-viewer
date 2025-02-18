package io.domainlifecycles.diagramviewer.session;

import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import io.domainlifecycles.diagramviewer.files.DirectoryWatcher;
import io.domainlifecycles.diagramviewer.kroki.FileType;
import io.domainlifecycles.diagramviewer.kroki.KrokiClient;
import io.domainlifecycles.diagramviewer.model.DiagramConfiguration;
import io.domainlifecycles.diagramviewer.util.DiagrammerUtils;
import io.domainlifecycles.diagramviewer.util.DomainModelUtils;
import io.domainlifecycles.diagramviewer.util.FileIOUtils;
import io.domainlifecycles.mirror.api.DomainModel;
import io.domainlifecycles.mirror.serialize.api.JacksonDomainSerializer;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

/**
 * Represents one project/diagram context.
 */
@SpringComponent
@VaadinSessionScope
public class AnalyzedDomainModel {

    private static final Logger log = LoggerFactory.getLogger(AnalyzedDomainModel.class);

    private final KrokiClient krokiClient;

    private DiagramConfiguration diagramConfiguration;

    private String diagramDirectory;
    private String initialTargetsDirectory;


    //TODO über DomainModelDialog steuern
    private String targetsDirectory;
    //Jar File per Komponente wählen bzw. komplettes Verzeichnis wählen per https://github.com/vaadin-component-factory/directory-upload
    private Path domainModelJarPath;
    private DomainModel domainModel;
    private String domainModelNomNoml;
    private byte[] domainModelSvg;

    //TODO über DomainModelDialog steuern
    private String shownContextPackage = "com.esentri";
    //TODO über DomainModelDialog steuern
    private List<String> seedClassNames;
    //TODO über DomainModelDialog steuern
    private List<String> analyzedDomainModelPackages = List.of("com.esentri");
    private DirectoryWatcher jarDirectoryWatcher;

    public AnalyzedDomainModel(
            @Value("${diagrams.location}")String diagramDirectory,
            @Value("${targets.location}")String initialTargetsDirectory,
            KrokiClient krokiClient) {
        this.diagramDirectory = diagramDirectory;
        this.initialTargetsDirectory = initialTargetsDirectory;
        this.krokiClient = krokiClient;
        this.diagramConfiguration = new DiagramConfiguration();
        setTargetsDirectory(this.initialTargetsDirectory);
    }

    private void setTargetsDirectory(String targetsDirectory) {
        this.targetsDirectory = targetsDirectory;
        if(jarDirectoryWatcher != null){
            jarDirectoryWatcher.stop();
        }
        initTargets();
        jarDirectoryWatcher = DirectoryWatcher.onDirectoryChange(Path.of(targetsDirectory),
            (evt) -> setDomainModelJarPath(Path.of(targetsDirectory,evt.context().toString())));
    }

    private void initTargets(){
        if(targetsDirectory != null){
            File dir = new File(targetsDirectory);
            if(dir.exists()){
                var files = dir.listFiles();
                if(files.length > 0){
                    setDomainModelJarPath(files[0].toPath());
                }
                if(files.length > 1){
                    log.warn("Only first target initialized currently!");
                }
            }
        }
    }

    public void setAnalyzedDomainModelPackages(List<String> analyzedDomainModelPackages) {
        this.analyzedDomainModelPackages = analyzedDomainModelPackages;
        if(analyzedDomainModelPackages != null){
            setDomainModelJarPath(this.domainModelJarPath);
        }
    }

    private void setDomainModelJarPath(Path domainModelJarPath) {
        this.domainModelJarPath = domainModelJarPath;
        if(domainModelJarPath != null){
            setDomainModel(
                DomainModelUtils.initializeDomainModelFromJar(
                    domainModelJarPath, analyzedDomainModelPackages.toArray(String[]::new))
            );
        }
    }

    private void setDomainModel(DomainModel domainModel) {
        this.domainModel = domainModel;
        if(domainModel != null){
            var ser = new JacksonDomainSerializer(true);
            var val = ser.serialize(domainModel);
            log.debug("DomainModel:\\n"+ val);
            generateNomnomlAndSvg();
        }
    }

    public void setShownContextPackage(String shownContextPackage) {
        this.shownContextPackage = Objects.requireNonNull(shownContextPackage);
        generateNomnomlAndSvg();
    }

    public void setSeedClassNames(List<String> seedClassNames) {
        this.seedClassNames = seedClassNames;
        generateNomnomlAndSvg();
    }

    private void generateNomnomlAndSvg(){
        this.domainModelNomNoml = DiagrammerUtils.generateNomnoml(domainModel, shownContextPackage, seedClassNames);
        this.domainModelSvg = krokiClient.convertTo(domainModelNomNoml, FileType.SVG);
        try {
            FileIOUtils.saveFile(diagramDirectory, new ByteArrayInputStream(domainModelSvg), "currentJar.svg");
        } catch (IOException e) {
            throw DiagramViewerException.fail(String.format("Could not save diagram to '%s'.", diagramDirectory), e);
        }
    }

    // Getters
    public DomainModel getDomainModel() {
        return domainModel;
    }

    public String getDomainModelNomNoml() {
        return domainModelNomNoml;
    }

    public byte[] getDomainModelSvg() {
        return domainModelSvg;
    }

    public String getShownContextPackage() {
        return shownContextPackage;
    }

    public List<String> getSeedClassNames() {
        return seedClassNames;
    }

    public String getTargetsDirectory() {
        return targetsDirectory;
    }

    public DiagramConfiguration getDiagramConfiguration() {
        return diagramConfiguration;
    }
}
