/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
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
package org.xmind.gef.draw2d;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.draw2d.AbstractBorder;
import org.eclipse.draw2d.Border;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;

/**
 * @author Frank Shaka
 */
public class MultiBorder extends AbstractBorder {
    
    private List<Border> borders;
    
    public MultiBorder() {
        this.borders = Collections.emptyList();
    }
    
    public MultiBorder( Border singleBorder ) {
        this.borders = Collections.singletonList( singleBorder );
    }

    public MultiBorder( List<Border> borders ) {
        this.borders = new ArrayList<Border>( borders );
    }
    
    public MultiBorder( Border... borders ) {
        this.borders = Arrays.asList( borders );
    }
    
    public List<Border> getBorders() {
        return new ArrayList<Border>( borders );
    }
    
    /**
     * @see org.eclipse.draw2d.Border#getInsets(org.eclipse.draw2d.IFigure)
     */
    public Insets getInsets( IFigure figure ) {
        Insets ins = null;
        for ( Border border : borders ) {
            Insets i = ( border == null ) ? IFigure.NO_INSETS : border.getInsets( figure );
            ins = ( ins == null ) ? new Insets( i ) : ins.getAdded( ins );
        }
        return ( ins == null ) ? new Insets() : ins;
    }
    
    private static final Dimension EMPTY = new Dimension();
    
    public Dimension getPreferredSize( IFigure f ) {
        Dimension prefSize = null;
        for ( Border border : borders ) {
            if ( prefSize != null ) {
                Insets ins = getInsets( f );
                prefSize.expand( ins.getWidth(), ins.getHeight() );
            }
            Dimension d = ( border == null ) ? EMPTY : border.getPreferredSize( f );
            prefSize = ( prefSize == null ) ? new Dimension( d ) : prefSize.expand( d );
        }
        return ( prefSize == null ) ? new Dimension() : prefSize;
    }
    
    public boolean isOpaque() {
        boolean opaque = false;
        for ( Border border : borders ) {
            opaque &= ( border == null ) ? false : border.isOpaque();
        }
        return opaque;
    }

    /**
     * @see org.eclipse.draw2d.Border#paint(org.eclipse.draw2d.IFigure, org.eclipse.draw2d.Graphics, org.eclipse.draw2d.geometry.Insets)
     */
    public void paint( IFigure figure, Graphics graphics, Insets insets ) {
        for ( Border border : borders ) {
            if ( border != null ) {
                graphics.pushState();
                border.paint( figure, graphics, insets );
                graphics.popState();
                insets = insets.getAdded( border.getInsets( figure ) );
            }
        }
    }

}