package io.domainlifecycles.diagramviewer.security;

import com.vaadin.flow.server.auth.AccessCheckResult;
import com.vaadin.flow.server.auth.NavigationAccessChecker;
import com.vaadin.flow.server.auth.NavigationContext;
import io.domainlifecycles.diagramviewer.service.SecurityService;
import io.domainlifecycles.diagramviewer.webapp.views.DiagramView;
import io.domainlifecycles.diagramviewer.webapp.views.ProjectView;
import org.springframework.stereotype.Component;

@Component
public class ProjectAndDiagramNavigationAccessChecker implements NavigationAccessChecker {

    private final SecurityService securityService;

    public ProjectAndDiagramNavigationAccessChecker(SecurityService securityService) {
        this.securityService = securityService;
    }

    @Override
    public AccessCheckResult check(NavigationContext context) {
        AccessCheckResult result;

        if (ProjectView.class.equals(context.getNavigationTarget()) || DiagramView.class.equals(
            context.getNavigationTarget())) {
            if (context.getParameters().getParameterNames().contains("projectName")) {
                String projectName = context.getParameters().get("projectName").orElseThrow();
                result = securityService.checkAccess(projectName,
                    securityService.getRegisteredUser()) ? AccessCheckResult.allow() : AccessCheckResult.reject(
                    "User has no access to this resource.");
            } else {
                result = AccessCheckResult.reject("Project name not specified");
            }
        } else {
            result = AccessCheckResult.neutral();
        }
        return result;
    }
}