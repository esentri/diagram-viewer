package fixtures.containedreadmodel;

import io.domainlifecycles.domain.types.ReadModel;

import java.util.List;

public class OrderOverview implements ReadModel {

    private String title;

    private List<LineView> lines;
}
