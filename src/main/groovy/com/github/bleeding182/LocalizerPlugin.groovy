/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 David Medenjak
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.bleeding182

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.ProjectConfigurationException
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

class LocalizerPlugin implements Plugin<Project> {
    def void apply(Project project) {
        if (!(project.plugins.findPlugin("com.android.application") || project.plugins.findPlugin("android")
                || project.plugins.findPlugin("com.android.library") || project.plugins.findPlugin("android-library"))) {
            throw new ProjectConfigurationException("The android or android-library plugin must be applied to the project", null)
        }

        project.tasks.preBuild.doFirst {
            File outputBaseDir = project.file(new File(project.buildDir, "generated/res/accented"))

            project.android.sourceSets.each {
                // android test, debug, ...
                // loop over their source dirs
                def fileAdded = false
                File outputResDir = null

                it.res.srcDirs.each {
                    outputResDir = new File(outputBaseDir, it.parentFile.name)
                    def outputDir = new File(outputResDir, "/values-zz")

                    File srcDir = new File(it, "values")
                    if (!srcDir.exists())
                        return

                    // Iterate over all resource values.xml files
                    srcDir.listFiles(new FileFilter() {
                        @Override
                        boolean accept(File file) {
                            return file.getName().toLowerCase().endsWith("xml") && !file.getName().toLowerCase().equals("donottranslate.xml");
                        }
                    }).each {
                        File out = new File(outputDir, it.getName())
                        out.delete()
                        out = new File(outputDir, it.getName())
                        if (parseXmlFile(it, out))
                            fileAdded = true
                    }
                }

                // add generated resources to res
                if (fileAdded && outputResDir != null) {
                    it.res.srcDirs += [outputResDir.absolutePath];
                }
            }
        }
    }

    private boolean parseXmlFile(File input, File output) {
        //Get the DOM Builder Factory
        DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();

        //Get the DOM Builder
        DocumentBuilder builder = factory.newDocumentBuilder();

        boolean hasStrings = false;

        Document document =
                builder.parse(new FileInputStream(input));

        Element root = document.getDocumentElement();

        //Iterating through the nodes and extracting the data.
        for (Node node : root.getChildNodes()) {
            if (node instanceof Element) {
                def translatableAttribute = node.getAttributes().getNamedItem("translatable");
                if (translatableAttribute != null && translatableAttribute.getNodeValue() == "false") {
                    root.removeChild(node);
                    continue;
                }

                if (node.getNodeName() == "string") {
                    hasStrings = true;
                    transformText(node.childNodes);
                } else if (node.getNodeName() == "string-array") {
                    hasStrings = true;
                    transformText(node.childNodes);
                } else if (node.getNodeName() == "plurals") {
                    hasStrings = true;
                    transformText(node.childNodes);
                } else {
                    continue;
                }
            }
        }

        if (hasStrings) {
            println "Writing $input to $output"
            if (!output.parentFile.exists())
                output.parentFile.mkdirs()
            TransformerFactory tFactory =
                    TransformerFactory.newInstance();
            Transformer transformer =
                    tFactory.newTransformer();

            DOMSource source = new DOMSource(document);
            FileOutputStream fos = new FileOutputStream(output);
            StreamResult result = new StreamResult(fos);
            transformer.transform(source, result);
            fos.close()
            return true;
        }
        return false;
    }

    private void transformText(NodeList nodes) {
        for (Node node : nodes) {
            if (node.nodeType == Node.TEXT_NODE) {
                node.setTextContent(accentString(node.textContent))
            } else {
                // ignore localization placeholders enclosed within <xliff:g/> tags.
                if (node.nodeName == "xliff:g")
                    continue;
                transformText(node.childNodes)
            }
        }
    }


    private String accentString(String input) {
        def ret = ""
        def isFormatterTag = false
        for (int i = 0; i < input.length(); i++) {
            def c = input.charAt(i);
            if (isFormatterTag) {
                // cancel formatting tag with the first 'normal' letter
                if (c == '%' || Character.isLowerCase(c) || Character.isUpperCase(c))
                    isFormatterTag = false
                ret += c
                continue
            }
            // '%' appeared, do not accent the until next text character
            if (c == '%') {
                isFormatterTag = true
            }
            def mapped = map[new String(c)]
            ret += (mapped == null ? c : mapped)
        }
        return ret
    }


    private static map = ['a': 'ā', 'b': 'ƃ', 'c': 'č', 'd': 'đ', 'e': 'ë', 'f': 'f', 'g': 'g', 'h': 'ɦ', 'i': 'î',
            'j': 'j', 'k': 'k', 'l': 'ł', 'm': 'm', 'n': 'ñ', 'o': 'ö', 'p': 'p', 'q': 'q', 'r': 'r', 's': 'š',
            't': 't', 'u': 'ü', 'v': 'v', 'w': 'ѡ', 'x': 'x', 'y': 'γ', z: 'ż', 'A': 'Ä', 'B': 'Ɓ', C: 'Č', D: 'Đ',
            E: 'Ę', F: 'F', G: 'G', H: 'H', I: 'Î', J: 'J', K: 'K', L: 'Ł', M: "M", N: 'Ń', O: 'Ō', P: 'P', Q: 'Q',
            R: 'Ȓ', S: 'Ś', T: 'T', U: 'Ü', V: 'V', W: 'W', X: 'X', Y: 'Ÿ', Z: 'Ž'];
}