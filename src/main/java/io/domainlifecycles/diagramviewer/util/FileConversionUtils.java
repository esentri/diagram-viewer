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
 *  Copyright 2019-2025 the original author or authors.
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
import java.nio.charset.StandardCharsets;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.JPEGTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;

public final class FileConversionUtils {

    public static byte[] convertSvgToPng(byte[] svgBytes) {
        byte[] cleanedSvg = modifySvg(svgBytes);

        PNGTranscoder transcoder = new PNGTranscoder();

        TranscoderInput input = new TranscoderInput(
            new ByteArrayInputStream(cleanedSvg)
        );

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TranscoderOutput output = new TranscoderOutput(baos);

        try {
            transcoder.transcode(input, output);
        } catch (TranscoderException e) {
            throw DiagramViewerException.fail("Could not convert svg file to png.", e);
        }

        return baos.toByteArray();
    }

    public static byte[] convertSvgToJpeg(byte[] svgBytes) {
        byte[] cleanedSvg = modifySvg(svgBytes);

        JPEGTranscoder transcoder = new JPEGTranscoder();

        TranscoderInput input = new TranscoderInput(
            new ByteArrayInputStream(cleanedSvg)
        );

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TranscoderOutput output = new TranscoderOutput(baos);

        try {
            transcoder.transcode(input, output);
        } catch (TranscoderException e) {
            throw DiagramViewerException.fail("Could not convert svg file to jpeg.", e);
        }

        return baos.toByteArray();
    }

    private static byte[] modifySvg(byte[] svgBytes) {
        String svg = new String(svgBytes, StandardCharsets.UTF_8);

        svg = svg.replace("fill=\"transparent\"", "fill=\"none\"");
        svg = svg.replace("stroke=\"transparent\"", "stroke=\"none\"");

        svg = svg.replace("fill: transparent", "fill: none");
        svg = svg.replace("stroke: transparent", "stroke: none");

        return svg.getBytes(StandardCharsets.UTF_8);
    }
}