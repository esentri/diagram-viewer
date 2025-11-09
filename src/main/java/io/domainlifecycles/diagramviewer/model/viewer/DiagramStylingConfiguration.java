package io.domainlifecycles.diagramviewer.model.viewer;

import io.domainlifecycles.diagramviewer.webapp.components.dialogs.values.Acycler;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.values.Direction;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.values.Font;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.values.Ranker;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "DiagramStylingConfiguration")
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class DiagramStylingConfiguration {

    @Id
    @GeneratedValue
    @Getter
    private UUID id;

    /**
     * Style declaration for AggregateRoots (see Nomnoml style options)
     */
    @Getter
    @Setter
    @Builder.Default private String aggregateRootStyle = "fill=#88ff00 bold";
    /**
     * Style declaration for AggregateRoot frames (see Nomnoml style options)
     */
    @Getter
    @Setter
    @Builder.Default private String aggregateFrameStyle = "visual=frame align=left";
    /**
     * Style declaration for Entities  (see Nomnoml style options)
     */
    @Getter
    @Setter
    @Builder.Default private String entityStyle = "fill=#88AAFF bold";
    /**
     * Style declaration for ValueObjects  (see Nomnoml style options)
     */
    @Getter
    @Setter
    @Builder.Default private String valueObjectStyle = "fill=#FFFFCC bold";
    /**
     * Style declaration for Enums  (see Nomnoml style options)
     */
    @Getter
    @Setter
    @Builder.Default private String enumStyle = "fill=#FFFFCC bold";
    /**
     * Style declaration for Identities  (see Nomnoml style options)
     */
    @Getter
    @Setter
    @Builder.Default private String identityStyle = "fill=#FFFFCC bold";
    /**
     * Style declaration for DomainEvents  (see Nomnoml style options)
     */
    @Getter
    @Setter
    @Builder.Default private String domainEventStyle = "fill=#CCFFFF bold";
    /**
     * Style declaration for DomainCommands  (see Nomnoml style options)
     */
    @Getter
    @Setter
    @Builder.Default private String domainCommandStyle = "fill=#FFB266 bold";
    /**
     * Style declaration for ApplicationServices (see Nomnoml style options)
     */
    @Getter
    @Setter
    @Builder.Default private String applicationServiceStyle = "bold";
    /**
     * Style declaration for DomainServices  (see Nomnoml style options)
     */
    @Getter
    @Setter
    @Builder.Default private String domainServiceStyle = "fill=#E0E0E0 bold";
    /**
     * Style declaration for Repositories  (see Nomnoml style options)
     */
    @Getter
    @Setter
    @Builder.Default private String repositoryStyle = "fill=#C0C0C0 bold";
    /**
     * Style declaration for ReadModels  (see Nomnoml style options)
     */
    @Getter
    @Setter
    @Builder.Default private String readModelStyle = "fill=#FFCCE5 bold";
    /**
     * Style declaration for QueryHandlers  (see Nomnoml style options)
     */
    @Getter
    @Setter
    @Builder.Default private String queryHandlerStyle = "fill=#C0C0C0 bold";
    /**
     * Style declaration for OutboundServices  (see Nomnoml style options)
     */
    @Getter
    @Setter
    @Builder.Default private String outboundServiceStyle = "fill=#C0C0C0 bold";
    /**
     * Style declaration for unspecified ServiceKinds  (see Nomnoml style options)
     */
    @Getter
    @Setter
    @Builder.Default private String unspecifiedServiceKindStyle = "fill=#C0C0C0 bold";
    /**
     * General font style declaration  (see Nomnoml style options)
     */
    @Getter
    @Setter
    @Builder.Default private Font font = Font.HELVETICA;
    /**
     * General layout direction style declaration (see Nomnoml style options, 'down' or 'right' is supported)
     */
    @Getter
    @Setter
    @Builder.Default private Direction direction = Direction.DOWN;
    /**
     * General layout direction style declaration (see Nomnoml style options, 'network-simplex' or 'tight-tree' or
     * 'longest-path' is supported)
     */
    @Getter
    @Setter
    @Builder.Default private Ranker ranker = Ranker.LONGEST_PATH;
    /**
     * General acycling style declaration  (see Nomnoml style options, only 'greedy' supported)
     */
    @Getter
    @Setter
    @Builder.Default private Acycler acycler = Acycler.GREEDY;
    /**
     * Background color style declaration (see Nomnoml style options, only 'transparent' or HEX color-codes supported)
     */
    @Getter
    @Setter
    @Builder.Default private String backgroundColor = "transparent";
    /**
     * If false, generally no fields are included
     */
    @Getter
    @Setter
    @Builder.Default private boolean showFields = true;
    /**
     * If true, generally full qualified class names are used
     */
    @Getter
    @Setter
    @Builder.Default private boolean showFullQualifiedClassNames = false;
    /**
     * If true, assertions (currently only if specified by Bean Validation Annotations) are included.
     */
    @Getter
    @Setter
    @Builder.Default private boolean showAssertions = true;
    /**
     * If false, generally no methods are included
     */
    @Getter
    @Setter
    @Builder.Default private boolean showMethods = true;
    /**
     * If true, generally only public methods are included
     */
    @Getter
    @Setter
    @Builder.Default private boolean showOnlyPublicMethods = true;
    /**
     * If true, Aggregate classes are included (AggregateRoot, included Entity, included ValueObject)
     */
    @Getter
    @Setter
    @Builder.Default private boolean showAggregates = true;
    /**
     * If true, fields of Aggregates are included
     */
    @Getter
    @Setter
    @Builder.Default private boolean showAggregateFields = true;
    /**
     * If true, methods of Aggregates are included
     */
    @Getter
    @Setter
    @Builder.Default private boolean showAggregateMethods = true;
    /**
     * If true, DomainEvent classes are included
     */
    @Getter
    @Setter
    @Builder.Default private boolean showDomainEvents = true;
    /**
     * If true, fields of DomainEvents are included
     */
    @Getter
    @Setter
    @Builder.Default private boolean showDomainEventFields = false;
    /**
     * If true, methods of DomainEvents are included
     */
    @Getter
    @Setter
    @Builder.Default private boolean showDomainEventMethods = false;
    /**
     * If true, DomainCommand classes are included
     */
    @Getter
    @Setter
    @Builder.Default private boolean showDomainCommands = true;
    /**
     * DomainCommands might be passed down to subsequent classes in the flow.
     * If true, DomainCommands relations are only drawn on the top most processing class.
     */
    @Getter
    @Setter
    @Builder.Default private boolean showOnlyTopLevelDomainCommandRelations = true;
    /**
     * If true, fields of DomainCommands are included
     */
    @Getter
    @Setter
    @Builder.Default private boolean showDomainCommandFields = false;
    /**
     * If true, methods of DomainCommands are included
     */
    @Getter
    @Setter
    @Builder.Default private boolean showDomainCommandMethods = false;
    /**
     * If true, DomainService classes are included
     */
    @Getter
    @Setter
    @Builder.Default private boolean showDomainServices = true;
    /**
     * If true, fields of DomainServices are included
     */
    @Getter
    @Setter
    @Builder.Default private boolean showDomainServiceFields = false;
    /**
     * If true, methods of DomainServices are included
     */
    @Getter
    @Setter
    @Builder.Default private boolean showDomainServiceMethods = true;
    /**
     * If true, ApplicationService/Driver classes are included
     */
    @Getter
    @Setter
    @Builder.Default private boolean showApplicationServices = true;
    /**
     * If true, fields of ApplicationServices/Drivers are included
     */
    @Getter
    @Setter
    @Builder.Default private boolean showApplicationServiceFields = false;
    /**
     * If true, methods of ApplicationServices/Drivers are included
     */
    @Getter
    @Setter
    @Builder.Default private boolean showApplicationServiceMethods = true;
    /**
     * If true, Repository classes are included
     */
    @Getter
    @Setter
    @Builder.Default private boolean showRepositories = true;
    /**
     * If true, fields of Repositories are included
     */
    @Getter
    @Setter
    @Builder.Default private boolean showRepositoryFields = false;
    /**
     * If true, methods of Repositories are included
     */
    @Getter
    @Setter
    @Builder.Default private boolean showRepositoryMethods = true;
    /**
     * If true, ReadModel classes are included
     */
    @Getter
    @Setter
    @Builder.Default private boolean showReadModels = true;
    /**
     * If true, fields of ReadModels are included
     */
    @Getter
    @Setter
    @Builder.Default private boolean showReadModelFields = true;
    /**
     * If true, methods of ReadModels are included
     */
    @Getter
    @Setter
    @Builder.Default private boolean showReadModelMethods = false;
    /**
     * If true, QueryHandler classes are included
     */
    @Getter
    @Setter
    @Builder.Default private boolean showQueryHandlers = true;
    /**
     * If true, fields of QueryHandlers are included
     */
    @Getter
    @Setter
    @Builder.Default private boolean showQueryHandlerFields = false;
    /**
     * If true, methods of QueryHandlers are included
     */
    @Getter
    @Setter
    @Builder.Default private boolean showQueryHandlerMethods = false;
    /**
     * If true, OutboundService classes are included
     */
    @Getter
    @Setter
    @Builder.Default private boolean showOutboundServices = true;
    /**
     * If true, fields of OutboundServices are included
     */
    @Getter
    @Setter
    @Builder.Default private boolean showOutboundServiceFields = false;
    /**
     * If true, methods of OutboundServices are included
     */
    @Getter
    @Setter
    @Builder.Default private boolean showOutboundServiceMethods = false;

    /**
     * If true, unspecified ServiceKind classes are included
     */
    @Getter
    @Setter
    @Builder.Default private boolean showUnspecifiedServiceKinds = true;
    /**
     * If true, fields of unspecified ServiceKinds are included
     */
    @Getter
    @Setter
    @Builder.Default private boolean showUnspecifiedServiceKindFields = false;
    /**
     * If true, methods of unspecified ServiceKinds are included
     */
    @Getter
    @Setter
    @Builder.Default private boolean showUnspecifiedServiceKindMethods = false;

    /**
     * If true, the stereotype {@code <Driver>} is used instead of {@code <ApplicationService>}
     */
    @Getter
    @Setter
    @Builder.Default private boolean callApplicationServiceDriver = false;

    /**
     * Fields with named like elements of this black list are excluded
     */
    @Getter
    @Setter
    @ElementCollection(fetch = FetchType.EAGER)
    @Builder.Default private List<String> fieldBlacklist = List.of("concurrencyVersion");

    /**
     * Methods with named like elements of this black list are excluded
     */
    @Getter
    @Setter
    @ElementCollection(fetch = FetchType.EAGER)
    @Builder.Default private List<String> methodBlacklist = List.of(
        "builder",
        "validate",
        "concurrencyVersion",
        "id",
        "findResultById",
        "publish",
        "increaseVersion",
        "equals",
        "hashCode",
        "toString");

    /**
     * If true, members declared by inherited classes are included
     */
    @Getter
    @Setter
    @Builder.Default private boolean showInheritedMembersInClasses = true;
    /**
     * If true, members declared by {@code java.lang.Object} are included
     */
    @Getter
    @Setter
    @Builder.Default private boolean showObjectMembersInClasses = true;
    /**
     * If true, inheritance relationships are rendered for all classes.
     */
    @Getter
    @Setter
    @Builder.Default
    private boolean showAllInheritanceStructures = false;

    /**
     * If true, inheritance relationships are rendered for classes within Aggregates.
     */
    @Getter
    @Setter
    @Builder.Default
    private boolean showInheritanceStructuresInAggregates = true;

    /**
     * If true, inheritance relationships are rendered for ReadModel classes.
     */
    @Getter
    @Setter
    @Builder.Default
    private boolean showInheritanceStructuresForReadModels = false;

    /**
     * If true, inheritance relationships are rendered for DomainEvent classes.
     */
    @Getter
    @Setter
    @Builder.Default
    private boolean showInheritanceStructuresForDomainEvents = false;

    /**
     * If true, inheritance relationships are rendered for DomainCommand classes.
     */
    @Getter
    @Setter
    @Builder.Default
    private boolean showInheritanceStructuresForDomainCommands = false;

    /**
     * If true, inheritance relationships are rendered for all Service classes.
     */
    @Getter
    @Setter
    @Builder.Default
    private boolean showInheritanceStructuresForServiceKinds = false;
    /**
     * If true, multiplicity is added to the associations label.
     */
    @Getter
    @Setter
    @Builder.Default private boolean multiplicityInLabel = true;

    /**
     * Show field 'stereotypes' like {@code <ID>, <ENUM, <IDREF> or <VO>}, indicating
     * what kind of field it is. This can bring clarity when dealing with complex domain models:
     * <ul>
     *   <li>{@code <ID>}: Represents a unique identifier for an Entity in the domain.</li>
     *   <li>{@code <ENUM>}: Indicates that the field is an enumeration, a distinct type that consists of a
     *       set of named constants.</li>
     *   <li>{@code <IDREF>}: Denotes an identifier reference field that holds a reference to another AggregateRoot
     *   .</li>
     *   <li>{@code <VO>}: Signifies a value object.</li>
     * </ul>
     */
    @Getter
    @Setter
    @Builder.Default private boolean fieldStereotypes = true;

    @Getter
    @CreationTimestamp
    private Instant createdAt;

    @Getter
    @UpdateTimestamp
    private Instant changedAt;
}
