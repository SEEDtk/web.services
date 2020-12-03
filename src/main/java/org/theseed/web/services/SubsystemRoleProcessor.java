/**
 *
 */
package org.theseed.web.services;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.theseed.proteins.Function;
import org.theseed.proteins.FunctionMap;
import org.theseed.reports.CoreHtmlUtilities;
import org.theseed.subsystems.CellData;
import org.theseed.subsystems.RowData;
import org.theseed.subsystems.SubsystemData;
import org.theseed.subsystems.SubsystemFilter;
import org.theseed.web.ColSpec;
import org.theseed.web.CookieFile;
import org.theseed.web.HtmlTable;
import org.theseed.web.Key;
import org.theseed.web.Row;
import org.theseed.web.WebProcessor;

import j2html.tags.DomContent;
import static j2html.TagCreator.*;

/**
 * This command generates a list of the roles that occur in subsystems, along with a count of the number of subsystems containing
 * each role in an active variant.
 *
 * The positional parameters are the name of the CoreSEED data directory and the name of the user's workspace.  There are no
 * command-line options.
 *
 * @author Bruce Parrello
 *
 */
public class SubsystemRoleProcessor extends WebProcessor {

    // FIELDS
    /** logging facility */
    protected static Logger log = LoggerFactory.getLogger(SubsystemRoleProcessor.class);
    /** function map */
    private FunctionMap funMap;
    /** function-subsystem map */
    private Map<String, Set<String>> funSubs;

    @Override
    protected void setWebDefaults() {
    }

    @Override
    protected boolean validateWebParms() throws IOException {
        this.funMap = new FunctionMap();
        this.funSubs = new HashMap<String, Set<String>>(12000);
        return true;
    }

    @Override
    protected String getCookieName() {
        return "services.subsystems";
    }

    @Override
    protected void runWebCommand(CookieFile cookies) throws Exception {
        // Get the subsystem directory.
        File subsysDir = new File(this.getCoreDir(), "Subsystems");
        // Get the subsystem IDs.
        File[] subDirs = subsysDir.listFiles(new SubsystemFilter());
        log.info("{} subsystem directories found.", subDirs.length);
        for (File subDir : subDirs) {
            // Only process public ones.
            String ssId = subDir.getName();
            if (! SubsystemData.isPrivate(this.getCoreDir(), ssId)) {
                // Load this subsystem.
                SubsystemData subsystem = SubsystemData.load(this.getCoreDir(), ssId);
                // This will be the set of roles found.
                Set<String> found = new HashSet<String>(subsystem.getWidth());
                // Now we loop through the rows.  For each active variant, we increment the count for the roles in that row.
                for (RowData row : subsystem.getRows()) {
                    // Only process active rows.
                    if (row.isActive()) {
                        for (int i = 0; i < subsystem.getWidth(); i++) {
                            CellData cell = row.getCell(i);
                            // Only process cells with features.
                            if (! cell.isEmpty()) {
                                String role = subsystem.getRole(i);
                                Function fun = this.funMap.findOrInsert(role);
                                found.add(fun.getId());
                            }
                        }
                    }
                }
                // Now count the roles found.
                for (String roleId : found) {
                    Set<String> roleSubs = this.funSubs.computeIfAbsent(roleId, x -> new HashSet<String>(10));
                    roleSubs.add(subsystem.getName());
                }
                log.info("{} roles active in {}.", found.size(), subsystem.getName());
            }
        }
        log.info("{} distinct roles found in active variants.", this.funMap.size());
        // Now we produce the output table.
        HtmlTable<Key.Text> roleTable = new HtmlTable<Key.Text>(new ColSpec.Normal("Role"), new ColSpec.Num("subsystems"));
        for (Map.Entry<String, Set<String>> roleSub : this.funSubs.entrySet()) {
            String roleDesc = this.funMap.getName(roleSub.getKey());
            DomContent roleLink = this.roleSearchLink(roleDesc);
            DomContent subList = CoreHtmlUtilities.subsystemList(roleSub.getValue());
            new Row<Key.Text>(roleTable, new Key.Text(roleDesc)).add(roleLink).add(subList);
        }
        // Format the web page.
        DomContent tableDiv = this.getPageWriter().highlightBlock(roleTable.output());
        DomContent notes = p("Click on a role link to see all features containing the specified role. " +
                "The count shows the number of subsystems in which the role is active.");
        this.getPageWriter().writePage("Roles in Subsystems", h1("Roles in Subsystems"), notes, tableDiv);
    }

}
