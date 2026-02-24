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
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;

public class FileIOUtils {

    public static Path saveTemporaryFile(String fileName, byte[] fileContents) {
        Path pathToFile;
        try {
            pathToFile = Files.createTempFile(
                fileName.substring(0, fileName.lastIndexOf('.')),
                fileName.substring(fileName.lastIndexOf('.')));
            Files.write(pathToFile, fileContents);
        } catch (IOException e) {
            throw DiagramViewerException.fail("Could not save temporary .jar file.", e);
        }
        return pathToFile;
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
