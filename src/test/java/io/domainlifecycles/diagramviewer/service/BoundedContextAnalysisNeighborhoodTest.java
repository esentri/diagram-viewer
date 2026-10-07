package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.diagramviewer.model.viewer.DiagramStylingConfiguration;
import io.domainlifecycles.diagramviewer.util.DiagrammerUtils;
import io.domainlifecycles.mirror.api.AggregateRootMirror;
import io.domainlifecycles.mirror.api.DomainMirror;
import io.domainlifecycles.mirror.reflect.ReflectiveDomainMirrorFactory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The neighborhood diagram of an aggregate shows what leads to it and what it leads to, two steps each. The fixture
 * {@code fixtures.factory} has the aggregate {@code Order}, created by the factory {@code OrderFactory} and the domain
 * service {@code OrderService}.
 */
class BoundedContextAnalysisNeighborhoodTest {

    private static final DomainMirror MIRROR =
        new ReflectiveDomainMirrorFactory("fixtures.factory").initializeDomainMirror();

    @Test
    void Should_FollowWhatLeadsToTheAggregateAndWhatItLeadsTo_TwoStepsEach() {

        // given
        AggregateRootMirror order = MIRROR.getAllAggregateRootMirrors().stream()
            .filter(aggregateRoot -> aggregateRoot.getTypeName().equals("fixtures.factory.Order"))
            .findFirst().orElseThrow();

        // when
        var visibility = BoundedContextAnalysisService.aggregateNeighborhood(order);

        // then
        assertThat(visibility.getIncludeConnectedToIngoingClassNames()).containsExactly(order.getTypeName());
        assertThat(visibility.getIncludeConnectedToOutgoingClassNames()).containsExactly(order.getTypeName());
        assertThat(visibility.getIncludeConnectedToIngoingDepth()).isEqualTo(2);
        assertThat(visibility.getIncludeConnectedToOutgoingDepth()).isEqualTo(2);
        String nomnoml = DiagrammerUtils.generateNomnoml(MIRROR, DiagramStylingConfiguration.builder().build(),
            visibility, List.of(), null);
        assertThat(nomnoml).contains("> Order <<", "> OrderFactory <<", "> OrderService <<");
    }
}
