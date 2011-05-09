package org.biomart.processors.sequence;

import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.biomart.common.exceptions.BioMartException;
import org.biomart.common.exceptions.ValidationException;
import org.biomart.common.resources.Log;
import org.biomart.processors.ProcessorImpl;
import org.biomart.processors.annotations.Required;
import org.biomart.processors.annotations.UserDefined;
import org.biomart.processors.fields.IntegerField;
import org.biomart.processors.fields.StringField;

import org.biomart.queryEngine.Query;
import org.jdom.Document;
import org.jdom.Element;

/**
 *
 * @author jhsu, jguberman
 *
 * TODO: Still need to implement logic to figure out attribute list from queryType 
 * (probably from containers?)
 *
 */
public class Sequence extends ProcessorImpl implements SequenceConstants {
    // Parser lookup map
    private static final Map<String,Class<? extends SequenceParser>> lookup;

    static {
        lookup  = new ImmutableMap.Builder<String,Class<? extends SequenceParser>>()
            .put(QueryType.CDNA.getCode(), CDNAParser.class)
            .put(QueryType.CODING.getCode(), CodingParser.class)
            .put(QueryType.CODING_GENE_FLANK.getCode(), CodingGeneFlankParser.class)
            .put(QueryType.CODING_TRANSCRIPT_FLANK.getCode(), CodingTranscriptFlankParser.class)
            .put(QueryType.FIVE_UTR.getCode(), FiveUTRParser.class)
            .put(QueryType.GENE_EXON.getCode(), ExonParser.class)
            .put(QueryType.GENE_EXON_INTRON.getCode(), GeneExonIntronParser.class)
            .put(QueryType.GENE_FLANK.getCode(), GeneFlankParser.class)
            .put(QueryType.PEPTIDE.getCode(), PeptideParser.class)
            .put(QueryType.THREE_UTR.getCode(), ThreeUTRParser.class)
            .put(QueryType.TRANSCRIPT_EXON_INTRON.getCode(), TranscriptExonIntronParser.class)
            .put(QueryType.TRANSCRIPT_FLANK.getCode(), TranscriptFlankParser.class)
            .build();
    }

    public Sequence() {
    }

    // Parameters and config
    private StringField jdbcConnectionURL = new StringField("Database connetion URL");

    private StringField username = new StringField("Database user");

    private StringField password = new StringField("Database Password");

    @Required
    @UserDefined
    public StringField type = new StringField("Query Type",
            new LinkedHashMap<String,String>(){{
                put("transcript_exon_intron", "Unspliced (Transcript)");
                put("gene_exon_intron", "Unspliced (Gene)");
                put("transcript_flank", "Flank (Transcript)");
                put("gene_flank", "Flank (Gene)");
                put("coding_transcript_flank", "Flank-coding region (Transcript)");
                put("coding_gene_flank", "Flank-coding region (Gene)");
                put("5utr", "5 UTR");
                put("3utr", "3 UTR");
                put("gene_exon", "Exon Sequences");
                put("cdna", "cDNA Sequences");
                put("coding", "Coding Sequences");
                put("peptide", "Protein");
            }});

    @UserDefined
    public IntegerField upstreamFlank = new IntegerField("Upstream Flank");

    @UserDefined
    public IntegerField downstreamFlank = new IntegerField("Downstream Flank");

    private String tableName;
    private int extraAttributes = 0;
    private int limit = Integer.MAX_VALUE;
    private SequenceParser parser;
    private OutputStream originalOut;

    /*
     * Add additional attribute list for sequence retrieval
     */
    @Override
    public void preprocess(final Document queryXML) {
        List<Element> datasets = queryXML.getRootElement().getChildren("Dataset");
        int userLimit;

        // Force headers to not return
        queryXML.getRootElement().setAttribute("header", "0");

        // set to not limit, but remember orignal limit for later
        userLimit = Integer.parseInt(queryXML.getRootElement().getAttributeValue("limit"));
        queryXML.getRootElement().setAttribute("limit", "-1");
        if (userLimit > 0) {
            limit = userLimit;
        }

        if (datasets.size() > 1) {
            throw new ValidationException("Sequence processor cannot take more than one dataset");
        }

        List<Element> header = new ArrayList<Element>();

        for (Element element : (List<Element>)datasets.get(0).getChildren("Attribute")) {
            header.add((Element)element.clone());
            extraAttributes++;
        }

        String[] datasetNames = datasets.get(0).getAttributeValue("name").split(",");

        if (datasetNames.length > 1) {
            throw new ValidationException("Sequence processor cannot take more than one dataset");
        }

        String speciesName = datasetNames[0].split("_")[0];

        tableName = speciesName + "_genomic_sequence__dna_chunks__main";

        // Remove header temporarily so attribute list comes first
        datasets.get(0).removeChildren("Attribute");

        Element attribute = new Element("Attribute");
        attribute.setAttribute("name", type.value);
        datasets.get(0).addContent(attribute);

        // Add back header
        for (Element element : header) {
            datasets.get(0).addContent(element);
        }

        Log.debug("Adding attribute " + type.value);
    }

    @Override
	public void beforeQuery(Query query, OutputStream out) throws IOException {
        originalOut = out;

        checkParameters();

        QueryType queryType = QueryType.get(type.value);

        try {
            if (lookup.containsKey(queryType.getCode())) {
                Class<? extends SequenceParser> clazz = lookup.get(queryType.getCode());
                parser = clazz.newInstance();

                if (downstreamFlank.value != null) {
                    parser.setDownstreamFlank(downstreamFlank.value);
                }

                if (upstreamFlank.value != null) {
                    parser.setUpstreamFlank(upstreamFlank.value);
                }

                parser
                    .setOutputStream(out)
                    .setDatabaseConnection(jdbcConnectionURL.value, tableName, username.value, password.value)
                    .setExtraAttributes(extraAttributes)
                    .setLimit(limit)
                    .validate()
                    .startUp();

                this.out = parser.getCallbackOutputStream();

            } else {
                // Handle error
            }
        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new BioMartException(e);
        }
    }

    @Override
	public void afterQuery() throws IOException {
        parser.shutDown();
    }

    private void checkParameters() {
        if (this.jdbcConnectionURL.value == null) {
            this.jdbcConnectionURL.value = System.getProperty("processors.sequence.connection");
        }
        if (this.username.value == null) {
            this.username.value = System.getProperty("processors.sequence.username");
        }
        if (this.password.value == null) {
            this.password.value = System.getProperty("processors.sequence.password");
        }
    }
}

