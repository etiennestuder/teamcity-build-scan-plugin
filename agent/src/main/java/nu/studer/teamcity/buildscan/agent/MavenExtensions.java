package nu.studer.teamcity.buildscan.agent;

import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.*;

/**
 * Detects Maven Extensions in {@code .mvn/extensions.xml} files by matching their groupId and artifactId
 */
final class MavenExtensions {

    private static final Logger LOG = Logger.getInstance(MavenExtensions.class.getName());

    private static final XPath xPath = XPathFactory.newInstance().newXPath();

    private final Document document;

    private MavenExtensions() {
        this(null);
    }

    private MavenExtensions(Document document) {
        this.document = document;
    }

    public static MavenExtensions fromFile(@NotNull File extensionsFile) {
        if (!extensionsFile.exists()) {
            return MavenExtensions.empty();
        }

        Document document;

        try {
            document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(extensionsFile);
            document.normalizeDocument();
        } catch (ParserConfigurationException e) {
            LOG.warn("Failed to parse extensions from: " + extensionsFile.getAbsolutePath(), e);
            return MavenExtensions.empty();
        } catch (IOException e) {
            LOG.warn("Failed to parse extensions from: " + extensionsFile.getAbsolutePath(), e);
            return MavenExtensions.empty();
        } catch (SAXException e) {
            LOG.warn("Failed to parse extensions from: " + extensionsFile.getAbsolutePath(), e);
            return MavenExtensions.empty();
        }

        return new MavenExtensions(document);
    }

    public static MavenExtensions empty() {
        return new MavenExtensions();
    }

    public boolean hasExtension(@NotNull MavenCoordinates coordinates) {
        return document != null && getExtension(document, coordinates).getLength() > 0;
    }

    private NodeList getExtension(Document doc, MavenCoordinates extension) {
        String expr = String.format("/extensions/extension[groupId = '%s' and artifactId = '%s']", extension.getGroupId(), extension.getArtifactId());

        try {
            XPathExpression xPathExpr = xPath.compile(expr);
            return (NodeList) xPathExpr.evaluate(doc, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            String message = String.format("Could not compile/evaluate XPath Expression : %s", expr);
            throw new IllegalArgumentException(message, e);
        }
    }
}