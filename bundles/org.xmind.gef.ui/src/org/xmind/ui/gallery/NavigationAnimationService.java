/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See http://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/

package org.xmind.ui.gallery;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LayoutListener;
import org.eclipse.draw2d.UpdateManager;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.xmind.gef.GraphicalViewer;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.gef.part.IPart;
import org.xmind.gef.service.GraphicalViewerService;

/**
 * @author Frank Shaka
 * 
 */
public class NavigationAnimationService extends GraphicalViewerService
        implements ISelectionChangedListener {

    private static final int DURATION = 100;

//    private static final int INTERVALS = 100;

    private static class ItemState {

        public double state;

        public Rectangle bounds;

        /**
         * 
         */
        public ItemState(NavigationItemFigure figure) {
            this.state = figure.getState();
            this.bounds = new Rectangle(figure.getBounds());
        }

    }

    private static class ItemStates {

        private Map<IFigure, ItemState> states = new HashMap<IFigure, ItemState>();

        public IFigure itemParent;

        public IFigure content;

        public IFigure contentParent;

        public Rectangle contentBounds;

        /**
         * 
         */
        public ItemStates(IFigure parent, IFigure content) {
            this.itemParent = parent;
            List children = parent.getChildren();
            for (int i = 0; i < children.size(); i++) {
                IFigure child = (IFigure) children.get(i);
                states.put(child, new ItemState((NavigationItemFigure) child));
            }
            this.content = content;
            this.contentParent = content.getParent();
            this.contentBounds = new Rectangle(content.getBounds());
        }

        public Iterator<IFigure> figures() {
            return states.keySet().iterator();
        }

        public ItemState getState(IFigure figure) {
            return states.get(figure);
        }

        public void apply() {
            Iterator<IFigure> it = figures();
            while (it.hasNext()) {
                IFigure figure = it.next();
                ItemState state = getState(figure);
                ((NavigationItemFigure) figure).setState(state.state);
                figure.setBounds(state.bounds);
            }
            content.setBounds(contentBounds);
        }

    }

    private class Transition extends LayoutListener.Stub implements Runnable {

        private UpdateManager updateManager;

        private ItemStates sourceStates;

        private ItemStates targetStates;

        private long start = 0;

        private long end = 0;

        /**
         * 
         */
        public Transition(UpdateManager updateManager, ItemStates sourceStates,
                ItemStates targetStates) {
            this.updateManager = updateManager;
            this.sourceStates = sourceStates;
            this.targetStates = targetStates;
        }

        public void start() {
            sourceStates.itemParent.addLayoutListener(this);
            sourceStates.contentParent.addLayoutListener(this);
            start = System.currentTimeMillis();
            end = start + DURATION;
            sourceStates.apply();
            try {
                while (System.currentTimeMillis() < end) {
                    run();
                }
            } catch (IllegalStateException e) {
            }
            targetStates.apply();
        }

        private double getRatio() {
            double time = System.currentTimeMillis();
            if (time > end)
                return -1;
            double ratio = ((double) (time - start)) / DURATION;
            return Math.max(0, Math.min(1, ratio));
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.eclipse.draw2d.LayoutListener.Stub#layout(org.eclipse.draw2d.
         * IFigure)
         */
        @Override
        public boolean layout(IFigure container) {
            double r = getRatio();
            if (r < 0) {
                return false;
            }
            if (container == sourceStates.itemParent) {
                Rectangle b = new Rectangle();
                List children = container.getChildren();
                for (int i = 0; i < children.size(); i++) {
                    IFigure child = (IFigure) children.get(i);
                    ItemState s1 = sourceStates.getState(child);
                    ItemState s2 = targetStates.getState(child);
                    int x = (int) seg(s1.bounds.x, s2.bounds.x, r);
                    int y = (int) seg(s1.bounds.y, s2.bounds.y, r);
                    int w = (int) seg(s1.bounds.width, s2.bounds.width, r);
                    int h = (int) seg(s1.bounds.height, s2.bounds.height, r);
                    b.setBounds(x, y, w, h);
                    child.setBounds(b);
                }
            } else if (container == sourceStates.contentParent) {
                Rectangle b = new Rectangle();
                Rectangle r1 = sourceStates.contentBounds;
                Rectangle r2 = targetStates.contentBounds;
                b.x = (int) seg(r1.x, r2.x, r);
                b.y = (int) seg(r1.y, r2.y, r);
                b.width = (int) seg(r1.width, r2.width, r);
                b.height = (int) seg(r1.height, r2.height, r);
                sourceStates.content.setBounds(b);
            }
            return true;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Runnable#run()
         */
        public void run() {
            double r = getRatio();
            if (r < 0) {
                throw new IllegalStateException("Animation is finished."); //$NON-NLS-1$
            }
            changeState(r);
            sourceStates.itemParent.revalidate();
            updateManager.performUpdate();
        }

        private void changeState(double r) {
            Iterator<IFigure> it = sourceStates.figures();
            while (it.hasNext()) {
                NavigationItemFigure figure = (NavigationItemFigure) it.next();
                ItemState s1 = sourceStates.getState(figure);
                ItemState s2 = targetStates.getState(figure);
                figure.setState(seg(s1.state, s2.state, r));
            }
        }

    }

//    private States currentStates = null;
//
//    private Queue<Transition> transitions = new LinkedList<Transition>();
//
//    private Transition runningTransition = null;

    /**
     * @param viewer
     */
    public NavigationAnimationService(IGraphicalViewer viewer) {
        super(viewer);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.gef.service.AbstractViewerService#activate()
     */
    @Override
    protected void activate() {
        getViewer().addFocusedPartChangedListener(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.gef.service.AbstractViewerService#deactivate()
     */
    @Override
    protected void deactivate() {
        getViewer().removeFocusedPartChangedListener(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(
     * org.eclipse.jface.viewers.SelectionChangedEvent)
     */
    public void selectionChanged(SelectionChangedEvent event) {
        if (!isActive())
            return;

        ItemStates sourceStates = captureStates();
        if (!applySelectionChanges(event.getSelection()))
            return;

        UpdateManager updateManager = ((GraphicalViewer) getViewer())
                .getLightweightSystem().getUpdateManager();
        updateManager.performValidation();

        ItemStates targetStates = captureStates();

        new Transition(updateManager, sourceStates, targetStates).start();
    }

    /**
     * @param selection
     */
    private boolean applySelectionChanges(ISelection selection) {
        if (selection.isEmpty())
            return false;

        IPart part = getViewer().findPart(
                ((IStructuredSelection) selection).getFirstElement());
        if (part == null)
            return false;
        IPart parent = getViewer().getRootPart().getContents();
        for (IPart child : parent.getChildren()) {
            NavigationItemFigure childFigure = (NavigationItemFigure) ((IGraphicalPart) child)
                    .getFigure();
            if (child == part) {
                // focused:
                childFigure.setState(1);
                childFigure.getParent().setConstraint(childFigure, childFigure);
            } else {
                // not focused:
                childFigure.setState(0);
            }
        }
        ((NavigationContentPart) getViewer().getRootPart().getContents())
                .resetScrollOffset();
        return true;
    }

    public ItemStates captureStates() {
        IGraphicalPart contentPart = (IGraphicalPart) getViewer().getRootPart()
                .getContents();
        return new ItemStates(contentPart.getContentPane(),
                contentPart.getFigure());
    }

//    private void runTransition(Transition transition) {
//        transitions.offer(transition);
//        if (runningTransition != null)
//            return;
//        runNextTransition();
//    }
//
//    private void runNextTransition() {
//        if (!isActive())
//            return;
//        if (transitions.isEmpty())
//            return;
//        runningTransition = transitions.poll();
//        System.out.println("Start: " + runningTransition);
//        runningTransition.start();
//    }
//
    private static double seg(double min, double max, double ratio) {
        return min + (max - min) * ratio;
    }

}
