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
package io.domainlifecycles.diagramviewer.webapp.components.various;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.dom.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * A name - of a project, folder, diagram or class - that wraps at the places a reader expects: after a dot or
 * underscore and before a new word in camel case, e.g. {@code Zimmer|Application|Service.|checke|Gast|Aus}. Only where
 * none of these fits the line, the name wraps anywhere (see diagram-viewer-styles.css).
 */
public final class WrappableName {

    /** styled to wrap, see diagram-viewer-styles.css */
    static final String CSS_CLASS = "wrappable-name";

    private WrappableName() {
    }

    /**
     * @param name the name to show
     * @return a span showing the name, with a break opportunity ({@code <wbr>}) between its segments
     */
    public static Span create(String name) {
        Span span = new Span();
        span.addClassName(CSS_CLASS);
        List<String> segments = segments(name);
        for (int i = 0; i < segments.size(); i++) {
            if (i > 0) {
                span.getElement().appendChild(new Element("wbr"));
            }
            span.getElement().appendChild(Element.createText(segments.get(i)));
        }
        return span;
    }

    /**
     * Splits a name into the segments it may wrap between: after a dot or underscore, before an upper case letter
     * following a lower case letter or digit, and before the last upper case letter of an acronym followed by a lower
     * case letter ({@code HTTP|Server}).
     *
     * @param name the name to split
     * @return the segments, together the name
     */
    static List<String> segments(String name) {
        List<String> segments = new ArrayList<>();
        int start = 0;
        for (int i = 1; i < name.length(); i++) {
            if (breaksBefore(name, i)) {
                segments.add(name.substring(start, i));
                start = i;
            }
        }
        segments.add(name.substring(start));
        return segments;
    }

    private static boolean breaksBefore(String name, int index) {
        char previous = name.charAt(index - 1);
        char current = name.charAt(index);
        if (previous == '.' || previous == '_') {
            return true;
        }
        if (!Character.isUpperCase(current)) {
            return false;
        }
        if (Character.isLowerCase(previous) || Character.isDigit(previous)) {
            return true;
        }
        boolean followedByLowerCase = index + 1 < name.length() && Character.isLowerCase(name.charAt(index + 1));
        return Character.isUpperCase(previous) && followedByLowerCase;
    }
}
