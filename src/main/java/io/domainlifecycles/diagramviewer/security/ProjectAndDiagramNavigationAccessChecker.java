package io.domainlifecycles.diagramviewer.security;

import com.vaadin.flow.server.auth.AccessCheckResult;
import com.vaadin.flow.server.auth.NavigationAccessChecker;
import com.vaadin.flow.server.auth.NavigationContext;
import io.domainlifecycles.diagramviewer.service.AuthenticatedUserService;
import io.domainlifecycles.diagramviewer.session.SessionStorage;
import io.domainlifecycles.diagramviewer.webapp.views.DiagramView;
import io.domainlifecycles.diagramviewer.webapp.views.ProjectView;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

@Component("projectAndDiagramNavigationAccessChecker")
@Scope(scopeName = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ProjectAndDiagramNavigationAccessChecker implements NavigationAccessChecker {

    private final AuthenticatedUserService authenticatedUserService;
    private final SessionStorage sessionStorage;

    public ProjectAndDiagramNavigationAccessChecker(AuthenticatedUserService authenticatedUserService,
                                                    @Qualifier("sessionStorage") SessionStorage sessionStorage) {
        this.authenticatedUserService = authenticatedUserService;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public AccessCheckResult check(NavigationContext context) {
        AccessCheckResult result;

        if (ProjectView.class.equals(context.getNavigationTarget()) || DiagramView.class.equals(context.getNavigationTarget())) {
            if (context.getParameters().getParameterNames().contains("projectName")) {
                String projectName = context.getParameters().get("projectName").get();
                result = authenticatedUserService.checkAccess(projectName, sessionStorage.getAuthenticatedUser()) ? AccessCheckResult.allow() : AccessCheckResult.reject("User has no access to this resource.");
            } else {
                result = AccessCheckResult.reject("Project name not specified");
            }
        } else {
            result = AccessCheckResult.neutral();
        }
        return result;
    }
}