package io.domainlifecycles.diagramviewer.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileIOUtils {

    public static Set<String> getFileNamesInDirectory(String directoryName) {
        File directory = new File(directoryName);

        if(!directory.isDirectory() ||  directory.listFiles() == null) {
            return Collections.emptySet();
        }

        return Stream.of(Objects.requireNonNull(directory.listFiles()))
            .filter(file -> !file.isDirectory())
            .map(File::getName)
            .collect(Collectors.toSet());
    }

    public static Set<File> getAllFilesIncludingSubsequent(File directory) {
        Set<File> fileSet = new HashSet<>();
        if (directory.exists() && directory.isDirectory()) {
            for (File file : directory.listFiles()) {
                if (file.isFile()) {
                    fileSet.add(file);
                } else if (file.isDirectory()) {
                    fileSet.addAll(getAllFilesIncludingSubsequent(file));
                }
            }
        }
        return fileSet;
    }

    public static void saveFile(String path, InputStream inputStream, String fileName) throws InvalidPathException, IOException {
        final Path filePath = Path.of(path, fileName);
        Files.createDirectories(filePath.getParent());
        Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
    }
}
