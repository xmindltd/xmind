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
package org.xmind.ui.gallery;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.graphics.Color;
import org.xmind.gef.IViewer;
import org.xmind.gef.draw2d.ITextFigure;
import org.xmind.gef.part.Decorator;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.gef.util.Properties;
import org.xmind.ui.viewers.IGraphicalToolTipProvider;
import org.xmind.ui.viewers.IToolTipProvider;

public class FrameDecorator extends Decorator {

    public static final FrameDecorator DEFAULT = new FrameDecorator();

    public void decorate(IGraphicalPart part, IFigure figure) {
        super.decorate(part, figure);
        FrameFigure frame = (FrameFigure) part.getFigure();
        Object model = part.getModel();

        IViewer viewer = part.getSite().getViewer();
        Properties properties = viewer.getProperties();
        IBaseLabelProvider labelProvider = (IBaseLabelProvider) viewer
                .getAdapter(IBaseLabelProvider.class);

        boolean hideTitle = properties.getBoolean(GalleryViewer.HideTitle,
                false);
        frame.setHideTitle(hideTitle);

        boolean flat = properties.getBoolean(GalleryViewer.FlatFrames, false);
        frame.setFlat(flat);
        frame.setContentSize(
                (Dimension) properties.get(GalleryViewer.FrameContentSize));

        int titlePlacement = properties.getInteger(GalleryViewer.TitlePlacement,
                GalleryViewer.TITLE_TOP.intValue());
        frame.setTitlePlacement(titlePlacement);
        if (!hideTitle) {
            decorateTitle(frame.getTitle(), model, labelProvider);
        }

        boolean useCustomDecorator = properties
                .getBoolean(GalleryViewer.CustomContentPaneDecorator, false);
        if (useCustomDecorator && labelProvider instanceof ILabelDecorator) {
            IDecorationContext context = viewer instanceof IDecorationContext
                    ? (IDecorationContext) viewer : null;
            ((ILabelDecorator) labelProvider)
                    .decorateFigure(frame.getContentPane(), model, context);
        }

        if (labelProvider instanceof IGraphicalToolTipProvider) {
            IGraphicalToolTipProvider toolTipProvider = (IGraphicalToolTipProvider) labelProvider;
            IFigure toolTipFigure = toolTipProvider.getToolTipFigure(model);
            frame.setToolTip(toolTipFigure);
        }
        if (labelProvider instanceof IToolTipProvider) {
            IToolTipProvider toolTipProvider = (IToolTipProvider) labelProvider;
            String toolTip = toolTipProvider.getToolTip(model);
            if (toolTip == null || "".equals(toolTip)) { //$NON-NLS-1$
                frame.setToolTip(null);
            } else {
                Label toolTipFigure = new Label();
                toolTipFigure.setText(toolTip);
                frame.setToolTip(toolTipFigure);
            }
        }

        ShadowedLayer layer = frame.getContentPane();
        layer.setBorderWidth(
                properties.getInteger(GalleryViewer.ContentPaneBorderWidth, 1));
        Object color = properties.get(GalleryViewer.ContentPaneBorderColor);
        if (color != null && color instanceof Color) {
            layer.setBorderAlpha(0xff);
            layer.setBorderColor((Color) color);
        }

    }

    private String getText(Object element, IBaseLabelProvider labelProvider) {
        if (labelProvider instanceof ILabelProvider)
            return ((ILabelProvider) labelProvider).getText(element);
        return null;
    }

    protected void decorateTitle(ITextFigure titleFigure, Object model,
            IBaseLabelProvider labelProvider) {
        String text = getText(model, labelProvider);
        if (text == null)
            text = ""; //$NON-NLS-1$
        titleFigure.setText(text);
        if (labelProvider instanceof IFontProvider) {
            IFontProvider fontProvider = (IFontProvider) labelProvider;
            titleFigure.setFont(fontProvider.getFont(model));
        }
        if (labelProvider instanceof IColorProvider) {
            IColorProvider colorProvider = (IColorProvider) labelProvider;
            titleFigure.setForegroundColor(colorProvider.getForeground(model));
            titleFigure.setBackgroundColor(colorProvider.getBackground(model));
        }
    }

}
