# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.3] - Unreleased
- Upgraded to Spring Boot 4.0, Vaadin 25, Jackson 3, Java 21 and DLC 3.5.0. The domain mirror and the static analysis
  result are read and written with the Jackson 3 serializers of DLC, which read the stored data and the uploads of the
  build plugin exactly like the Jackson 2 ones; the Okta starter 3.1.0 supports Spring Boot 4. Container and build
  pipeline run on Java 21
- Flow filter: with the result of a [static analysis](./USER_GUIDE.md#static-analysis) uploaded by the DLC build
  plugin (3.4.0 or later, `runStaticAnalysis = true`), a diagram can be restricted to the
  [flows](./USER_GUIDE.md#filter-on-flows) starting at a class, method, domain command or domain event - forward
  ("what it leads to") or backward ("what leads into it"). The flows follow method calls, published events, processed
  commands and factory methods. Two switches of the flow filter show only the methods called in the flows (on by
  default) and connect classes calling each other by a `<<calls>>` relationship where nothing else connects them (off
  by default); without a static analysis result the flow filter only shows a hint
- A flow can be [shown as text](./USER_GUIDE.md#show-a-flow-as-text): the backward part above the forward part, both
  read in call order, narrowed down by depth, without accessors or by a search, to be copied or downloaded
- [Stream upload](./USER_GUIDE.md#stream-upload): the upload endpoint accepts the gzip-compressed upload of the build
  plugin, also streamed with chunked transfer encoding (`streamUpload = true`), so that large domain models with their
  static analysis result never need to be held in memory completely, neither in the build nor in the viewer
- [Bounded Contexts](./USER_GUIDE.md#filter-on-bounded-contexts): projects declaring Bounded Contexts offer them as
  a filter of a diagram, combined with the package filter. The domain model packages of an upload are kept at the
  project, so that DLC's fallback of one nameless Bounded Context per package is not mistaken for declared ones
- [Analyze Bounded Contexts](./USER_GUIDE.md#analyze-bounded-contexts) in the project view creates a folder per Bounded
  Context with the diagrams of the analyses chosen in a dialog: `Aggregates`, `Aggregate Neighborhood` (per aggregate,
  what leads to it and what it leads to, two steps each), `Read Models` (per top level read model, what leads into
  it) and `Commands` (per command, the flow it triggers and what leads into its processing). Running it again only
  adds what is missing, matched by folder and diagram name, so changed diagrams are kept. The analysis runs in the
  background with a progress dialog. Read Models and Commands show flows: without a static analysis result they are
  greyed out in the dialog, which tells why, while the other analyses remain available
- The structural [connection filters](./USER_GUIDE.md#filter-on-connections--relations) "Include ingoing connections
  to" (what leads to a class) and "Include outgoing connections from" (what it leads to) can be limited in `Depth`
  (0 shows the complete path, the default), and a class can be followed in both directions at once - likewise in
  both exclude filters, offered as "include/exclude ingoing and outgoing connections" in the view filters of a class
- Folders can be nested, and diagram names only need to be unique within their folder. Diagram images are stored
  under the diagram's id (existing images are renamed at startup) and the diagram view is routed by id; the view shows
  the diagram's name above it, below its folder path
- Factories are shown with a style and visibility settings of their own (fields, methods, `<<creates>>` relations),
  see [Factories](./USER_GUIDE.md#factories); classes not implementing any domain marker interface are shown if they
  are related to a service kind, see [Non-domain classes](./USER_GUIDE.md#non-domain-classes)
- New diagram settings: aggregates drawn as their frame only (`Frame only` in the aggregate visibility settings, which
  greys out their fields, methods and inlined value objects meanwhile), the number of fields up to which value objects
  are shown inline, and the methods of outbound services shown by default
- Long names of projects, folders, diagrams and classes wrap in the project and folder views, the side navigation, the
  diagram title and the element level filter - preferably after a dot or underscore or before a new word in camel case
- An own zoom component replaces Zoomist: it fits a diagram also below the minimal scale, fits it again when its
  viewport changes size until the user zooms or moves it, and zooms smoothly on a touchpad
- A diagram can be downloaded as nomnoml source, generated the way it is rendered, flows included; the container
  brings the fonts the diagrams are rendered with
- Performance for large projects (thousands of types, about a million analyzed call sites): the deserialized domain
  models are cached for all users within a memory budget, diagrams are rendered in the background and refreshed in the
  cards once done, projects are loaded without joining all their collections at once, and diagram cards show large
  diagrams as placeholders instead of loading them as preview. New configuration options, see
  [Configuration options](./README.md#configuration-options) and [Memory](./README.md#memory)
- Fixed a path traversal in the resource API serving diagram images
- Only the actuator health check is reachable without signing in; any further actuator endpoint is no longer public
- Fixed refreshing the user interface after a rendering or an analysis in the background, which lacked the signed in
  user
- Added a `llms.txt` describing the project for LLM based tools, fixed all checkstyle findings and compiler warnings,
  and stopped tracking the frontend files generated by Vaadin

## [0.2] - 2026-05-22
- New diagram settings for relationships: show their labels and their stereotypes (both on by default)
- Upgraded to DLC 3.1.0
- Prepared the open source release: Apache License 2.0 (`LICENSE`, `NOTICE`)

## [0.1] - 2026-03-17
- First release of the DLC Diagram Viewer, a service to create and share DDD specific UML class diagrams of domain
  models built with DLC 3.0
- Upload of the domain model by the DLC build plugin (Gradle or Maven) to an API secured by the user's API key; a
  manual upload of a JAR or JSON file can be enabled (`JAR_UPLOAD_ENABLED`). Every new upload regenerates the
  diagrams of the project in the background
- Projects shared with other users, diagrams organized in folders (drag and drop), created from scratch or from
  another diagram as template, renamed and deleted
- Filters per diagram: packages, kinds of building blocks with their fields and methods, single classes, and
  connections to classes (include/exclude ingoing and outgoing connections)
- Notes per class, shown in the diagrams
- Layout and styling options (direction, ranker, acycler, colors and font of the building blocks), see the
  [User Guide](./USER_GUIDE.md)
- Download of diagrams as SVG, PNG or JPEG, and of an SQL DDL script of the domain model if a generator plugin is
  installed
- Self service registration and sign in, optionally Okta (OAuth2) login (`OKTA_LOGIN_ENABLED`, off by default)
- Diagrams rendered by Kroki (nomnoml), data stored in PostgreSQL; Docker images and Docker Compose files for running
  the viewer and for development
