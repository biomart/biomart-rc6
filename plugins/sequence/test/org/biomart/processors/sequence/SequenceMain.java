package org.biomart.processors.sequence;

import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.biomart.api.Portal;
import org.biomart.api.factory.MartRegistryFactory;
import org.biomart.api.factory.XmlMartRegistryFactory;
import org.biomart.processors.ProcessorRegistry;
import org.biomart.processors.TSV;

/**
 * @author Anthony Cros
 * class to test sequences
 * arguments are self-explanatory
 */
public class SequenceMain {

    static {
        System.setProperty("biomart.debug", "true");
        System.setProperty("log.level", "debug");
    }

    public static void main(String[] args) throws Exception {
    	
    	String operation = null;
    	Integer upstreamFlank = null;
    	Integer downstreamFlank = null;
    	String ids = null;
    	String species = null;		// like hsapiens, mmusculus, ...
    	String chromosome = null;
    	
    	if (args.length==0) {
	    	operation = "fcrg";
	    	upstreamFlank = 0;
	    	downstreamFlank = 80;
	    	ids = "ENSG00000101204";
	    	species = "hsapiens";
	    	chromosome = "20";
    	} else {
	    	operation = args[0];
	    	upstreamFlank = Integer.valueOf(args[1]);
	    	downstreamFlank = Integer.valueOf(args[2]);
	    	ids = args[3];
	    	species = args[4];
	    	chromosome = args[5];
    	}
    	
		process(operation, upstreamFlank, downstreamFlank, ids, species, chromosome);
	}

	private static String getCode(String operation) {
		String code = null;
		if ("usg".equals(operation)) {
			code = SequenceConstants.QueryType.GENE_EXON_INTRON.getCode();
		} else if ("ust".equals(operation)) {
			code = SequenceConstants.QueryType.TRANSCRIPT_EXON_INTRON.getCode();
		} else if ("flankg".equals(operation)) {
			code = SequenceConstants.QueryType.GENE_FLANK.getCode();
		} else if ("flankt".equals(operation)) {
			code = SequenceConstants.QueryType.TRANSCRIPT_FLANK.getCode();
		} else if ("fcrg".equals(operation)) {
			code = SequenceConstants.QueryType.CODING_GENE_FLANK.getCode();
		} else if ("fcrt".equals(operation)) {
			code = SequenceConstants.QueryType.CODING_TRANSCRIPT_FLANK.getCode();
		} else if ("5utr".equals(operation)) {
			code = SequenceConstants.QueryType.FIVE_UTR.getCode();
		} else if ("3utr".equals(operation)) {
			code = SequenceConstants.QueryType.THREE_UTR.getCode();
		} else if ("exon".equals(operation)) {
			code = SequenceConstants.QueryType.GENE_EXON.getCode();	//TODO rename to transcript exon?
		} else if ("cdna".equals(operation)) {
			code = SequenceConstants.QueryType.CDNA.getCode();
		} else if ("coding".equals(operation)) {
			code = SequenceConstants.QueryType.CODING.getCode();
		} else if ("prot".equals(operation)) {
			code = SequenceConstants.QueryType.PEPTIDE.getCode();
		} else assert false;
		return code;
	}
	
	private static void process(String operation, int upstreamFlank, int downstreamFlank, String ids, String species2, String chromosome) throws Exception {
		List<String> idList = ids==null || ids.isEmpty() || ids.equals("\"\"") ?
			new ArrayList<String>() : 
			new ArrayList<String>(Arrays.asList(ids.split(",")));
		
		String xmlQuery = buildXmlQuery(operation, upstreamFlank, downstreamFlank, idList, species2, chromosome);
		System.err.println("xmlQuery = " + xmlQuery);
    	
		Portal portal = initialize();
    	
		String output = executeQuery(portal, xmlQuery);
		System.out.println(output);
    	
		System.exit(0);
	}

