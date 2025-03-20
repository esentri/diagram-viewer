package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.model.Diagram;
import io.domainlifecycles.diagramviewer.repository.DiagramRepository;

public class DiagramServiceImpl implements DiagramService {

    private final DiagramRepository repository;

    public DiagramServiceImpl(DiagramRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(Diagram diagram) {
        repository.save(diagram);
    }
}
