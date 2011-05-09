package org.biomart.processors.sequence;

import com.google.common.base.Function;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.biomart.common.constants.OutputConstants;
import org.biomart.common.exceptions.BioMartException;
import org.biomart.common.exceptions.TechnicalException;
import org.biomart.common.exceptions.ValidationException;
import org.biomart.common.resources.Log;
import org.biomart.common.utils.CallbackOutputStream;

/**
 *
 * @author jhsu, jguberman
 */
public abstract class SequenceParser implements SequenceConstants {
    protected class SequenceCallback implements Function<String,String>, OutputConstants {
        @Override
        public String apply(String input) {
            if (total < limit) {
                String results = SequenceParser.this.parseLine(input.split(DELIMITER));
                // Increment total when printed results is non-empty
                if (!"".equals(results)) {
                    total ++;
                    return results;
                }
            }
            return "";
        }
    }

    protected static boolean isDebug = Boolean.getBoolean("biomart.debug");

	private static Connection databaseConnection = null;
    private List<List<String>> headerInfo;
    private String tableName;

    private int limit = Integer.MAX_VALUE;
    private int total = 0;

    // DB info
    private String URL;
    private String username;
    private String password;

    // Flank info
    protected int downstreamFlank = 0;
    protected int upstreamFlank = 0;

	// Size of each block of sequence in the database, currently {@value}
	protected static final int CHUNK_SIZE = 100000;

    protected int extraAttributes = 0;

    protected final int headerStartCol;

    protected OutputStream out;

    protected SequenceParser(int headerStartCol) {
        this.headerStartCol = headerStartCol;
    }

    public abstract String parseLine(String[] line);
    public abstract String parseLast();

    public abstract SequenceParser validate() throws ValidationException;

    public SequenceParser setOutputStream(OutputStream out) {
        this.out = out;
        return this;
    }

    public CallbackOutputStream getCallbackOutputStream() throws TechnicalException, IOException {
        return new CallbackOutputStream(out, new SequenceCallback());
    }

    	/** Given a chromosome name, start coordinate, and end coordinate,
	 * returns the corresponding sequence.
	 *
	 * @param seqChrName	Chromosome name.
	 * @param seqStart	Sequence start position. If < 0, is changed to 0.
	 * @param seqEnd	Sequence end position.
	 * @return	Sequence from given region.
	 * @throws TechnicalException
	 */
	public String getSequence(String seqChrName, int seqStart, int seqEnd) {
		if (seqStart < 1){
			seqStart = 1;
			System.err.println("Sequence start cannot be less than 1, changing to 1");
		}
		if (seqEnd < 0 || "".equals(seqChrName) || seqEnd-seqStart+1 < 1){
			return "";
		}
		/* Construct SQL queries to retrieve the sequence for the region of interest
		 *
		 * sqlQueryStart: retrieve the sequence from the beginning of the region
		 * to the end of the chunk or the end of the region, whichever comes first.
		 * sqlQuery: retrieve the full sequences for any full chunks contained
		 * within the sequence. Query will return empty if the region doesn't span
		 * more than 2 chunks.
		 * sqlQueryEnd: retrieve the sequence from the beginning of the last
		 * chunk to the end of the region.
		 *
		 * sqlQuery and sqlQueryEnd will only be run if the region spans more than
		 * one chunk.
		 */

		String sqlQueryStart = String.format("SELECT substring(sequence,%d,%d) FROM %s WHERE chr_name = \"%s\" AND chr_start <= %d AND chr_start+%d >= %d;", ((seqStart-1)%CHUNK_SIZE)+1, Math.min((seqEnd-seqStart/CHUNK_SIZE*CHUNK_SIZE)+1, CHUNK_SIZE+1)-seqStart%CHUNK_SIZE, tableName, seqChrName, seqStart, CHUNK_SIZE-1, seqStart);
		String sqlQuery = String.format("SELECT sequence FROM %s WHERE chr_name = \"%s\" AND chr_start <= %d AND chr_start+%d >= %d ORDER BY chr_start;", tableName, seqChrName, seqEnd/CHUNK_SIZE*CHUNK_SIZE, CHUNK_SIZE-1, 1+(1+seqStart/CHUNK_SIZE)*CHUNK_SIZE);
		String sqlQueryEnd = String.format("SELECT substring(sequence,%d,%d) FROM %s WHERE chr_name = \"%s\" AND chr_start <= %d AND chr_start+%d >= %d;", 1, seqEnd%CHUNK_SIZE, tableName, seqChrName, seqEnd, CHUNK_SIZE-1, seqEnd);

		StringBuilder retrievedSequence = new StringBuilder(seqEnd-seqStart+1);

		try {
			databaseConnection = DriverManager.getConnection(URL, username, password);

			Statement stmt = null;
			stmt = databaseConnection.createStatement();

			ResultSet result = stmt.executeQuery(sqlQueryStart);
			boolean hasRows = result.next();

            if (hasRows) {
                retrievedSequence.append(result.getString(1));

                // If the region spans more than one chunk, execute sqlQuery and sqlQueryEnd
                if ((seqStart-1)/CHUNK_SIZE != (seqEnd-1)/CHUNK_SIZE){
                    result = stmt.executeQuery(sqlQuery);

                    // Stitch together all retrieved sequences
                    while (result.next()){
                        retrievedSequence.append(result.getString(1));
                    }

                    result = stmt.executeQuery(sqlQueryEnd);

                    result.next();
                    retrievedSequence.append(result.getString(1));
                }
            }

            databaseConnection.close();
		} catch (SQLException e){
            Log.error(e);
            throw new BioMartException("Error retrieving sequence", e);
		}

		return retrievedSequence.toString();
	}

