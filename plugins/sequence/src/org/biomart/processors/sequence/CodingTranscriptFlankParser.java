package org.biomart.processors.sequence;

import java.io.IOException;
import java.util.List;
import org.biomart.common.exceptions.ValidationException;

/**
 *
 * @author jhsu, jguberman
 */
public class CodingTranscriptFlankParser extends SequenceParser {
    public CodingTranscriptFlankParser() {
        super(11);
    }

    @Override
    public SequenceParser validate() throws ValidationException {
		checkFlank();
        return this;
    }

	/**
	 * Parses input rows for QueryType CODING_TRANSCRIPT_FLANK.
	 * @param inputQuery	List containing query rows. Each Entry is expected to be a list containing the fields ensembl_transcript_id, chromosome_name, exon_chrom_start, exon_chrom_end, coding_start_offset, coding_end_offset, strand, exon_id, rank, start_exon_id, end_exon_id.
	 * @param flank	Array with position 0 containing the upstream flank size and position 1 containing the downstream flank size.
	 * @throws IOException
	 */
	@Override
    public void parse(List<List<String>> inputQuery) throws IOException {
		// "Flank-coding region (Transcript)"
		// The coding_start_offset and coding_end_offset are both given as a distance from
		// the exon_chrom_start of the exon that has exon_id equal to start_exon_id or end_exon_id, respectively
		final int transcriptIDField = 0;
		final int chrField = 1;
		final int startField = 2;
		final int endField = 3;
		int codingOffsetField; // set depending on flank
		final int strandField = 6;
		final int exonIDField = 7;
		int terminalExonField;

        boolean done = false;

		if (upstreamFlank >0 ){
			terminalExonField = 9;
			codingOffsetField = 4;
		} else {
			terminalExonField = 10;
			codingOffsetField = 5;
		}
        String transcriptID = null;
        String chr = null;
        String strand = null;
		int start = 0;
		int end = 0;
		String terminalExonID = null;
		String exonID = null;
		int codingOffset = 0;

		for (List<String> line : inputQuery){
			exonID = line.get(exonIDField);
			if (!line.get(transcriptIDField).equals(transcriptID)) {
                if (transcriptID != null) {
                    done = printCodingTranscriptFlank(getHeader(), chr, start, end, codingOffset, strand);
                }
				transcriptID = line.get(transcriptIDField);
				terminalExonID = line.get(terminalExonField);
				chr = "";
				start = 0;
				end = 0;
				strand = line.get(strandField);
                clearHeader();
                if (done) {
                    break;
                }
			}
			if (exonID.equals(terminalExonID)){
				chr = line.get(chrField);
				start = Integer.parseInt(line.get(startField));
				end = Integer.parseInt(line.get(endField));
				codingOffset = Integer.parseInt(line.get(codingOffsetField));
			}
            storeHeaderInfo(line);
		}
        if (!done) {
            printCodingTranscriptFlank(getHeader(), chr, start, end, codingOffset,strand);
        }
	}

	/**
	 * Retrieves and prints sequence for QueryType CODING_TRANSCRIPT_FLANK
	 * @param flank	Array containing upstream/downstream flank distance
	 * @param header	Header for sequence.
	 * @param chr	Chromosome name.
	 * @param start	Sequence start position.
	 * @param end	Sequence end position.
	 * @param codingOffset Coding offset.
	 * @param strand Sequence strand.
	 * @throws IOException
	 */
	protected final boolean printCodingTranscriptFlank(String header,
            String chr, int start, int end, int codingOffset,String strand) throws IOException {
        try {
            String sequence;
            // Check whether we're dealing with the 5' flank or the 3', and handle accordingly
            if (chr.equals("")){
                sequence = null;
            } else if (upstreamFlank>0){
                if (strand.equals("-1")){
                    sequence = reverseComplement(getSequence(chr, end+2-codingOffset, end+upstreamFlank-codingOffset+1));
                } else {
                    sequence = (getSequence(chr, start-upstreamFlank+codingOffset-1, start-2+codingOffset));
                }
            } else {
                if (strand.equals("-1")){
                    sequence = reverseComplement(getSequence(chr, end+1-codingOffset-downstreamFlank, end-codingOffset));
                } else {
                    sequence = (getSequence(chr, start+codingOffset, start-1+codingOffset+downstreamFlank));
                }
            }
            return printFASTA(sequence, header);
        } catch (Exception e) {
            e.printStackTrace();
            return printFASTA(SEQUENCE_UNAVAILABLE, header);
        }
	}

}
