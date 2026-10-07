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

import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import io.domainlifecycles.diagramviewer.util.CompressedJson;
import io.domainlifecycles.mirror.serialize.DomainSerializer;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonEncoding;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.core.StreamWriteFeature;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

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
 * The request body is walked field by field with a streaming {@link JsonParser}, and the domain mirror
 * and the static analysis result are copied out token by token as compact JSON directly into a gzip
 * stream ({@link CompressedJson}) - never built up as a {@code JsonNode} tree, nor as an uncompressed
 * string. For a large domain model the uncompressed JSON reaches several gigabytes; only its compressed
 * form, a fraction of that, is held in memory and stored.
 * <p>
 * The domain mirror is validated by deserializing it once, streamed from its compressed form; the
 * resulting object graph is discarded. The static analysis result is deliberately not validated on
 * upload: it is only resolved against the domain mirror when a flow filter first needs it.
 */
@Component
public class DomainMirrorUploadPayloadReader {

    private static final String FIELD_DOMAIN_MIRROR = "domainMirror";
    private static final String FIELD_DOMAIN_CALLS = "domainCalls";
    private static final String FIELD_DOMAIN_MODEL_PACKAGES = "domainModelPackages";

    private final ObjectMapper objectMapper;
    private final DomainSerializer domainSerializer;

    public DomainMirrorUploadPayloadReader(DomainSerializer domainSerializer) {
        // the generators copy into a gzip stream that is finished by the caller, and the packages are read in the
        // middle of the request body, which goes on after them
        this.objectMapper = JsonMapper.builder()
            .disable(StreamWriteFeature.AUTO_CLOSE_TARGET)
            .disable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
            .build();
        this.domainSerializer = domainSerializer;
    }

    /**
     * Reads and validates a domain mirror upload request body from the given (already
     * gzip-decompressed) stream. The stream is read from, but not closed.
     *
     * @param requestBody the request body to read, not gzip-compressed
     * @return the gzip-compressed JSON of the uploaded domain mirror (validated) and static analysis
     * result (if any), and the associated domain model packages
     */
    public DomainMirrorUploadPayload read(InputStream requestBody) {
        try {
            return doRead(requestBody);
        } catch (IOException | JacksonException e) {
            throw DiagramViewerException.fail("Could not read the domain mirror upload request body.", e);
        }
    }

    private DomainMirrorUploadPayload doRead(InputStream requestBody) throws IOException {
        byte[] domainMirrorGz = null;
        byte[] domainCallsGz = null;
        List<String> domainModelPackages = List.of();

        try (JsonParser parser = objectMapper.createParser(requestBody)) {
            if (parser.nextToken() != JsonToken.START_OBJECT) {
                throw DiagramViewerException.fail("Expected a JSON object as the domain mirror upload request body.");
            }

            while (parser.nextToken() == JsonToken.PROPERTY_NAME) {
                String fieldName = parser.currentName();
                parser.nextToken();

                switch (fieldName) {
                    case FIELD_DOMAIN_MIRROR -> domainMirrorGz = readCompressedJson(parser);
                    case FIELD_DOMAIN_CALLS -> domainCallsGz = readCompressedJson(parser);
                    case FIELD_DOMAIN_MODEL_PACKAGES -> domainModelPackages = readStringList(parser);
                    default -> parser.skipChildren();
                }
            }
        }

        if (domainMirrorGz == null) {
            throw DiagramViewerException.fail(
                "The domain mirror upload request body is missing the required '%s' field.", FIELD_DOMAIN_MIRROR);
        }

        CompressedJson.checkStorable(domainMirrorGz, "domain mirror");
        CompressedJson.checkStorable(domainCallsGz, "static analysis result (domainCalls)");

        // validation only: a domain mirror that cannot be read back must not be stored
        try (InputStream domainMirrorJson = CompressedJson.decompress(domainMirrorGz)) {
            domainSerializer.deserialize(domainMirrorJson);
        }

        return new DomainMirrorUploadPayload(domainMirrorGz, domainCallsGz, domainModelPackages);
    }

    /**
     * Copies the JSON value the parser currently points at, token by token, as compact JSON into a gzip
     * stream.
     *
     * @return the gzip-compressed JSON, or {@code null} for a JSON {@code null} value
     */
    private byte[] readCompressedJson(JsonParser parser) {
        if (parser.currentToken() == JsonToken.VALUE_NULL) {
            return null;
        }
        return CompressedJson.compress(out -> {
            try (JsonGenerator generator = objectMapper.createGenerator(out, JsonEncoding.UTF8)) {
                generator.copyCurrentStructure(parser);
            }
        });
    }

    private List<String> readStringList(JsonParser parser) {
        return objectMapper.readValue(parser, new TypeReference<List<String>>() {
        });
    }
}
