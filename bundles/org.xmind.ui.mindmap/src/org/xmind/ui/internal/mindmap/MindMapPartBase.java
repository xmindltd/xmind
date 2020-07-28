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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.CoreEventRegister;
import org.xmind.core.event.ICoreEventListener;
import org.xmind.core.event.ICoreEventRegister;
import org.xmind.gef.graphicalpolicy.IGraphicalPolicy;
import org.xmind.gef.graphicalpolicy.IStructure;
import org.xmind.gef.graphicalpolicy.IStyleSelector;
import org.xmind.gef.graphicalpolicy.NullGraphicalPolicy;
import org.xmind.gef.part.GraphicalEditPart;
import org.xmind.gef.service.IAnimationService;
import org.xmind.gef.service.IFeedback;
import org.xmind.gef.service.IFeedbackService;
import org.xmind.gef.service.IShadowService;
import org.xmind.ui.mindmap.IAnimatablePart;
import org.xmind.ui.mindmap.ICacheManager;
import org.xmind.ui.mindmap.ISelectionFeedbackHelper;
import org.xmind.ui.util.MindMapUtils;

public abstract class MindMapPartBase extends GraphicalEditPart
        implements IAnimatablePart {

    private ICoreEventRegister eventRegister = null;

    private ISelectionFeedbackHelper selectionFeedbackHelper = null;

    private IFeedback feedback = null;

    private IGraphicalPolicy graphicalPolicy = null;

    private boolean graphicalPolicyActivated = false;

    private ICacheManager cacheManager = null;

    protected MindMapPartBase() {
    }

    public IGraphicalPolicy getGraphicalPolicy() {
        if (graphicalPolicy == null)
            return NullGraphicalPolicy.getInstance();
        return graphicalPolicy;
    }

    public void setGraphicalPolicy(IGraphicalPolicy graphicalPolicy) {
        if (this.graphicalPolicy != null && graphicalPolicyActivated) {
            this.graphicalPolicy.deactivate(this);
            graphicalPolicyActivated = false;
        }
        this.graphicalPolicy = graphicalPolicy;
        if (hasFigure() && graphicalPolicy != null
                && !graphicalPolicyActivated) {
            graphicalPolicy.activate(this);
            graphicalPolicyActivated = true;
        }
    }

    public Object getRealModel() {
        return MindMapUtils.toRealModel(super.getModel());
    }

    protected void installModelListeners() {
        super.installModelListeners();
        Object source = getRealModel();
        if (eventRegister == null)
            eventRegister = new CoreEventRegister(source,
                    getCoreEventListener(source));
        registerCoreEvents(source, eventRegister);
    }

    protected ICoreEventListener getCoreEventListener(Object source) {
        final MindMapPartBase handler = this;
        return new ICoreEventListener() {
            public void handleCoreEvent(final CoreEvent event) {
                runInUI(new Runnable() {
                    public void run() {
                        handler.handleCoreEvent(event);
                    }
                });
            }
        };
    }

    protected void registerCoreEvents(Object source,
            ICoreEventRegister register) {
    }

    protected void handleCoreEvent(CoreEvent event) {
    }

    protected void runInUI(Runnable job) {
        runInUI(job, false);
    }

    protected void runInUI(Runnable job, boolean async) {
        Control control = getSite().getViewerControl();
        if (control == null || control.isDisposed()) {
            return;
        }

        Display display = control.getDisplay();
        if (display == null || display.isDisposed())
            return;
        if (async) {
            display.asyncExec(job);
        } else {
            display.syncExec(job);
        }
    }

    protected void uninstallModelListeners() {
        if (eventRegister != null) {
            eventRegister.unregisterAll();
            eventRegister = null;
        }
        super.uninstallModelListeners();
    }

    protected boolean isFigureAnimatable() {
        return false;
    }

    public IFigure getAnimatableFigure() {
        return getFigure();
    }

    public boolean isFigureAnimating() {
        IAnimationService anim = getAnimationService();
        if (anim != null) {
            return anim.isAnimating(getAnimatableFigure());
        }
        return false;
    }

    protected IAnimationService getAnimationService() {
        return (IAnimationService) getSite().getViewer()
                .getService(IAnimationService.class);
    }

    protected IFeedbackService getFeedbackService() {
        return (IFeedbackService) getSite().getViewer()
                .getService(IFeedbackService.class);
    }

    protected IShadowService getShadowService() {
        return (IShadowService) getSite().getViewer()
                .getService(IShadowService.class);
    }

    protected void onActivated() {
        super.onActivated();
        registerAnimatableFigure();
        addShadow();
        feedback = createFeedback();
        selectionFeedbackHelper = createSelectionFeedbackHelper();
        if (selectionFeedbackHelper != null) {
            selectionFeedbackHelper.setHost(this);
            selectionFeedbackHelper.setFeedbackService(getFeedbackService());
        }
    }

    protected void initFigure(IFigure figure) {
        super.initFigure(figure);
        if (graphicalPolicy != null && !graphicalPolicyActivated) {
            graphicalPolicy.activate(this);
            graphicalPolicyActivated = true;
        }
    }

    protected void onDeactivated() {
        if (graphicalPolicy != null && graphicalPolicyActivated) {
            graphicalPolicy.deactivate(this);
            graphicalPolicyActivated = false;
        }
        feedback = null;
        if (selectionFeedbackHelper != null) {
            selectionFeedbackHelper.setFeedbackService(null);
            selectionFeedbackHelper.setHost(null);
            selectionFeedbackHelper = null;
        }
        removeShadow();
        unregisterAnimatableFigure();
        super.onDeactivated();
    }

    protected void registerAnimatableFigure() {
        if (isFigureAnimatable()) {
            IAnimationService anim = getAnimationService();
            if (anim != null) {
                anim.registerFigure(getAnimatableFigure(), this);
            }
        }
    }

    protected void unregisterAnimatableFigure() {
        if (isFigureAnimatable()) {
            IAnimationService anim = getAnimationService();
            if (anim != null) {
                anim.unregisterFigure(getAnimatableFigure());
            }
        }
    }

    protected void addShadow() {
        IFigure source = getShadowSource();
        if (source != null) {
            IShadowService service = getShadowService();
            if (service != null) {
                service.addShadow(source);
            }
        }
    }

    protected void removeShadow() {
        IFigure source = getShadowSource();
        if (source != null) {
            IShadowService service = getShadowService();
            if (service != null) {
                service.removeShadow(source);
            }
        }
    }

    protected IFigure getShadowSource() {
        return null;
    }

    protected ISelectionFeedbackHelper createSelectionFeedbackHelper() {
        return null;
    }

    protected IFeedback createFeedback() {
        return null;
    }

    public ISelectionFeedbackHelper getSelectionFeedbackHelper() {
        return selectionFeedbackHelper;
    }

    public IFeedback getFeedback() {
        return feedback;
    }

    protected void updateView() {
        super.updateView();
        updateFeedback();
    }

    protected void updateFeedback() {
        if (selectionFeedbackHelper != null) {
            selectionFeedbackHelper.updateFeedback(false);
        }
    }

    public void setCacheManager(ICacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public ICacheManager getCacheManager() {
        if (cacheManager == null)
            cacheManager = new CacheManager(this);
        return cacheManager;
    }

    public <T> T getAdapter(Class<T> adapter) {
        if (adapter == IGraphicalPolicy.class)
            return adapter.cast(getGraphicalPolicy());
        if (adapter == IStyleSelector.class)
            return adapter.cast(getGraphicalPolicy().getStyleSelector(this));
        if (adapter == IStructure.class)
            return adapter.cast(getGraphicalPolicy().getStructure(this));
        if (adapter == ISelectionFeedbackHelper.class)
            return adapter.cast(getSelectionFeedbackHelper());
        if (adapter == IFeedback.class)
            return adapter.cast(getFeedback());
        if (adapter == ICacheManager.class)
            return adapter.cast(getCacheManager());
        return super.getAdapter(adapter);
    }

}
