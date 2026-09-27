/*
 *     ________  .__
 *     \______ \ |__|____     ________________    _____
 *      |    |  \|  \__  \   / ___\_  __ \__  \  /     \
 *      |    |   \  |/ __ \_/ /_/  >  | \// __ \|  Y Y  \
 *     /_______  /__(____  /\___  /|__|  (____  /__|_|  /
 *             \/        \//_____/            \/      \/
 *     ____   ____.__
 *     \   \ /   /|__| ______  _  __ ___________
 *      \   Y   / |  |/ __ \ \/ \/ // __ \_  __ \
 *       \     /  |  \  ___/\     /\  ___/|  | \/
 *        \___/   |__|\___  >\/\_/  \___  >__|
 *                        \/            \/
 *
 *  Copyright 2025-2026 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.domainlifecycles.diagramviewer.service;

import io.domainlifecycles.mirror.api.AggregateRootMirror;
import io.domainlifecycles.mirror.api.DomainMirror;
import io.domainlifecycles.mirror.api.DomainTypeMirror;
import io.domainlifecycles.staticanalysis.DomainCalls;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * The model data of one project at one point in time, as held by the {@link ProjectModelCache} and shared by
 * all sessions and the diagram regeneration: the domain mirror, the type lists derived from it for the view
 * filters, and - loaded lazily, at most once - the static analysis result.
 */
public final class ProjectModel {

    private final Instant lastUpdated;
    private final DomainMirror domainMirror;
    private final List<AggregateRootMirror> aggregateRootMirrors;
    private final List<DomainTypeMirror> domainTypeMirrors;
    private final boolean domainCallsAvailable;
    private final Supplier<Optional<DomainCalls>> domainCallsLoader;
    private final long domainMirrorBytes;
    private volatile Consumer<ProjectModel> domainCallsLoadedListener = model -> { };
    private DomainCalls domainCalls;
    private boolean domainCallsLoaded;
    private volatile long domainCallsBytes;

    /**
     * @param lastUpdated          the project's change timestamp this model reflects
     * @param domainMirror         the project's domain mirror
     * @param aggregateRootMirrors the aggregate roots of the domain mirror
     * @param domainTypeMirrors    the domain types offered by the view filters
     * @param domainCallsAvailable whether a static analysis result was uploaded for the project
     * @param domainCallsLoader    loads the static analysis result on first access
     */
    public ProjectModel(Instant lastUpdated,
                        DomainMirror domainMirror,
                        List<AggregateRootMirror> aggregateRootMirrors,
                        List<DomainTypeMirror> domainTypeMirrors,
                        boolean domainCallsAvailable,
                        Supplier<Optional<DomainCalls>> domainCallsLoader) {
        this.lastUpdated = lastUpdated;
        this.domainMirror = domainMirror;
        this.aggregateRootMirrors = aggregateRootMirrors;
        this.domainTypeMirrors = domainTypeMirrors;
        this.domainCallsAvailable = domainCallsAvailable;
        this.domainCallsLoader = domainCallsLoader;
        this.domainMirrorBytes = HeapEstimate.of(domainMirror);
    }

    /**
     * @param listener informed once the static analysis result has been loaded, e.g. so that the cache can account
     *                 for the grown model
     */
    void onDomainCallsLoaded(Consumer<ProjectModel> listener) {
        this.domainCallsLoadedListener = listener;
    }

    /**
     * @return the estimated heap this model occupies: the domain mirror, plus the static analysis result once
     * loaded (see {@link HeapEstimate})
     */
    public long estimatedBytes() {
        return domainMirrorBytes + domainCallsBytes;
    }

    public Instant lastUpdated() {
        return lastUpdated;
    }

    public DomainMirror domainMirror() {
        return domainMirror;
    }

    public List<AggregateRootMirror> aggregateRootMirrors() {
        return aggregateRootMirrors;
    }

    public List<DomainTypeMirror> domainTypeMirrors() {
        return domainTypeMirrors;
    }

    /**
     * @return {@code true} if a static analysis result was uploaded for the project; does not load it
     */
    public boolean domainCallsAvailable() {
        return domainCallsAvailable;
    }

    /**
     * Returns the static analysis result, loading and deserializing it on first access - only flow filters need
     * it, and it is typically the largest part of a project's model data. Synchronized, since the model is
     * shared: concurrent first accesses load it only once.
     *
     * @return the static analysis result, empty if none was uploaded
     */
    public Optional<DomainCalls> domainCalls() {
        if (!domainCallsAvailable) {
            return Optional.empty();
        }
        boolean loadedNow = false;
        DomainCalls result;
        synchronized (this) {
            if (!domainCallsLoaded) {
                domainCalls = domainCallsLoader.get().orElse(null);
                domainCallsBytes = HeapEstimate.of(domainCalls);
                domainCallsLoaded = true;
                loadedNow = true;
            }
            result = domainCalls;
        }
        if (loadedNow) {
            // outside the lock: the listener may touch the cache
            domainCallsLoadedListener.accept(this);
        }
        return Optional.ofNullable(result);
    }
}
