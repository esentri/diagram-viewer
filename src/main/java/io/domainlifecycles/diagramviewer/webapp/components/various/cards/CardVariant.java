package io.domainlifecycles.diagramviewer.webapp.components.various.cards;

import com.vaadin.flow.component.shared.ThemeVariant;

/**
 * Set of theme variants applicable for {@code vaadin-card} component.
 */
public enum CardVariant implements ThemeVariant {
    LUMO_ELEVATED("elevated"),
    LUMO_OUTLINED("outlined"),
    LUMO_HORIZONTAL("horizontal"),
    LUMO_STRETCH_MEDIA("stretch-media"),
    LUMO_COVER_MEDIA("cover-media"),
    /**
     * @deprecated Since 24.7, the Material theme is deprecated and will be
     *             removed in Vaadin 25.
     */
    @Deprecated
    MATERIAL_ELEVATED("elevated"),
    /**
     * @deprecated Since 24.7, the Material theme is deprecated and will be
     *             removed in Vaadin 25.
     */
    @Deprecated
    MATERIAL_OUTLINED("outlined"),
    /**
     * @deprecated Since 24.7, the Material theme is deprecated and will be
     *             removed in Vaadin 25.
     */
    @Deprecated
    MATERIAL_HORIZONTAL("horizontal"),
    /**
     * @deprecated Since 24.7, the Material theme is deprecated and will be
     *             removed in Vaadin 25.
     */
    @Deprecated
    MATERIAL_STRETCH_MEDIA("stretch-media"),
    /**
     * @deprecated Since 24.7, the Material theme is deprecated and will be
     *             removed in Vaadin 25.
     */
    @Deprecated
    MATERIAL_COVER_MEDIA("cover-media");

    private final String variant;

    CardVariant(String variant) {
        this.variant = variant;
    }

    /**
     * Gets the variant name.
     *
     * @return variant name
     */
    public String getVariantName() {
        return variant;
    }
}
