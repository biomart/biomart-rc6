/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.biomart.processors;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.sql.Timestamp;
import java.util.HashSet;
import org.biomart.queryEngine.Query;
import org.mskcc.netbox.script.NetAnalyze;

/**
 *
 * @author Syed Haider
 */
public class NETBOX extends ProcessorImpl {

    @Override
    public void beforeQuery(Query query, OutputStream outputHandle) throws IOException {
        this.out = new ByteArrayOutputStream();
    }

    public void processGeneList(OutputStream queryResults) throws IOException{
        HashSet <String> geneSymbols = new HashSet <String>();
		BufferedReader in = new BufferedReader(new StringReader(queryResults.toString()));
        String geneSymbol = null;
		while ((geneSymbol = in.readLine()) != null) {
            //this.outputHandle.write(geneSymbol.getBytes());
            //this.outputHandle.write("\n".getBytes());
            geneSymbols.add(geneSymbol);
        }

        // lets call NETBOX
        //String netBoxHome = "/Users/sah84/Desktop/oicr/biomart/biomart-java/trunk/plugins/netbox/";
        String netBoxHome = System.getProperty("user.dir") + "/../plugins/netbox/";
        String sessionId = (new Timestamp((new java.util.Date()).getTime())).toString();

        NetAnalyze netBoxObj = new NetAnalyze(
            netBoxHome,
            sessionId,
            geneSymbols,
            netBoxHome + "logs/testconf" + sessionId,
            netBoxHome + "logs/testgenes" + sessionId,
            "Test Results" + sessionId,
            "2",
            "0.05",
            "0",
            "0"
            );

//        this.outputHandle.write(netBoxObj.getHTMLResults().getBytes());
    }
}
