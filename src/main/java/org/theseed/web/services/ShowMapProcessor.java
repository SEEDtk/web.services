/**
 *
 */
package org.theseed.web.services;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.theseed.counters.CountMap;
import org.theseed.io.TabbedLineReader;
import org.theseed.proteins.Function;
import org.theseed.proteins.FunctionMap;
import org.theseed.utils.FunctionCounter;
import org.theseed.web.ColSpec;
import org.theseed.web.CookieFile;
import org.theseed.web.HtmlTable;
import org.theseed.web.Key;
import org.theseed.web.Row;
import org.theseed.web.WebProcessor;

import j2html.tags.DomContent;
import static j2html.TagCreator.*;

/**
 * This command processes the latest protein mapping.  The protein mapping is in "protMapping.tbl" in the CoreSEED data directory.  The
 * "core_function" column contains a CoreSEED function, the "patric_function" column a corresponding PATRIC function, the "count" the
 * number of times the PATRIC function maps to that CoreSEED function, and "good" is TRUE if the mapping is good and FALSE otherwise.
 *
 * The output page will contain all of the mappings in which the CoreSEED and PATRIC functions are different, and will include the
 * count of PATRIC occurrences.
 *
 * The positional parameters are the name of the CoreSEED data directory and the name of the user's workspace.  There are no
 * additional options.
 *
 * @author Bruce Parrello
 *
 */
public class ShowMapProcessor extends WebProcessor {

    // FIELDS
    /** logging facility */
    protected static Logger log = LoggerFactory.getLogger(ShowMapProcessor.class);
    /** function map */
    private FunctionMap funMap;
    /** map of PATRIC function IDs to output information */
    private Map<String, FunctionCounter> patFunMap;
    /** CoreSEED occurrence counters */
    private CountMap<String> coreCounts;

    /**
     * This is a utility class for sorting PATRIC functions.  We sort first by CoreSEED function count, then the function itself, then the PATRIC function.
     */
    public class FunctionSorter implements Comparator<String> {

        @Override
        public int compare(String o1, String o2) {
            FunctionCounter ctr1 = patFunMap.get(o1);
            FunctionCounter ctr2 = patFunMap.get(o2);
            String core1 = ctr1.getMappedFunction();
            String core2 = ctr2.getMappedFunction();
            int retVal = coreCounts.getCount(core2) - coreCounts.getCount(core1);
            if (retVal == 0) {
                retVal = core1.compareTo(core2);
                if (retVal == 0)
                    retVal = o1.compareTo(o2);
            }
            return retVal;
        }

    }


    @Override
    protected void setWebDefaults() {
    }

    @Override
    protected boolean validateWebParms() throws IOException {
        // Create the maps.
        this.funMap = new FunctionMap();
        this.patFunMap = new HashMap<String, FunctionCounter>(5000);
        this.coreCounts = new CountMap<String>();
        return true;
    }

    @Override
    protected String getCookieName() {
        return "services.showMap";
    }

    @Override
    protected void runWebCommand(CookieFile cookies) throws Exception {
        // Loop through the input file.
        File inFile = new File(this.getCoreDir(), "protMapping.tbl");
        log.info("Processing mappings in {}.", inFile);
        try (TabbedLineReader inStream = new TabbedLineReader(inFile)) {
            int coreColIdx = inStream.findField("core_function");
            int patColIdx = inStream.findField("patric_function");
            int countColIdx = inStream.findField("count");
            int goodColIdx = inStream.findField("good");
            int lineCount = 0;
            for (TabbedLineReader.Line line : inStream) {
                if (line.getFlag(goodColIdx)) {
                    // Here we have a good mapping.  Verify that the mapping is a real change.
                    Function patFun = this.funMap.findOrInsert(line.get(patColIdx));
                    Function coreFun = this.funMap.findOrInsert(line.get(coreColIdx));
                    if (! patFun.equals(coreFun)) {
                        // Here we have a function-name transformation.
                        int mapCount = line.getInt(countColIdx);
                        FunctionCounter funCounter = new FunctionCounter(coreFun.getId(), mapCount);
                        this.patFunMap.put(patFun.getId(), funCounter);
                        this.coreCounts.count(coreFun.getId(), mapCount);
                    }
                }
                lineCount++;
            }
            log.info("{} mappings found in {} input lines, targeting {} coreSEED functions.", this.patFunMap.size(), lineCount, this.coreCounts.size());
        }
        // Now we need to sort the PATRIC functions.
        log.info("Sorting output.");
        Comparator<String> sorter = this.new FunctionSorter();
        List<String> patFunList = this.patFunMap.keySet().stream().sorted(sorter).collect(Collectors.toList());
        // Start the output table.
        log.info("Formatting output table.");
        HtmlTable<Key.Null> tableOut = new HtmlTable<Key.Null>(new ColSpec.Normal("core_function"), new ColSpec.Normal("patric_function"),
                new ColSpec.Num("patric_count"));
        // Loop through the rows, building the table.
        for (String patFunId : patFunList) {
            FunctionCounter funCounter = this.patFunMap.get(patFunId);
            // Get the function descriptions.
            String coreFunDesc = this.funMap.getName(funCounter.getMappedFunction());
            String patFunDesc = this.funMap.getName(patFunId);
            // Turn the core function into a search link.
            DomContent roleSearch = roleSearchLink(coreFunDesc);
            int count = funCounter.getCount();
            new Row<Key.Null>(tableOut, Key.NONE).add(roleSearch).add(patFunDesc).add(count);
        }
        DomContent tableDiv = this.getPageWriter().highlightBlock(tableOut.output());
        DomContent legend = p("Each PATRIC function is mapped to a single CoreSEED function.  The count indicates the number of times " +
                "the unmapped function occurred in the 8000 sample genomes from PATRIC.  Click on a link in the first column " +
                "to search for CoreSEED features containing the specified function.");
        this.getPageWriter().writePage("Function Mapping", h1("Function Mapping"), legend, tableDiv);
    }

}