	private static Portal initialize() {
		System.setProperty("org.biomart.baseDir", ".");
		
		System.setProperty("processors.sequence.connection", "jdbc:mysql://dcc-qa.oicr.on.ca:3306/sequence_mart_61");
		System.setProperty("processors.sequence.username", "dcc_web");
		System.setProperty("processors.sequence.password", "sgg32fde");

		Portal portal = null;
		try {
			MartRegistryFactory factory = new XmlMartRegistryFactory(
				"./testdata/sequence.xml",
				"./testdata/.sequence"
			);	
			portal = new Portal(factory);
		} catch(Exception e) {
			e.printStackTrace();
			fail("Exception initializing registry: " + e.getMessage());
		}
		ProcessorRegistry.register("Sequence", Sequence.class);
		ProcessorRegistry.register("TSV", TSV.class);
		return portal;
	}

	private static String executeQuery(Portal portal, String xmlQuery) throws Exception {
		OutputStream seqOut = new ByteArrayOutputStream();
		portal.executeQuery(xmlQuery, seqOut);
		seqOut.close();
		String output = seqOut.toString();
		return output;
	}

	private static String buildXmlQuery(String operation, int upstreamFlank, int downstreamFlank, List<String> idList, String species2, String chromosome) {
		
		String code = getCode(operation);
		Integer id_type = getIdType(code);
	
		System.err.println("id_type = " + id_type);
		
		String ids = idList.toString().replace(", ", ",").replace("[", "").replace("]", "");
		String filter = null;
		if (chromosome.isEmpty()) {
			filter = id_type>1 ? 
				"<Filter name=\"ensembl_transcript_id\" value=\"" + ids + "\"/>" : 
				"<Filter name=\"ensembl_gene_id\" value=\"" + ids + "\"/>";
		} else {
			filter = "<Filter name=\"chromosome_name\" value=\"" + chromosome + "\"/>";
		}
		String xml = 
        	 "<!DOCTYPE Query>" +
        	 "<Query client=\"biomartclient\"  limit=\"-1\" header=\"0\">" +
        	 "<Processor name=\"sequence\">" +
	        	 "<Parameter name=\"type\" value=\"" + code + "\"/>" +
	        	 "<Parameter name=\"upstreamFlank\" value=\"" + upstreamFlank + "\"/>" +
	        	 "<Parameter name=\"downstreamFlank\" value=\"" + downstreamFlank + "\"/>" +
        	 "</Processor>" +
        	 "<Dataset name=\"" + species2 + "_gene_ensembl\" config=\"gene_ensembl_config\">" +
        	    filter +
             	"<Attribute name=\"ensembl_gene_id\"/>" +
	        	(id_type>1 ? "<Attribute name=\"ensembl_transcript_id\"/>" : "") +
	        	(id_type>2 ? "<Attribute name=\"ensembl_exon_id\"/>" : "") +
        	 "</Dataset>" +
        	 "</Query>";
		return xml;
	}

	private static Integer getIdType(String code) {
		Integer id_type = null;
		if (
				SequenceConstants.QueryType.GENE_EXON_INTRON.getCode().equals(code) ||
				SequenceConstants.QueryType.GENE_FLANK.getCode().equals(code) ||
				SequenceConstants.QueryType.CODING_GENE_FLANK.getCode().equals(code)
			) {
			id_type = 1;
		} else if (
			SequenceConstants.QueryType.TRANSCRIPT_EXON_INTRON.getCode().equals(code) ||
			SequenceConstants.QueryType.TRANSCRIPT_FLANK.getCode().equals(code) ||
			SequenceConstants.QueryType.CODING_TRANSCRIPT_FLANK.getCode().equals(code) ||
			SequenceConstants.QueryType.FIVE_UTR.getCode().equals(code) ||
			SequenceConstants.QueryType.THREE_UTR.getCode().equals(code) ||
			SequenceConstants.QueryType.CDNA.getCode().equals(code) ||
			SequenceConstants.QueryType.CODING.getCode().equals(code) ||
			SequenceConstants.QueryType.PEPTIDE.getCode().equals(code)
			) {
			id_type = 2;
		} else if (
			SequenceConstants.QueryType.GENE_EXON.getCode().equals(code)
			) {
			id_type = 3;
		} else assert false;
		return id_type;
	}
}