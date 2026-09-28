package fixtures.containedreadmodel;

import io.domainlifecycles.domain.types.ReadModel;

import java.util.List;

/**
 * Contains only itself.
 */
public class CategoryTree implements ReadModel {

    private String name;

    private List<CategoryTree> children;
}
