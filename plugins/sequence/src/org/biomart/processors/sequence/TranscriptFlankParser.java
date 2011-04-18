package org.biomart.processors.sequence;

import java.io.IOException;
import java.util.List;
import org.biomart.common.exceptions.ValidationException;

/**
 *
 * @author jhsu, jguberman
 */
public class TranscriptFlankParser extends SequenceParser {
    public TranscriptFlankParser() {
        super(5);
    }

    public TranscriptFlankParser(int i) {
        super(i);
    }

    @Override
    public SequenceParser validate() throws ValidationException {
		// Make sure the flank region request makes sense
		checkFlank();
        return this;
    }

	/**
	 * Parses input rows for QueryType TRANSCRIPT_FLANK.
	 * @param inputQuery	List containing query rows. Each Entry is expected to be a list containing the fields ensembl_transcript_id, chromosome_name, exon_chrom_start, exon_chrom_end, strand, ordered by transcript_id.
	 * @param flank	Array with position 0 containing the upstream flank size and position 1 containing the downstream flank size.
	 * @throws IOException
	 */
	@Override
    public void parse(List<List<String>> inputQuery) throws IOException {
		// "Flank (Transcript)"

		// Set up the fields
		final int transcriptIDField = 0;
		final int chrField = 1;
		final int startField = 2;
		final int endField = 3;
		final int strandField = 4;

		// Initialize the variables for the first transcript ID
        String transcriptID = null;
        String chr = null;
        String strand = null;
		int start = 0;
		int end = 0;

        boolean done = false;

		for(List<String> line : inputQuery){

			// Check if the current row belongs to the same transcript
			if (line.get(transcriptIDField).equals(transcriptID)) {
				// If it does, adjust start and end as needed
				start = Math.min(start, Integer.parseInt(line.get(startField)));
				end = Math.max(end, Integer.parseInt(line.get(endField)));
			} else {
                if (transcriptID != null) {
                    // If it isn't, we print the last sequence and initialize the next
                    done = printTranscriptFlank(getHeader(), chr, start, end, strand);
                }

				transcriptID = line.get(transcriptIDField);
				chr = line.get(chrField);
				start = Integer.parseInt(line.get(startField));
				end = Integer.parseInt(line.get(endField));
				strand = line.get(strandField);
                clearHeader();

                if (done) {
                    break;
                }

			}
            storeHeaderInfo(line);
		}
		// Print the final sequence
        if (!done) {
            printTranscriptFlank(getHeader(), chr, start, end, strand);
        }
	}
	/**
	 * Retrieves and prints sequence for QueryType TRANSCRIPT_FLANK
	 * @param flank	Array containing the upstream/downstream flank information.
	 * @param header Header for the sequence.
	 * @param chr	Chromosome name.
	 * @param start	Sequence start position.
	 * @param end	Sequence end position.
	 * @param strand	Sequence strand.
	 * @throws IOException
	 */
	protected final boolean printTranscriptFlank(String header, String chr,
			int start, int end, String strand) throws IOException {
		String sequence;
		// Check whether we're dealing with the 5' flank or the 3', and handle accordingly
		if (upstreamFlank > 0){
			if (strand.equals("-1")){
				sequence = reverseComplement(getSequence(chr, end+1, end+upstreamFlank));
			} else {
				sequence = (getSequence(chr, Math.max(start-upstreamFlank,0), start-1));
			}
		} else {
			if (strand.equals("-1")){
				sequence = reverseComplement(getSequence(chr, Math.max(start-downstreamFlank,0), start-1));
			} else {
				sequence = (getSequence(chr, end+1, end+downstreamFlank));
			}
		}
		return printFASTA(sequence, header);
	}
}
