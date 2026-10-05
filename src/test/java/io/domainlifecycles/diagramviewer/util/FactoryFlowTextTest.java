package io.domainlifecycles.diagramviewer.util;

import io.domainlifecycles.mirror.api.DomainMirror;
import io.domainlifecycles.mirror.api.DomainTypeMirror;
import io.domainlifecycles.mirror.reflect.ReflectiveDomainMirrorFactory;
import io.domainlifecycles.staticanalysis.DomainCalls;
import io.domainlifecycles.staticanalysis.DomainMethod;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The flow text shows the domain types the factory methods of a flow create, and leads from a domain type back to the
 * factory methods creating it. The fixture {@code fixtures.factory}: {@code OrderService.cancel} calls
 * {@code OrderFactory.create}, which creates an {@code Order}, as does {@code OrderService.reorder}.
 */
class FactoryFlowTextTest {

    private static DomainMirror mirror;
    private static DomainCalls calls;

    @BeforeAll
    static void init() {
        mirror = new ReflectiveDomainMirrorFactory("fixtures.factory").initializeDomainMirror();
        calls = DomainCalls.builder()
            .add(method("fixtures.factory.OrderService", "cancel"), List.of(new DomainCalls.CallSite(
                method("fixtures.factory.OrderFactory", "create"), "fixtures.factory.OrderService", 1)))
            .build();
    }

    @Test
    void Should_ShowTheAggregateAFactoryMethodCreates_InAForwardFlow() {

        // when
        String text = FlowText.toText(new FlowText(mirror, calls, List.of("fixtures.factory.OrderService#cancel"), List.of())
            .render(new FlowText.Options(false, 0, null)));

        // then
        assertThat(text).contains("OrderFactory.create(OrderId)", "[Aggregate] Order");
    }

    @Test
    void Should_LeadBackToTheFactoryMethodsCreatingTheAggregate_InABackwardFlow() {

        // when
        String text = FlowText.toText(new FlowText(mirror, calls, List.of(), List.of("fixtures.factory.Order"))
            .render(new FlowText.Options(false, 0, null)));

        // then: both factory methods creating it, and the caller of the factory
        assertThat(text).contains("OrderFactory.create(OrderId)", "OrderService.reorder(Order)", "OrderService.cancel(Order)");
    }

    private static DomainMethod method(String typeName, String methodName) {
        DomainTypeMirror type = mirror.getDomainTypeMirror(typeName).orElseThrow();
        return new DomainMethod(typeName, type.getMethods().stream()
            .filter(method -> method.getName().equals(methodName))
            .findFirst()
            .orElseThrow());
    }
}
