package io.domainlifecycles.diagramviewer.rest.api.jackson;

import tools.jackson.databind.ObjectMapper;
import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import io.domainlifecycles.diagramviewer.scenario.RezeptionScenario;
import io.domainlifecycles.diagramviewer.util.CompressedJson;
import io.domainlifecycles.mirror.serialize.jackson3.JacksonDomainSerializer;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Covers reading the upload request body into gzip-compressed JSON.
 */
class DomainMirrorUploadPayloadReaderTest {

    private final DomainMirrorUploadPayloadReader reader =
        new DomainMirrorUploadPayloadReader(new JacksonDomainSerializer(false));

    @Test
    void Should_ReadAllDomainModelPackages() throws Exception {

        // when
        DomainMirrorUploadPayload payload = read("{\"domainMirror\":" + RezeptionScenario.domainMirrorJson()
            + ",\"domainModelPackages\":[\"a.first\",\"b.second\"]}");

        // then
        assertThat(payload.domainModelPackages()).containsExactly("a.first", "b.second");
    }

    @Test
    void Should_ReturnMirrorAndDomainCallsAsCompressedJson() throws Exception {

        // when
        DomainMirrorUploadPayload payload = read(RezeptionScenario.uploadRequestBody());

        // then: decompressed, the JSON is semantically unchanged and can be read back
        ObjectMapper objectMapper = new ObjectMapper();
        assertThat(objectMapper.readTree(decompress(payload.domainMirrorGz())))
            .isEqualTo(objectMapper.readTree(RezeptionScenario.domainMirrorJson()));
        assertThat(objectMapper.readTree(decompress(payload.domainCallsGz())))
            .isEqualTo(objectMapper.readTree(RezeptionScenario.domainCallsJson()));
        assertThat(payload.domainModelPackages()).containsExactly(RezeptionScenario.DOMAIN_MODEL_PACKAGE);
        assertThat(new JacksonDomainSerializer(false).deserialize(decompress(payload.domainMirrorGz()))
            .getDomainTypeMirror(RezeptionScenario.BUCHUNG_AGGREGATE)).isPresent();
        // and it is actually compressed
        assertThat(payload.domainCallsGz().length).isLessThan(RezeptionScenario.domainCallsJson().length() / 5);
    }

    @Test
    void Should_ReturnNoDomainCalls_When_TheyAreMissingOrNull() {

        // when
        DomainMirrorUploadPayload missing = read("{\"domainMirror\":" + RezeptionScenario.domainMirrorJson() + "}");
        DomainMirrorUploadPayload explicitNull = read("{\"domainMirror\":" + RezeptionScenario.domainMirrorJson()
            + ",\"domainCalls\":null}");

        // then: an explicit JSON null must not be stored as the text "null"
        assertThat(missing.domainCallsGz()).isNull();
        assertThat(explicitNull.domainCallsGz()).isNull();
    }

    @Test
    void Should_NotValidateDomainCalls_When_Uploaded() throws Exception {

        // when: the static analysis result does not match the domain mirror at all
        DomainMirrorUploadPayload payload = read("{\"domainMirror\":" + RezeptionScenario.domainMirrorJson()
            + ",\"domainCalls\":{\"unexpected\":true}}");

        // then: it is accepted as is - it is only resolved when a flow filter first needs it
        assertThat(decompress(payload.domainCallsGz())).isEqualTo("{\"unexpected\":true}");
    }

    @Test
    void Should_RejectUpload_When_DomainMirrorIsInvalid() {

        assertThatThrownBy(() -> read("{\"domainMirror\":{\"unexpected\":true}}"))
            .isInstanceOf(RuntimeException.class);
    }

    @Test
    void Should_RejectUpload_When_DomainMirrorIsMissing() {

        assertThatThrownBy(() -> read("{\"domainCalls\":{}}"))
            .isInstanceOf(DiagramViewerException.class)
            .hasMessageContaining("domainMirror");
    }

    private static String decompress(byte[] compressed) throws IOException {
        try (InputStream json = CompressedJson.decompress(compressed)) {
            return new String(json.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private DomainMirrorUploadPayload read(String body) {
        return reader.read(new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)));
    }
}
