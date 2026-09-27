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

package io.domainlifecycles.diagramviewer.util;

import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.function.Consumer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * gzip compression of the stored domain model JSON (domain mirror and static analysis result).
 * <p>
 * The JSON of a large domain model can reach several gigabytes, beyond PostgreSQL's limit of 1 GB per
 * field value, and holding it uncompressed in memory would dominate the heap. It is therefore
 * compressed as it is produced and decompressed as it is read, never materialized as one large string.
 */
public final class CompressedJson {

    /**
     * Upper bound for a single compressed value: PostgreSQL limits a {@code bytea} value to 1 GB; a
     * little headroom is kept below that for the protocol overhead.
     */
    public static final long MAX_COMPRESSED_BYTES = 1000L * 1024 * 1024;

    private static final int BUFFER_SIZE = 1 << 16;

    private CompressedJson() {
    }

    /**
     * Compresses what the given writer writes.
     *
     * @param writer writes the uncompressed JSON to the stream it is given
     * @return the gzip-compressed bytes
     */
    public static byte[] compress(Consumer<OutputStream> writer) {
        ByteArrayOutputStream compressed = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(compressed, BUFFER_SIZE)) {
            writer.accept(gzip);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return compressed.toByteArray();
    }

    /**
     * @param compressed gzip-compressed bytes
     * @return a stream of the decompressed content
     */
    public static InputStream decompress(byte[] compressed) {
        try {
            return new GZIPInputStream(new ByteArrayInputStream(compressed), BUFFER_SIZE);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Fails with a descriptive message if a compressed value exceeds what can be stored.
     *
     * @param compressed the compressed value
     * @param what       what the value is, for the message
     */
    public static void checkStorable(byte[] compressed, String what) {
        if (compressed != null && compressed.length > MAX_COMPRESSED_BYTES) {
            throw DiagramViewerException.fail(String.format(
                "The %s is too large to be stored: %,d bytes even compressed, the limit is %,d bytes. "
                    + "Restrict the uploaded domain model, e.g. via domainModelPackages or staticAnalysisPackages.",
                what, compressed.length, MAX_COMPRESSED_BYTES));
        }
    }
}
