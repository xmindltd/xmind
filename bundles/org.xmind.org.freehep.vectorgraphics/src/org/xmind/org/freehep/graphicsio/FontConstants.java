// Copyright 2003, FreeHEP.
package org.xmind.org.freehep.graphicsio;

/**
 *
 * @author Mark Donszelmann
 * @author Jason Wong
 */
public class FontConstants {

    private FontConstants() {
    }

    // Font Embedding
    public static final String EMBED_FONTS = "EmbedFonts"; //$NON-NLS-1$

    public static final String EMBED_FONTS_AS = "EmbedFontsAs"; //$NON-NLS-1$

    public static final String EMBED_FONTS_TYPE1 = "Type1"; //$NON-NLS-1$

    public static final String EMBED_FONTS_TYPE3 = "Type3"; //$NON-NLS-1$

    public static final String TEXT_AS_SHAPES = "TEXT_AS_SHAPES"; //$NON-NLS-1$

    public static final String[] getEmbedFontsAsList() {
        return new String[] { EMBED_FONTS_TYPE1, EMBED_FONTS_TYPE3 };
    }
}
