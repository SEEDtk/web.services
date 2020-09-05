/**
 *
 */
package org.theseed.web.services;

import java.io.IOException;

import org.theseed.web.CookieFile;
import org.theseed.web.WebProcessor;

/**
 * Placeholder to prevent warnings.
 *
 * @author Bruce Parrello
 *
 */
public class TempProcessor extends WebProcessor {

    @Override
    protected void setWebDefaults() {
    }

    @Override
    protected boolean validateWebParms() throws IOException {
        return false;
    }

    @Override
    protected String getCookieName() {
        return null;
    }

    @Override
    protected void runWebCommand(CookieFile cookies) throws Exception {

    }
}
