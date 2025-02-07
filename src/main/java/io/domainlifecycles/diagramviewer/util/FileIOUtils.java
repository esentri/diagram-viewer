package io.domainlifecycles.diagramviewer.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileIOUtils {

    public static Set<String> getFileNamesInDiagramDirectory(String directory) {
        return Stream.of(Objects.requireNonNull(new File(directory).listFiles()))
            .filter(file -> !file.isDirectory())
            .map(File::getName)
            .collect(Collectors.toSet());
    }

    public static void saveFile(String diagramFolderLocation, InputStream inputStream, String fileName) throws InvalidPathException, IOException {
        final Path filePath = Path.of(diagramFolderLocation, fileName);
        Files.createDirectories(filePath.getParent());
        Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
    }
}
