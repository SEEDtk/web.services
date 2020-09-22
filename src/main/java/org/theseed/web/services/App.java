package org.theseed.web.services;

import java.util.Arrays;

import org.theseed.web.WebProcessor;

/**
 * Commands for General website utilities.
 *
 * parse	display a page of markdown text
 *
 */
public class App
{
    public static void main( String[] args )
    {
        // Get the control parameter.
        String command = args[0];
        String[] newArgs = Arrays.copyOfRange(args, 1, args.length);
        WebProcessor processor;
        // Determine the command to process.
        switch (command) {
        case "showMap" :
            processor = new ShowMapProcessor();
            break;
        default:
            throw new RuntimeException("Invalid command " + command);
        }
        // Process it.
        boolean ok = processor.parseCommand(newArgs);
        if (ok) {
            processor.run();
        }
    }
}
