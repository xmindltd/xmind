// Copyright 2000, CERN, Geneva, Switzerland and University of Santa Cruz, California, U.S.A.
package org.xmind.org.freehep.graphics2d;


/**
 *
 * @author Mark Donszelmann
 * @version $Id: TagString.java 8584 2006-08-10 23:06:37Z duns $
 */
public class TagString {

    private String string;

    public TagString(String value) {
        string = value;
    }

    public int hashCode() {
        return string.hashCode();
    }

    public boolean equals(Object obj) {
        return string.equals(obj);
    }

    public String toString() {
        return string;
    }
}
