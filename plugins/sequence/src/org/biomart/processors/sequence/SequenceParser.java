package org.biomart.processors.sequence;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.biomart.common.exceptions.BioMartException;
import org.biomart.common.exceptions.TechnicalException;
import org.biomart.common.exceptions.ValidationException;
import org.biomart.common.resources.Log;
import org.biomart.common.utils2.MyUtils;

/**
 *
 * @author jhsu, jguberman
 */
public abstract class SequenceParser implements SequenceConstants {

    protected static boolean isDebug = Boolean.getBoolean("biomart.debug");

	private static Connection databaseConnection = null;
    private OutputStream out;
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

    protected SequenceParser(int headerStartCol) {
        this.headerStartCol = headerStartCol;
    }

	public abstract void parse(List<List<String>> inputQuery) throws IOException;

    public abstract SequenceParser validate() throws ValidationException;

    public void streamSequence(OutputStream in, OutputStream out) throws TechnicalException, IOException {
        streamSequence(in.toString(), out);
    }
    public void streamSequence(String results, OutputStream out) throws TechnicalException, IOException {
        this.out = out;

        try {
            connectDB();

            if ("".equals(results.trim())) {
                out.write(SEQUENCE_UNAVAILABLE_BYTES);

            } else {
                clearHeader(); // Initial header storage

                List<List<String>> inputQuery =  new ArrayList<List<String>>();

                BufferedReader in = new BufferedReader(new StringReader(results));
                String line;

                while ((line = in.readLine()) != null) {
                    if (!MyUtils.isEmpty(line)) {
                        inputQuery.add(MyUtils.splitLine("\t", line));
                    }
                }

                this.parse(inputQuery);
            }

        } finally{
            disconnectDB();
        }
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
			Statement stmt = null;
			stmt = databaseConnection.createStatement();

			ResultSet result = stmt.executeQuery(sqlQueryStart);
			result.next();
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
	protected final boolean printFASTA(String sequence, String header, int lineLength) {
		if (sequence == null || sequence.equals("") || sequence.equals("null")){
			sequence = SEQUENCE_UNAVAILABLE;
		}

        try {
            out.write((">" + header + "\n").getBytes());

            int sequenceLength = sequence.length();

            for(int i = 0; i < sequenceLength; i+=lineLength){
                out.write((sequence.substring(i,Math.min(i+lineLength,sequenceLength)) + "\n").getBytes());
            }

            return ++total >= limit;
        } catch (IOException e) {
            throw new BioMartException(e);
        }
	}
	protected final boolean printFASTA(String sequence, int lineLength) {
		return printFASTA(sequence, "", lineLength);
	}
	protected final boolean printFASTA(String sequence, String header) {
		return printFASTA(sequence, header, 60);
	}
	protected final boolean printFASTA(String sequence) {
		return printFASTA(sequence, "", 60);
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

    protected final void storeHeaderInfo(List<String> line) {
        for (int i=0; i<extraAttributes; i++) {
            String curr = line.get(headerStartCol+i);
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
	private void disconnectDB(){
		try {
			databaseConnection.close();
		} catch (Exception e) {
            Log.error(e);
		}
    }

	/**
	 * Connects to the database at the beginning of the query.
	 * Currently the database parameters are hard-coded.
	 */
	private void connectDB() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (Exception e) {
			Log.error("Failed to load JDBC/ODBC driver.");
		}

		try {
			databaseConnection = DriverManager.getConnection(URL, username, password);
		} catch (SQLException e) {
			Log.error("problems connecting to "+URL);
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
