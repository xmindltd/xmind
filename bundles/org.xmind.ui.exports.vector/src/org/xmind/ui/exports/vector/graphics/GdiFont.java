/* ******************************************************************************
 * Copyright (c) 2006-2013 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See https://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package org.xmind.ui.exports.vector.graphics;

import java.awt.Font;

/**
 * @author Jason Wong
 * 
 */
public class GdiFont {
    private int m_height = 12;
    private boolean m_bItalic = false;
    private boolean m_bUnderlined = false;
    private boolean m_bBold = false;
    private boolean m_bStrikeout = false;
    private String m_faceName = "SanSerif"; //$NON-NLS-1$

    private int m_escapement = 0;

    /**
     * Creates a default font.
     */
    public GdiFont() {
        // Nothing to initialize.
    }

    /**
     * Copy constructor
     * 
     * @param font
     */
    GdiFont(GdiFont font) {
        m_height = font.m_height;
        m_bItalic = font.m_bItalic;
        m_bBold = font.m_bBold;
        m_faceName = font.m_faceName;
        m_bUnderlined = font.m_bUnderlined;
        m_escapement = font.m_escapement;
        m_bStrikeout = font.m_bStrikeout;
    }

    /**
     * Creates a font with the specified parameters.
     * 
     * @param height
     *            Height of the font
     * @param bItalic
     *            Whether or not the font is italic
     * @param bUnderlined
     *            Whether or not the font is underlined
     * @param bStrikeout
     *            Whether or not the font is a strikout font
     * @param bBold
     *            Whether or not the font is bold
     * @param faceName
     *            Name of the font
     * @param escapement
     *            Font escapement (angle) of the text to be drawn.
     */
    public GdiFont(int height, boolean bItalic, boolean bUnderlined,
            boolean bStrikeout, boolean bBold, String faceName, int escapement) {
        m_height = height;
        m_bItalic = bItalic;
        m_bBold = bBold;
        m_faceName = faceName;
        m_bUnderlined = bUnderlined;
        m_escapement = escapement;
        m_bStrikeout = bStrikeout;
    }

    /**
     * @return The Java font object that represents most of the attributes for
     *         drawing the font.
     */
    public Font getFont() {
        int style = Font.PLAIN;

        if (m_bItalic) {
            style = Font.ITALIC;
        }

        if (m_bBold) {
            style = Font.BOLD;
        }

        if (m_bItalic && m_bBold) {
            style = Font.ITALIC + Font.BOLD;
        }

        return new Font(m_faceName, style, m_height);
    }

    /**
     * @return The angle at which to draw the font.
     */
    public int getEscapement() {
        return m_escapement;
    }
}
