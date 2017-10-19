// Copyright 2001, FreeHEP.
package org.xmind.org.freehep.util.io;

/**
 * Constants for the ASCII85 encoding.
 *
 * @author Mark Donszelmann
 * @version $Id: src/main/java/org/freehep/util/io/ASCII85.java 96b41b903496
 *          2005/11/21 19:50:18 duns $
 */
public interface ASCII85 {

    /**
     * Maxmimum line length for ASCII85
     */
    public final static int MAX_CHARS_PER_LINE = 80;

    /**
     * 85^1
     */
    public static long a85p1 = 85;

    /**
     * 85^2
     */
    public static long a85p2 = a85p1 * a85p1;

    /**
     * 85^3
     */
    public static long a85p3 = a85p2 * a85p1;

    /**
     * 85^4
     */
    public static long a85p4 = a85p3 * a85p1;

}
