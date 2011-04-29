package org.biomart.processors.sequence;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;
import org.biomart.common.exceptions.ValidationException;

/**
 *
 * @author jhsu, jguberman
 */
public class CodingParser extends SequenceParser {
    private final boolean isProtein;

    public CodingParser() { this(false, 12); }
    public CodingParser(boolean isProtein, int i) {
        super(i);
        this.isProtein = isProtein;
    }

    @Override
    public SequenceParser validate() throws ValidationException {
		if (isProtein && (upstreamFlank > 0  || downstreamFlank > 0)){
			throw new ValidationException("Validation Error: Protein sequences cannot have flanking regions.");
		}
        return this;
    }

	/**
	 * Parses input rows for QueryType CODING.
	 * @param inputQuery	List containing query rows. Each Entry is expected to be a list containing the fields ensembl_transcript_id, chromosome_name, exon_chrom_start, exon_chrom_end, coding_start_offset, coding_end_offset, strand, exon_id, rank, start_exon_id, end_exon_id, phase.
	 * @param isProtein If TRUE, translates the resulting sequence to a protein sequence.
	 * @throws IOException
	 */
	@Override
    public void parse(List<List<String>> inputQuery) throws IOException {
		// "Coding sequence" and "Protein"
		final int transcriptIDField = 0;
		final int chrField = 1;
		final int startField = 2;
		final int endField = 3;
		final int codingOffsetStartField = 4;
		final int codingOffsetEndField = 5;
		final int strandField = 6;
		final int exonIDField = 7;
		final int rankField = 8;
		final int startExonIDField = 9;
		final int endExonIDField = 10;
		final int phaseField = 11;
		final int codonTableField = 12;
		final int seqEditField = 13;

        boolean done = false;

		// These TreeMaps will keep track of the exons
		TreeMap<Integer, Integer> start = new TreeMap<Integer, Integer>();
		TreeMap<Integer, Integer> end = new TreeMap<Integer, Integer>();

		String transcriptID = null;
		String startExonID = null;
		String endExonID = null;
		int startExonRank = 0;
		int endExonRank = 0;
		String exonID = null;
		String chr = null;
		int codingStartOffset = 0;
		int codingEndOffset = 0;
		int startPhase = 0;
		int currentRank = 0;
		String strand = null;
		String codonTableID = null;

		HashSet<String> seqEdit = new HashSet<String>();

		for(List<String> line : inputQuery) {
			exonID = line.get(exonIDField);
			if (!line.get(transcriptIDField).equals(transcriptID)) {
				//If it's a new transcript, print the current sequence
                if (transcriptID != null) {
                    done = printCoding(getHeader(), chr, start, end, codingStartOffset, codingEndOffset, startExonRank, endExonRank, strand, startPhase, codonTableID, seqEdit,isProtein);
                }

				transcriptID = line.get(transcriptIDField);
				startExonID = line.get(startExonIDField);
				endExonID = line.get(endExonIDField);
				startExonRank = 0;
				endExonRank = 0;
				startPhase = 0;
				chr = "";
				start.clear();
				end.clear();
				strand = line.get(strandField);
				if(isProtein){
					codonTableID = line.get(codonTableField);
					seqEdit = new HashSet<String>();
					seqEdit.add(line.get(seqEditField));
				}
                clearHeader();
                if (done) {
                    break;
                }
			}
			currentRank = Integer.parseInt(line.get(rankField))-1; // Subtract 1 to convert to zero indexing
			if(isProtein){
				seqEdit.add(line.get(seqEditField));
			}
			if (!startExonID.equals("")) {
				start.put(currentRank, Integer.parseInt(line
						.get(startField)));
				end.put(currentRank, Integer.parseInt(line
						.get(endField)));
			}
			if (exonID.equals(startExonID)){
				// If it's the terminal exon, record the chromosome and codingOffset and Phase
				chr = line.get(chrField);
				codingStartOffset = Integer.parseInt(line.get(codingOffsetStartField));
				startExonRank = currentRank;
				startPhase = Integer.parseInt(line.get(phaseField));
			}
			if (exonID.equals(endExonID)){
				// If it's the terminal exon, record the chromosome and codingOffset
				chr = line.get(chrField);
				codingEndOffset = Integer.parseInt(line.get(codingOffsetEndField));
				endExonRank = currentRank;
			}

            storeHeaderInfo(line);
		}
        if (!done) {
            printCoding(getHeader(), chr, start, end, codingStartOffset, codingEndOffset, startExonRank, endExonRank, strand, startPhase, codonTableID, seqEdit, isProtein);
        }
	}

	/**
	 * Retrieves and prints sequence for QueryType CODING
	 * @param header Header for the sequence.
	 * @param chr	Chromosome name.
	 * @param start	Sequence start position.
	 * @param end	Sequence end position.
	 * @param codingStartOffset Coding start offset.
	 * @param codingEndOffset	Coding end offset.
	 * @param startExonRank	Rank of start exon.
	 * @param endExonRank	Rank of end exon.
	 * @param strand	Sequence strand.
	 * @param startPhase
	 * @param isProtein	If TRUE, translate sequence to amino acid sequence.
	 */
	protected final boolean printCoding(String header, String chr,
			TreeMap<Integer, Integer> start, TreeMap<Integer, Integer> end, int codingStartOffset,
			int codingEndOffset, int startExonRank, int endExonRank, String strand, int startPhase, String codonTableID ,HashSet<String> seqEdit,boolean isProtein) throws IOException {
        try {
            StringBuilder sequence = new StringBuilder();
            if (!chr.equals("")){
                if (strand.equals("-1")){
                    if (startExonRank == endExonRank){
                        sequence.append(reverseComplement(getSequence(chr,end.get(startExonRank)-codingEndOffset+1-downstreamFlank,end.get(startExonRank)-codingStartOffset+1+upstreamFlank)));
                    } else {
                        sequence.append(reverseComplement(getSequence(chr,start.get(startExonRank),end.get(startExonRank)-codingStartOffset+1+upstreamFlank)));
                        for (int i = startExonRank+1; i < endExonRank; i++){
                            sequence.append(reverseComplement(getSequence(chr, start.get(i), end.get(i))));
                        }
                        sequence.append(reverseComplement(getSequence(chr,end.get(endExonRank)-codingEndOffset+1-downstreamFlank,end.get(endExonRank))));
                    }
                } else {
                    if (startExonRank == endExonRank){
                        sequence.append(getSequence(chr,start.get(startExonRank)+codingStartOffset-1-upstreamFlank,start.get(startExonRank)+codingEndOffset-1+downstreamFlank));
                    } else {
                        sequence.append(getSequence(chr,start.get(startExonRank)+codingStartOffset-1-upstreamFlank,end.get(startExonRank)));
                        for (int i = startExonRank+1; i < endExonRank; i++){
                            sequence.append((getSequence(chr, start.get(i), end.get(i))));
                        }
                        sequence.append(getSequence(chr,start.get(endExonRank),start.get(endExonRank)+codingEndOffset-1+downstreamFlank));
                    }
                }
            }
            if (sequence.length()>0){
                for(int i = startPhase; i > 0; --i){
                    sequence.insert(0, 'N');
                }
            }
            if(isProtein){
                return printFASTA(SequenceTranslator.translateSequence(sequence.toString(), seqEdit, codonTableID), header);
            } else {
                return  printFASTA(sequence.toString(), header);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return printFASTA(SEQUENCE_UNAVAILABLE, header);
        }
	}
}
