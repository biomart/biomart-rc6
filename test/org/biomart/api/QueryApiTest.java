package org.biomart.api;

import org.biomart.processors.TSV;
import org.biomart.processors.ProcessorRegistry;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import org.biomart.api.factory.MartRegistryFactory;
import org.biomart.api.factory.XmlMartRegistryFactory;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for the Java API (org.biomart.api.Query object)
 *
 * @author jhsu
 */
public class QueryApiTest {
    private static Portal _portal;
    private static Portal _portal2;

    static {
        try {
            MartRegistryFactory factory = new XmlMartRegistryFactory("./testdata/javaapi.xml", null);
            _portal = new Portal(factory);
            _portal2 = new Portal(factory, "http://jaysoo.myopenid.com/");
        } catch(Exception e) {
            fail("Exception initializing registry");
        }
        ProcessorRegistry.register("TSV", TSV.class);
    }

    @Test
    public void testDefaults() {
        Query query = new Query(_portal);

        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><!DOCTYPE Query>"
                + "<Query client=\"biomartclient\" processor=\"TSV\" limit=\"-1\" header=\"1\" />";

        String xml = query.getXml();

        xml = replaceNewlines(xml);

        assertEquals(expected, xml);
    }

    @Test
    public void testSettings() {
        Query query = new Query(_portal)
            .setHeader(false)
            .setLimit(1337)
            .setClient("helloworld")
            .setProcessor("TSVX");

        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><!DOCTYPE Query>"
                + "<Query client=\"helloworld\" processor=\"TSVX\" limit=\"1337\" header=\"0\" />";

        String xml = query.getXml();

        xml = replaceNewlines(xml);

        assertEquals(expected, xml);
    }

    @Test
    public void testMissingEndCallXml() {
        Query query = new Query(_portal);

        query
            .addDataset("dataset1", null)
                .addFilter("hello", "world")
                .addAttribute("bar");

        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><!DOCTYPE Query>"
                + "<Query client=\"biomartclient\" processor=\"TSV\" limit=\"-1\" header=\"1\">"
                + "<Dataset name=\"dataset1\">"
                + "<Filter name=\"hello\" value=\"world\" />"
                + "<Attribute name=\"bar\" />"
                + "</Dataset></Query>";

        String xml = query.getXml();

        xml = replaceNewlines(xml);

        assertEquals(expected, xml);
    }

    @Test
    public void testSingleDatasetElementXml() {
        Query query = new Query(_portal)
            .setProcessor("TSV")
            .setClient("test")
            .setHeader(true)
            .setLimit(1000)
            .addDataset("dataset1", "config1")
                .addFilter("hello", "world")
                .addAttribute("foo")
                .addAttribute("bar")
                .addAttribute("faz")
            .end();

        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<!DOCTYPE Query><Query client=\"test\" processor=\"TSV\" limit=\"1000\" header=\"1\">"
                + "<Dataset name=\"dataset1\" config=\"config1\">"
                + "<Filter name=\"hello\" value=\"world\" />"
                + "<Attribute name=\"foo\" />"
                + "<Attribute name=\"bar\" />"
                + "<Attribute name=\"faz\" /></Dataset></Query>";

        String xml = query.getXml();

        xml = replaceNewlines(xml);

        assertEquals(expected, xml);
    }

    @Test
    public void testMultipleDatasetElementXml() {
        Query query = new Query(_portal)
            .setProcessor("TSV")
            .setClient("test")
            .setHeader(true)
            .setLimit(1000)
            .addDataset("dataset1", "config1")
                .addFilter("hello", "world")
                .addAttribute("foo")
                .addAttribute("bar")
                .addAttribute("faz")
            .end()
            .addDataset("sample1", null)
                .addAttribute("xyz")
            .end();

        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<!DOCTYPE Query><Query client=\"test\" processor=\"TSV\" limit=\"1000\" header=\"1\">"
                + "<Dataset name=\"dataset1\" config=\"config1\">"
                + "<Filter name=\"hello\" value=\"world\" />"
                + "<Attribute name=\"foo\" />"
                + "<Attribute name=\"bar\" />"
                + "<Attribute name=\"faz\" /></Dataset>"
                + "<Dataset name=\"sample1\">"
                + "<Attribute name=\"xyz\" />"
                + "</Dataset></Query>";

        String xml = query.getXml();

        xml = replaceNewlines(xml);

        assertEquals(expected, xml);
    }

    @Test
    public void testResults() {
        Query query = new Query(_portal)
                .setProcessor("TSV")
                .setClient("test")
                .setHeader(true)
                .setLimit(10)
                .addDataset("hsapiens_gene_ensembl", "hsapiens_gene_ensembl_config")
                    .addFilter("chromosome_name", "1")
                    .addAttribute("ensembl_gene_id")
                .end();

        OutputStream out = new ByteArrayOutputStream();
        query.getResults(out);

        String expected = "Ensembl Gene ID\n"
                + "ENSG00000249935\n"
                + "ENSG00000248149\n"
                + "ENSG00000245123\n"
                + "ENSG00000251327\n"
                + "ENSG00000248957\n"
                + "ENSG00000215059\n"
                + "ENSG00000250449\n"
                + "ENSG00000250797\n"
                + "ENSG00000245549\n"
                + "ENSG00000249753\n";
        String results = out.toString();

        assertEquals(expected, results);
    }


    @Test
    public void testAuthenticatedQuery() {
        Query query = new Query(_portal2)
                .setProcessor("TSV")
                .setClient("test")
                .setHeader(true)
                .setLimit(10)
                .addDataset("hsapiens_gene_vega", "hsapiens_gene_vega_config")
                    .addAttribute("go_id")
                .end();

        OutputStream out = new ByteArrayOutputStream();
        query.getResults(out);

        String expected = "GO ID\n"
                + "GO:0004984\n"
                + "GO:0005886\n"
                + "GO:0016021\n"
                + "GO:0007608\n"
                + "GO:0050896\n"
                + "GO:0005634\n"
                + "GO:0005730\n"
                + "GO:0005515\n"
                + "GO:0045211\n"
                + "GO:0014069\n";
        String results = out.toString();

        assertEquals(expected, results);
    }

    private String replaceNewlines(String str) {
        return str.replaceAll("\r", "").replaceAll("\n", "");

    }
}
