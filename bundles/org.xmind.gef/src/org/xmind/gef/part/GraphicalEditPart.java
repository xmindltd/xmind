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
package org.xmind.gef.part;

import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LayoutManager;
import org.eclipse.draw2d.Viewport;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Display;
import org.xmind.gef.IDecorator;
import org.xmind.gef.IViewer;
import org.xmind.gef.IViewer.IPartSearchCondition;
import org.xmind.gef.NullDecorator;
import org.xmind.gef.draw2d.IUseTransparency;

/**
 * @author Brian Sun
 */
public abstract class GraphicalEditPart extends EditPart
        implements IGraphicalEditPart {

    private IFigure figure = null;

    private boolean figureInitiated = false;

    private IDecorator decorator = null;

    public IFigure getFigure() {
        if (figure == null) {
            figure = createFigure();
        }
        return figure;
    }

    protected boolean hasFigure() {
        return figure != null;
    }

    public IFigure getContentPane() {
        return getFigure();
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.gef.part.EditPart#onActivated()
     */
    @Override
    protected void onActivated() {
        super.onActivated();
        if (!figureInitiated) {
            initFigure(getFigure());
            figureInitiated = true;
        }
    }

    protected void initFigure(IFigure figure) {
        LayoutManager layout = createLayoutManager();
        if (layout != null)
            getContentPane().setLayoutManager(layout);
        getDecorator().activate(this, figure);
    }

    protected LayoutManager createLayoutManager() {
        return null;
    }

    protected abstract IFigure createFigure();

    protected void addChildView(IPart child, int index) {
        getContentPane().add(((IGraphicalPart) child).getFigure(), index);
    }

    protected void removeChildView(IPart child) {
        getContentPane().remove(((IGraphicalPart) child).getFigure());
    }

    public IDecorator getDecorator() {
        if (decorator == null)
            return NullDecorator.getInstance();
        return decorator;
    }

    public void setDecorator(IDecorator decorator) {
        this.decorator = decorator;
    }

    @Override
    protected void onDeactivated() {
        super.onDeactivated();
        if (figure != null && figureInitiated) {
            getDecorator().deactivate(this, figure);
            figureInitiated = false;
        }
    }

    protected void updateView() {
        super.updateView();
        if (getFigure() != null) {
            if (!figureInitiated) {
                initFigure(getFigure());
                figureInitiated = true;
            }
            getDecorator().decorate(this, getFigure());
        }
    }

    protected void updateChildren() {
        super.updateChildren();
        if (getFigure() != null) {
            if (!figureInitiated) {
                initFigure(getFigure());
                figureInitiated = true;
            }
            getDecorator().decorateChildren(this, getFigure());
        }
    }

    /**
     * @see org.xmind.gef.part.Part#getAdapter(java.lang.Class)
     */
    public <T> T getAdapter(Class<T> adapter) {
        if (adapter == IFigure.class)
            return adapter.cast(getFigure());
        if (adapter == IUseTransparency.class) {
            IFigure f = getFigure();
            if (f instanceof IUseTransparency) {
                return adapter.cast(f);
            }
        }
        if (adapter == IDecorator.class)
            return adapter.cast(getDecorator());
        return super.getAdapter(adapter);
    }

    /**
     * @see org.xmind.gef.part.IGraphicalEditPart#findAt(org.eclipse.draw2d.geometry.Point)
     */
    public IPart findAt(Point position) {
        return findAt(position, null);
    }

    /*
     * (non-Javadoc)
     * @see
     * org.xmind.gef.part.IGraphicalEditPart#findAt(org.eclipse.draw2d.geometry
     * .Point, org.xmind.gef.IViewer.IPartSearchCondition)
     */
    public IPart findAt(Point position, IPartSearchCondition condition) {
        if (!(getFigure() instanceof Viewport) && !containsPoint(position)) {
            return null;
        }
        IPart ret;
        ret = findChildAt(position);
        if (ret != null)
            return ret;
        if (containsPoint(position)
                && (condition == null || condition.evaluate(this))) {
            return this;
        }
        return null;
    }

    protected IPart findChildAt(Point position) {
        List<IPart> children = getChildren();
        for (int i = children.size() - 1; i >= 0; i--) {
            IPart child = children.get(i);
            IPart ret = findChildAt(child, position);
            if (ret != null)
                return ret;
        }
        return null;
    }

    protected IPart findChildAt(IPart child, Point position) {
        if (child instanceof IGraphicalEditPart) {
            return ((IGraphicalEditPart) child).findAt(position);
        }
        return null;
    }

    /**
     * @see org.xmind.gef.part.IGraphicalEditPart#findAt(org.eclipse.draw2d.geometry.Point)
     */
    public IFigure findTooltipAt(Point position) {
        if (containsPoint(position))
            return getFigure().getToolTip();
        return null;
    }

    public boolean containsPoint(Point position) {
        return getFigure().isShowing() && getFigure().containsPoint(position);
    }

    /**
     * @see org.xmind.gef.part.IGraphicalEditPart#getCursor(org.eclipse.draw2d.geometry.Point)
     */
    public Cursor getCursor(Point pos) {
        return null;
    }

    public void updateToolTip() {
        IFigure fig = getFigure();
        if (fig != null) {
            fig.setToolTip(createToolTip());
            Display.getCurrent().asyncExec(new Runnable() {
                public void run() {
                    if (!getStatus().isActive())
                        return;

                    IViewer viewer = getSite().getViewer();
                    if (viewer == null || viewer.getControl().isDisposed())
                        return;

                    viewer.updateToolTip();
                }
            });
        }
    }

    protected IFigure createToolTip() {
        return null;
    }

    public String getActionId() {
        return null;
    }
}
