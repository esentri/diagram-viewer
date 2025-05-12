package io.domainlifecycles.diagramviewer.webapp.components.session;

import io.domainlifecycles.diagramviewer.model.Project;
import io.domainlifecycles.diagramviewer.model.ProjectDomainMirror;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

@Component
@SessionScope
public class SessionStorage {

    private final Map<UUID, Project> projects;
    private final Map<UUID, ProjectDomainMirror> projectDomainMirrors;

    public SessionStorage() {
        this.projects = new HashMap<>();
        this.projectDomainMirrors = new HashMap<>();
    }
}
