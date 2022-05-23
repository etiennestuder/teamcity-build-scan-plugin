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
 * Detects Maven Extensions in {@code .mvn/extensions.xml} files by matching their groupId and artifactId
 */
final class MavenExtensions {

    private static final Logger LOG = Logger.getInstance(MavenExtensions.class.getName());

    private static final XPath xPath = XPathFactory.newInstance().newXPath();

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

        Document document;
        try {
            document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(extensionsFile);
            document.normalizeDocument();
            return new MavenExtensions(document);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            LOG.warn("Failed to parse extensions from file " + extensionsFile.getAbsolutePath(), e);
            return MavenExtensions.empty();
        }
    }

    boolean hasExtension(@NotNull MavenCoordinates coordinates) {
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
