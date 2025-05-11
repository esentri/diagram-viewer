package io.domainlifecycles.diagramviewer.util;

import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileIOUtils {

    public static Set<File> getFilesInDirectory(Path directory) {
        File directoryFile = new File(directory.toUri());

        if (!directoryFile.isDirectory() || directoryFile.listFiles() == null) {
            return Collections.emptySet();
        }

        return Stream.of(Objects.requireNonNull(directoryFile.listFiles()))
            .filter(file -> !file.isDirectory())
            .collect(Collectors.toSet());
    }

    public static Path saveFile(String locationPath, String filenameIncludingSuffix, InputStream inputStream) throws IOException {
        final Path filePath = Path.of(locationPath, filenameIncludingSuffix);
        saveFile(filePath, inputStream);
        return filePath;
    }

    public static void saveFile(String path, InputStream inputStream) throws IOException {
        final Path filePath = Path.of(path);
        saveFile(filePath, inputStream);
    }

    public static void saveFile(Path path, InputStream inputStream) throws IOException {
        Files.createDirectories(path.getParent());
        Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
    }

    public static void renameFile(Path path, String newFilename) {
        if(newFilename == null || newFilename.isBlank()) {
            throw DiagramViewerException.fail("Filename may not be empty for renaming.");
        }

        Path parentDir = path.getParent();
        if (parentDir == null) {
            throw DiagramViewerException.fail(
                String.format("Specified file path '%s' for renaming has no parent directory.", path.toAbsolutePath()));
        }

        Path newPath = parentDir.resolve(newFilename);

        try {
            Files.move(path, newPath, StandardCopyOption.REPLACE_EXISTING);
        } catch(RuntimeException | IOException e) {
            throw DiagramViewerException.fail(
                String.format("Could not rename file '%s' to '%s'.", path.toAbsolutePath(), newPath.getFileName()), e);
        }
    }

    public static byte[] readFile(String absoluteLocationPath) {
        try {
            Path path = Paths.get(absoluteLocationPath);
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw DiagramViewerException.fail(String.format("Could not read file at '%s'.", absoluteLocationPath), e);
        }
    }

    public static void deleteDirectoryRecursively(Path directoryPath) throws IOException {
        if (Files.notExists(directoryPath)) return;

        Files.walk(directoryPath)
            .sorted(Comparator.reverseOrder())
            .forEach(path -> {
                try {
                    Files.delete(path);
                } catch (IOException e) {
                    throw DiagramViewerException.fail(String.format("Failed to delete directory '%s'.", path), e);
                }
            });
    }

    public static void deleteFileByAbsolutePath(String absolutePath) throws IOException {
        Path path = Paths.get(absolutePath);
        if (Files.exists(path) && Files.isRegularFile(path)) {
            Files.delete(path);
        } else {
            throw DiagramViewerException.fail(String.format("File not found or not a regular file '%s'.", absolutePath));
        }
    }
}
