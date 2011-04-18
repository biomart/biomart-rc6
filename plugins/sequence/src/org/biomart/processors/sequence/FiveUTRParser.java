package org.biomart.processors.sequence;

import java.io.IOException;
import java.util.List;
import java.util.TreeMap;
import org.biomart.common.exceptions.ValidationException;

/**
 *
 * @author jhsu, jguberman
 */
public class FiveUTRParser extends SequenceParser {
    public FiveUTRParser() {
        super(11);
    }

    @Override
    public SequenceParser validate() throws ValidationException {
        return this;
    }

	/**
	 * Parses input rows for QueryType FIVE_UTR.
	 * @param inputQuery	List containing query rows. Each Entry is expected to be a list containing the fields ensembl_transcript_id, chromosome_name, exon_chrom_start, exon_chrom_end, coding_start_offset, coding_end_offset, strand, exon_id, rank, start_exon_id, end_exon_id.
	 * @param flank	Array with position 0 containing the upstream flank size and position 1 containing the downstream flank size.
	 * @throws IOException
	 */
    @Override
	public void parse(List<List<String>> inputQuery) throws IOException{
		// "5' UTR"

		final int transcriptIDField = 0;
		final int chrField = 1;
		final int startField = 2;
		final int endField = 3;
		final int codingOffsetField = 4;
		final int strandField = 6;
		final int exonIDField = 7;
		final int rankField = 8;
		final int terminalExonField = 9;

        boolean done = false;

		// These TreeMaps will keep track of the exons before the start_exon
		TreeMap<Integer, Integer> start = new TreeMap<Integer, Integer>();
		TreeMap<Integer, Integer> end = new TreeMap<Integer, Integer>();

		String transcriptID = null;
		String terminalExonID = null;
		int terminalExonRank = 0;
		String exonID = null;
		String chr = null;
		int codingOffset = 0;
		int currentRank = 0;
		String strand = null;

		for(List<String> line : inputQuery){
			exonID = line.get(exonIDField);
			if (!line.get(transcriptIDField).equals(transcriptID)){
				//If it's a new transcript, print the current sequence
                if (transcriptID != null) {
                    done = print5UTR(getHeader(), chr, start, end, codingOffset, terminalExonRank, strand);
                }
				transcriptID = line.get(transcriptIDField);
				terminalExonID = line.get(terminalExonField);
				terminalExonRank = 0;
				chr = "";
				start.clear();
				end.clear();
				strand = line.get(strandField);
                clearHeader();
                if (done) {
                    break;
                }
			}
			currentRank = Integer.parseInt(line.get(rankField))-1; // Subtract 1 to convert to zero indexing
			start.put(currentRank, Integer.parseInt(line.get(startField)));
			end.put(currentRank, Integer.parseInt(line.get(endField)));
			if (exonID.equals(terminalExonID)){
				// If it's the terminal exon, record the chromosome and codingOffset
				chr = line.get(chrField);
				codingOffset = Integer.parseInt(line.get(codingOffsetField));
				terminalExonRank = currentRank;
			}
            storeHeaderInfo(line);
		}
        if (!done) {
            print5UTR(getHeader(), chr, start, end, codingOffset,terminalExonRank, strand);
        }
	}

	/**
	 * Retrieves and prints sequence for QueryType TRANSCRIPT_EXON_INTRON
	 * @param header	Header for sequence.
	 * @param chr	Chromosome name.
	 * @param start	Sequence start position.
	 * @param end	Sequence end position.
	 * @param terminalExonRank	Rank of the terminal exon.
	 * @param strand	Sequence strand.
	 * @throws IOException
	 */
	protected final boolean print5UTR(String header, String chr,
            TreeMap<Integer, Integer> start, TreeMap<Integer, Integer> end,
            int codingOffset,int terminalExonRank, String strand) throws IOException {
		StringBuilder sequence = new StringBuilder();
		if (!chr.equals("")){
			if (strand.equals("-1")){
				if(!(getSequence(chr,end.get(terminalExonRank)-codingOffset+2,end.get(terminalExonRank)).equals(""))){
					sequence.append(reverseComplement(getSequence(chr,end.get(0)+1,end.get(0)+upstreamFlank)));
					for (int i = 0; i < terminalExonRank; i++){
						sequence.append(reverseComplement(getSequence(chr, start.get(i), end.get(i))));
					}
					sequence.append(reverseComplement(getSequence(chr,end.get(terminalExonRank)-codingOffset+2-downstreamFlank,end.get(terminalExonRank))));
				}
			} else {
				if(!(getSequence(chr,start.get(terminalExonRank),start.get(terminalExonRank)+codingOffset-2).equals(""))){
					sequence.append(getSequence(chr,start.get(0)-upstreamFlank,start.get(0)-1));
					for (int i = 0; i < terminalExonRank; i++){
						sequence.append((getSequence(chr, start.get(i), end.get(i))));
					}
					sequence.append(getSequence(chr,start.get(terminalExonRank),start.get(terminalExonRank)+codingOffset-2+downstreamFlank));
				}
			}
		}
		return printFASTA(sequence.toString(), header);
	}

}
