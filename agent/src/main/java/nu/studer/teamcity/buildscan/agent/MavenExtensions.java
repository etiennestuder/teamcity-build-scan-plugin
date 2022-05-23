package nu.studer.teamcity.buildscan.agent;

import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;

/**
 * Represents a Maven extensions XML file, typically present at {@code .mvn/extensions.xml}.
 */
final class MavenExtensions {

    private static final Logger LOG = Logger.getInstance(MavenExtensions.class.getName());

    private static final XPath XPATH = XPathFactory.newInstance().newXPath();

    private static final String EXTENSION_XPATH_EXPR = "/extensions/extension[groupId = '%s' and artifactId = '%s']";

    @Nullable
    private final Document document;

    private MavenExtensions(@Nullable Document document) {
        this.document = document;
    }

    static MavenExtensions empty() {
        return new MavenExtensions(null);
    }

    static MavenExtensions fromFile(@NotNull File extensionsFile) {
        if (!extensionsFile.exists()) {
            return MavenExtensions.empty();
        }

        try {
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(extensionsFile);
            document.normalizeDocument();
            return new MavenExtensions(document);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            LOG.warn("Failed to parse file: " + extensionsFile.getAbsolutePath(), e);
            return MavenExtensions.empty();
        }
    }

    boolean hasExtension(@NotNull MavenCoordinates coordinates) {
        if (document == null) {
            return false;
        }

        String expr = String.format(EXTENSION_XPATH_EXPR, coordinates.getGroupId(), coordinates.getArtifactId());
        try {
            XPathExpression exprCompiled = XPATH.compile(expr);
            NodeList extension = (NodeList) exprCompiled.evaluate(document, XPathConstants.NODESET);
            return extension != null && extension.getLength() > 0;
        } catch (XPathExpressionException e) {
            LOG.warn("Could not apply XPath expression: " + expr, e);
            return false;
        }
    }

}
