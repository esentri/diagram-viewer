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
 *  Copyright 2019-2025 the original author or authors.
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

package io.domainlifecycles.diagramviewer.security;

import com.vaadin.flow.server.auth.AccessCheckResult;
import com.vaadin.flow.server.auth.NavigationAccessChecker;
import com.vaadin.flow.server.auth.NavigationContext;
import io.domainlifecycles.diagramviewer.service.SecurityService;
import io.domainlifecycles.diagramviewer.webapp.views.DiagramView;
import io.domainlifecycles.diagramviewer.webapp.views.ProjectView;
import io.domainlifecycles.diagramviewer.webapp.views.SignInView;
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

        if(SignInView.class.equals(context.getNavigationTarget())) return AccessCheckResult.allow();

        if (ProjectView.class.equals(context.getNavigationTarget()) || DiagramView.class.equals(
            context.getNavigationTarget())) {
            if (context.getParameters().getParameterNames().contains(ProjectView.PROJECT_NAME_ROUTE_PARAMETER)) {
                String projectName = context.getParameters().get(ProjectView.PROJECT_NAME_ROUTE_PARAMETER).orElseThrow();
                result = securityService.checkAccess(projectName,
                    securityService.getCurrentlySignedInUser()) ? AccessCheckResult.allow() : AccessCheckResult.reject(
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