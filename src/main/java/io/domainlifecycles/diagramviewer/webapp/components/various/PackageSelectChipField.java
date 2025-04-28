package io.domainlifecycles.diagramviewer.webapp.components.various;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import java.util.LinkedHashSet;
import java.util.Set;

public class PackageSelectChipField extends CustomField<Set<String>> {

    private final HorizontalLayout tagsLayout = new HorizontalLayout();
    private final Set<String> tags = new LinkedHashSet<>();

    private TextField inputField;

    public PackageSelectChipField() {
        this(null);
    }

    public PackageSelectChipField(Set<String> initialPackages) {
        if (initialPackages != null) {
            tags.addAll(initialPackages);
        }

        this.setWidthFull();

        HorizontalLayout inputLayout = new HorizontalLayout(createAndGetPackageInputField());
        inputLayout.setWidthFull();
        inputLayout.setAlignItems(Alignment.END);

        for (String initialTag : tags) {
            tagsLayout.add(createTag(initialTag));
        }

        add(inputLayout, tagsLayout);
    }

    private TextField createAndGetPackageInputField() {
        inputField = new TextField();
        inputField.setPlaceholder("Package-Name...");
        inputField.setSuffixComponent(new Icon("vaadin:enter"));
        inputField.setWidthFull();
        tagsLayout.setWidthFull();
        tagsLayout.getStyle().set("flex-wrap", "wrap");
        tagsLayout.getStyle().set("gap", "2px");

        inputField.addKeyPressListener(Key.ENTER, e -> addTag(inputField.getValue()));
        inputField.addKeyPressListener(Key.SPACE, e -> addTag(inputField.getValue()));
        return inputField;
    }

    private void addTag(String text) {
        if (text == null || text.trim().isEmpty()) return;

        String trimmedText = text.trim();
        if (tags.contains(trimmedText)) {
            inputField.clear();
            return;
        }

        tags.add(trimmedText);
        inputField.clear();

        Div tag = createTag(trimmedText);
        tagsLayout.add(tag);
    }

    private Div createTag(String tagName) {
        Div tag = new Div();
        tag.getStyle().set("display", "inline-flex");
        tag.getStyle().set("align-items", "center");
        tag.getStyle().set("background-color", "#e0e0e0");
        tag.getStyle().set("border-radius", "16px");
        tag.getStyle().set("padding", "2px 6px");
        tag.getStyle().set("font-size", "12px");
        tag.getStyle().set("height", "28px");
        tag.getStyle().set("margin-right", "2px");
        tag.getStyle().set("margin-bottom", "2px");

        Div text = new Div();
        text.setText(tagName);
        text.getStyle().set("margin-right", "4px");

        Button removeButton = new Button("×", e -> {
            tags.remove(tagName);
            tagsLayout.remove(tag);
        });
        removeButton.getElement().getStyle().set("border", "none");
        removeButton.getElement().getStyle().set("background", "transparent");
        removeButton.getElement().getStyle().set("cursor", "pointer");
        removeButton.getElement().getStyle().set("font-size", "12px");
        removeButton.getElement().getStyle().set("padding", "0 4px");
        removeButton.getElement().getStyle().set("width", "auto");
        removeButton.getElement().getStyle().set("height", "auto");
        removeButton.getElement().getStyle().set("line-height", "1");
        removeButton.getElement().getStyle().set("min-width", "16px");
        removeButton.getElement().getStyle().set("text-align", "center");

        tag.add(text, removeButton);
        return tag;
    }

    @Override
    protected Set<String> generateModelValue() {
        return tags;
    }

    @Override
    protected void setPresentationValue(Set<String> selectedItems) {
        tags.clear();
        tags.addAll(selectedItems);
        tagsLayout.removeAll();
        selectedItems.forEach(tag -> tagsLayout.add(createTag(tag)));
    }
}
