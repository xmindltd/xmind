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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Set;

import org.eclipse.draw2d.IFigure;
import org.xmind.core.Core;
import org.xmind.core.ITopic;
import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.ICoreEventRegister;
import org.xmind.gef.GEF;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.draw2d.RotatableWrapLabel;
import org.xmind.gef.part.IPart;
import org.xmind.gef.part.IRequestHandler;
import org.xmind.gef.policy.NullEditPolicy;
import org.xmind.ui.internal.decorators.LabelDecorator;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.ILabelPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.MindMapUtils;

public class LabelPart extends MindMapPartBase implements ILabelPart,
        PropertyChangeListener {

    private String labelText = null;

    public LabelPart() {
        setDecorator(LabelDecorator.getInstance());
    }

    protected IFigure createFigure() {
        boolean useAdvancedRenderer = getSite().getViewer().getProperties()
                .getBoolean(IGraphicalViewer.VIEWER_RENDER_TEXT_AS_PATH, false);
        return new RotatableWrapLabel(
                useAdvancedRenderer ? RotatableWrapLabel.ADVANCED
                        : RotatableWrapLabel.NORMAL);
    }

    public String getLabelText() {
        if (labelText == null) {
            labelText = buildLabelText();
        }
        return labelText;
    }

    private String buildLabelText() {
        return MindMapUtils.getLabelText(getLabels());
    }

    public Set<String> getLabels() {
        return getTopic().getLabels();
    }

    public IBranchPart getOwnedBranch() {
        if (getParent() instanceof IBranchPart)
            return ((IBranchPart) getParent());
        return null;
    }

    public ITopic getTopic() {
        return (ITopic) super.getRealModel();
    }

    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
        if (adapter.isAssignableFrom(ITopic.class))
            return getTopic();
        return super.getAdapter(adapter);
    }

    public void setParent(IPart parent) {
        if (getParent() instanceof BranchPart) {
            BranchPart branch = (BranchPart) getParent();
            if (branch.getLabel() == this) {
                branch.setLabel(null);
            }
        }
        super.setParent(parent);
        if (getParent() instanceof BranchPart) {
            BranchPart branch = (BranchPart) getParent();
            branch.setLabel(this);
        }
    }

    protected void declareEditPolicies(IRequestHandler reqHandler) {
        super.declareEditPolicies(reqHandler);
        reqHandler.installEditPolicy(GEF.ROLE_EDITABLE,
                NullEditPolicy.getInstance());
        reqHandler.installEditPolicy(GEF.ROLE_NAVIGABLE,
                MindMapUI.POLICY_TOPIC_NAVIGABLE);

    }

    protected void registerCoreEvents(Object source, ICoreEventRegister register) {
        super.registerCoreEvents(source, register);
        register.register(Core.Labels);
    }

    public void handleCoreEvent(CoreEvent event) {
        String type = event.getType();
        if (Core.Labels.equals(type)) {
            runInUI(new Runnable() {
                public void run() {
                    labelText = null;
                    update();
                }
            });
        } else {
            super.handleCoreEvent(event);
        }
    }

    protected void onActivated() {
        super.onActivated();
        getSite()
                .getViewer()
                .getProperties()
                .addPropertyChangeListener(
                        IGraphicalViewer.VIEWER_RENDER_TEXT_AS_PATH, this);
    }

    protected void onDeactivated() {
        getSite()
                .getViewer()
                .getProperties()
                .removePropertyChangeListener(
                        IGraphicalViewer.VIEWER_RENDER_TEXT_AS_PATH, this);
        super.onDeactivated();
    }

    public void propertyChange(PropertyChangeEvent evt) {
        boolean useAdvancedRenderer = getSite().getViewer().getProperties()
                .getBoolean(IGraphicalViewer.VIEWER_RENDER_TEXT_AS_PATH, false);
        ((RotatableWrapLabel) getFigure())
                .setRenderStyle(useAdvancedRenderer ? RotatableWrapLabel.ADVANCED
                        : RotatableWrapLabel.NORMAL);
    }

}