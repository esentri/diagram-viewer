package io.domainlifecycles.diagramviewer.webapp.components.dialogs;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.server.streams.DownloadResponse;
import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.service.DiagramServiceImpl;
import io.domainlifecycles.diagramviewer.util.FileConversionUtils;
import io.domainlifecycles.diagramviewer.util.FileIOUtils;
import java.io.ByteArrayInputStream;
import java.nio.file.Path;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

public class DownloadDiagramDialog extends Dialog {

    private final String diagramsLocation;
    private final DownloadDiagramOptions downloadDiagramOptions;
    private final Binder<DownloadDiagramOptions> binder;

    private final ProgressBar progressBar;

    private Diagram diagram;

    private Select<FileType> fileTypeSelect;
    private Button downloadDiagramButton;

    public DownloadDiagramDialog(String diagramsLocation) {
        setHeaderTitle("Download Diagram");

        this.diagramsLocation = diagramsLocation;
        this.binder = new Binder<>();
        this.downloadDiagramOptions = new DownloadDiagramOptions(FileType.SVG);

        binder.setBean(downloadDiagramOptions);

        add(createDialogLayout());

        progressBar = new ProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        add(progressBar);
    }

    private FormLayout createDialogLayout() {
        FormLayout formLayout = new FormLayout();

        fileTypeSelect = new Select<>();
        fileTypeSelect.setItems(FileType.values());

        binder.forField(fileTypeSelect)
            .asRequired("Format is required.")
            .bind(DownloadDiagramOptions::getFileType, DownloadDiagramOptions::setFileType);

        formLayout.addFormItem(fileTypeSelect, "Filetype");

        return formLayout;
    }

    private Anchor getDiagramDownloadButton() {
        String diagramFileName = diagram.getName() + DiagramServiceImpl.SVG_FILE_SUFFIX;
        DownloadHandler downloadHandler = getDownloadHandler(diagramFileName);

        Anchor downloadAnchor = new Anchor(downloadHandler, "Download Diagram");
        downloadAnchor.getStyle().set("cursor", "pointer");
        downloadAnchor.setId("diagramDownloadButton");
        downloadAnchor.getElement().setAttribute("download", true);
        downloadAnchor.removeAll();

        downloadDiagramButton = new Button("Download Diagram", new Icon(VaadinIcon.DOWNLOAD_ALT));
        downloadDiagramButton.getStyle().set("cursor", "pointer");

        downloadDiagramButton.addClickListener(e -> {
            progressBar.setVisible(true);
            fileTypeSelect.setEnabled(false);
            downloadDiagramButton.setEnabled(false);
        });

        downloadAnchor.add(downloadDiagramButton);

        return downloadAnchor;
    }

    private DownloadHandler getDownloadHandler(String diagramFileName) {
        Path diagramLocation = Path.of(
            diagramsLocation,
            diagram.getProject().getId().toString(),
            diagramFileName
        );

        return DownloadHandler.fromInputStream(e -> {
                byte[] svgFileContents = FileIOUtils.readFile(diagramLocation.toString());

                FileType selectedType = downloadDiagramOptions.getFileType();
                byte[] fileContents = convertImage(svgFileContents, selectedType);

                return new DownloadResponse(
                    new ByteArrayInputStream(fileContents),
                    String.join(".", diagram.getName(), selectedType.getExtension()),
                    selectedType.getMimeType(),
                    fileContents.length
                );
            }
        ).whenComplete(success -> {
            this.close();
            progressBar.setVisible(false);
            fileTypeSelect.setEnabled(true);
            downloadDiagramButton.setEnabled(true);
        });
    }

    private byte[] convertImage(byte[] svgFileContents, FileType fileType) {
        switch (fileType) {
            case JPEG -> {
                return FileConversionUtils.convertSvgToJpeg(svgFileContents);
            }
            case PNG -> {
                return FileConversionUtils.convertSvgToPng(svgFileContents);
            }
            case SVG -> {
                return svgFileContents;
            }
            default -> throw DiagramViewerException.fail("Invalid Filetype selected.");
        }
    }

    public void setDiagram(Diagram diagram) {
        this.diagram = diagram;
        getFooter().removeAll();
        getFooter().add(getDiagramDownloadButton());
    }

    @AllArgsConstructor
    private enum FileType {
        SVG("svg", "image/svg+xml"),
        PNG("png", "image/png"),
        JPEG("jpeg", "image/jpeg");

        @Getter
        private final String extension;
        @Getter
        private final String mimeType;
    }

    @Data
    @AllArgsConstructor
    private static class DownloadDiagramOptions {
        private FileType fileType;
    }
}