package io.domainlifecycles.diagramviewer.webapp.components.dialogs.components;

import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import io.domainlifecycles.diagramviewer.model.Project;
import io.domainlifecycles.mirror.api.DomainType;
import io.domainlifecycles.mirror.api.DomainTypeMirror;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DomainTypeMirrorMultiSelect extends MultiSelectComboBox<DomainTypeMirror> {

    public DomainTypeMirrorMultiSelect(Project project) {
        Map<DomainType, List<DomainTypeMirror>> grouped = project.getDomainModel().allTypeMirrors().values().stream()
            .collect(Collectors.groupingBy(DomainTypeMirror::getDomainType));

        List<DomainTypeMirror> displayItems = new ArrayList<>();
        grouped.keySet().stream().sorted().forEach(type -> {
            displayItems.addAll(grouped.get(type));
        });

        setItems(displayItems);
        setValue(displayItems);

        setItemLabelGenerator(DomainTypeMirrorMultiSelect::generateItemLabels);

        Set<DomainType> renderedTypes = new HashSet<>();

        setRenderer(new ComponentRenderer<>(item -> {
            VerticalLayout layout = new VerticalLayout();
            layout.setPadding(false);
            layout.setSpacing(false);
            layout.setMargin(false);

            // Add header if it's the first time seeing this type
            if (renderedTypes.add(item.getDomainType())) {
                NativeLabel header = new NativeLabel(item.getDomainType().name());
                header.getStyle().set("font-weight", "bold");
                layout.add(header);
            }

            NativeLabel name = new NativeLabel("  " + item.getTypeName());
            layout.add(name);
            return layout;
        }));
    }

    private static String generateItemLabels(Object o) {
        if (o instanceof DomainType type) {
            return type.name();
        } else if (o instanceof DomainTypeMirror domainTypeMirror) {
            return domainTypeMirror.getTypeName();
        }
        else return null;
    }
}
