package io.domainlifecycles.diagramviewer.model;

import io.domainlifecycles.diagram.domain.mapper.TransitiveDomainTypeFilter;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.values.Acycler;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.values.Direction;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.values.Font;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.values.Ranker;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import java.util.Collections;
import java.util.List;

@Entity
public class DiagramStylingConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long diagramStylingConfigurationId;

    private String contextPackageName = null;

    /**
     * Style declaration for AggregateRoots (see Nomnoml style options)
     */
    private String aggregateRootStyle = "fill=#8f8f bold";
    /**
     * Style declaration for AggregateRoot frames (see Nomnoml style options)
     */
    private String aggregateFrameStyle = "visual=frame align=left";
    /**
     * Style declaration for Entities  (see Nomnoml style options)
     */
    private String entityStyle = "fill=#88AAFF bold";
    /**
     * Style declaration for ValueObjects  (see Nomnoml style options)
     */
    private String valueObjectStyle = "fill=#FFFFCC bold";
    /**
     * Style declaration for Enums  (see Nomnoml style options)
     */
    private String enumStyle = "fill=#FFFFCC bold";
    /**
     * Style declaration for Identities  (see Nomnoml style options)
     */
    private String identityStyle = "fill=#FFFFCC bold";
    /**
     * Style declaration for DomainEvents  (see Nomnoml style options)
     */
    private String domainEventStyle = "fill=#CCFFFF bold";
    /**
     * Style declaration for DomainCommands  (see Nomnoml style options)
     */
    private String domainCommandStyle = "fill=#FFB266 bold";
    /**
     * Style declaration for ApplicationServices (see Nomnoml style options)
     */
    private String applicationServiceStyle = "bold";
    /**
     * Style declaration for DomainServices  (see Nomnoml style options)
     */
    private String domainServiceStyle = "fill=#E0E0E0 bold";
    /**
     * Style declaration for Repositories  (see Nomnoml style options)
     */
    private String repositoryStyle = "fill=#C0C0C0 bold";
    /**
     * Style declaration for ReadModels  (see Nomnoml style options)
     */
    private String readModelStyle = "fill=#FFCCE5 bold";
    /**
     * Style declaration for QueryHandlers  (see Nomnoml style options)
     */
    private String queryHandlerStyle = "fill=#C0C0C0 bold";
    /**
     * Style declaration for OutboundServices  (see Nomnoml style options)
     */
    private String outboundServiceStyle = "fill=#C0C0C0 bold";
    /**
     * Style declaration for unspecified ServiceKinds  (see Nomnoml style options)
     */
    private String unspecifiedServiceKindStyle = "fill=#C0C0C0 bold";
    /**
     * General font style declaration  (see Nomnoml style options)
     */
    private Font font = Font.HELVETICA;
    /**
     * General layout direction style declaration (see Nomnoml style options, 'down' or 'right' is supported)
     */
    private Direction direction = Direction.DOWN;
    /**
     * General layout direction style declaration (see Nomnoml style options, 'network-simplex' or 'tight-tree' or
     * 'longest-path' is supported)
     */
    private Ranker ranker = Ranker.LONGEST_PATH;
    /**
     * General acycling style declaration  (see Nomnoml style options, only 'greedy' supported)
     */
    private Acycler acycler = Acycler.GREEDY;
    /**
     * Background color style declaration (see Nomnoml style options, only 'transparent' or HEX color-codes supported)
     */
    private String backgroundColor = "transparent";
    /**
     * Full qualified class names to be excluded from the diagram
     */
    @ElementCollection
    private List<String> classesBlacklist = Collections.emptyList();
    /**
     * If false, generally no fields are included
     */
    private boolean showFields = true;
    /**
     * If true, generally full qualified class names are used
     */
    private boolean showFullQualifiedClassNames = false;
    /**
     * If true, assertions (currently only if specified by Bean Validation Annotations) are included.
     */
    private boolean showAssertions = true;
    /**
     * If false, generally no methods are included
     */
    private boolean showMethods = true;
    /**
     * If true, generally only public methods are included
     */
    private boolean showOnlyPublicMethods = true;
    /**
     * If true, DomainEvent classes are included
     */
    private boolean showDomainEvents = true;
    /**
     * If true, fields of DomainEvents are included
     */
    private boolean showDomainEventFields = false;
    /**
     * If true, methods of DomainEvents are included
     */
    private boolean showDomainEventMethods = false;
    /**
     * If true, DomainCommand classes are included
     */
    private boolean showDomainCommands = true;
    /**
     * DomainCommands might be passed down to subsequent classes in the flow.
     * If true, DomainCommands relations are only drawn on the top most processing class.
     */
    private boolean showOnlyTopLevelDomainCommandRelations = true;
    /**
     * If true, fields of DomainCommands are included
     */
    private boolean showDomainCommandFields = false;
    /**
     * If true, methods of DomainCommands are included
     */
    private boolean showDomainCommandMethods = false;
    /**
     * If true, DomainService classes are included
     */
    private boolean showDomainServices = true;
    /**
     * If true, fields of DomainServices are included
     */
    private boolean showDomainServiceFields = false;
    /**
     * If true, methods of DomainServices are included
     */
    private boolean showDomainServiceMethods = true;
    /**
     * If true, ApplicationService/Driver classes are included
     */
    private boolean showApplicationServices = true;
    /**
     * If true, fields of ApplicationServices/Drivers are included
     */
    private boolean showApplicationServiceFields = false;
    /**
     * If true, methods of ApplicationServices/Drivers are included
     */
    private boolean showApplicationServiceMethods = true;
    /**
     * If true, Repository classes are included
     */
    private boolean showRepositories = true;
    /**
     * If true, fields of Repositories are included
     */
    private boolean showRepositoryFields = false;
    /**
     * If true, methods of Repositories are included
     */
    private boolean showRepositoryMethods = true;
    /**
     * If true, ReadModel classes are included
     */
    private boolean showReadModels = true;
    /**
     * If true, fields of ReadModels are included
     */
    private boolean showReadModelFields = true;
    /**
     * If true, methods of ReadModels are included
     */
    private boolean showReadModelMethods = false;
    /**
     * If true, QueryHandler classes are included
     */
    private boolean showQueryHandlers = true;
    /**
     * If true, fields of QueryHandlers are included
     */
    private boolean showQueryHandlerFields = false;
    /**
     * If true, methods of QueryHandlers are included
     */
    private boolean showQueryHandlerMethods = false;
    /**
     * If true, OutboundService classes are included
     */
    private boolean showOutboundServices = true;
    /**
     * If true, fields of OutboundServices are included
     */
    private boolean showOutboundServiceFields = false;
    /**
     * If true, methods of OutboundServices are included
     */
    private boolean showOutboundServiceMethods = false;

    /**
     * If true, unspecified ServiceKind classes are included
     */
    private boolean showUnspecifiedServiceKinds = true;
    /**
     * If true, fields of unspecified ServiceKinds are included
     */
    private boolean showUnspecifiedServiceKindFields = false;
    /**
     * If true, methods of unspecified ServiceKinds are included
     */
    private boolean showUnspecifiedServiceKindMethods = false;

    /**
     * If true, the stereotype {@code <Driver>} is used instead of {@code <ApplicationService>}
     */
    private boolean callApplicationServiceDriver = true;

    /**
     * Fields with named like elements of this black list are excluded
     */
    @ElementCollection
    private List<String> fieldBlacklist = List.of("concurrencyVersion");

    /**
     * Methods with named like elements of this black list are excluded
     */
    @ElementCollection
    private List<String> methodBlacklist = List.of(
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
    private boolean showInheritedMembersInClasses = true;
    /**
     * If true, members declared by {@code java.lang.Object} are included
     */
    private boolean showObjectMembersInClasses = true;

    /**
     * If true, multiplicity is added to the associations label.
     */
    private boolean multiplicityInLabel = true;

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
    private boolean fieldStereotypes = true;

    /**
     * Enabling and initializing the seed for the {@link TransitiveDomainTypeFilter}.
     */
    @ElementCollection
    private List<String> transitiveFilterSeedDomainServiceTypeNames = Collections.emptyList();


    public void setDiagramStylingConfigurationId(Long diagramConfigurationId) {
        this.diagramStylingConfigurationId = diagramConfigurationId;
    }

    public Long getDiagramStylingConfigurationId() {
        return diagramStylingConfigurationId;
    }

    public String getContextPackageName() {
        return contextPackageName;
    }

    public void setContextPackageName(String contextPackageName) {
        this.contextPackageName = contextPackageName;
    }

    public String getAggregateRootStyle() {
        return aggregateRootStyle;
    }

    public void setAggregateRootStyle(String aggregateRootStyle) {
        this.aggregateRootStyle = aggregateRootStyle;
    }

    public String getAggregateFrameStyle() {
        return aggregateFrameStyle;
    }

    public void setAggregateFrameStyle(String aggregateFrameStyle) {
        this.aggregateFrameStyle = aggregateFrameStyle;
    }

    public String getEntityStyle() {
        return entityStyle;
    }

    public void setEntityStyle(String entityStyle) {
        this.entityStyle = entityStyle;
    }

    public String getValueObjectStyle() {
        return valueObjectStyle;
    }

    public void setValueObjectStyle(String valueObjectStyle) {
        this.valueObjectStyle = valueObjectStyle;
    }

    public String getEnumStyle() {
        return enumStyle;
    }

    public void setEnumStyle(String enumStyle) {
        this.enumStyle = enumStyle;
    }

    public String getIdentityStyle() {
        return identityStyle;
    }

    public void setIdentityStyle(String identityStyle) {
        this.identityStyle = identityStyle;
    }

    public String getDomainEventStyle() {
        return domainEventStyle;
    }

    public void setDomainEventStyle(String domainEventStyle) {
        this.domainEventStyle = domainEventStyle;
    }

    public String getDomainCommandStyle() {
        return domainCommandStyle;
    }

    public void setDomainCommandStyle(String domainCommandStyle) {
        this.domainCommandStyle = domainCommandStyle;
    }

    public String getApplicationServiceStyle() {
        return applicationServiceStyle;
    }

    public void setApplicationServiceStyle(String applicationServiceStyle) {
        this.applicationServiceStyle = applicationServiceStyle;
    }

    public String getDomainServiceStyle() {
        return domainServiceStyle;
    }

    public void setDomainServiceStyle(String domainServiceStyle) {
        this.domainServiceStyle = domainServiceStyle;
    }

    public String getRepositoryStyle() {
        return repositoryStyle;
    }

    public void setRepositoryStyle(String repositoryStyle) {
        this.repositoryStyle = repositoryStyle;
    }

    public String getReadModelStyle() {
        return readModelStyle;
    }

    public void setReadModelStyle(String readModelStyle) {
        this.readModelStyle = readModelStyle;
    }

    public String getQueryHandlerStyle() {
        return queryHandlerStyle;
    }

    public void setQueryHandlerStyle(String queryHandlerStyle) {
        this.queryHandlerStyle = queryHandlerStyle;
    }

    public String getOutboundServiceStyle() {
        return outboundServiceStyle;
    }

    public void setOutboundServiceStyle(String outboundServiceStyle) {
        this.outboundServiceStyle = outboundServiceStyle;
    }

    public String getUnspecifiedServiceKindStyle() {
        return unspecifiedServiceKindStyle;
    }

    public void setUnspecifiedServiceKindStyle(String unspecifiedServiceKindStyle) {
        this.unspecifiedServiceKindStyle = unspecifiedServiceKindStyle;
    }

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        this.font = font;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public Ranker getRanker() {
        return ranker;
    }

    public void setRanker(Ranker ranker) {
        this.ranker = ranker;
    }

    public Acycler getAcycler() {
        return acycler;
    }

    public void setAcycler(Acycler acycler) {
        this.acycler = acycler;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public List<String> getClassesBlacklist() {
        return classesBlacklist;
    }

    public void setClassesBlacklist(List<String> classesBlacklist) {
        this.classesBlacklist = classesBlacklist;
    }

    public boolean isShowFields() {
        return showFields;
    }

    public void setShowFields(boolean showFields) {
        this.showFields = showFields;
    }

    public boolean isShowFullQualifiedClassNames() {
        return showFullQualifiedClassNames;
    }

    public void setShowFullQualifiedClassNames(boolean showFullQualifiedClassNames) {
        this.showFullQualifiedClassNames = showFullQualifiedClassNames;
    }

    public boolean isShowAssertions() {
        return showAssertions;
    }

    public void setShowAssertions(boolean showAssertions) {
        this.showAssertions = showAssertions;
    }

    public boolean isShowMethods() {
        return showMethods;
    }

    public void setShowMethods(boolean showMethods) {
        this.showMethods = showMethods;
    }

    public boolean isShowOnlyPublicMethods() {
        return showOnlyPublicMethods;
    }

    public void setShowOnlyPublicMethods(boolean showOnlyPublicMethods) {
        this.showOnlyPublicMethods = showOnlyPublicMethods;
    }

    public boolean isShowDomainEvents() {
        return showDomainEvents;
    }

    public void setShowDomainEvents(boolean showDomainEvents) {
        this.showDomainEvents = showDomainEvents;
    }

    public boolean isShowDomainEventFields() {
        return showDomainEventFields;
    }

    public void setShowDomainEventFields(boolean showDomainEventFields) {
        this.showDomainEventFields = showDomainEventFields;
    }

    public boolean isShowDomainEventMethods() {
        return showDomainEventMethods;
    }

    public void setShowDomainEventMethods(boolean showDomainEventMethods) {
        this.showDomainEventMethods = showDomainEventMethods;
    }

    public boolean isShowDomainCommands() {
        return showDomainCommands;
    }

    public void setShowDomainCommands(boolean showDomainCommands) {
        this.showDomainCommands = showDomainCommands;
    }

    public boolean isShowOnlyTopLevelDomainCommandRelations() {
        return showOnlyTopLevelDomainCommandRelations;
    }

    public void setShowOnlyTopLevelDomainCommandRelations(boolean showOnlyTopLevelDomainCommandRelations) {
        this.showOnlyTopLevelDomainCommandRelations = showOnlyTopLevelDomainCommandRelations;
    }

    public boolean isShowDomainCommandFields() {
        return showDomainCommandFields;
    }

    public void setShowDomainCommandFields(boolean showDomainCommandFields) {
        this.showDomainCommandFields = showDomainCommandFields;
    }

    public boolean isShowDomainCommandMethods() {
        return showDomainCommandMethods;
    }

    public void setShowDomainCommandMethods(boolean showDomainCommandMethods) {
        this.showDomainCommandMethods = showDomainCommandMethods;
    }

    public boolean isShowDomainServices() {
        return showDomainServices;
    }

    public void setShowDomainServices(boolean showDomainServices) {
        this.showDomainServices = showDomainServices;
    }

    public boolean isShowDomainServiceFields() {
        return showDomainServiceFields;
    }

    public void setShowDomainServiceFields(boolean showDomainServiceFields) {
        this.showDomainServiceFields = showDomainServiceFields;
    }

    public boolean isShowDomainServiceMethods() {
        return showDomainServiceMethods;
    }

    public void setShowDomainServiceMethods(boolean showDomainServiceMethods) {
        this.showDomainServiceMethods = showDomainServiceMethods;
    }

    public boolean isShowApplicationServices() {
        return showApplicationServices;
    }

    public void setShowApplicationServices(boolean showApplicationServices) {
        this.showApplicationServices = showApplicationServices;
    }

    public boolean isShowApplicationServiceFields() {
        return showApplicationServiceFields;
    }

    public void setShowApplicationServiceFields(boolean showApplicationServiceFields) {
        this.showApplicationServiceFields = showApplicationServiceFields;
    }

    public boolean isShowApplicationServiceMethods() {
        return showApplicationServiceMethods;
    }

    public void setShowApplicationServiceMethods(boolean showApplicationServiceMethods) {
        this.showApplicationServiceMethods = showApplicationServiceMethods;
    }

    public boolean isShowRepositories() {
        return showRepositories;
    }

    public void setShowRepositories(boolean showRepositories) {
        this.showRepositories = showRepositories;
    }

    public boolean isShowRepositoryFields() {
        return showRepositoryFields;
    }

    public void setShowRepositoryFields(boolean showRepositoryFields) {
        this.showRepositoryFields = showRepositoryFields;
    }

    public boolean isShowRepositoryMethods() {
        return showRepositoryMethods;
    }

    public void setShowRepositoryMethods(boolean showRepositoryMethods) {
        this.showRepositoryMethods = showRepositoryMethods;
    }

    public boolean isShowReadModels() {
        return showReadModels;
    }

    public void setShowReadModels(boolean showReadModels) {
        this.showReadModels = showReadModels;
    }

    public boolean isShowReadModelFields() {
        return showReadModelFields;
    }

    public void setShowReadModelFields(boolean showReadModelFields) {
        this.showReadModelFields = showReadModelFields;
    }

    public boolean isShowReadModelMethods() {
        return showReadModelMethods;
    }

    public void setShowReadModelMethods(boolean showReadModelMethods) {
        this.showReadModelMethods = showReadModelMethods;
    }

    public boolean isShowQueryHandlers() {
        return showQueryHandlers;
    }

    public void setShowQueryHandlers(boolean showQueryHandlers) {
        this.showQueryHandlers = showQueryHandlers;
    }

    public boolean isShowQueryHandlerFields() {
        return showQueryHandlerFields;
    }

    public void setShowQueryHandlerFields(boolean showQueryHandlerFields) {
        this.showQueryHandlerFields = showQueryHandlerFields;
    }

    public boolean isShowQueryHandlerMethods() {
        return showQueryHandlerMethods;
    }

    public void setShowQueryHandlerMethods(boolean showQueryHandlerMethods) {
        this.showQueryHandlerMethods = showQueryHandlerMethods;
    }

    public boolean isShowOutboundServices() {
        return showOutboundServices;
    }

    public void setShowOutboundServices(boolean showOutboundServices) {
        this.showOutboundServices = showOutboundServices;
    }

    public boolean isShowOutboundServiceFields() {
        return showOutboundServiceFields;
    }

    public void setShowOutboundServiceFields(boolean showOutboundServiceFields) {
        this.showOutboundServiceFields = showOutboundServiceFields;
    }

    public boolean isShowOutboundServiceMethods() {
        return showOutboundServiceMethods;
    }

    public void setShowOutboundServiceMethods(boolean showOutboundServiceMethods) {
        this.showOutboundServiceMethods = showOutboundServiceMethods;
    }

    public boolean isShowUnspecifiedServiceKinds() {
        return showUnspecifiedServiceKinds;
    }

    public void setShowUnspecifiedServiceKinds(boolean showUnspecifiedServiceKinds) {
        this.showUnspecifiedServiceKinds = showUnspecifiedServiceKinds;
    }

    public boolean isShowUnspecifiedServiceKindFields() {
        return showUnspecifiedServiceKindFields;
    }

    public void setShowUnspecifiedServiceKindFields(boolean showUnspecifiedServiceKindFields) {
        this.showUnspecifiedServiceKindFields = showUnspecifiedServiceKindFields;
    }

    public boolean isShowUnspecifiedServiceKindMethods() {
        return showUnspecifiedServiceKindMethods;
    }

    public void setShowUnspecifiedServiceKindMethods(boolean showUnspecifiedServiceKindMethods) {
        this.showUnspecifiedServiceKindMethods = showUnspecifiedServiceKindMethods;
    }

    public boolean isCallApplicationServiceDriver() {
        return callApplicationServiceDriver;
    }

    public void setCallApplicationServiceDriver(boolean callApplicationServiceDriver) {
        this.callApplicationServiceDriver = callApplicationServiceDriver;
    }

    public List<String> getFieldBlacklist() {
        return fieldBlacklist;
    }

    public void setFieldBlacklist(List<String> fieldBlacklist) {
        this.fieldBlacklist = fieldBlacklist;
    }

    public List<String> getMethodBlacklist() {
        return methodBlacklist;
    }

    public void setMethodBlacklist(List<String> methodBlacklist) {
        this.methodBlacklist = methodBlacklist;
    }

    public boolean isShowInheritedMembersInClasses() {
        return showInheritedMembersInClasses;
    }

    public void setShowInheritedMembersInClasses(boolean showInheritedMembersInClasses) {
        this.showInheritedMembersInClasses = showInheritedMembersInClasses;
    }

    public boolean isShowObjectMembersInClasses() {
        return showObjectMembersInClasses;
    }

    public void setShowObjectMembersInClasses(boolean showObjectMembersInClasses) {
        this.showObjectMembersInClasses = showObjectMembersInClasses;
    }

    public boolean isMultiplicityInLabel() {
        return multiplicityInLabel;
    }

    public void setMultiplicityInLabel(boolean multiplicityInLabel) {
        this.multiplicityInLabel = multiplicityInLabel;
    }

    public boolean isFieldStereotypes() {
        return fieldStereotypes;
    }

    public void setFieldStereotypes(boolean fieldStereotypes) {
        this.fieldStereotypes = fieldStereotypes;
    }

    public List<String> getTransitiveFilterSeedDomainServiceTypeNames() {
        return transitiveFilterSeedDomainServiceTypeNames;
    }

    public void setTransitiveFilterSeedDomainServiceTypeNames(List<String> transitiveFilterSeedDomainServiceTypeNames) {
        this.transitiveFilterSeedDomainServiceTypeNames = transitiveFilterSeedDomainServiceTypeNames;
    }
}
