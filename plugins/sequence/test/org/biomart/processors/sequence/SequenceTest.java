package org.biomart.processors.sequence;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import java.io.IOException;
import org.biomart.common.exceptions.TechnicalException;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import org.biomart.api.Portal;
import org.biomart.api.factory.MartRegistryFactory;
import org.biomart.api.factory.XmlMartRegistryFactory;
import org.biomart.processors.ProcessorRegistry;
import org.biomart.processors.TSV;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jhsu
 */
public class SequenceTest {
    private static String _jdbcURL;
    private static String _username;
    private static String _password;
    private static String _tableName;

    private static Portal _portal;

    static {
        File propertyfile = new File("test/org/biomart/processors/sequence/sequence.properties");
        Properties props = new Properties();

        try {
            FileInputStream fis = new FileInputStream(propertyfile);
            props.load(fis);
        } catch (Exception e) {
        }

        for (Object key : props.keySet()) {
            System.setProperty((String)key, (String)props.get(key));
        }

        String baseDir = System.getProperty("org.biomart.baseDir");

        try {
            MartRegistryFactory factory = new XmlMartRegistryFactory(baseDir + "/registry/sequence.xml",
                    baseDir + "/registry/.sequence");
            _portal = new Portal(factory);
        } catch(Exception e) {
            fail("Exception initializing registry");
        }

        _jdbcURL = System.getProperty("test.jdbcURL");
        _tableName = "hsapiens_genomic_sequence__dna_chunks__main";
        _username = System.getProperty("test.username");
        _password = System.getProperty("test.password");

        ProcessorRegistry.register("TSV", TSV.class);
    }

    @Test
    public void testCDNA() throws TechnicalException, IOException {
        String xml = "<!DOCTYPE Query><Query client=\"biomartclient\" processor=\"TSV\" limit=\"-1\" header=\"0\">"
                + "<Dataset name=\"hsapiens_gene_ensembl\" config=\"gene_ensembl_config\">"
                + "<Filter name=\"ensembl_gene_id\" value=\"ENSG00000004478\"/>"
                + "<Attribute name=\"ensembl_transcript_id\"/>"
                + "<Attribute name=\"chromosome_name\"/>"
                + "<Attribute name=\"exon_chrom_start\"/>"
                + "<Attribute name=\"exon_chrom_end\"/>"
                + "<Attribute name=\"strand\"/><Attribute name=\"rank\"/>"
                // Header
                + "<Attribute name=\"ensembl_gene_id\"/>"
                + "<Attribute name=\"ensembl_transcript_id\"/>"
                + "</Dataset>"
                + "</Query>";

        OutputStream seqOut = new ByteArrayOutputStream();
        OutputStream bufOut = new ByteArrayOutputStream();

        _portal.executeQuery(xml, bufOut);

        bufOut.flush();
        bufOut.close();

        SequenceParser parser = new CDNAParser();

        parser
            .setExtraAttributes(2)
            .setDatabaseConnection(_jdbcURL, _tableName, _username, _password)
            .streamSequence(bufOut, seqOut);

        String results = seqOut.toString();
        System.out.println(results);
        assertTrue("ID is present", results.contains("ENSG00000004478"));
        assertTrue("Sequence is available", !results.contains("Sequence unavailable"));
    }

    @Test
    public void testCodingGeneFlank()  throws TechnicalException, IOException {
        String xml = "<!DOCTYPE Query><Query client=\"biomartclient\" processor=\"TSV\" limit=\"-1\" header=\"0\">"
                + "<Dataset name=\"hsapiens_gene_ensembl\" config=\"gene_ensembl_config\">"
                + "<Filter name=\"ensembl_gene_id\" value=\"ENSG00000004478\"/>"
                + "<Attribute name=\"ensembl_gene_id\"/>"
                + "<Attribute name=\"ensembl_transcript_id\"/>"
                + "<Attribute name=\"chromosome_name\"/>"
                + "<Attribute name=\"exon_chrom_start\"/>"
                + "<Attribute name=\"exon_chrom_end\"/>"
                + "<Attribute name=\"coding_start_offset\"/>"
                + "<Attribute name=\"coding_end_offset\"/>"
                + "<Attribute name=\"strand\"/>"
                + "<Attribute name=\"exon_id\"/>"
                + "<Attribute name=\"rank\"/>"
                + "<Attribute name=\"start_exon_id\"/>"
                + "<Attribute name=\"end_exon_id\"/>"
                + "<Attribute name=\"transcript_count\"/>"
                // Header
                + "<Attribute name=\"ensembl_gene_id\"/>"
                + "<Attribute name=\"ensembl_transcript_id\"/>"
                + "</Dataset>"
                + "</Query>";

        OutputStream seqOut = new ByteArrayOutputStream();
        OutputStream bufOut = new ByteArrayOutputStream();

        _portal.executeQuery(xml, bufOut);

        bufOut.flush();
        bufOut.close();

        SequenceParser parser = new CodingGeneFlankParser();

        parser
            .setExtraAttributes(2)
            .setDownstreamFlank(100)
            .setDatabaseConnection(_jdbcURL, _tableName, _username, _password)
            .streamSequence(bufOut, seqOut);

        String results = seqOut.toString();
        System.out.println(results);
        assertTrue("ID is present", results.contains("ENSG00000004478"));
        assertTrue("Sequence is available", !results.contains("Sequence unavailable"));
    }

