package io.domainlifecycles.diagramviewer.scenario;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.zip.GZIPOutputStream;

/**
 * Real-world test data for the "rezeption" ({@code com.esentri.rezeption}) hotel booking domain,
 * taken from the esentri/ddd-hotel-demo project (a DLC sample application). This is the actual
 * domain a real DLC build plugin run analyzed: a {@code Buchung} (booking) aggregate with a guest
 * check-in/check-out flow, a {@code Zimmer} (room) aggregate, and the application services and
 * repositories tying them together.
 * <p>
 * The fixtures ({@code /rezeption/domain-mirror.json}, {@code /rezeption/domain-calls.json}) are a
 * real serialized {@code DomainMirror} and the real result of running the DLC static analysis
 * (producing {@code DomainCalls}) against that project's compiled classes - not hand-written stubs.
 * They are frozen, self-contained copies: using them here does not require the ddd-hotel-demo
 * project to be cloned, built, or even present.
 * <p>
 * The check-out flow starting at {@link #CHECK_OUT_COMMAND} is the same one the ddd-hotel-demo
 * project itself restricts its own diagram to (see its {@code build.gradle}
 * {@code dlcGradlePlugin.diagram.diagrams.diagramSvg.includeFlowsFrom}), so tests built on it
 * exercise the flow filter against its intended, real-world use case.
 */
public final class RezeptionScenario {

    public static final String DOMAIN_MODEL_PACKAGE = "com.esentri.rezeption";

    /** The {@code Buchung} (booking) aggregate root. */
    public static final String BUCHUNG_AGGREGATE = "com.esentri.rezeption.domain.buchung.Buchung";

    /** The {@code Zimmer} (room) aggregate root. */
    public static final String ZIMMER_AGGREGATE = "com.esentri.rezeption.domain.zimmer.Zimmer";

    /** Application service orchestrating the booking use cases, among them the check-out flow. */
    public static final String BUCHUNG_APPLICATION_SERVICE = "com.esentri.rezeption.application.buchung.BuchungApplicationService";

    /**
     * The domain command that starts the guest check-out flow: a whole-type flow starting point
     * (a command needs no method - it starts the flow it triggers).
     */
    public static final String CHECK_OUT_COMMAND = "com.esentri.rezeption.domain.buchung.CheckeGastAus";

    /** The domain command that starts the (unrelated) guest check-in flow. */
    public static final String CHECK_IN_COMMAND = "com.esentri.rezeption.domain.buchung.CheckeGastEin";

    /** An unrelated command, part of neither the check-in nor the check-out flow. */
    public static final String UNRELATED_COMMAND = "com.esentri.rezeption.domain.buchung.AktualisiereGastdaten";

    /**
     * The {@link #BUCHUNG_APPLICATION_SERVICE} method that handles {@link #CHECK_OUT_COMMAND}: a
     * method level flow starting point, restricting the flow to the overloads of this one method.
     */
    public static final String CHECK_OUT_METHOD_NAME = "checkeGastAus";

    /** {@link #CHECK_OUT_COMMAND} as a {@code Type#method} flow starting point. */
    public static final String CHECK_OUT_METHOD_FLOW_STARTING_POINT =
        BUCHUNG_APPLICATION_SERVICE + "#" + CHECK_OUT_METHOD_NAME;

    /** Some of the types the check-out flow is expected to reach - see {@code VerifyFlow} output. */
    public static final String ZIMMER_FREIGABE_LISTENER = "com.esentri.rezeption.application.buchung.ZimmerFreigabeListener";
    public static final String GAST_AUSGECHECKT_EVENT = "com.esentri.rezeption.domain.buchung.GastAusgecheckt";

    private RezeptionScenario() {
    }

    /** @return the raw, real serialized {@code DomainMirror} JSON of the rezeption domain */
    public static String domainMirrorJson() {
        return resourceAsString("/rezeption/domain-mirror.json");
    }

    /** @return the raw, real serialized {@code DomainCalls} JSON of the rezeption domain */
    public static String domainCallsJson() {
        return resourceAsString("/rezeption/domain-calls.json");
    }

    public static byte[] domainMirrorJsonBytes() {
        return domainMirrorJson().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * @return the domain mirror upload request body, in the wire format used by the DLC build
     * plugin since version 3.4.0: {@code {"domainMirror": ..., "domainCalls": ..., "domainModelPackages": [...]}}
     */
    public static String uploadRequestBody() {
        return "{\"domainMirror\":" + domainMirrorJson()
            + ",\"domainCalls\":" + domainCallsJson()
            + ",\"domainModelPackages\":[\"" + DOMAIN_MODEL_PACKAGE + "\"]}";
    }

    /**
     * @return {@link #uploadRequestBody()}, gzip-compressed, matching the {@code Content-Encoding: gzip}
     * body the DLC build plugin sends (for both its plain and its streaming upload)
     */
    public static byte[] gzippedUploadRequestBody() {
        return gzip(uploadRequestBody());
    }

    /**
     * @return the upload request body of a build plugin configured with {@code runStaticAnalysis = false},
     * gzip-compressed: the domain mirror only, without a static analysis result
     */
    public static byte[] gzippedUploadRequestBodyWithoutDomainCalls() {
        return gzip("{\"domainMirror\":" + domainMirrorJson()
            + ",\"domainModelPackages\":[\"" + DOMAIN_MODEL_PACKAGE + "\"]}");
    }

    private static byte[] gzip(String value) {
        try {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            try (GZIPOutputStream gzipStream = new GZIPOutputStream(byteStream)) {
                gzipStream.write(value.getBytes(StandardCharsets.UTF_8));
            }
            return byteStream.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String resourceAsString(String resourcePath) {
        try (var in = RezeptionScenario.class.getResourceAsStream(resourcePath)) {
            Objects.requireNonNull(in, "Test resource not found: " + resourcePath);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
