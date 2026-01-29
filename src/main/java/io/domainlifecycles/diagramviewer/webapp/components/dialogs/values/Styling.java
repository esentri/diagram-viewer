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

package io.domainlifecycles.diagramviewer.webapp.components.dialogs.values;

import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum Styling {
    BOLD("bold", "Bold"),
    CENTER("center", "Center"),
    ITALIC("italic", "Italic"),
    LEFT("left", "Left"),
    UNDERLINE("underline", "Underline");

    private final String nomnomlValue;
    private final String displayValue;

    Styling(String nomnomlValue, String displayValue) {
        this.nomnomlValue = nomnomlValue;
        this.displayValue = displayValue;
    }

    public static Set<Styling> map(String[] stylingOptionsNomnomlValue) {
        return Arrays.stream(stylingOptionsNomnomlValue).map(Styling::of).collect(Collectors.toSet());
    }

    public static Styling of(final String nomnomlValue) {
        for (Styling styling : Styling.values()) {
            if (styling.nomnomlValue.equalsIgnoreCase(nomnomlValue)) {
                return styling;
            }
        }
        throw DiagramViewerException.fail("No Styling option found with nomnoml value: " + nomnomlValue);
    }

    public String getNomnomlValue() {
        return nomnomlValue;
    }

    public String getDisplayValue() {
        return displayValue;
    }
}