    @Test
    public void testCoding()  throws TechnicalException, IOException {
        String xml = "<!DOCTYPE Query><Query client=\"biomartclient\" processor=\"TSV\" limit=\"-1\" header=\"0\">"
                + "<Dataset name=\"hsapiens_gene_ensembl\" config=\"gene_ensembl_config\">"
                + "<Filter name=\"ensembl_gene_id\" value=\"ENSG00000004478\"/>"
                + "<Attribute name=\"ensembl_transcript_id\"/>"
                + "<Attribute name=\"chromosome_name\"/>"
                + "<Attribute name=\"exon_chrom_start\"/>"
                + "<Attribute name=\"exon_chrom_end\"/>"
                + "<Attribute name=\"coding_start_offset\"/>"
                + "<Attribute name=\"coding_end_offset\"/>"
                + "<Attribute name=\"strand\"/>"
                + "<Attribute name=\"exon_id\"/>"
                + "<Attribute name=\"rank\"/>"
                + "<Attribute name=\"start_exon_id\"/>"
                + "<Attribute name=\"end_exon_id\"/>"
                + "<Attribute name=\"phase\"/>"
                // Header
                + "<Attribute name=\"ensembl_gene_id\"/>"
                + "<Attribute name=\"ensembl_transcript_id\"/>"
                + "</Dataset>"
                + "</Query>";

        OutputStream seqOut = new ByteArrayOutputStream();
        OutputStream bufOut = new ByteArrayOutputStream();

        _portal.executeQuery(xml, bufOut);

        bufOut.flush();
        bufOut.close();

        SequenceParser parser = new CodingParser();

        parser
            .setExtraAttributes(2)
            .setDatabaseConnection(_jdbcURL, _tableName, _username, _password)
            .streamSequence(bufOut, seqOut);

        String results = seqOut.toString();
        System.out.println(results);
        assertTrue("ID is present", results.contains("ENSG00000004478"));
        assertTrue("Sequence is available", !results.contains("Sequence unavailable"));
    }

    @Test
    public void testPeptide()  throws TechnicalException, IOException {
        String xml = "<!DOCTYPE Query><Query client=\"biomartclient\" processor=\"TSV\" limit=\"-1\" header=\"0\">"
                + "<Dataset name=\"hsapiens_gene_ensembl\" config=\"gene_ensembl_config\">"
                + "<Filter name=\"ensembl_gene_id\" value=\"ENSG00000004478\"/>"
                + "<Attribute name=\"ensembl_transcript_id\"/>"
                + "<Attribute name=\"chromosome_name\"/>"
                + "<Attribute name=\"exon_chrom_start\"/>"
                + "<Attribute name=\"exon_chrom_end\"/>"
                + "<Attribute name=\"coding_start_offset\"/>"
                + "<Attribute name=\"coding_end_offset\"/>"
                + "<Attribute name=\"strand\"/>"
                + "<Attribute name=\"exon_id\"/>"
                + "<Attribute name=\"rank\"/>"
                + "<Attribute name=\"start_exon_id\"/>"
                + "<Attribute name=\"end_exon_id\"/>"
                + "<Attribute name=\"phase\"/>"
                + "<Attribute name=\"codon_table_id\"/>"
                + "<Attribute name=\"seq_edits\"/>"
                // Header
                + "<Attribute name=\"ensembl_gene_id\"/>"
                + "<Attribute name=\"ensembl_transcript_id\"/>"
                + "</Dataset>"
                + "</Query>";

        OutputStream seqOut = new ByteArrayOutputStream();
        OutputStream bufOut = new ByteArrayOutputStream();

        _portal.executeQuery(xml, bufOut);

        bufOut.flush();
        bufOut.close();

        SequenceParser parser = new PeptideParser();

        parser
            .setExtraAttributes(2)
            .setDatabaseConnection(_jdbcURL, _tableName, _username, _password)
            .streamSequence(bufOut, seqOut);

        String results = seqOut.toString();
        System.out.println(results);
        assertTrue("ID is present", results.contains("ENSG00000004478"));
        assertTrue("Sequence is available", !results.contains("Sequence unavailable"));
    }

