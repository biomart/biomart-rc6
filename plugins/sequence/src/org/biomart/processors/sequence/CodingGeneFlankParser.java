package org.biomart.processors.sequence;


import org.biomart.common.exceptions.TechnicalException;
import org.biomart.common.exceptions.ValidationException;
import org.biomart.common.resources.Log;

/**
 *
 * @author jhsu, jguberman
 */
public class CodingGeneFlankParser extends SequenceParser {
    // "Flank-coding region (Gene)"
    // TODO Optimize by using the transcript_count info when initializing lists? (Except transcript_count seems to be empty)
    // TODO Clean up in general

    // Set up the fields
    private static final int geneIDfield = 0;
    private static final int transcriptIDfield = 1;
    private static final int chrField = 2;
    private static final int startField = 3;
    private static final int endField = 4;
    private static final int codingStartOffsetField = 5;
    private static final int codingEndOffsetField = 6;
    private static final int strandField = 7;
    private static final int exonIDField = 8;
    private static final int rankField = 9;
    private static final int startExonIDField = 10;
    private static final int endExonIDField = 11;
    private static final int transcriptCountField = 12;

    private int terminalExonField;
    private int codingOffsetField;

    private String transcriptID = null;
    private String terminalExonID = null;
    private String chr = "";
    private Integer start = null;
    private Integer end = null;
    private String strand = null;
    private int codingOffset = 0;
    private String exonID = null;

    private String currGeneID = null;

    public CodingGeneFlankParser() {
        super(13);
    }

    @Override
    public SequenceParser validate() throws ValidationException {
		checkFlank();

		if (upstreamFlank > 0) {
			terminalExonField = startExonIDField;
			codingOffsetField = codingStartOffsetField;
		} else {
			terminalExonField = endExonIDField;
			codingOffsetField = codingEndOffsetField;
		}

        return this;
    }

	/**
	 * Parses input rows for QueryType CODING_GENE_FLANK.
	 * @param inputQuery	List containing query rows. Each Entry is expected to be a list containing the fields ensembl_gene_id, ensembl_transcript_id, chromosome_name, exon_chrom_start, exon_chrom_end, coding_start_offset, coding_end_offset, strand, exon_id, rank, start_exon_id, end_exon_id, transcript_count.
	 * @param flank	Array with position 0 containing the upstream flank size and position 1 containing the downstream flank size.
	 * @throws TechnicalException
	 */
	@Override
	public String parseLine(String[] line) {
        String results = "";

        // Check if new gene ID is seen
        String geneID = line[geneIDfield];

        if (!geneID.equals(currGeneID)) {
            if (currGeneID != null) {
                results = getCodingGeneFlank(getHeader(), chr, start, end, strand);
                start = null;
                end = null;
            }

            clearHeader();

            currGeneID = geneID;
        }

        storeHeaderInfo(line);

        exonID = line[exonIDField];

        if (!line[transcriptIDfield].equals(transcriptID)){
            transcriptID = line[transcriptIDfield];
            terminalExonID = line[terminalExonField];
            strand = line[strandField];
        }

        if (terminalExonID.equals(exonID)){
            codingOffset = Integer.parseInt(line[codingOffsetField]);

            int startTmp, endTmp;
            if (strand.equals("-1")) {
                startTmp = Integer.parseInt(line[startField]);
                int startTmp2 = startTmp + codingOffset;
                start = start!=null ? Math.max(start, startTmp2) : startTmp2;
                endTmp = Integer.parseInt(line[endField]);
                int endTmp2 = endTmp - codingOffset;
                end = end!=null ? Math.min(end, endTmp2) : endTmp2;
            } else {
                startTmp = Integer.parseInt(line[startField]);
                int startTmp2 = startTmp + codingOffset;
                start = start!=null ? Math.min(start, startTmp2) : startTmp2;
                endTmp = Integer.parseInt(line[endField]);
                int endTmp2 = endTmp - codingOffset;
                end = end!=null ? Math.max(end, endTmp2) : endTmp2;
            }
            if (isDebug) System.out.println(startTmp + ", " + endTmp + ", " + (endTmp-startTmp) + ", " + exonID + ", " + codingOffset);

            chr = line[chrField];
        }

        return results;
	}

    @Override
    public String parseLast() {
        return getCodingGeneFlank(getHeader(), chr, start, end, strand);
    }

	protected final String getCodingGeneFlank(String header, String chr, int start,
            int end, String strand) {

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
                start = end - downstreamFlank + 1;
            } else {
                end = start + downstreamFlank - 1;
            }
        }

        if (isDebug) System.out.println(">>" + start + ", " + end + ", " + (end-start));

        // Take the reverse complement if necessary
        if (strand.equals("-1")){
            sequence = reverseComplement(getSequence(chr, start, end));
        } else {
            sequence = getSequence(chr, start, end);
        }
        return getFASTA(sequence, header);
	}
}

