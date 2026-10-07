package fixtures.factory;

import io.domainlifecycles.domain.types.AggregateRoot;

/**
 * An Aggregate created by the {@link OrderFactory}.
 */
public class Order implements AggregateRoot<OrderId> {

    private final OrderId id;

    public Order(OrderId id) {
        this.id = id;
    }

    @Override
    public OrderId id() {
        return id;
    }

    @Override
    public long concurrencyVersion() {
        return 0;
    }
}
