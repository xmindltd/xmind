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

import java.util.List;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.IFigure;
import org.eclipse.jface.util.Util;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.xmind.gef.EditDomain;
import org.xmind.gef.GEF;
import org.xmind.gef.GraphicalViewer;
import org.xmind.gef.event.MouseEvent;
import org.xmind.gef.part.GraphicalEditPart;
import org.xmind.gef.part.GraphicalRootEditPart;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.gef.part.IPart;
import org.xmind.gef.part.IPartFactory;
import org.xmind.gef.part.IRootPart;
import org.xmind.gef.tool.SelectTool;

/**
 * @author Frank Shaka
 * 
 */
public class NavigationViewer extends GraphicalViewer {

    private static class NavigationSelectTool extends SelectTool {

        @Override
        protected boolean handleMouseDown(MouseEvent me) {
            if (me.leftOrRight) {
                if (me.target.hasRole(GEF.ROLE_SELECTABLE)) {
                    selectSingle(me.target);
                    return true;
                }
                return super.handleMouseDown(me);
            } else {
                return false;
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xmind.gef.tool.SelectTool#select(java.util.List,
         * org.xmind.gef.part.IPart)
         */
        @Override
        protected void select(List<? extends IPart> toSelect, IPart toFocus) {
            if (toSelect.isEmpty())
                return;
            super.select(toSelect, toFocus);
        }

    }

    private class NavigationScrollHandler implements Listener {

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets
         * .Event)
         */
        public void handleEvent(Event event) {
            int offset = event.count;
            offset = (int) (Math.sqrt(Math.abs(offset)) * offset);
            ((NavigationContentPart) getRootPart().getContents())
                    .addScrollOffset(offset);
        }

    }

    public static final int PREF_HEIGHT = 60;

    public static final int BIG_HEIGHT = 70;

    public static final int SMALL_HEIGHT = 50;

    public static final int BIG_ALPHA = 0;

    public static final int SMALL_ALPHA = 40;

    private static class EmptyPart extends GraphicalEditPart {
        protected IFigure createFigure() {
            return new Figure();
        }
    }

    private static IPartFactory DEFAULT_PART_FACTORY = new IPartFactory() {
        public IPart createPart(IPart context, Object model) {
            if (context instanceof NavigationContentPart) {
                return new NavigationItemPart(model);
            } else if (context instanceof IRootPart) {
                return new NavigationContentPart(model);
            } else {
                return new EmptyPart();
            }
        }
    };

    private class LabelProviderListener implements ILabelProviderListener {
        public void labelProviderChanged(LabelProviderChangedEvent event) {
            update(event.getElements());
        }
    }

    private IStructuredContentProvider contentProvider = null;

    private IBaseLabelProvider labelProvider = null;

    private ILabelProviderListener labelProviderListener = null;

    /**
     * 
     */
    public NavigationViewer() {
        setPartFactory(DEFAULT_PART_FACTORY);
        setRootPart(new GraphicalRootEditPart());
        EditDomain editDomain = new EditDomain();
        editDomain.installTool(GEF.TOOL_SELECT, new NavigationSelectTool());
        setEditDomain(editDomain);
        NavigationAnimationService animationService = new NavigationAnimationService(
                this);
        installService(NavigationAnimationService.class, animationService);
        animationService.setActive(true);
    }

    public <T> T getAdapter(Class<T> adapter) {
        if (IContentProvider.class.equals(adapter)
                || IStructuredContentProvider.class.equals(adapter))
            return adapter.cast(getContentProvider());
        if (IBaseLabelProvider.class.equals(adapter))
            return adapter.cast(getLabelProvider());
        return super.getAdapter(adapter);
    }

    public IStructuredContentProvider getContentProvider() {
        return contentProvider;
    }

    public void setContentProvider(IStructuredContentProvider contentProvider) {
        if (contentProvider == null || contentProvider == this.contentProvider)
            return;
        IStructuredContentProvider oldContentProvider = this.contentProvider;
        this.contentProvider = contentProvider;
        if (oldContentProvider != null) {
            oldContentProvider.dispose();
        }
        contentProvider.inputChanged(this, getInput(), getInput());
        refresh();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.gef.GraphicalViewer#internalInputChanged(java.lang.Object,
     * java.lang.Object)
     */
    @Override
    protected void internalInputChanged(Object input, Object oldInput) {
        if (getContentProvider() != null) {
            getContentProvider().inputChanged(this, oldInput, input);
        }
        super.internalInputChanged(input, oldInput);
    }

    public void update() {
        update(null);
    }

    public void update(Object[] elements) {
        if (elements == null) {
            IPart contents = getRootPart().getContents();
            if (contents.getStatus().isActive())
                contents.refresh();
            for (IPart p : contents.getChildren()) {
                if (p.getStatus().isActive()) {
                    ((IGraphicalPart) p).refresh();
                }
            }
        } else {
            for (Object element : elements) {
                IPart p = findPart(element);
                if (p != null && p.getStatus().isActive()) {
                    ((IGraphicalPart) p).refresh();
                }
            }
        }
    }

    public IBaseLabelProvider getLabelProvider() {
        if (labelProvider == null) {
            labelProvider = new LabelProvider();
        }
        return labelProvider;
    }

    public void setLabelProvider(IBaseLabelProvider labelProvider) {
        if (labelProvider == null)
            return;

        IBaseLabelProvider oldLabelProvider = this.labelProvider;
        if (labelProvider == oldLabelProvider)
            return;

        if (oldLabelProvider != null) {
            if (labelProviderListener != null) {
                oldLabelProvider.removeListener(labelProviderListener);
            }
        }
        this.labelProvider = labelProvider;
        if (labelProviderListener == null)
            labelProviderListener = new LabelProviderListener();
        labelProvider.addListener(labelProviderListener);
        refresh();

        if (oldLabelProvider != null) {
            oldLabelProvider.dispose();
        }
    }

    protected void handleDispose(DisposeEvent e) {
        if (labelProvider != null) {
            if (labelProviderListener != null)
                labelProvider.removeListener(labelProviderListener);
            labelProvider.dispose();
            labelProvider = null;
        }
        if (contentProvider != null) {
            contentProvider.dispose();
            contentProvider = null;
        }
        super.handleDispose(e);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.gef.GraphicalViewer#internalCreateControl(org.eclipse.swt.
     * widgets .Composite, int)
     */
    @Override
    protected Control internalCreateControl(Composite parent, int style) {
        FigureCanvas canvas = (FigureCanvas) super.internalCreateControl(parent,
                style);
        canvas.setScrollBarVisibility(FigureCanvas.NEVER);
        getViewport().setContentsTracksWidth(true);
        getViewport().setContentsTracksHeight(true);
        if (Util.isMac()) {
            canvas.addListener(SWT.MouseHorizontalWheel,
                    new NavigationScrollHandler());
        } else {
            canvas.addListener(SWT.MouseVerticalWheel,
                    new NavigationScrollHandler());
        }
        return canvas;
    }
}
