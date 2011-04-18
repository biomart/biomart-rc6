package org.biomart.processors.sequence;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.biomart.common.exceptions.ValidationException;

/**
 *
 * @author jhsu, jguberman
 */
public class CDNAParser extends SequenceParser {
    public CDNAParser() {
        super(6);
    }

    @Override
    public SequenceParser validate() throws ValidationException {
        return this;
    }

	/**
	 * Parses input rows for QueryType CDNA.
	 * @param inputQuery	List containing query rows. Each Entry is expected to be a list containing the fields ensembl_transcript_id, chromosome_name, exon_chrom_start, exon_chrom_end, strand, rank.
	 * @param flank	Array with position 0 containing the upstream flank size and position 1 containing the downstream flank size.
	 * @throws IOException
	 */
    @Override
	public void parse(List<List<String>> inputQuery) throws IOException {
		// "cDNA sequences"

		final int transcriptIDfield = 0;
		final int chrField = 1;
		final int startField = 2;
		final int endField = 3;
		final int strandField = 4;
		final int rankField = 5;

        boolean done = false;

		// These TreeMaps will keep track of the exons
		Map<Integer,Integer> start = new TreeMap<Integer,Integer>();
		Map<Integer,Integer> end = new TreeMap<Integer,Integer>();

        String transcriptID = null;
        String chr = null;
        String strand = null;

		for (List<String> line : inputQuery){
			int currentRank = Integer.parseInt(line.get(rankField))-1; // Subtract 1 to convert to zero indexing

            // If new transcript ID or last line
			if (!line.get(transcriptIDfield).equals(transcriptID)) {
                if (transcriptID != null) {
                    done = printCDNA(getHeader(), chr, start, end, strand);
                    start.clear();
                    end.clear();
                    clearHeader();
                    if (done) {
                        break;
                    }
                }

                chr = line.get(chrField);
                transcriptID = line.get(transcriptIDfield);
                strand = line.get(strandField);
			}

			start.put(currentRank, Integer.parseInt(line.get(startField)));
			end.put(currentRank, Integer.parseInt(line.get(endField)));

            storeHeaderInfo(line);
		}

        // Print last line
        if (!done) {
            printCDNA(getHeader(), chr, start, end, strand);
        }
	}

	/**
	 * Retrieves and prints sequence for QueryType CDNA.
	 * @param transcriptID	Header for sequence.
	 * @param chr	Chromosome name.
	 * @param start	Sequence start position.
	 * @param end	Sequence end position.
	 * @param strand	Sequence strand.
	 * @throws IOException
	 */
	protected final boolean printCDNA(String header, String chr,
			Map<Integer,Integer> start, Map<Integer,Integer> end, String strand) throws IOException {
		StringBuilder sequence = new StringBuilder();
		if (!chr.equals("")) {
			if (strand.equals("-1")){
				sequence.append(reverseComplement(getSequence(chr,end.get(0)+1,end.get(0)+upstreamFlank)));
				for (int i = 0; i < start.size(); i++){
					sequence.append(reverseComplement(getSequence(chr, start.get(i), end.get(i))));
				}
				sequence.append(reverseComplement(getSequence(chr,start.get(start.size()-1)-downstreamFlank,start.get(start.size()-1)-1)));
			} else {
				sequence.append(getSequence(chr,start.get(0)-upstreamFlank,start.get(0)-1));
				for (int i = 0; i < start.size(); i++){
					sequence.append((getSequence(chr, start.get(i), end.get(i))));
				}
				sequence.append(getSequence(chr,end.get(end.size()-1)+1,end.get(end.size()-1)+downstreamFlank));
			}
		}
		return printFASTA(sequence.toString(), header);
	}
}
