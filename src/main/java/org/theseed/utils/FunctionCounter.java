/**
 *
 */
package org.theseed.utils;

/**
 * This is a simple utility object that contains a function string, a count, and a list of feature IDs.
 * It is used to track results for ShowMapProcessor.
 *
 * @author Bruce Parrello
 */
public class FunctionCounter {

    // FIELDS
    /** CoreSEED functional assignment string */
    private String mappedFunction;
    /** occurrence count */
    private int count;

    /**
     * Construct a function counter.
     *
     * @param coreFunction		CoreSEED function ID
     * @param count				number of PATRIC occurrences
     */
    public FunctionCounter(String coreFunction, int count) {
        this.mappedFunction = coreFunction;
        this.count = count;
    }

    /**
     * @return the mapped PATRIC function
     */
    public String getMappedFunction() {
        return this.mappedFunction;
    }

    /**
     * @return the number of occurrences in PATRIC
     */
    public int getCount() {
        return this.count;
    }

}
