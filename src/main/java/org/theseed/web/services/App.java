package org.theseed.web.services;

import java.util.Arrays;

import org.theseed.web.WebProcessor;

/**
 * Commands for General website utilities.
 *
 * showMap		show the mapping between CoreSEED and PATRIC functions
 * subsystems	display the roles in subsystems.
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
        case "subsystems" :
            processor = new SubsystemRoleProcessor();
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
