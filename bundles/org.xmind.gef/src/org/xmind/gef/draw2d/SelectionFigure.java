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

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;

public class SelectionFigure extends PathFigure implements IUseTransparency {

    private static final int FLAG_SELECTED = MAX_FLAG << 1;
    private static final int FLAG_PRESELECTED = MAX_FLAG << 2;
    private static final int FLAG_FOCUSED = MAX_FLAG << 3;

    static {
        MAX_FLAG = FLAG_FOCUSED;
    }

    private Color focusColor = ColorConstants.darkBlue;

    private Color selectionColor = ColorConstants.blue;

    private Color preselectionColor = ColorConstants.lightGray;

    private Color focusFillColor = null;

    private Color selectionFillColor = null;

    private Color preselectionFillColor = null;

    private int focusAlpha = 0xe0;

    private int selectionAlpha = 0xc0;

    private int preselectionAlpha = 0x80;

    private int focusFillAlpha = 0;

    private int selectionFillAlpha = 0;

    private int preselectionFillAlpha = 0x38;

    private int outlineAlpha = 0xff;

    private int fillAlpha = 0;

    public SelectionFigure() {
        setLineStyle(SWT.LINE_SOLID);
        update();
    }

    public boolean isSelected() {
        return getFlag(FLAG_SELECTED);
    }

    public boolean isPreselected() {
        return getFlag(FLAG_PRESELECTED);
    }

    public boolean isFocused() {
        return getFlag(FLAG_FOCUSED);
    }

    public void setSelected(boolean selected) {
        boolean currentSelection = isSelected();
        if (selected == currentSelection)
            return;
        setFlag(FLAG_SELECTED, selected);
        update();
    }

    public void setPreselected(boolean preselected) {
        boolean currentPreselection = isPreselected();
        if (preselected == currentPreselection)
            return;
        setFlag(FLAG_PRESELECTED, preselected);
        update();
    }

    public void setFocused(boolean focused) {
        boolean currentFocused = isFocused();
        if (focused == currentFocused)
            return;
        setFlag(FLAG_FOCUSED, focused);
        update();
    }

    private void update() {
        updateColors(getOutlineColor(), getFillColor());
        setMainAlpha(getFillAlpha());
        setSubAlpha(getOutlineAlpha());
    }

    private void updateColors(Color outlineColor, Color fillColor) {
        setOutline(outlineColor != null);
        setForegroundColor(outlineColor);
        setFill(fillColor != null);
        setBackgroundColor(fillColor);
    }

    private Color getOutlineColor() {
        if (isFocused())
            return getFocusColor();
        if (isSelected())
            return getSelectionColor();
        if (isPreselected())
            return getPreselectionColor();
        return null;
    }

    private Color getFillColor() {
        if (isFocused())
            return getFocusFillColor();
        if (isSelected())
            return getSelectionFillColor();
        if (isPreselected())
            return getPreselectionFillColor();
        return null;
    }

    private int getOutlineAlpha() {
        if (isFocused())
            return getFocusAlpha();
        if (isSelected())
            return getSelectionAlpha();
        if (isPreselected())
            return getPreselectionAlpha();
        return 0xff;
    }

    private int getFillAlpha() {
        if (isFocused())
            return getFocusFillAlpha();
        if (isSelected())
            return getSelectionFillAlpha();
        if (isPreselected())
            return getPreselectionFillAlpha();
        return 0;
    }

    public Color getSelectionColor() {
        return selectionColor;
    }

    public Color getFocusColor() {
        return focusColor;
    }

    public Color getPreselectionColor() {
        return preselectionColor;
    }

    public Color getFocusFillColor() {
        return focusFillColor;
    }

    public Color getSelectionFillColor() {
        return selectionFillColor;
    }

    public Color getPreselectionFillColor() {
        return preselectionFillColor;
    }

    public int getFocusAlpha() {
        return focusAlpha;
    }

    public int getSelectionAlpha() {
        return selectionAlpha;
    }

    public int getPreselectionAlpha() {
        return preselectionAlpha;
    }

    public int getFocusFillAlpha() {
        return focusFillAlpha;
    }

    public int getSelectionFillAlpha() {
        return selectionFillAlpha;
    }

    public int getPreselectionFillAlpha() {
        return preselectionFillAlpha;
    }

    public void setSelectionColor(Color selectionColor) {
        if (equals(selectionColor, this.selectionColor))
            return;
        this.selectionColor = selectionColor;
        update();
    }

    public void setFocusColor(Color focusColor) {
        if (equals(focusColor, this.focusColor))
            return;
        this.focusColor = focusColor;
        update();
    }

    public void setPreselectionColor(Color preselectionColor) {
        if (equals(preselectionColor, this.preselectionColor))
            return;
        this.preselectionColor = preselectionColor;
        update();
    }

    public void setSelectionFillColor(Color selectionFillColor) {
        if (equals(selectionFillColor, this.selectionFillColor))
            return;
        this.selectionFillColor = selectionFillColor;
        update();
    }

    public void setFocusFillColor(Color focusFillColor) {
        if (equals(focusFillColor, this.focusFillColor))
            return;
        this.focusFillColor = focusFillColor;
        update();
    }

    public void setPreselectionFillColor(Color preselectionFillColor) {
        if (equals(preselectionFillColor, this.preselectionFillColor))
            return;
        this.preselectionFillColor = preselectionFillColor;
        update();
    }

    public void setFocusAlpha(int focusAlpha) {
        if (focusAlpha == getFocusAlpha())
            return;
        this.focusAlpha = focusAlpha;
        update();
    }

    public void setSelectionAlpha(int selectionAlpha) {
        if (selectionAlpha == getSelectionAlpha())
            return;
        this.selectionAlpha = selectionAlpha;
        update();
    }

    public void setPreselectionAlpha(int preselectionAlpha) {
        if (preselectionAlpha == getPreselectionAlpha())
            return;
        this.preselectionAlpha = preselectionAlpha;
        update();
    }

    public void setFocusFillAlpha(int focusFillAlpha) {
        if (focusFillAlpha == getFocusFillAlpha())
            return;
        this.focusFillAlpha = focusFillAlpha;
        update();
    }

    public void setSelectionFillAlpha(int selectionFillAlpha) {
        if (selectionFillAlpha == getSelectionFillAlpha())
            return;
        this.selectionFillAlpha = selectionFillAlpha;
        update();
    }

    public void setPreselectionFillAlpha(int preselectionFillAlpha) {
        if (preselectionFillAlpha == getPreselectionFillAlpha())
            return;
        this.preselectionFillAlpha = preselectionFillAlpha;
        update();
    }

    public int getMainAlpha() {
        return fillAlpha;
    }

    public int getSubAlpha() {
        return outlineAlpha;
    }

    public void setMainAlpha(int alpha) {
        if (alpha == getMainAlpha())
            return;
        this.fillAlpha = alpha;
        repaint();
    }

    public void setSubAlpha(int alpha) {
        if (alpha == getSubAlpha())
            return;
        this.outlineAlpha = alpha;
        repaint();
    }

    protected void fillShape(Graphics graphics) {
        graphics.setAlpha(getMainAlpha());
        super.fillShape(graphics);
    }

    protected void outlineShape(Graphics graphics) {
        int alpha = getSubAlpha();
        graphics.setAlpha(Math.max(0, alpha));
        super.outlineShape(graphics);
    }

    private static boolean equals(Object o1, Object o2) {
        return o1 == o2 || (o1 != null && o1.equals(o2));
    }

}