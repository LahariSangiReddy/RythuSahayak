package com.rythusahayak.service;

import com.rythusahayak.model.CropAdvisory;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class XmlAdvisoryServiceTest {
    private final XmlAdvisoryService service = new XmlAdvisoryService();

    @Test
    void classpathResourcesExistAndCataloguePassesDtdAndXsdValidation() throws Exception {
        try (InputStream xml = resource("/xml/crop-advisories.xml");
             InputStream xsd = resource("/xml/crop-advisories.xsd");
             InputStream dtdXml = resource("/xml/crop-advisories.xml");
             InputStream dtd = resource("/xml/crop-advisories.dtd")) {
            assertEquals(10, service.parseCatalog(xml, xsd).size());
            service.validateDtd(dtdXml, dtd);
        }
    }

    @Test
    void everyRecordHasRequiredFieldsAndUniqueIds() throws Exception {
        List<CropAdvisory> advisories = service.getAllAdvisories();
        Set<String> ids = new HashSet<>();

        assertEquals(10, advisories.size());
        for (CropAdvisory advisory : advisories) {
            assertFalse(advisory.id().isBlank());
            assertFalse(advisory.crop().isBlank());
            assertFalse(advisory.category().isBlank());
            assertFalse(advisory.title().isBlank());
            assertFalse(advisory.description().isBlank());
            assertFalse(advisory.recommendation().isBlank());
            assertTrue(advisory.lastReviewed() != null);
            assertTrue(ids.add(advisory.id()), "Duplicate ID: " + advisory.id());
        }
    }

    @Test
    void filtersByCropCategoryAndSearchAndReturnsEmptyResults() throws Exception {
        List<CropAdvisory> paddy = service.findAdvisories("paddy", null, null);
        assertEquals(2, paddy.size());
        assertTrue(paddy.stream().allMatch(item -> item.crop().equals("Paddy")));

        List<CropAdvisory> pests = service.findAdvisories(null, "pest", null);
        assertEquals(2, pests.size());
        assertTrue(pests.stream().allMatch(item -> item.category().equals("pest")));

        List<CropAdvisory> matching = service.findAdvisories("Tomato", "pest", "leaf damage");
        assertEquals(1, matching.size());
        assertTrue(matching.get(0).title().contains("leaf damage"));

        assertTrue(service.findAdvisories("Unknown crop", null, null).isEmpty());
        assertEquals(10, service.findAdvisories(null, null, null).size());
    }

    @Test
    void rejectsMalformedXmlAndXsdViolationsWithControlledException() throws Exception {
        String malformed = "<?xml version=\"1.0\"?><cat:catalog xmlns:cat=\"urn:rythusahayak:crop-advisories\"><";
        assertThrows(AdvisoryCatalogException.class,
                () -> service.parseCatalog(input(malformed), resource("/xml/crop-advisories.xsd")));

        String invalidCategory = """
                <?xml version="1.0" encoding="UTF-8"?>
                <cat:catalog xmlns:cat="urn:rythusahayak:crop-advisories">
                  <cat:advisory id="ADV-BAD">
                    <cat:crop>Paddy</cat:crop><cat:category>made-up</cat:category>
                    <cat:title>Example title</cat:title><cat:description>Example description long enough</cat:description>
                    <cat:recommendation>A sufficiently long example recommendation.</cat:recommendation>
                    <cat:language>English</cat:language><cat:lastReviewed>2026-02-30</cat:lastReviewed>
                    <cat:source>Educational sample source.</cat:source>
                  </cat:advisory>
                </cat:catalog>
                """;
        assertThrows(AdvisoryCatalogException.class,
                () -> service.parseCatalog(input(invalidCategory), resource("/xml/crop-advisories.xsd")));
    }

    @Test
    void rejectsDoctypeAndExternalEntityExpansion() throws Exception {
        String hostile = """
                <?xml version="1.0"?>
                <!DOCTYPE cat:catalog [<!ENTITY xxe SYSTEM "file:///C:/Windows/win.ini">]>
                <cat:catalog xmlns:cat="urn:rythusahayak:crop-advisories">
                  <cat:advisory id="ADV-XXE">
                    <cat:crop>Paddy</cat:crop><cat:category>general</cat:category>
                    <cat:title>&xxe;</cat:title><cat:description>A description long enough.</cat:description>
                    <cat:recommendation>A sufficiently long safe recommendation.</cat:recommendation>
                    <cat:language>English</cat:language><cat:lastReviewed>2026-01-15</cat:lastReviewed>
                    <cat:source>Educational sample source.</cat:source>
                  </cat:advisory>
                </cat:catalog>
                """;
        assertThrows(AdvisoryCatalogException.class,
                () -> service.parseCatalog(input(hostile), resource("/xml/crop-advisories.xsd")));
    }

    private InputStream resource(String name) {
        InputStream input = getClass().getResourceAsStream(name);
        if (input == null) {
            throw new IllegalStateException("Missing test resource: " + name);
        }
        return input;
    }

    private InputStream input(String value) {
        return new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8));
    }
}
