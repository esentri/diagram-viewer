package fixtures.factory;

import io.domainlifecycles.domain.types.Factory;

/**
 * A Factory whose public method returning an Aggregate is its factory method.
 */
public class OrderFactory implements Factory {

    public Order create(OrderId id) {
        return new Order(id);
    }
}
