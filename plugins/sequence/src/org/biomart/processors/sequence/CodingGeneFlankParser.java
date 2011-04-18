package org.biomart.processors.sequence;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.biomart.common.exceptions.TechnicalException;
import org.biomart.common.exceptions.ValidationException;
import org.biomart.common.resources.Log;

/**
 *
 * @author jhsu, jguberman
 */
public class CodingGeneFlankParser extends SequenceParser {
    public CodingGeneFlankParser() {
        super(13);
    }

    @Override
    public SequenceParser validate() throws ValidationException {
		checkFlank();
        return this;
    }

	/**
	 * Parses input rows for QueryType CODING_GENE_FLANK.
	 * @param inputQuery	List containing query rows. Each Entry is expected to be a list containing the fields ensembl_gene_id, ensembl_transcript_id, chromosome_name, exon_chrom_start, exon_chrom_end, coding_start_offset, coding_end_offset, strand, exon_id, rank, start_exon_id, end_exon_id, transcript_count.
	 * @param flank	Array with position 0 containing the upstream flank size and position 1 containing the downstream flank size.
	 * @throws TechnicalException
	 */
	@Override
    public void parse(List<List<String>> inputQuery) throws IOException {
		// "Flank-coding region (Gene)"
		// TODO Optimize by using the transcript_count info when initializing lists? (Except transcript_count seems to be empty)
		// TODO Clean up in general

		// Set up the fields
		final int geneIDfield = 0;
		final int transcriptIDfield = 1;
		final int chrField = 2;
		final int startField = 3;
		final int endField = 4;
		final int codingStartOffsetField = 5;
		final int codingEndOffsetField = 6;
		final int strandField = 7;
		final int exonIDField = 8;
		final int rankField = 9;
		final int startExonIDField = 10;
		final int endExonIDField = 11;
		final int transcriptCountField = 12;

        boolean done = false;

		int terminalExonField ;
		int codingOffsetField ;

		if (upstreamFlank >0 ){
			terminalExonField = startExonIDField;
			codingOffsetField = codingStartOffsetField;
		} else {
			terminalExonField = endExonIDField;
			codingOffsetField = codingEndOffsetField;
		}

		// Initialize hashmap mapping geneIDs to input lines, so we don't need to worry about the order of the input
		Map<String, List<List<String>>> geneMap = new HashMap<String,List<List<String>>>();

		for(List<String> line : inputQuery) {
			String currentGeneID = line.get(geneIDfield);
            List<List<String>> list = geneMap.get(currentGeneID);
			if(null == list) {
				list = new ArrayList<List<String>>();
                geneMap.put(currentGeneID, list);
			}
			list.add(line);
		}

		for(String geneID : geneMap.keySet()) {
            List<List<String>> lines = geneMap.get(geneID);
			String transcriptID = null;
			String terminalExonID = null;
			String chr = "";
			Integer start = Integer.MAX_VALUE;
			Integer end = Integer.MIN_VALUE;
			String strand = null;
			int codingOffset = 0;
			String exonID = null;

			for(List<String> line : lines){
                storeHeaderInfo(line);

                exonID = line.get(exonIDField);

				if (!line.get(transcriptIDfield).equals(transcriptID)){
					transcriptID = line.get(transcriptIDfield);
					terminalExonID = line.get(terminalExonField);
					strand = line.get(strandField);
				}

                if (terminalExonID.equals(exonID)){
					codingOffset = Integer.parseInt(line.get(codingOffsetField));

                    start = Math.min(start, Integer.parseInt(line.get(startField)) + codingOffset);
                    end = Math.max(end, Integer.parseInt(line.get(endField)) - codingOffset);

					chr = line.get(chrField);
				}
			}

            done = printCodingGeneFlank(getHeader(), chr, start, end, strand);

            clearHeader();

            if (done) {
                break;
            }
		}
	}

	protected final boolean printCodingGeneFlank(String header, String chr, int start,
            int end, String strand) throws IOException {
		String sequence;

        Log.info(String.format("start = %s, end = %s", start, end));

        if (upstreamFlank > 0) {
            if (strand.equals("-1")) {
                start = end + 2;
                end = end + upstreamFlank + 1;
            } else {
                end = start - 2;
                start = start - upstreamFlank - 1;
            }
        } else {
            if (strand.equals("-1")) {
                start = end - downstreamFlank;
                end = end - 1;
            } else {
                end = start - 1 + downstreamFlank;
            }
        }

		// Take the reverse complement if necessary
		if (strand.equals("-1")){
			sequence = reverseComplement(getSequence(chr, start, end));
		} else {
			sequence = getSequence(chr, start, end);
		}
		return printFASTA(sequence, header);
	}
}
