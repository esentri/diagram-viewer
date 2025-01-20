package io.domainlifecycles.diagramviewer.rest;

import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller("/resources")
public class ResourceController {

    @Value("${diagrams.location}")
    private String diagramFolderLocation;

    @GetMapping(
        value = "/{fileName}",
        produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    public @ResponseBody byte[] getFile(@PathVariable String fileName) throws IOException {
        InputStream in = getClass().getResourceAsStream(diagramFolderLocation + "/" + fileName);

        if(in == null) {
            throw DiagramViewerException.fail(
                String.format("Could not find file %s in directory %s.", fileName, diagramFolderLocation));
        }

        return IOUtils.toByteArray(in);
    }
}
