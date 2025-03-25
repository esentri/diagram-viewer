package io.domainlifecycles.diagramviewer.util;

import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileIOUtils {

    public static Set<File> getFilesInDirectory(Path directory) {
        File directoryFile = new File(directory.toUri());

        if(!directoryFile.isDirectory() ||  directoryFile.listFiles() == null) {
            return Collections.emptySet();
        }

        return Stream.of(Objects.requireNonNull(directoryFile.listFiles()))
            .filter(file -> !file.isDirectory())
            .collect(Collectors.toSet());
    }

    public static void saveFile(String locationPath, String filenameIncludingSuffix, InputStream inputStream) throws IOException {
        final Path filePath = Path.of(locationPath, filenameIncludingSuffix);
        saveFile(filePath, inputStream);
    }

    public static void saveFile(String path, InputStream inputStream) throws IOException {
        final Path filePath = Path.of(path);
        saveFile(filePath, inputStream);
    }

    public static void saveFile(Path path, InputStream inputStream) throws IOException {
        Files.createDirectories(path.getParent());
        Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
    }

    public static byte[] readFile(String absoluteLocationPath) {
        try {
            Path path = Paths.get(absoluteLocationPath);
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw DiagramViewerException.fail(String.format("Could not read file at '%s'.", absoluteLocationPath), e);
        }
    }
}
