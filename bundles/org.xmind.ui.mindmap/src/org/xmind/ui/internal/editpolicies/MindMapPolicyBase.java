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
package org.xmind.ui.internal.editpolicies;

import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.xmind.gef.IViewer;
import org.xmind.gef.Request;
import org.xmind.gef.command.Command;
import org.xmind.gef.part.IPart;
import org.xmind.gef.policy.AbstractEditPolicy;
import org.xmind.gef.service.IAnimationService;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.MindMapUtils;

public abstract class MindMapPolicyBase extends AbstractEditPolicy {

    protected static final String EMPTY = ""; //$NON-NLS-1$

    protected IAnimationService getAnimationService(IViewer viewer) {
        return (IAnimationService) viewer.getService(IAnimationService.class);
    }

    protected void select(Object element, IViewer viewer) {
        ISelection selection = element == null ? StructuredSelection.EMPTY
                : new StructuredSelection(element);

        viewer.setSelection(selection, true);
    }

    protected void select(List<?> elements, IViewer viewer) {
        viewer.setSelection(new StructuredSelection(elements), true);
    }

    protected void selectPart(IPart part, IViewer viewer) {
        select(MindMapUtils.getRealModel(part), viewer);
    }

    protected void selectParts(List<? extends IPart> parts, IViewer viewer) {
        select(MindMapUtils.getRealModels(parts), viewer);
    }

    protected boolean isAnimationRequired(Request req) {
        return req.isParameter(MindMapUI.PARAM_WITH_ANIMATION);
    }

    protected boolean isAnimationEnalbed(IViewer viewer) {
        return MindMapUI.isAnimationEnabled()
                && viewer.hasService(IAnimationService.class);
    }

    protected boolean animateCommand(Command cmd, IViewer viewer) {
        if (!isAnimationEnalbed(viewer))
            return false;
        IAnimationService anim = getAnimationService(viewer);
        if (anim == null)
            return false;
        anim.stop();
        doAnimateCommand(cmd, anim, viewer);
        return true;
    }

    protected void doAnimateCommand(final Command cmd, IAnimationService anim,
            final IViewer viewer) {
        Runnable job = new Runnable() {
            public void run() {
                createAnimation(cmd, viewer);
            }
        };
        anim.start(job, createBeforeEffect(cmd, viewer), createAfterEffect(cmd,
                viewer));
    }

    protected Runnable createBeforeEffect(Command cmd, IViewer viewer) {
        return null;
    }

    protected Runnable createAfterEffect(Command cmd, IViewer viewer) {
        return null;
    }

    protected void createAnimation(Command cmd, IViewer viewer) {
        saveAndRun(cmd, viewer.getEditDomain());
    }

}