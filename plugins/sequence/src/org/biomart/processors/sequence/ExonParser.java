package org.biomart.processors.sequence;

import java.io.IOException;
import java.util.List;

/**
 *
 * @author jhsu, jguberman
 */
public class ExonParser extends TranscriptExonIntronParser {
    public ExonParser() {
        super(6);
    }

	/**
	 * Parses input rows for QueryType TRANSCRIPT_EXON_INTRON.
	 * @param inputQuery	List containing query rows. Each Entry is expected to be a list containing the fields ensembl_transcript_id, chromosome_name, exon_chrom_start, exon_chrom_end, strand, ordered by transcript_id.
	 * @param flank	Array with position 0 containing the upstream flank size and position 1 containing the downstream flank size.
	 * @throws IOException
	 */
	@Override
    public void parse(List<List<String>> inputQuery) throws IOException {
		// "Unspliced (Transcript)"
		// Doesn't yet handle flank

		// Set up the fields
		final int transcriptIDfield = 0;
		final int chrField = 1;
		final int startField = 2;
		final int endField = 3;
		final int strandField = 4;
		final int exonIDField = 5;

		// Read the first line of the input and initialize the variables
        String transcriptID = null;
        String chr = null;
        String strand = null;
		int start = 0;
		int end = 0;
        String exonID = null;

        boolean done = false;

		for(List<String> line : inputQuery){
			// Check if the current row belongs to the same transcript
			if (line.get(exonIDField).equals(exonID)) {
				// If it does, adjust the start and end positions if needed
				start = Math.min(start, Integer.parseInt(line.get(startField)));
				end = Math.max(end, Integer.parseInt(line.get(endField)));
				// Update header as necessary
			} else {
                if (exonID != null) {
                    done = printTranscriptExonIntron(getHeader(), chr, start, end, strand);
                }

				// Initialize for the next transcript ID, and re-enter the loop
				transcriptID = line.get(transcriptIDfield);
				exonID = line.get(exonIDField);
				chr = line.get(chrField);
				start = Integer.parseInt(line.get(startField));
				end = Integer.parseInt(line.get(endField));
				strand = line.get(strandField);
				// Re-initialize header
                clearHeader();

                if (done) {
                    break;
                }
			}
            storeHeaderInfo(line);
		}
        if (!done) {
            printTranscriptExonIntron(getHeader(), chr, start, end, strand);
        }
	}
}
