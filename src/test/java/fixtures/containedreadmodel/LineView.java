package fixtures.containedreadmodel;

import io.domainlifecycles.domain.types.ReadModel;

import java.util.Optional;

/**
 * Contained in {@link OrderOverview}, and containing a read model itself.
 */
public class LineView implements ReadModel {

    private Optional<ProductView> product;
}
