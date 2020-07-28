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
package org.xmind.ui.internal.mindmap;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.xmind.gef.GEF;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.IViewer;
import org.xmind.gef.draw2d.IAnchor;
import org.xmind.gef.draw2d.IAnchorListener;
import org.xmind.gef.draw2d.ITextFigure;
import org.xmind.gef.draw2d.ITitledFigure;
import org.xmind.gef.draw2d.decoration.IConnectionDecoration;
import org.xmind.gef.draw2d.decoration.ICorneredDecoration;
import org.xmind.gef.draw2d.decoration.IDecoration;
import org.xmind.gef.draw2d.decoration.ILineDecoration;
import org.xmind.gef.draw2d.decoration.IShapeDecoration;
import org.xmind.gef.draw2d.geometry.Geometry;
import org.xmind.gef.graphicalpolicy.IStyleSelector;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.ui.mindmap.IConnectionPart;
import org.xmind.ui.mindmap.INodePart;
import org.xmind.ui.mindmap.ITitleTextPart;
import org.xmind.ui.style.StyleUtils;
import org.xmind.ui.style.Styles;

public abstract class DecoratedLineFeedback extends DecoratedFeedback implements
        IAnchorListener {

    protected class TitledDecoratedFigure extends DecoratedFigure implements
            ITitledFigure {

        public ITextFigure getTitle() {
            ITitleTextPart title = (ITitleTextPart) host
                    .getAdapter(ITitleTextPart.class);
            if (title != null)
                return title.getTextFigure();
            return null;
        }

        public void setTitle(ITextFigure title) {
        }

    }

    private IGraphicalPart host;

    private IFigure realLayer;

    private int lineWidthExpansion = 0;

    private Color lineColor = null;

    private int lineStyle = SWT.DEFAULT;

    private IAnchor sourceAnchor = null;

    private IAnchor targetAnchor = null;

    public DecoratedLineFeedback(IGraphicalPart part) {
        this.host = part;
    }

    public IGraphicalPart getHost() {
        return host;
    }

    protected DecoratedFigure createDecoratedFigure() {
        return new TitledDecoratedFigure();
    }

    public void addToLayer(IFigure layer) {
        if (realLayer == null) {
            IViewer viewer = host.getSite().getViewer();
            if (viewer instanceof IGraphicalViewer) {
                realLayer = ((IGraphicalViewer) viewer)
                        .getLayer(GEF.LAYER_PRESENTATION);
            }
        }
        if (realLayer != null)
            layer = realLayer;
        super.addToLayer(layer);
    }

    public void removeFromLayer(IFigure layer) {
        if (realLayer != null)
            layer = realLayer;
        super.removeFromLayer(layer);
    }

    public Color getLineColor() {
        return lineColor;
    }

    public int getLineStyle() {
        return lineStyle;
    }

    public int getLineWidthExpansion() {
        return lineWidthExpansion;
    }

    public void setLineColor(Color lineColor) {
        this.lineColor = lineColor;
    }

    public void setLineStyle(int lineStyle) {
        this.lineStyle = lineStyle;
    }

    public void setLineWidthExpansion(int expansion) {
        this.lineWidthExpansion = expansion;
    }

    protected void updateBounds(IFigure figure) {
        IFigure client = host.getFigure();
        int exp = calcExpansion();
        Rectangle bounds = client.getBounds().getExpanded(exp, exp);
        Insets ins = Geometry.add(new Insets(client.getInsets()), exp);
        figure.setBounds(bounds);
        figure.setBorder(new MarginBorder(ins));
    }

    protected int calcExpansion() {
        return getLineWidthExpansion();
    }

    protected void updateDecoration(IFigure figure, IDecoration decoration) {
        String decorationId = decoration.getId();
        IStyleSelector ss = StyleUtils.getStyleSelector(host);
        updateDecoration(figure, decoration, decorationId, ss);
        if (decoration instanceof IConnectionDecoration) {
            ((IConnectionDecoration) decoration).reroute(figure);
        }
    }

    protected void updateDecoration(IFigure figure, IDecoration decoration,
            String decorationId, IStyleSelector ss) {
        if (decoration instanceof IShapeDecoration) {
            decoration.setAlpha(figure, 0xff);
            ((IShapeDecoration) decoration).setFillAlpha(figure, 0);
            ((IShapeDecoration) decoration).setFillColor(figure, null);
            ((IShapeDecoration) decoration).setGradient(figure, false);
            ((IShapeDecoration) decoration).setLineAlpha(figure,
                    getDecorationAlpha(figure, decoration));
        } else {
            decoration.setAlpha(figure, getDecorationAlpha(figure, decoration));
        }
        if (decoration instanceof ILineDecoration) {
            Color lineColor = getLineColor();
            if (lineColor == null) {
                lineColor = StyleUtils.getColor(host, ss, Styles.LineColor,
                        decorationId, Styles.DEF_BOUNDARY_LINE_COLOR);
            }
            ((ILineDecoration) decoration).setLineColor(figure, lineColor);

            int lineStyle = getLineStyle();
            if (lineStyle == SWT.DEFAULT) {
                lineStyle = StyleUtils.getLineStyle(host, ss, decorationId,
                        SWT.LINE_DASH);
            }
            ((ILineDecoration) decoration).setLineStyle(figure, lineStyle);

            int originalLineWidth = StyleUtils.getInteger(host, ss,
                    Styles.LineWidth, decorationId,
                    Styles.DEF_BOUNDARY_LINE_WIDTH);
            int lineWidth = originalLineWidth + getLineWidthExpansion() * 2;
            ((ILineDecoration) decoration).setLineWidth(figure, lineWidth);
        }
        setSourceAnchor(getSourceAnchor(host), figure, decoration);
        setTargetAnchor(getTargetAnchor(host), figure, decoration);
        if (decoration instanceof ICorneredDecoration) {
            int corner = StyleUtils.getInteger(host, ss, Styles.ShapeCorner,
                    decorationId, 10);
            ((ICorneredDecoration) decoration).setCornerSize(figure, corner);
        }
        decoration.setVisible(figure, isDecorationVisible(figure, decoration));
    }

    protected int getDecorationAlpha(IFigure figure, IDecoration decoration) {
        if (host.getStatus().isPreSelected() && !host.getStatus().isSelected()) {
            return 0x60;
        }
        return 0xff;
    }

    protected boolean isDecorationVisible(IFigure figure, IDecoration decoration) {
        return host.getFigure().isVisible();
    }

    protected void disposeOldDecoration(IFigure figure, IDecoration decoration) {
        setSourceAnchor(null, figure, decoration);
        setTargetAnchor(null, figure, decoration);
    }

    protected IAnchor getSourceAnchor(IGraphicalPart part) {
        if (part instanceof IConnectionPart) {
            INodePart node = ((IConnectionPart) part).getSourceNode();
            if (node != null)
                return node.getSourceAnchor(part);
        }
        return null;
    }

    protected IAnchor getTargetAnchor(IGraphicalPart part) {
        if (part instanceof IConnectionPart) {
            INodePart node = ((IConnectionPart) part).getTargetNode();
            if (node != null)
                return node.getTargetAnchor(part);
        }
        return null;
    }

    protected void setSourceAnchor(IAnchor anchor, IFigure figure,
            IDecoration decoration) {
        if (anchor != this.sourceAnchor) {
            if (this.sourceAnchor != null) {
                unhookAnchor(this.sourceAnchor);
            }
            this.sourceAnchor = anchor;
            if (anchor != null) {
                hookAnchor(anchor);
            }
            figure.revalidate();
            figure.repaint();
        }
        if (decoration instanceof IConnectionDecoration) {
            ((IConnectionDecoration) decoration)
                    .setSourceAnchor(figure, anchor);
        }
    }

    protected void setTargetAnchor(IAnchor anchor, IFigure figure,
            IDecoration decoration) {
        if (anchor != this.targetAnchor) {
            if (this.targetAnchor != null) {
                unhookAnchor(this.targetAnchor);
            }
            this.targetAnchor = anchor;
            if (anchor != null) {
                hookAnchor(anchor);
            }
            figure.revalidate();
            figure.repaint();
        }
        if (decoration instanceof IConnectionDecoration) {
            ((IConnectionDecoration) decoration)
                    .setTargetAnchor(figure, anchor);
        }
    }

    protected void unhookAnchor(IAnchor anchor) {
        anchor.removeAnchorListener(this);
    }

    protected void hookAnchor(IAnchor anchor) {
        anchor.addAnchorListener(this);
    }

    public void anchorMoved(IAnchor anchor) {
        if (getFigure() != null) {
            updateBounds(getFigure());
        }
    }
}