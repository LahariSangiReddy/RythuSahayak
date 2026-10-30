package com.rythusahayak.service;

import com.rythusahayak.model.CropAdvisory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class XmlAdvisoryService {
    private static final String NAMESPACE = "urn:rythusahayak:crop-advisories";
    private static final String XML_RESOURCE = "/xml/crop-advisories.xml";
    private static final String XSD_RESOURCE = "/xml/crop-advisories.xsd";
    private static final String BUNDLED_DOCTYPE =
            "<!DOCTYPE cat:catalog SYSTEM \"crop-advisories.dtd\">";

    public List<CropAdvisory> getAllAdvisories() throws AdvisoryCatalogException {
        return parseClasspathCatalogue();
    }

    public List<CropAdvisory> findAdvisories(String crop, String category, String query)
            throws AdvisoryCatalogException {
        String normalizedCrop = normalize(crop);
        String normalizedCategory = normalize(category);
        String normalizedQuery = normalize(query);
        return getAllAdvisories().stream()
                .filter(advisory -> normalizedCrop.isEmpty()
                        || advisory.crop().toLowerCase(Locale.ROOT).equals(normalizedCrop))
                .filter(advisory -> normalizedCategory.isEmpty()
                        || advisory.category().toLowerCase(Locale.ROOT).equals(normalizedCategory))
                .filter(advisory -> normalizedQuery.isEmpty()
                        || searchableText(advisory).contains(normalizedQuery))
                .toList();
    }

    public List<CropAdvisory> parseCatalog(InputStream xmlInput, InputStream xsdInput)
            throws AdvisoryCatalogException {
        Objects.requireNonNull(xmlInput, "XML input stream");
        Objects.requireNonNull(xsdInput, "XSD input stream");
        try (InputStream xml = xmlInput; InputStream xsd = xsdInput) {
            byte[] xmlBytes = xml.readAllBytes();
            Schema schema = loadSchema(xsd);
            Document document = parseSecurely(removeBundledDoctype(xmlBytes), schema);
            return mapAdvisories(document);
        } catch (ParserConfigurationException exception) {
            throw new AdvisoryCatalogException(
                    "The XML parser does not support required secure configuration.", exception);
        } catch (IOException | SAXException exception) {
            throw new AdvisoryCatalogException("The crop advisory XML is invalid or could not be read.", exception);
        } catch (RuntimeException exception) {
            throw new AdvisoryCatalogException("The crop advisory XML contains an invalid value.", exception);
        }
    }

    public void validateDtd(InputStream xmlInput, InputStream dtdInput) throws AdvisoryCatalogException {
        Objects.requireNonNull(xmlInput, "XML input stream");
        Objects.requireNonNull(dtdInput, "DTD input stream");
        try (InputStream xml = xmlInput; InputStream dtd = dtdInput) {
            byte[] dtdBytes = dtd.readAllBytes();
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setValidating(true);
            setXIncludeAware(factory, false);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            setFeature(factory, "http://xml.org/sax/features/external-general-entities", false);
            setFeature(factory, "http://xml.org/sax/features/external-parameter-entities", true);
            setFeature(factory, "http://apache.org/xml/features/nonvalidating/load-external-dtd", true);
            setAttribute(factory, XMLConstants.ACCESS_EXTERNAL_DTD, "");
            setAttribute(factory, XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setEntityResolver(onlyBundledDtd(dtdBytes));
            builder.setErrorHandler(throwingErrorHandler());
            builder.parse(xml);
        } catch (IOException | SAXException | ParserConfigurationException exception) {
            throw new AdvisoryCatalogException("The crop advisory XML failed local DTD validation.", exception);
        }
    }

    private List<CropAdvisory> parseClasspathCatalogue() throws AdvisoryCatalogException {
        InputStream xml = XmlAdvisoryService.class.getResourceAsStream(XML_RESOURCE);
        InputStream xsd = XmlAdvisoryService.class.getResourceAsStream(XSD_RESOURCE);
        if (xml == null || xsd == null) {
            closeResource(xml);
            closeResource(xsd);
            throw new AdvisoryCatalogException("Required advisory catalogue XML or XSD resource is missing.");
        }
        return parseCatalog(xml, xsd);
    }

    private Schema loadSchema(InputStream xsd) throws SAXException {
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        try {
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            return factory.newSchema(new javax.xml.transform.stream.StreamSource(xsd));
        } catch (SAXException exception) {
            throw new SAXException("Unable to securely configure or compile the advisory XSD.", exception);
        }
    }

    private Document parseSecurely(byte[] xmlBytes, Schema schema)
            throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setSchema(schema);
        setXIncludeAware(factory, false);
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        setFeature(factory, "http://apache.org/xml/features/disallow-doctype-decl", true);
        setFeature(factory, "http://xml.org/sax/features/external-general-entities", false);
        setFeature(factory, "http://xml.org/sax/features/external-parameter-entities", false);
        setFeature(factory, "http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        setAttribute(factory, XMLConstants.ACCESS_EXTERNAL_DTD, "");
        setAttribute(factory, XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        DocumentBuilder builder = factory.newDocumentBuilder();
        builder.setEntityResolver(rejectExternalEntities());
        builder.setErrorHandler(throwingErrorHandler());
        return builder.parse(new ByteArrayInputStream(xmlBytes));
    }

    private byte[] removeBundledDoctype(byte[] xmlBytes) {
        String xml = new String(xmlBytes, StandardCharsets.UTF_8);
        return xml.replace(BUNDLED_DOCTYPE, "").getBytes(StandardCharsets.UTF_8);
    }

    private List<CropAdvisory> mapAdvisories(Document document) throws SAXException {
        Element root = document.getDocumentElement();
        if (root == null || !NAMESPACE.equals(root.getNamespaceURI()) || !"catalog".equals(root.getLocalName())) {
            throw new SAXException("Unexpected advisory catalogue root element or namespace.");
        }
        NodeList records = root.getElementsByTagNameNS(NAMESPACE, "advisory");
        List<CropAdvisory> advisories = new ArrayList<>(records.getLength());
        for (int index = 0; index < records.getLength(); index++) {
            Element advisory = (Element) records.item(index);
            advisories.add(new CropAdvisory(
                    advisory.getAttribute("id"),
                    text(advisory, "crop"),
                    text(advisory, "category"),
                    text(advisory, "title"),
                    text(advisory, "description"),
                    text(advisory, "recommendation"),
                    optionalText(advisory, "growthStage"),
                    text(advisory, "language"),
                    LocalDate.parse(text(advisory, "lastReviewed")),
                    text(advisory, "source")));
        }
        return List.copyOf(advisories);
    }

    private EntityResolver onlyBundledDtd(byte[] dtdBytes) {
        return (publicId, systemId) -> {
            if (systemId != null && systemId.endsWith("crop-advisories.dtd")) {
                InputSource source = new InputSource(new ByteArrayInputStream(dtdBytes));
                source.setPublicId(publicId);
                source.setSystemId("classpath:/xml/crop-advisories.dtd");
                return source;
            }
            throw new SAXException("External DTD access is blocked.");
        };
    }

    private EntityResolver rejectExternalEntities() {
        return (publicId, systemId) -> {
            throw new SAXException("External entities are disabled.");
        };
    }

    private DefaultHandler throwingErrorHandler() {
        return new DefaultHandler() {
            @Override
            public void warning(SAXParseException exception) throws SAXException {
                throw exception;
            }

            @Override
            public void error(SAXParseException exception) throws SAXException {
                throw exception;
            }

            @Override
            public void fatalError(SAXParseException exception) throws SAXException {
                throw exception;
            }
        };
    }

    private void setFeature(DocumentBuilderFactory factory, String feature, boolean value)
            throws ParserConfigurationException {
        factory.setFeature(feature, value);
    }

    private void setXIncludeAware(DocumentBuilderFactory factory, boolean value)
            throws ParserConfigurationException {
        try {
            factory.setXIncludeAware(value);
        } catch (UnsupportedOperationException exception) {
            ParserConfigurationException configurationException =
                    new ParserConfigurationException("XML parser does not support the required XInclude setting.");
            configurationException.initCause(exception);
            throw configurationException;
        }
    }

    private void setAttribute(DocumentBuilderFactory factory, String name, String value)
            throws ParserConfigurationException {
        try {
            factory.setAttribute(name, value);
        } catch (IllegalArgumentException exception) {
            ParserConfigurationException configurationException =
                    new ParserConfigurationException("XML parser does not support required security property " + name);
            configurationException.initCause(exception);
            throw configurationException;
        }
    }

    private String text(Element parent, String localName) throws SAXException {
        NodeList nodes = parent.getElementsByTagNameNS(NAMESPACE, localName);
        if (nodes.getLength() != 1) {
            throw new SAXException("Expected exactly one " + localName + " element.");
        }
        return nodes.item(0).getTextContent().trim();
    }

    private String optionalText(Element parent, String localName) {
        NodeList nodes = parent.getElementsByTagNameNS(NAMESPACE, localName);
        return nodes.getLength() == 0 ? "" : nodes.item(0).getTextContent().trim();
    }

    private String searchableText(CropAdvisory advisory) {
        return (advisory.title() + " " + advisory.description()).toLowerCase(Locale.ROOT);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private void closeResource(InputStream stream) throws AdvisoryCatalogException {
        if (stream == null) {
            return;
        }
        try {
            stream.close();
        } catch (IOException exception) {
            throw new AdvisoryCatalogException("Unable to close an advisory resource.", exception);
        }
    }
}
