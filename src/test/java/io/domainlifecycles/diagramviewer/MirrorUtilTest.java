package io.domainlifecycles.diagramviewer;

import io.domainlifecycles.diagramviewer.util.MirrorUtils;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;


public class MirrorUtilTest {

    @Test
    public void loadClasses()  {
        MirrorUtils.initializeMirrorFromJar(Path.of("targets", "rezeption-0e.w0.1-SNAPSOT.jar"));

    }


}
