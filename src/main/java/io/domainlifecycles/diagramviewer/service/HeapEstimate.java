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

package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.mirror.api.DomainMirror;
import io.domainlifecycles.mirror.api.DomainTypeMirror;
import io.domainlifecycles.mirror.api.MethodMirror;
import io.domainlifecycles.staticanalysis.DomainCalls;
import io.domainlifecycles.staticanalysis.DomainMethod;

/**
 * Estimates the heap occupied by a project's model data from its structure, so that the {@link ProjectModelCache}
 * can be bounded by memory instead of by a number of projects.
 * <p>
 * The factors were measured on two real projects (heap after full GC, 2026-09-27) and agree within a few percent:
 * the domain mirror takes about 700 bytes per element (type, field, method or method parameter; esprit_2: 91 MB for
 * 142 703 elements, Esprit_Bonitaetspruefung: 53 MB for 80 914), the static analysis result about 215 bytes per call
 * site (esprit_2: 223 MB for 1 080 664 call sites, Esprit_Bonitaetspruefung: 86 MB for 429 815).
 */
final class HeapEstimate {

    static final long BYTES_PER_MIRROR_ELEMENT = 700;
    static final long BYTES_PER_CALL_SITE = 220;

    private HeapEstimate() {
    }

    static long of(DomainMirror domainMirror) {
        if (domainMirror == null || domainMirror.getAllDomainTypeMirrors() == null) {
            return 0;
        }
        long elements = 0;
        for (DomainTypeMirror type : domainMirror.getAllDomainTypeMirrors()) {
            elements += 1 + type.getAllFields().size() + type.getMethods().size();
            for (MethodMirror method : type.getMethods()) {
                elements += method.getParameters().size();
            }
        }
        return elements * BYTES_PER_MIRROR_ELEMENT;
    }

    static long of(DomainCalls domainCalls) {
        if (domainCalls == null) {
            return 0;
        }
        long callSites = 0;
        for (DomainMethod caller : domainCalls.callers()) {
            callSites += domainCalls.callsFor(caller).callSites().size();
        }
        return callSites * BYTES_PER_CALL_SITE;
    }
}
