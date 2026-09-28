package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.mirror.api.BoundedContextMirror;
import io.domainlifecycles.mirror.api.DomainTypeMirror;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BoundedContextAnalysisNamesTest {

    @Test
    void Should_NameDiagramsBySimpleName_And_AddThePackage_When_SimpleNamesCollideInAFolder() {

        // given: two commands of one bounded context sharing a simple name
        BoundedContextMirror vertrag = mock(BoundedContextMirror.class);
        when(vertrag.getPackageName()).thenReturn("shop.vertrag");
        List<DomainTypeMirror> commands = List.of(
            type("shop.vertrag.core.domain.teilvertrag.AktiviereCommand"),
            type("shop.vertrag.core.domain.vertrag.AktiviereCommand"),
            type("shop.vertrag.core.domain.vertrag.BeendeCommand"));

        // when
        Map<String, String> names = BoundedContextAnalysisService.diagramNames(commands, vertrag);

        // then: no bounded context prefix - names only have to be unique within their folder
        assertThat(names).containsEntry("shop.vertrag.core.domain.vertrag.BeendeCommand", "BeendeCommand")
            .containsEntry("shop.vertrag.core.domain.teilvertrag.AktiviereCommand", "AktiviereCommand (core.domain.teilvertrag)")
            .containsEntry("shop.vertrag.core.domain.vertrag.AktiviereCommand", "AktiviereCommand (core.domain.vertrag)");
    }

    private static DomainTypeMirror type(String typeName) {
        DomainTypeMirror type = mock(DomainTypeMirror.class);
        when(type.getTypeName()).thenReturn(typeName);
        return type;
    }
}
