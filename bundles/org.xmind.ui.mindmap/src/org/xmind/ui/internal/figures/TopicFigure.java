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

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LayoutManager;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.SWT;
import org.xmind.gef.draw2d.DecoratedShapeFigure;
import org.xmind.gef.draw2d.IMinimizable;
import org.xmind.gef.draw2d.IReferenceDescriptor;
import org.xmind.gef.draw2d.IRelayerableFigure;
import org.xmind.gef.draw2d.IRotatable;
import org.xmind.gef.draw2d.IRotatableReferenceDescriptor;
import org.xmind.gef.draw2d.IRotatableReferencedFigure;
import org.xmind.gef.draw2d.IRotatableReferencedLayout;
import org.xmind.gef.draw2d.IShadowedFigure;
import org.xmind.gef.draw2d.ITextFigure;
import org.xmind.gef.draw2d.ITitledFigure;
import org.xmind.gef.draw2d.ITransparentableFigure;
import org.xmind.gef.draw2d.decoration.IShadowedDecoration;
import org.xmind.gef.draw2d.geometry.PrecisionDimension;
import org.xmind.gef.draw2d.geometry.PrecisionInsets;
import org.xmind.gef.draw2d.geometry.PrecisionRectangle;
import org.xmind.gef.draw2d.geometry.PrecisionRotator;
import org.xmind.gef.draw2d.graphics.AlphaGraphics;
import org.xmind.gef.draw2d.graphics.GrayedGraphics;
import org.xmind.ui.decorations.ITopicDecoration;

