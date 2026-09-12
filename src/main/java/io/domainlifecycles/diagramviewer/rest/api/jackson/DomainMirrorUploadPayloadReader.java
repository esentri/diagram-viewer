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

package io.domainlifecycles.diagramviewer.rest.api.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import io.domainlifecycles.mirror.api.DomainMirror;
import io.domainlifecycles.mirror.serialize.DomainSerializer;
import io.domainlifecycles.staticanalysis.serialize.DomainCallsSerializer;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Reads a domain mirror upload request body, in the wire format used by the DLC build plugin since
 * version 3.4.0:
 * <pre>{@code
 * {
 *   "domainMirror": { ... },
 *   "domainCalls": { ... },
 *   "domainModelPackages": [ "..." ]
 * }
 * }</pre>
 * {@code domainCalls} - the result of a static analysis of the domain classes - is optional and
 * omitted entirely when the upload did not run one.
 * <p>
 * The request body is walked field by field with a streaming {@link JsonParser} rather than read
 * into a single {@link JsonNode} tree upfront: the plugin sends both the plain and the
 * (chunked-transfer-encoded) streaming upload as gzip-compressed JSON that, for a domain of a few
 * hundred types, can already reach the tens of megabytes, so buffering the whole request body as one
 * in-memory tree in addition to the {@link DomainMirror} object graph it is deserialized into would
 * undercut the very memory savings the streaming upload is meant to provide.
 */
@Component
public class DomainMirrorUploadPayloadReader {

    private static final String FIELD_DOMAIN_MIRROR = "domainMirror";
    private static final String FIELD_DOMAIN_CALLS = "domainCalls";
    private static final String FIELD_DOMAIN_MODEL_PACKAGES = "domainModelPackages";

    private final ObjectMapper objectMapper;
    private final DomainSerializer domainSerializer;
    private final DomainCallsSerializer domainCallsSerializer;

    public DomainMirrorUploadPayloadReader(DomainSerializer domainSerializer, DomainCallsSerializer domainCallsSerializer) {
        this.objectMapper = new ObjectMapper();
        this.domainSerializer = domainSerializer;
        this.domainCallsSerializer = domainCallsSerializer;
    }

    /**
     * Reads and validates a domain mirror upload request body from the given (already
     * gzip-decompressed) stream. The stream is read from, but not closed.
     *
     * @param requestBody the request body to read, not gzip-compressed
     * @return the uploaded domain mirror, the raw JSON of the uploaded static analysis result (if
     * any), and the associated domain model packages
     */
    public DomainMirrorUploadPayload read(InputStream requestBody) {
        try {
            return doRead(requestBody);
        } catch (IOException e) {
            throw DiagramViewerException.fail("Could not read the domain mirror upload request body.", e);
        }
    }

    private DomainMirrorUploadPayload doRead(InputStream requestBody) throws IOException {
        String domainMirrorJson = null;
        String domainCallsJson = null;
        List<String> domainModelPackages = List.of();

        try (JsonParser parser = objectMapper.getFactory().createParser(requestBody)) {
            if (parser.nextToken() != JsonToken.START_OBJECT) {
                throw DiagramViewerException.fail("Expected a JSON object as the domain mirror upload request body.");
            }

            while (parser.nextToken() == JsonToken.FIELD_NAME) {
                String fieldName = parser.currentName();
                parser.nextToken();

                switch (fieldName) {
                    case FIELD_DOMAIN_MIRROR -> domainMirrorJson = readRawJson(parser);
                    case FIELD_DOMAIN_CALLS -> domainCallsJson = readRawJson(parser);
                    case FIELD_DOMAIN_MODEL_PACKAGES -> domainModelPackages = readStringList(parser);
                    default -> parser.skipChildren();
                }
            }
        }

        if (domainMirrorJson == null) {
            throw DiagramViewerException.fail(
                "The domain mirror upload request body is missing the required '%s' field.", FIELD_DOMAIN_MIRROR);
        }

        DomainMirror domainMirror = domainSerializer.deserialize(domainMirrorJson);
        if (domainCallsJson != null) {
            // fail fast if the static analysis result cannot be resolved against the uploaded domain mirror,
            // rather than persisting a DomainCalls that could never be read back
            domainCallsSerializer.deserialize(domainCallsJson, domainMirror);
        }

        return new DomainMirrorUploadPayload(domainMirror, domainCallsJson, domainModelPackages);
    }

    private String readRawJson(JsonParser parser) throws IOException {
        JsonNode node = objectMapper.readTree(parser);
        return node.toString();
    }

    private List<String> readStringList(JsonParser parser) throws IOException {
        return objectMapper.readValue(parser, new TypeReference<List<String>>() {
        });
    }
}