    @Test
    public void testCodingTranscriptFlank()  throws TechnicalException, IOException {
        String xml = "<!DOCTYPE Query><Query client=\"biomartclient\" processor=\"TSV\" limit=\"-1\" header=\"0\">"
                + "<Dataset name=\"hsapiens_gene_ensembl\" config=\"gene_ensembl_config\">"
                + "<Filter name=\"ensembl_gene_id\" value=\"ENSG00000004478\"/>"
                + "<Attribute name=\"ensembl_transcript_id\"/>"
                + "<Attribute name=\"chromosome_name\"/>"
                + "<Attribute name=\"exon_chrom_start\"/>"
                + "<Attribute name=\"exon_chrom_end\"/>"
                + "<Attribute name=\"coding_start_offset\"/>"
                + "<Attribute name=\"coding_end_offset\"/>"
                + "<Attribute name=\"strand\"/>"
                + "<Attribute name=\"exon_id\"/>"
                + "<Attribute name=\"rank\"/>"
                + "<Attribute name=\"start_exon_id\"/>"
                + "<Attribute name=\"end_exon_id\"/>"
                // Header
                + "<Attribute name=\"ensembl_gene_id\"/>"
                + "<Attribute name=\"ensembl_transcript_id\"/>"
                + "</Dataset>"
                + "</Query>";

        OutputStream seqOut = new ByteArrayOutputStream();
        OutputStream bufOut = new ByteArrayOutputStream();

        _portal.executeQuery(xml, bufOut);

        bufOut.flush();
        bufOut.close();

        SequenceParser parser = new CodingTranscriptFlankParser();

        parser
            .setDownstreamFlank(100)
            .setExtraAttributes(2)
            .setDatabaseConnection(_jdbcURL, _tableName, _username, _password)
            .streamSequence(bufOut, seqOut);

        String results = seqOut.toString();
        System.out.println(results);
        assertTrue("ID is present", results.contains("ENSG00000004478"));
        assertTrue("Sequence is available", !results.contains("Sequence unavailable"));
    }

    @Test
    public void testFiveUTR()  throws TechnicalException, IOException {
        String xml = "<!DOCTYPE Query><Query client=\"biomartclient\" processor=\"TSV\" limit=\"-1\" header=\"0\">"
                + "<Dataset name=\"hsapiens_gene_ensembl\" config=\"gene_ensembl_config\">"
                + "<Filter name=\"ensembl_gene_id\" value=\"ENSG00000004478\"/>"
                + "<Attribute name=\"ensembl_transcript_id\"/>"
                + "<Attribute name=\"chromosome_name\"/>"
                + "<Attribute name=\"exon_chrom_start\"/>"
                + "<Attribute name=\"exon_chrom_end\"/>"
                + "<Attribute name=\"coding_start_offset\"/>"
                + "<Attribute name=\"coding_end_offset\"/>"
                + "<Attribute name=\"strand\"/>"
                + "<Attribute name=\"exon_id\"/>"
                + "<Attribute name=\"rank\"/>"
                + "<Attribute name=\"start_exon_id\"/>"
                + "<Attribute name=\"end_exon_id\"/>"
                // Header
                + "<Attribute name=\"ensembl_gene_id\"/>"
                + "<Attribute name=\"ensembl_transcript_id\"/>"
                + "</Dataset>"
                + "</Query>";

        OutputStream seqOut = new ByteArrayOutputStream();
        OutputStream bufOut = new ByteArrayOutputStream();

        _portal.executeQuery(xml, bufOut);

        bufOut.flush();
        bufOut.close();

        SequenceParser parser = new FiveUTRParser();

        parser
            .setExtraAttributes(2)
            .setDatabaseConnection(_jdbcURL, _tableName, _username, _password)
            .streamSequence(bufOut, seqOut);

        String results = seqOut.toString();
        System.out.println(results);
        assertTrue("ID is present", results.contains("ENSG00000004478"));
        assertTrue("Sequence is available", !results.contains("Sequence unavailable"));
    }

