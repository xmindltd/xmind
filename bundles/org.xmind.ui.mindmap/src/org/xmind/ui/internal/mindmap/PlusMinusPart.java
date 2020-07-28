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

import org.eclipse.draw2d.IFigure;
import org.xmind.core.ITopic;
import org.xmind.gef.GEF;
import org.xmind.gef.IViewer;
import org.xmind.gef.part.IPart;
import org.xmind.gef.part.IRequestHandler;
import org.xmind.gef.part.IRootPart;
import org.xmind.gef.policy.NullEditPolicy;
import org.xmind.ui.internal.decorators.PlusMinusDecorator;
import org.xmind.ui.internal.figures.PlusMinusFigure;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.IPlusMinusPart;
import org.xmind.ui.mindmap.ISelectionFeedbackHelper;

public class PlusMinusPart extends MindMapPartBase implements IPlusMinusPart {

    private PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {

        public void propertyChange(PropertyChangeEvent evt) {
            updateView();
        }
    };

    public PlusMinusPart() {
        setDecorator(PlusMinusDecorator.getInstance());
    }

    @Override
    protected void onActivated() {
        super.onActivated();
        if (getViewer() != null) {
            getViewer().getProperties().addPropertyChangeListener(
                    propertyChangeListener);
        }
    }

    @Override
    protected void onDeactivated() {
        getViewer().getProperties().removePropertyChangeListener(
                propertyChangeListener);
        super.onDeactivated();
    }

    private IViewer getViewer() {
        IPart parent = getParent();
        while (parent != null && !(parent instanceof IRootPart)) {
            parent = parent.getParent();
        }
        if (parent != null) {
            return ((IRootPart) parent).getViewer();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
        if (adapter.isAssignableFrom(ITopic.class))
            return getTopic();
        return super.getAdapter(adapter);
    }

    public ITopic getTopic() {
        return (ITopic) getRealModel();
    }

    public IBranchPart getOwnerBranch() {
        if (getParent() instanceof IBranchPart)
            return (IBranchPart) getParent();
        return null;
    }

    protected IFigure createFigure() {
        return new PlusMinusFigure();
    }

    public void setParent(IPart parent) {
        if (getParent() instanceof BranchPart) {
            BranchPart branch = (BranchPart) getParent();
            if (branch.getPlusMinus() == this) {
                branch.setPlusMinus(null);
            }
        }
        super.setParent(parent);
        if (getParent() instanceof BranchPart) {
            ((BranchPart) getParent()).setPlusMinus(this);
        }
    }

    protected void declareEditPolicies(IRequestHandler reqHandler) {
        super.declareEditPolicies(reqHandler);
        reqHandler.installEditPolicy(GEF.ROLE_SELECTABLE, NullEditPolicy
                .getInstance());
    }

//    protected void uninstallPolicies(IRequestProcessor reqProc) {
//        reqProc.uninstallPolicy(GEF.ROLE_SELECTABLE);
//        super.uninstallPolicies(reqProc);
//    }

    protected ISelectionFeedbackHelper createSelectionFeedbackHelper() {
        return new PlusMinusSelectionHelper();
    }

}