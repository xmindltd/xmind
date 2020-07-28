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
package org.xmind.ui.internal.figures;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.TextStyle;
import org.xmind.gef.draw2d.ITextFigure;
import org.xmind.gef.draw2d.ITitledFigure;
import org.xmind.gef.draw2d.ReferencedFigure;
import org.xmind.gef.draw2d.RotatableWrapLabel;
import org.xmind.gef.draw2d.SizeableImageFigure;
import org.xmind.ui.internal.svgsupport.SVGImageData;
import org.xmind.ui.internal.svgsupport.SVGImageFigure;

public class LegendItemFigure extends Figure implements ITitledFigure {

    private static final int SPACING = 7;

    private SizeableImageFigure icon = null;

    private SVGImageFigure svgIcon = null;

    private ITextFigure caption;

    private Dimension cachedPrefSize = null;

    public LegendItemFigure(int textRenderStyle,
            LocalResourceManager resourceManager) {
        if (resourceManager != null) {
            svgIcon = new SVGImageFigure();
            svgIcon.setManager(resourceManager);
            svgIcon.setConstrained(true);
            add(svgIcon);
        } else {
            icon = new SizeableImageFigure();
            icon.setConstrained(true);
            add(icon);
        }
        caption = new RotatableWrapLabel(textRenderStyle);
        add(caption);
    }

    public ITextFigure getTitle() {
        return caption;
    }

    public void setTitle(ITextFigure title) {
    }

    public ReferencedFigure getIcon() {
        return svgIcon != null ? svgIcon : icon;
    }

    public ITextFigure getCaption() {
        return caption;
    }

    public Dimension getPreferredSize(int wHint, int hHint) {
        if (prefSize != null)
            return prefSize;

        if (cachedPrefSize != null)
            return cachedPrefSize;

        Dimension s1 = svgIcon != null ? svgIcon.getPreferredSize()
                : icon.getPreferredSize();
        Dimension s2 = caption.getPreferredSize();
        cachedPrefSize = new Dimension(s1.width + SPACING + s2.width,
                Math.max(s1.height, s2.height));
        return cachedPrefSize;
    }

    public void invalidate() {
        super.invalidate();
        cachedPrefSize = null;
    }

    protected void layout() {
        Rectangle r = getBounds();
        Dimension s1 = svgIcon != null ? svgIcon.getPreferredSize()
                : icon.getPreferredSize();
        Dimension s2 = caption.getPreferredSize();
        if (svgIcon != null)
            svgIcon.setBounds(new Rectangle(r.x,
                    r.y + (r.height - s1.height) / 2, s1.width, s1.height));
        else
            icon.setBounds(new Rectangle(r.x, r.y + (r.height - s1.height) / 2,
                    s1.width, s1.height));
        caption.setBounds(new Rectangle(r.x + s1.width + SPACING,
                r.y + (r.height - s2.height) / 2, s2.width, s2.height));
    }

    public int getLineSpacing() {
        return caption.getLineSpacing();
    }

    public TextStyle getStyle() {
        return caption.getStyle();
    }

    public String getText() {
        return caption.getText();
    }

    public int getTextAlignment() {
        return caption.getTextAlignment();
    }

    public int getTextRenderStyle() {
        return ((RotatableWrapLabel) caption).getRenderStyle();
    }

    public void setLineSpacing(int spacing) {
        caption.setLineSpacing(spacing);
    }

    public void setStyle(TextStyle style) {
        caption.setStyle(style);
    }

    public void setText(String text) {
        caption.setText(text);
    }

    public void setTextAlignment(int align) {
        caption.setTextAlignment(align);
    }

    public void setTextRenderStyle(int textRenderStyle) {
        ((RotatableWrapLabel) caption).setRenderStyle(textRenderStyle);
    }

    public void setIconImage(Image image) {
        if (icon != null)
            icon.setImage(image);
    }

    public void setSVGData(SVGImageData svgImageData) {
        if (svgIcon != null)
            svgIcon.setSVGData(svgImageData);
    }

    public void setFont(Font f) {
        super.setFont(f);
        caption.setFont(f);
    }

    public void setForegroundColor(Color fg) {
        super.setForegroundColor(fg);
        caption.setForegroundColor(fg);
    }

    public void setBackgroundColor(Color bg) {
        super.setBackgroundColor(bg);
        caption.setBackgroundColor(bg);
    }

}