	/**
	 * Given a sequence, prints FASTA formatted output
	 *
	 * @param sequence The sequence to be printed.
	 * @param header The header to be printed above the sequence (optional).
	 * @param lineLength The length of each sequence line (optional; default 60).
	 * @param isProtein If true, translate the DNA sequence to protein sequence (optional, default false).
	 */
	protected String getFASTA(String sequence, String header, int lineLength) {
        StringBuilder sb = new StringBuilder();

		if (sequence == null || sequence.equals("") || sequence.equals("null")){
			sequence = SEQUENCE_UNAVAILABLE;
		}

        sb.append(">").append(header).append("\n");

        int sequenceLength = sequence.length();

        for(int i = 0; i < sequenceLength; i+=lineLength){
            sb.append(sequence.substring(i,Math.min(i+lineLength,sequenceLength))).append("\n");
        }
        return sb.toString();
	}
	protected String getFASTA(String sequence, int lineLength) {
		return getFASTA(sequence, "", lineLength);
	}
	protected String getFASTA(String sequence, String header) {
		return getFASTA(sequence, header, 60);
	}
	protected String getFASTA(String sequence) {
		return getFASTA(sequence, "", 60);
	}

	/**
	 * Returns the reverse complement of a DNA or RNA sequence.
	 * @param sequence The input sequence as a String.
	 * @return	The reverse complement sequence as a String.
	 */

	protected final String reverseComplement(String sequence){
		// I'm sure there are better and faster ways to do this, but this will work for now
		if(sequence == null){
			return null;
		}
		StringBuilder reversed = new StringBuilder(sequence.length());
		for (int i = sequence.length()-1;i >= 0;--i){
			switch (sequence.charAt(i)) {
			case 'A': reversed.append('T'); break;
			case 'T': reversed.append('A'); break;
			case 'C': reversed.append('G'); break;
			case 'G': reversed.append('C'); break;
			case 'U': reversed.append('A'); break;
			case 'R': reversed.append('Y'); break;
			case 'Y': reversed.append('R'); break;
			case 'K': reversed.append('M'); break;
			case 'M': reversed.append('K'); break;
			case 'S': reversed.append('S'); break;
			case 'W': reversed.append('W'); break;
			case 'B': reversed.append('V'); break;
			case 'V': reversed.append('B'); break;
			case 'D': reversed.append('H'); break;
			case 'H': reversed.append('D'); break;
			/*			case 'N': reversed.append('N'); break;
			case 'X': reversed.append('X'); break;
			case '-': reversed.append('-'); break;*/
			default: reversed.append(sequence.charAt(i)); break;
			}
		}
		return reversed.toString();
	}

	/**
	 * Checks that one and only one entry of flank is greater than zero.
	 * @param flank The array containing the flank parameters
	 */
	protected final void checkFlank() throws ValidationException {
        if (upstreamFlank == 0 && downstreamFlank == 0){
            throw new ValidationException("Validation Error: Requests for flank sequence must be accompanied by an upstream_flank or downstream_flank request");
        } else if (upstreamFlank > 0 && downstreamFlank> 0){
            throw new ValidationException("Validation Error: For this sequence option choose upstream OR downstream gene flanking sequence, NOT both, as makes no sense to simply concatenate them together.");
        } else if (upstreamFlank < 0 || downstreamFlank < 0){
            throw new ValidationException("Validation Error: Flank distance can not be negative.");
        }
	}

    protected final String getHeader() {
        String[] arr = new String[headerInfo.size()];
        int i = 0;
        for (List<String> values : headerInfo) {
            arr[i++] = StringUtils.join(values, HEADER_VALUE_DELIMITER);
        }
        return StringUtils.join(arr, HEADER_COLUMN_DELIMITER);
    }

    protected final void storeHeaderInfo(String[] line) {
        for (int i=0; i<extraAttributes; i++) {
            String curr = line[headerStartCol+i];
            List<String> prev = headerInfo.get(i);
            if (!prev.contains(curr)) {
                prev.add(curr);
            }
        }
    }

    protected final void clearHeader() {
        headerInfo = new ArrayList<List<String>>();
        for (int i=0; i<extraAttributes; i++) {
            headerInfo.add(new ArrayList<String>());
        }
    }


	/**
	 * Disconnects from the database at the end of execution.
	 */
	protected void shutDown() {
        // Print the last sequence if limit not reached
        if (total < limit) {
            String lastSequence = parseLast();
            if (!"".equals(lastSequence)) {
                try {
                    out.write(lastSequence.getBytes());
                } catch (IOException e) {
                    // nothing
                }
            }
        }
    }

	/**
	 * Connects to the database at the beginning of the query.
	 * Currently the database parameters are hard-coded.
	 */
	protected void startUp() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (Exception e) {
			Log.error("Failed to load JDBC/ODBC driver.");
		}
	}

    public final SequenceParser setDatabaseConnection(String URL, String tableName, String username, String password) {
        this.URL = URL;
        this.tableName = tableName;
        this.username = username;
        this.password = password;
        return this;
    }

    public final SequenceParser setExtraAttributes(int num) {
        extraAttributes = num;
        return this;
    }

    public final SequenceParser setDownstreamFlank(int i) {
        downstreamFlank = i;
        return this;
    }

    public final SequenceParser setUpstreamFlank(int i) {
        upstreamFlank = i;
        return this;
    }

    public final SequenceParser setLimit(int i) {
        limit = i;
        return this;
    }
}
