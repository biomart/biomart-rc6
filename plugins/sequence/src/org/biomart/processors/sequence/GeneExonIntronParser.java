package org.biomart.processors.sequence;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author jhsu, jguberman
 */
public class GeneExonIntronParser extends TranscriptExonIntronParser {
    public GeneExonIntronParser() {
        super(6);
    }

	/**
	 * Parses input rows for QueryType GENE_EXON_INTRON.
	 * @param inputQuery	List containing query rows. Each Entry is expected to be a list containing the fields ensembl_gene_id, ensembl_transcript_id, chromosome_name, exon_chrom_start, exon_chrom_end, strand, transcript_count.
	 * @param flank	Array with position 0 containing the upstream flank size and position 1 containing the downstream flank size.
	 * @throws IOException
	 */
	@Override
    public void parse(List<List<String>> inputQuery) throws IOException {
		// "Unspliced (Gene)"
		/*	This code theoretically could be exactly the same as parseTranscriptExonIntron,
		 *  with the field numbering changed to accommodate the geneID. However,
		 *  parseTranscriptExonIntron relies on the ordering of the input by transcriptID.
		 *  Since the output of martservice can't be reordered by geneID, the more complicated
		 *  parsing is necessary. On the other hand, this method is much more robust, as it doesn't
		 *  depend on the input ordering.
		 *
		 *  Note that this implementation STILL relies on the ordering by transcriptID, but it does
		 *  demonstrate how a generalized parser might be made to deal with unordered input in any field.
		 */
		// TODO Doesn't yet handle flank
		// TODO Optimize by using the transcript_count info when initializing lists? (Except transcript_count seems to be empty)

		// Set up the fields
		final int transcriptIDfield = 0;
		final int chrField = 1;
		final int startField = 2;
		final int endField = 3;
		final int strandField = 4;
		final int geneIDfield = 5;

        boolean done = false;

		// Initialize hashmap mapping geneIDs to input lines, so we don't need to worry about the order of the input
		HashMap<String, List<List<String>>> geneMap = new HashMap<String,List<List<String>>>();
		String currentGeneID;
		List<List<String>> appendedList = null;
		for(List<String> line : inputQuery){
			currentGeneID = line.get(geneIDfield);
			appendedList = geneMap.get(currentGeneID);
			if(null==appendedList) {
				appendedList = new ArrayList<List<String>>();
			}
			appendedList.add(line);
			geneMap.put(currentGeneID, appendedList);
		}
		List<List<String>> currentGene = null;
		List<String> firstLine = null;

		for(String geneID : geneMap.keySet()){
			currentGene = geneMap.get(geneID);
			firstLine = currentGene.get(0);
			String chr = firstLine.get(chrField);
			int start = Integer.parseInt(firstLine.get(startField));
			int end = Integer.parseInt(firstLine.get(endField));
			String strand = firstLine.get(strandField);
			for(List<String> line : currentGene){
                storeHeaderInfo(line);
				start = Math.min(start, Integer.parseInt(line.get(startField)));
				end = Math.max(end, Integer.parseInt(line.get(endField)));
			}
			done = printTranscriptExonIntron(getHeader(), chr, start, end, strand);
            clearHeader();
            if (done) {
                break;
            }
		}
	}

}