    @Test
    public void testGeneExonIntron()  throws TechnicalException, IOException {
        String xml = "<!DOCTYPE Query><Query client=\"biomartclient\" processor=\"TSV\" limit=\"-1\" header=\"0\">"
                + "<Dataset name=\"hsapiens_gene_ensembl\" config=\"gene_ensembl_config\">"
                + "<Filter name=\"ensembl_gene_id\" value=\"ENSG00000004478\"/>"
                + "<Attribute name=\"ensembl_transcript_id\"/>"
                + "<Attribute name=\"chromosome_name\"/>"
                + "<Attribute name=\"exon_chrom_start\"/>"
                + "<Attribute name=\"exon_chrom_end\"/>"
                + "<Attribute name=\"strand\"/>"
                + "<Attribute name=\"ensembl_gene_id\"/>"
                // Header
                + "<Attribute name=\"ensembl_gene_id\"/>"
                + "<Attribute name=\"ensembl_transcript_id\"/>"
                + "</Dataset>"
                + "</Query>";

        OutputStream seqOut = new ByteArrayOutputStream();
        OutputStream bufOut = new ByteArrayOutputStream();

        _portal.executeQuery(xml, bufOut);

        bufOut.flush();
        bufOut.close();

        SequenceParser parser = new GeneExonIntronParser();

        parser
            .setExtraAttributes(2)
            .setDatabaseConnection(_jdbcURL, _tableName, _username, _password)
            .streamSequence(bufOut, seqOut);

        String results = seqOut.toString();
        System.out.println(results);
        assertTrue("ID is present", results.contains("ENSG00000004478"));
        assertTrue("Sequence is available", !results.contains("Sequence unavailable"));
    }

    @Test
    public void testTranscriptExonIntron()  throws TechnicalException, IOException {
        String xml = "<!DOCTYPE Query><Query client=\"biomartclient\" processor=\"TSV\" limit=\"-1\" header=\"0\">"
                + "<Dataset name=\"hsapiens_gene_ensembl\" config=\"gene_ensembl_config\">"
                + "<Filter name=\"ensembl_gene_id\" value=\"ENSG00000004478\"/>"
                + "<Attribute name=\"ensembl_transcript_id\"/>"
                + "<Attribute name=\"chromosome_name\"/>"
                + "<Attribute name=\"exon_chrom_start\"/>"
                + "<Attribute name=\"exon_chrom_end\"/>"
                + "<Attribute name=\"strand\"/>"
                // Header
                + "<Attribute name=\"ensembl_gene_id\"/>"
                + "<Attribute name=\"ensembl_transcript_id\"/>"
                + "</Dataset>"
                + "</Query>";

        OutputStream seqOut = new ByteArrayOutputStream();
        OutputStream bufOut = new ByteArrayOutputStream();

        _portal.executeQuery(xml, bufOut);

        bufOut.flush();
        bufOut.close();

        SequenceParser parser = new TranscriptExonIntronParser();

        parser
            .setExtraAttributes(2)
            .setDatabaseConnection(_jdbcURL, _tableName, _username, _password)
            .streamSequence(bufOut, seqOut);

        String results = seqOut.toString();
        System.out.println(results);
        assertTrue("ID is present", results.contains("ENSG00000004478"));
        assertTrue("Sequence is available", !results.contains("Sequence unavailable"));
    }

    @Test
    public void testTranscriptFlank()  throws TechnicalException, IOException {
        String xml = "<!DOCTYPE Query><Query client=\"biomartclient\" processor=\"TSV\" limit=\"-1\" header=\"0\">"
                + "<Dataset name=\"hsapiens_gene_ensembl\" config=\"gene_ensembl_config\">"
                + "<Filter name=\"ensembl_gene_id\" value=\"ENSG00000004478\"/>"
                + "<Attribute name=\"ensembl_transcript_id\"/>"
                + "<Attribute name=\"chromosome_name\"/>"
                + "<Attribute name=\"exon_chrom_start\"/>"
                + "<Attribute name=\"exon_chrom_end\"/>"
                + "<Attribute name=\"strand\"/>"
                // Header
                + "<Attribute name=\"ensembl_gene_id\"/>"
                + "<Attribute name=\"ensembl_transcript_id\"/>"
                + "</Dataset>"
                + "</Query>";

        OutputStream seqOut = new ByteArrayOutputStream();
        OutputStream bufOut = new ByteArrayOutputStream();

        _portal.executeQuery(xml, bufOut);

        bufOut.flush();
        bufOut.close();

        SequenceParser parser = new TranscriptFlankParser();

        parser
            .setDownstreamFlank(100)
            .setExtraAttributes(2)
            .setDatabaseConnection(_jdbcURL, _tableName, _username, _password)
            .streamSequence(bufOut, seqOut);

        String results = seqOut.toString();
        System.out.println(results);
        assertTrue("ID is present", results.contains("ENSG00000004478"));
        assertTrue("Sequence is available", !results.contains("Sequence unavailable"));
    }

