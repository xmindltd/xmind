//Copyright 2001-2005 FreeHep
package org.xmind.org.freehep.graphics2d.font;

/**
 * Abstract Character Table, inherited by all the Generated Encoding Tables
 *
 * @author Simon Fischer
 * @author Jason Wong
 */
public abstract class AbstractCharTable implements CharTable {

    public int toEncoding(char unicode) {
        try {
            String name = toName(unicode);
            if (name == null)
                return 0;
            int enc = toEncoding(name);
            if (enc > 255) {
                System.out.println("toEncoding() returned illegal value for '" //$NON-NLS-1$
                        + name + "': " + enc); //$NON-NLS-1$
                return 0;
            }
            return enc;
        } catch (Exception e) {
            return 0;
        }
    }

    public String toName(char c) {
        return toName(new Character(c));
    }

    public String toName(Integer enc) {
        return toName(enc.intValue());
    }
}
