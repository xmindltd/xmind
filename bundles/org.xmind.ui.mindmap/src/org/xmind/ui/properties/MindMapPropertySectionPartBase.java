/*
 * *****************************************************************************
 * * Copyright (c) 2006-2012 XMind Ltd. and others. This file is a part of XMind
 * 3. XMind releases 3 and above are dual-licensed under the Eclipse Public
 * License (EPL), which is available at
 * http://www.eclipse.org/legal/epl-v10.html and the GNU Lesser General Public
 * License (LGPL), which is available at http://www.gnu.org/licenses/lgpl.html
 * See https://www.xmind.net/license.html for details. Contributors: XMind Ltd. -
 * initial API and implementation
 *******************************************************************************/
package org.xmind.ui.properties;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.CoreEventRegister;
import org.xmind.core.event.ICoreEventListener;
import org.xmind.core.event.ICoreEventRegister;
import org.xmind.gef.EditDomain;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.ISourceProvider;
import org.xmind.gef.IViewer;
import org.xmind.gef.Request;
import org.xmind.gef.command.Command;
import org.xmind.gef.command.ICommandStack;
import org.xmind.gef.graphicalpolicy.IStyleSelector;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.gef.part.IPart;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.gef.ui.properties.PropertySectionPart;
import org.xmind.ui.style.StyleUtils;

public abstract class MindMapPropertySectionPartBase extends PropertySectionPart
        implements ICoreEventListener {

    protected static final Object[] NO_ELEMENTS = new Object[0];

    private ICoreEventRegister eventRegister = null;

    private boolean refreshing = false;

    protected boolean isRefreshing() {
        return refreshing;
    }

    public void refresh() {
        super.refresh();
        refreshing = true;
        doRefresh();
        refreshing = false;
    }

    protected abstract void doRefresh();

    protected IGraphicalEditorPage getActivePage() {
        return getContributedEditor().getActivePageInstance();
    }

    protected IGraphicalViewer getActiveViewer() {
        IGraphicalEditorPage editorPage = getActivePage();
        return editorPage == null ? null : editorPage.getViewer();
    }

    protected EditDomain getActiveDomain() {
        IGraphicalEditorPage editorPage = getActivePage();
        return editorPage == null ? null : editorPage.getEditDomain();
    }

    protected IPart getPart(Object o) {
        IViewer viewer = getActiveViewer();
        return viewer == null ? null : viewer.findPart(o);
    }

    protected IGraphicalPart getGraphicalPart(Object o) {
        return getGraphicalPart(o, getActiveViewer());
    }

    protected IGraphicalPart getGraphicalPart(Object o, IViewer viewer) {
        if (viewer == null)
            return null;
        IPart p = viewer.findPart(o);
        return p instanceof IGraphicalPart ? (IGraphicalPart) p : null;
    }

    protected IStyleSelector getStyleSelector(IGraphicalPart part) {
        return StyleUtils.getStyleSelector(part);
    }

    protected void saveAndRun(Command command) {
        ICommandStack cs = getContributedEditor().getCommandStack();
        if (cs != null) {
            cs.execute(command);
            if (command instanceof ISourceProvider) {
                List<Object> sources = ((ISourceProvider) command).getSources();
                getContributedEditor().getSite().getSelectionProvider()
                        .setSelection(new StructuredSelection(sources));
            }
        }
    }

    protected void sendRequest(String request) {
        EditDomain domain = getActiveDomain();
        if (domain != null) {
            domain.handleRequest(request, getActiveViewer());
        }
    }

    protected void sendRequest(Request request) {
        if (request == null)
            return;
        EditDomain domain = getActiveDomain();
        if (domain != null) {
            domain.handleRequest(request);
        }
    }

    protected Request fillTargets(Request request) {
        IViewer viewer = getActiveViewer();
        if (viewer != null) {
            request.setViewer(viewer);
            request.setDomain(viewer.getEditDomain());
            Object[] elements = getSelectedElements();
            List<IPart> parts = new ArrayList<IPart>(elements.length);
            for (Object o : elements) {
                IPart p = viewer.findPart(o);
                if (p != null && !parts.contains(p)
                        && isRequestTarget(request, p))
                    parts.add(p);
            }
            if (!parts.isEmpty()) {
                request.setTargets(parts);
            }
        }
        return request;
    }

    protected boolean isRequestTarget(Request request, IPart part) {
        return true;
    }

    protected Object[] getSelectedElements() {
        if (getCurrentSelection() != null
                && getCurrentSelection() instanceof IStructuredSelection) {
            return ((IStructuredSelection) getCurrentSelection()).toArray();
        }
        return NO_ELEMENTS;
    }

    protected void hookSelection(ISelection selection) {
        super.hookSelection(selection);
        for (Object source : getSelectedElements()) {
            if (eventRegister == null)
                eventRegister = new CoreEventRegister(this);
            eventRegister.setNextSourceFrom(source);
            registerEventListener(source, eventRegister);
        }
    }

    public void createControl(Composite parent) {
        parent.setLayout(createLayout(parent));
        createContent(parent);
    }

    protected GridLayout createLayout(Composite parent) {
        GridLayout layout = new GridLayout(1, true);
        layout.marginWidth = 3;
        layout.marginHeight = 3;
        layout.horizontalSpacing = 3;
        layout.verticalSpacing = 3;
        return layout;
    }

    protected abstract void createContent(Composite parent);

    protected abstract void registerEventListener(Object source,
            ICoreEventRegister register);

    protected void unhookSelection(ISelection selection) {
        if (eventRegister != null) {
            eventRegister.unregisterAll();
            eventRegister = null;
        }
        super.unhookSelection(selection);
    }

    public void handleCoreEvent(final CoreEvent event) {
        if (getContainer().getContainerSite() != null) {
            getContainer().getContainerSite().getWorkbenchWindow()
                    .getWorkbench().getDisplay().syncExec(new Runnable() {
                        public void run() {
                            refresh();
                        }
                    });
        } else {
            PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {

                @Override
                public void run() {
                    refresh();
                }
            });
        }
    }
}
