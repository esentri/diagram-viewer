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

package io.domainlifecycles.diagramviewer.rest.filter;

import io.domainlifecycles.diagramviewer.rest.api.DomainMirrorUploadController;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Transparently decompresses gzip-compressed request bodies, so downstream code (e.g. the domain
 * mirror upload endpoint) can read the request body as if it had never been compressed.
 * <p>
 * The DLC build plugin gzip-compresses the domain mirror (and, optionally, static analysis) upload
 * request body, sending it with a {@code Content-Encoding: gzip} header, both for the plain and the
 * streaming upload. Servlet containers do not decompress request bodies on their own - that is only
 * done for response bodies - so without this filter, the upload endpoint would receive raw gzip
 * bytes instead of JSON.
 */
@Component
public class GzipRequestDecompressingFilter extends OncePerRequestFilter {

    private static final String CONTENT_ENCODING_HEADER = "Content-Encoding";
    private static final String GZIP_ENCODING = "gzip";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

        String path = request.getRequestURI();
        boolean isGzipEncoded = GZIP_ENCODING.equalsIgnoreCase(request.getHeader(CONTENT_ENCODING_HEADER));

        if (path.startsWith(DomainMirrorUploadController.UPLOAD_DOMAIN_MIRROR_API_PATH) && isGzipEncoded) {
            filterChain.doFilter(new GzipDecompressingRequestWrapper(request), response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private static final class GzipDecompressingRequestWrapper extends HttpServletRequestWrapper {

        private final GzipServletInputStream decompressedInputStream;

        GzipDecompressingRequestWrapper(HttpServletRequest request) throws IOException {
            super(request);
            this.decompressedInputStream = new GzipServletInputStream(request.getInputStream());
        }

        @Override
        public ServletInputStream getInputStream() {
            return decompressedInputStream;
        }

        @Override
        public BufferedReader getReader() throws IOException {
            String encoding = getCharacterEncoding() != null ? getCharacterEncoding() : StandardCharsets.UTF_8.name();
            return new BufferedReader(new InputStreamReader(decompressedInputStream, encoding));
        }
    }

    private static final class GzipServletInputStream extends ServletInputStream {

        private final GZIPInputStream gzipInputStream;

        GzipServletInputStream(InputStream requestInputStream) throws IOException {
            this.gzipInputStream = new GZIPInputStream(requestInputStream);
        }

        @Override
        public int read() throws IOException {
            return gzipInputStream.read();
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return gzipInputStream.read(b, off, len);
        }

        @Override
        public boolean isFinished() {
            try {
                return gzipInputStream.available() == 0;
            } catch (IOException e) {
                return true;
            }
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener readListener) {
            throw new UnsupportedOperationException(
                "Async read listeners are not supported for gzip-decompressed request bodies.");
        }

        @Override
        public void close() throws IOException {
            gzipInputStream.close();
        }
    }
}