public class TopicFigure extends DecoratedShapeFigure
        implements ITitledFigure, IMinimizable, IShadowedFigure,
        IRotatableReferencedFigure, IRelayerableFigure, ITransparentableFigure {

    protected static final int FLAG_MINIMIZED = MAX_FLAG << 1;
    protected static final int FLAG_RELAYERED = MAX_FLAG << 2;

    static {
        MAX_FLAG = FLAG_RELAYERED;
    }

    private ITextFigure title = null;

    private PrecisionRotator rotator = null;

    private int selfAlpha = 0xff;
    private int treeAlpha = 0xff;

    public ITextFigure getTitle() {
        return title;
    }

    public void setTitle(ITextFigure title) {
        if (title == this.title)
            return;

        this.title = title;
        revalidate();
        repaint();
    }

    protected PrecisionRotator r() {
        if (rotator == null)
            rotator = new PrecisionRotator();
        return rotator;
    }

    public ITopicDecoration getDecoration() {
        return (ITopicDecoration) super.getDecoration();
    }

    protected Insets calculatePreferredInsets() {
        if (isMinimized())
            return NO_INSETS;
        return super.calculatePreferredInsets();
    }

    public boolean isMinimized() {
        return getFlag(FLAG_MINIMIZED);
    }

    public void setMinimized(boolean minimized) {
        if (minimized == isMinimized())
            return;

        setFlag(FLAG_MINIMIZED, minimized);
        revalidate();
        repaint();
    }

    public boolean isShadowShowing() {
        IFigure p = getParent();
        while (p != null) {
            if (p instanceof IShadowedFigure
                    && !((IShadowedFigure) p).isShadowShowing())
                return false;
            p = p.getParent();
        }
        return isShowing();
    }

    public void paintShadow(Graphics graphics) {
        if (getDecoration() != null
                && getDecoration() instanceof IShadowedDecoration) {
            ((IShadowedDecoration) getDecoration()).paintShadow(this, graphics);
        }
    }

    public String toString() {
        if (title != null)
            return "TopicFigure(" + title.getText() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
        return super.toString();
    }

    public PrecisionRectangle getNormalPreferredBounds(Point reference) {
        PrecisionInsets ins = getNormalReferenceDescription();
        return new PrecisionRectangle(reference.x - ins.left,
                reference.y - ins.top, ins.getWidth(), ins.getHeight());
    }

    public PrecisionInsets getNormalReferenceDescription() {
        if (getReferenceDescriptor() instanceof IRotatableReferenceDescriptor) {
            return ((IRotatableReferenceDescriptor) getReferenceDescriptor())
                    .getNormalReferenceDescription(this);
        }
        return new PrecisionInsets(getReferenceDescription());
    }

    public PrecisionDimension getNormalPreferredSize(int wHint, int hHint) {
        if (getLayoutManager() instanceof IRotatableReferencedLayout) {
            return ((IRotatableReferencedLayout) getLayoutManager())
                    .getNormalPreferredSize(this, wHint, hHint);
        }
        return new PrecisionDimension(getPreferredSize(wHint, hHint));
    }

    public double getRotationDegrees() {
        return r().getAngle();
    }

    public void setRotationDegrees(double degrees) {
        double oldAngle = getRotationDegrees();
        r().setAngle(degrees);
        if (getBorder() instanceof IRotatable) {
            ((IRotatable) getBorder()).setRotationDegrees(degrees);
        }
        if (getLayoutManager() instanceof IRotatable) {
            ((IRotatable) getLayoutManager()).setRotationDegrees(degrees);
        }
        if (getReferenceDescriptor() instanceof IRotatable) {
            ((IRotatable) getReferenceDescriptor()).setRotationDegrees(degrees);
        }
        for (Object child : getChildren()) {
            if (child instanceof IRotatable) {
                ((IRotatable) child).setRotationDegrees(degrees);
            }
        }
        if (degrees != oldAngle) {
            revalidate();
            repaint();
        }
    }

    public void add(IFigure figure, Object constraint, int index) {
        super.add(figure, constraint, index);
        if (figure instanceof IRotatable) {
            ((IRotatable) figure).setRotationDegrees(getRotationDegrees());
        }
    }

    public void setLayoutManager(LayoutManager manager) {
        super.setLayoutManager(manager);
        if (manager instanceof IRotatable) {
            ((IRotatable) manager).setRotationDegrees(getRotationDegrees());
        }
    }

    public void setReferenceDescriptor(IReferenceDescriptor descriptor) {
        super.setReferenceDescriptor(descriptor);
        if (descriptor instanceof IRotatable) {
            ((IRotatable) descriptor).setRotationDegrees(getRotationDegrees());
        }
    }

    public boolean isRelayered() {
        return getFlag(FLAG_RELAYERED);
    }

    public void setRelayered(boolean relayered) {
        if (relayered == isRelayered())
            return;

        setFlag(FLAG_RELAYERED, relayered);
        repaint();
    }

    public void paintRelayered(Graphics graphics) {
        super.paint(graphics);
    }

    public void paint(Graphics graphics) {
        if (isRelayered())
            return;

        if (getMainAlpha() < 0xff) {
            AlphaGraphics ag = new AlphaGraphics(graphics);
            ag.setMainAlpha(getMainAlpha());
            ag.setAlpha(graphics.getAlpha());
            try {
                doPaint(ag);
            } finally {
                ag.dispose();
            }
        } else {
            doPaint(graphics);
        }
    }

    private void doPaint(Graphics graphics) {
        if (isEnabled()) {
            super.paint(graphics);
        } else {
            GrayedGraphics gg = new GrayedGraphics(graphics);
            try {
                super.paint(gg);
            } finally {
                gg.dispose();
            }
        }
    }

    protected void paintFigure(Graphics graphics) {
        if (getSubAlpha() < 0xff) {
            AlphaGraphics ag = new AlphaGraphics(graphics);
            ag.setMainAlpha(getSubAlpha());
            try {
                doPaintFigure(ag);
            } finally {
                ag.dispose();
            }
        } else {
            doPaintFigure(graphics);
        }
    }

    private void doPaintFigure(Graphics graphics) {
        graphics.setAntialias(SWT.ON);
        super.paintFigure(graphics);
    }

    public int getMainAlpha() {
        return treeAlpha;
    }

    public int getSubAlpha() {
        return selfAlpha;
    }

    public void setMainAlpha(int alpha) {
        if (alpha == this.treeAlpha)
            return;
        this.treeAlpha = alpha;
        repaint();
    }

    public void setSubAlpha(int alpha) {
        if (alpha == this.selfAlpha)
            return;
        this.selfAlpha = alpha;
        repaint();
    }

}