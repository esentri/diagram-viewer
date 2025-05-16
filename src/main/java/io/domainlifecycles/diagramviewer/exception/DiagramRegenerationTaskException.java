package io.domainlifecycles.diagramviewer.exception;

import io.domainlifecycles.diagramviewer.scheduled.DiagramRegenerationTask.DiagramRegenerationError;
import java.util.List;
import java.util.stream.Collectors;

public class DiagramRegenerationTaskException extends RuntimeException {

    private final List<DiagramRegenerationError> errors;

    public DiagramRegenerationTaskException(List<DiagramRegenerationError> errors) {
        super(String.format("%s diagram regenerations have failed in scheduled task", errors.size()));
        this.errors = errors;
    }

    @Override
    public String getMessage() {
        return errors.stream()
            .map(error -> String.format("Regeneration of diagram '%s', UUID: '%s' failed. Cause: %s",
                error.getDiagramName(),
                error.getDiagramId(),
                error.getCaughtException().getMessage()))
            .collect(Collectors.joining("\n"));
    }
}
