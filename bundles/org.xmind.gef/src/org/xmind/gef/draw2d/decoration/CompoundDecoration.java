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
package org.xmind.gef.draw2d.decoration;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;

/**
 * @author Frank Shaka
 */
public class CompoundDecoration extends AbstractDecoration implements
        ICompoundDecoration {

    private List<IDecoration> decorations = new ArrayList<IDecoration>();

    public CompoundDecoration() {
    }

    public CompoundDecoration(String id) {
        super(id);
    }

    protected List<IDecoration> getDecorations() {
        return decorations;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.gef.draw2d.decoration.ICompoundDecoration#getDecoration(int)
     */
    public IDecoration getDecoration(int index) {
        if (index < 0 || index >= decorations.size())
            return null;
        return decorations.get(index);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.gef.draw2d.decoration.ICompoundDecoration#setDecoration(org
     * .eclipse.draw2d.IFigure, int,
     * org.xmind.gef.draw2d.decoration.IDecoration)
     */
    public IDecoration setDecoration(IFigure figure, int index,
            IDecoration decoration) {
        if (index < 0 || index >= decorations.size())
            return null;
        IDecoration set = decorations.set(index, decoration);
        if (decoration != null)
            update(figure, decoration);
        return set;
    }

    protected void update(IFigure figure, IDecoration decoration) {
        decoration.setAlpha(figure, getAlpha());
        decoration.setVisible(figure, isVisible());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.gef.draw2d.decoration.ICompoundDecoration#add(org.eclipse.draw2d
     * .IFigure, org.xmind.gef.draw2d.decoration.IDecoration)
     */
    public void add(IFigure figure, IDecoration decoration) {
        add(figure, -1, decoration);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.gef.draw2d.decoration.ICompoundDecoration#add(org.eclipse.draw2d
     * .IFigure, int, org.xmind.gef.draw2d.decoration.IDecoration)
     */
    public void add(IFigure figure, int index, IDecoration decoration) {
        if (index < 0 || index > decorations.size()) {
            decorations.add(decoration);
        } else {
            decorations.add(index, decoration);
        }
        if (decoration != null)
            update(figure, decoration);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.gef.draw2d.decoration.ICompoundDecoration#size()
     */
    public int size() {
        return decorations.size();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.gef.draw2d.decoration.ICompoundDecoration#isEmpty()
     */
    public boolean isEmpty() {
        return decorations.isEmpty();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.gef.draw2d.decoration.ICompoundDecoration#contains(org.xmind
     * .gef.draw2d.decoration.IDecoration)
     */
    public boolean contains(IDecoration decoration) {
        return decorations.contains(decoration);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.gef.draw2d.decoration.ICompoundDecoration#indexOf(org.xmind
     * .gef.draw2d.decoration.IDecoration)
     */
    public int indexOf(IDecoration decoration) {
        return decorations.indexOf(decoration);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.gef.draw2d.decoration.ICompoundDecoration#move(org.eclipse.
     * draw2d.IFigure, int, int)
     */
    public IDecoration move(IFigure figure, int oldIndex, int newIndex) {
        IDecoration decoration = decorations.remove(oldIndex);
        decorations.add(newIndex, decoration);
        if (decoration != null)
            update(figure, decoration);
        return decoration;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.gef.draw2d.decoration.ICompoundDecoration#remove(int)
     */
    public IDecoration remove(IFigure figure, int index) {
        if (index < 0 || index >= decorations.size())
            return null;
        return decorations.remove(index);
    }

    public void invalidate() {
        super.invalidate();
        for (IDecoration decoration : decorations) {
            if (decoration != null)
                decoration.invalidate();
        }
    }

    public void validate(IFigure figure) {
        super.validate(figure);
        for (IDecoration decoration : decorations) {
            if (decoration != null)
                decoration.validate(figure);
        }
    }

    public void setAlpha(IFigure figure, int alpha) {
        super.setAlpha(figure, alpha);
        for (IDecoration decoration : decorations) {
            if (decoration != null)
                decoration.setAlpha(figure, alpha);
        }
    }

    public boolean isVisible() {
        boolean visible = false;
        for (IDecoration decoration : decorations) {
            if (decoration != null)
                visible |= decoration.isVisible();
        }
        return visible & super.isVisible();
    }

    public void setVisible(IFigure figure, boolean visible) {
        super.setVisible(figure, visible);
        for (IDecoration decoration : decorations) {
            if (decoration != null)
                decoration.setVisible(figure, visible);
        }
    }

    protected void performPaint(IFigure figure, Graphics g) {
        for (IDecoration decoration : decorations) {
            if (decoration != null)
                decoration.paint(figure, g);
        }
    }

}