    @Test
    public void testGeneFlank()  throws TechnicalException, IOException {
        String xml = "<!DOCTYPE Query><Query client=\"biomartclient\" processor=\"TSV\" limit=\"-1\" header=\"0\">"
                + "<Dataset name=\"hsapiens_gene_ensembl\" config=\"gene_ensembl_config\">"
                + "<Filter name=\"ensembl_gene_id\" value=\"ENSG00000004478\"/>"
                + "<Attribute name=\"ensembl_transcript_id\"/>"
                + "<Attribute name=\"chromosome_name\"/>"
                + "<Attribute name=\"exon_chrom_start\"/>"
                + "<Attribute name=\"exon_chrom_end\"/>"
                + "<Attribute name=\"strand\"/>"
                + "<Attribute name=\"ensembl_gene_id\"/>"
                // Header
                + "<Attribute name=\"ensembl_gene_id\"/>"
                + "<Attribute name=\"ensembl_transcript_id\"/>"
                + "</Dataset>"
                + "</Query>";

        OutputStream seqOut = new ByteArrayOutputStream();
        OutputStream bufOut = new ByteArrayOutputStream();

        _portal.executeQuery(xml, bufOut);

        bufOut.flush();
        bufOut.close();

        SequenceParser parser = new GeneFlankParser();

        parser
            .setDownstreamFlank(100)
            .setExtraAttributes(2)
            .setDatabaseConnection(_jdbcURL, _tableName, _username, _password)
            .streamSequence(bufOut, seqOut);

        String results = seqOut.toString();
        System.out.println(results);
        assertTrue("ID is present", results.contains("ENSG00000004478"));
        assertTrue("Sequence is available", !results.contains("Sequence unavailable"));
    }

    @Test
    public void testThreeUTR()  throws TechnicalException, IOException {
        String xml = "<!DOCTYPE Query><Query client=\"biomartclient\" processor=\"TSV\" limit=\"-1\" header=\"0\">"
                + "<Dataset name=\"hsapiens_gene_ensembl\" config=\"gene_ensembl_config\">"
                + "<Filter name=\"ensembl_gene_id\" value=\"ENSG00000004478\"/>"
                + "<Attribute name=\"ensembl_transcript_id\"/>"
                + "<Attribute name=\"chromosome_name\"/>"
                + "<Attribute name=\"exon_chrom_start\"/>"
                + "<Attribute name=\"exon_chrom_end\"/>"
                + "<Attribute name=\"coding_start_offset\"/>"
                + "<Attribute name=\"coding_end_offset\"/>"
                + "<Attribute name=\"strand\"/>"
                + "<Attribute name=\"exon_id\"/>"
                + "<Attribute name=\"rank\"/>"
                + "<Attribute name=\"start_exon_id\"/>"
                + "<Attribute name=\"end_exon_id\"/>"
                // Header
                + "<Attribute name=\"ensembl_gene_id\"/>"
                + "<Attribute name=\"ensembl_transcript_id\"/>"
                + "</Dataset>"
                + "</Query>";

        OutputStream seqOut = new ByteArrayOutputStream();
        OutputStream bufOut = new ByteArrayOutputStream();

        _portal.executeQuery(xml, bufOut);

        bufOut.flush();
        bufOut.close();

        SequenceParser parser = new ThreeUTRParser();

        parser
            .setExtraAttributes(2)
            .setDatabaseConnection(_jdbcURL, _tableName, _username, _password)
            .streamSequence(bufOut, seqOut);

        String results = seqOut.toString();
        System.out.println(results);
        assertTrue("ID is present", results.contains("ENSG00000004478"));
        assertTrue("Sequence is available", !results.contains("Sequence unavailable"));
    }
}