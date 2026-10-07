package fixtures.factory;

import io.domainlifecycles.domain.types.DomainService;
import io.domainlifecycles.domain.types.FactoryMethod;

/**
 * A DomainService with a factory method besides its other operations.
 */
public class OrderService implements DomainService {

    @FactoryMethod
    public Order reorder(Order order) {
        return new Order(new OrderId(2L));
    }

    public void cancel(Order order) {
    }
}
