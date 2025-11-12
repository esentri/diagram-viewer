package io.domainlifecycles.diagramviewer.util;

import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;

class FileIOUtilsTest {

    @TempDir
    Path tempDir;

    @Test
    void GivenValidFileNameAndContents_WhenSaveTemporaryFile_ThenFileIsCreatedAndReturned() throws IOException {

        // given
        String fileName = "example.txt";
        byte[] contents = "Hello World".getBytes();

        // when
        Path result = FileIOUtils.saveTemporaryFile(fileName, contents);

        // then
        assertNotNull(result);
        assertTrue(Files.exists(result));
        assertArrayEquals(contents, Files.readAllBytes(result));
    }

    @Test
    void GivenIOExceptionOccurs_WhenSaveTemporaryFile_ThenThrowsDiagramViewerException() {

        // given
        String fileName = "badfile.jar";
        byte[] contents = new byte[]{1, 2, 3};

        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.createTempFile(anyString(), anyString()))
                .thenThrow(new IOException("Disk full"));

            // when
            // then
            assertThatThrownBy(() -> FileIOUtils.saveTemporaryFile(fileName, contents))
                .isInstanceOf(DiagramViewerException.class)
                .hasMessage("Could not save temporary .jar file.");
        }
    }

    @Test
    void GivenValidPathAndInputStream_WhenSaveFile_ThenFileIsCreated() throws IOException {

        // given
        Path target = tempDir.resolve("file.txt");
        byte[] data = "test".getBytes();
        InputStream input = new ByteArrayInputStream(data);

        // when
        FileIOUtils.saveFile(target, input);

        // then
        assertThat(Files.exists(target)).isTrue();
        assertThat(Files.readAllBytes(target)).isEqualTo(data);
    }

    @Test
    void GivenIOExceptionOccurs_WhenSaveFile_ThenPropagatesIOException() {

        // given
        Path target = tempDir.resolve("bad.txt");
        InputStream input = new ByteArrayInputStream("fail".getBytes());

        try (MockedStatic<Files> mocked = mockStatic(Files.class)) {
            mocked.when(() -> Files.createDirectories(any())).thenThrow(new IOException("disk full"));

            // when
            // then
            assertThatThrownBy(() -> FileIOUtils.saveFile(target, input))
                .isInstanceOf(IOException.class)
                .hasMessage("disk full");
        }
    }

    @Test
    void GivenValidPathAndNewName_WhenRenameFile_ThenFileRenamed() throws IOException {

        // given
        Path file = Files.createFile(tempDir.resolve("old.txt"));
        String newName = "new.txt";

        // when
        FileIOUtils.renameFile(file, newName);

        // then
        Path newPath = tempDir.resolve(newName);
        assertThat(Files.exists(newPath)).isTrue();
        assertThat(Files.notExists(file)).isTrue();
    }

    @Test
    void GivenNullNewName_WhenRenameFile_ThenThrowsDiagramViewerException() {

        // given
        Path dummy = tempDir.resolve("dummy.txt");

        // when
        // then
        assertThatThrownBy(() -> FileIOUtils.renameFile(dummy, null))
            .isInstanceOf(DiagramViewerException.class)
            .hasMessage("Filename may not be empty for renaming.");
    }

    @Test
    void GivenPathWithoutParent_WhenRenameFile_ThenThrowsDiagramViewerException() {

        // given
        Path rootPath = Path.of("file.txt"); // no parent dir

        // when
        // then
        assertThatThrownBy(() -> FileIOUtils.renameFile(rootPath, "new.txt"))
            .isInstanceOf(DiagramViewerException.class)
            .hasMessageContaining("has no parent directory");
    }

    @Test
    void GivenIOExceptionDuringMove_WhenRenameFile_ThenThrowsDiagramViewerException() throws IOException {

        // given
        Path path = tempDir.resolve("source.txt");
        Files.createFile(path);

        try (MockedStatic<Files> mocked = mockStatic(Files.class)) {
            mocked.when(() -> Files.move(any(), any(), any())).thenThrow(new IOException("I/O failure"));
            mocked.when(() -> Files.exists(any())).thenReturn(true);

            // when
            // then
            assertThatThrownBy(() -> FileIOUtils.renameFile(path, "dest.txt"))
                .isInstanceOf(DiagramViewerException.class)
                .hasMessageContaining("Could not rename file");
        }
    }

    @Test
    void GivenExistingFile_WhenReadFile_ThenReturnsContents() throws IOException {

        // given
        Path file = tempDir.resolve("data.txt");
        byte[] expected = "hello".getBytes();
        Files.write(file, expected);

        // when
        byte[] result = FileIOUtils.readFile(file.toString());

        // then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void GivenIOException_WhenReadFile_ThenThrowsDiagramViewerException() throws IOException {
        // given
        String path = "/nonexistent/file.txt";

        try (MockedStatic<Files> mocked = mockStatic(Files.class)) {
            mocked.when(() -> Files.readAllBytes(any())).thenThrow(new IOException("cannot read"));

            // when / then
            assertThatThrownBy(() -> FileIOUtils.readFile(path))
                .isInstanceOf(DiagramViewerException.class)
                .hasMessage("Could not read file at '/nonexistent/file.txt'.");
        }
    }

    @Test
    void GivenDirectoryExists_WhenDeleteDirectoryRecursively_ThenAllFilesDeleted() throws IOException {

        // given
        Path dir = tempDir.resolve("dir");
        Path subDir = Files.createDirectories(dir.resolve("sub"));
        Path file = Files.createFile(subDir.resolve("file.txt"));

        assertThat(Files.exists(file)).isTrue();

        // when
        FileIOUtils.deleteDirectoryRecursively(dir);

        // then
        assertThat(Files.exists(dir)).isFalse();
    }

    @Test
    void GivenDirectoryNotExists_WhenDeleteDirectoryRecursively_ThenNoActionTaken() throws IOException {

        // given
        Path nonExistent = tempDir.resolve("doesNotExist");

        // when
        // then
        FileIOUtils.deleteDirectoryRecursively(nonExistent);
    }

    @Test
    void GivenIOExceptionDuringDelete_WhenDeleteDirectoryRecursively_ThenThrowsDiagramViewerException() throws IOException {

        // given
        Path dir = tempDir.resolve("ioFailDir");
        Files.createDirectories(dir);

        try (MockedStatic<Files> mocked = mockStatic(Files.class)) {
            mocked.when(() -> Files.notExists(any())).thenReturn(false);
            mocked.when(() -> Files.walk(any())).thenAnswer(inv ->
                java.util.stream.Stream.of(dir)
            );
            mocked.when(() -> Files.delete(any())).thenThrow(new IOException("delete failed"));

            // when
            // then
            assertThatThrownBy(() -> FileIOUtils.deleteDirectoryRecursively(dir))
                .isInstanceOf(DiagramViewerException.class)
                .hasMessageContaining("Failed to delete directory");
        }
    }

    @Test
    void GivenExistingRegularFile_WhenDeleteFileByAbsolutePath_ThenFileDeleted() throws IOException {

        // given
        Path file = Files.createFile(tempDir.resolve("delete.txt"));
        assertThat(Files.exists(file)).isTrue();

        // when
        FileIOUtils.deleteFileByAbsolutePath(file.toString());

        // then
        assertThat(Files.exists(file)).isFalse();
    }

    @Test
    void GivenNonexistentFile_WhenDeleteFileByAbsolutePath_ThenThrowsDiagramViewerException() {
        // given
        String missingPath = tempDir.resolve("missing.txt").toString();

        // when
        // then
        assertThatThrownBy(() -> FileIOUtils.deleteFileByAbsolutePath(missingPath))
            .isInstanceOf(DiagramViewerException.class)
            .hasMessageContaining("File not found or not a regular file");
    }
}