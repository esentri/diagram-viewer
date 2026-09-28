package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.mirror.api.DomainMirror;
import io.domainlifecycles.mirror.api.DomainTypeMirror;
import io.domainlifecycles.mirror.reflect.ReflectiveDomainMirrorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Only read models contained in no other read model get a diagram of their own: a contained one is shown in the
 * diagram of the read model containing it.
 */
class BoundedContextAnalysisReadModelsTest {

    private static final String PACKAGE = "fixtures.containedreadmodel";

    private static DomainMirror mirror;

    @BeforeAll
    static void initMirror() {
        mirror = new ReflectiveDomainMirrorFactory(PACKAGE).initializeDomainMirror();
    }

    @Test
    void Should_FindTheReadModelsContainedInAnother_DirectlyOrNested() {

        // when
        var contained = BoundedContextAnalysisService.containedReadModelTypeNames(mirror);

        // then: a read model containing only itself is not contained by that
        assertThat(contained).containsExactlyInAnyOrder(PACKAGE + ".LineView", PACKAGE + ".ProductView");
    }

    @Test
    void Should_OfferOnlyTheTopLevelReadModelsForDiagrams() {

        // given
        var boundedContext = mirror.getAllBoundedContextMirrors().get(0);

        // when
        var topLevel = BoundedContextAnalysisService.topLevelReadModels(boundedContext,
            BoundedContextAnalysisService.containedReadModelTypeNames(mirror));

        // then
        assertThat(topLevel).extracting(DomainTypeMirror::getTypeName)
            .containsExactly(PACKAGE + ".CategoryTree", PACKAGE + ".OrderOverview");
    }
}
