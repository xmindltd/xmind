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
package org.xmind.ui.internal.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.xmind.core.ITopic;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.part.IPart;
import org.xmind.gef.ui.actions.IActionRegistry;
import org.xmind.gef.ui.actions.ISelectionAction;
import org.xmind.gef.ui.actions.PageAction;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;

public abstract class SelectionRetargetAction extends PageAction implements
        ISelectionAction, IPropertyChangeListener {

    private IAction handler = null;

    private String oldText = null;

    private String oldToolTipText = null;

    private ImageDescriptor oldImage = null;

    private ImageDescriptor oldDisabledImage = null;

    private ImageDescriptor oldHoverImage = null;

    private boolean oldEnabled = false;

    protected SelectionRetargetAction(IGraphicalEditorPage page) {
        super(page);
    }

    protected SelectionRetargetAction(String id, IGraphicalEditorPage page) {
        super(id, page);
    }

    public void run() {
        if (isDisposed())
            return;

        if (handler != null) {
            handler.run();
        } else {
            runWithNoHandler();
        }
    }

    protected abstract void runWithNoHandler();

    protected void setHandler(IAction handler) {
        if (handler != this.handler) {
            if (this.handler != null) {
                this.handler.removePropertyChangeListener(this);
            }
            if (handler != null) {
                handler.addPropertyChangeListener(this);
            }
        }
        this.handler = handler;
        update(null);
    }

    public void setText(String text) {
        this.oldText = text;
        update(TEXT);
    }

    public void setToolTipText(String toolTipText) {
        this.oldToolTipText = toolTipText;
        update(TOOL_TIP_TEXT);
    }

    public void setImageDescriptor(ImageDescriptor newImage) {
        this.oldImage = newImage;
        update(IMAGE);
    }

    public void setDisabledImageDescriptor(ImageDescriptor newImage) {
        this.oldDisabledImage = newImage;
        update(IMAGE);
    }

    public void setHoverImageDescriptor(ImageDescriptor newImage) {
        this.oldHoverImage = newImage;
        update(IMAGE);
    }

    public void setEnabled(boolean enabled) {
        this.oldEnabled = enabled;
        update(ENABLED);
    }

    protected void doSetText(String text) {
        super.setText(text);
    }

    protected void doSetToolTipText(String toolTipText) {
        super.setToolTipText(toolTipText);
    }

    protected void doSetImageDescriptor(ImageDescriptor image) {
        super.setImageDescriptor(image);
    }

    protected void doSetDisabledImageDescriptor(ImageDescriptor image) {
        super.setDisabledImageDescriptor(image);
    }

    protected void doSetHoverImageDescriptor(ImageDescriptor image) {
        super.setHoverImageDescriptor(image);
    }

    protected void doSetEnabled(boolean enabled) {
        super.setEnabled(enabled);
    }

    protected void update(String id) {
        boolean textChanged = id == null || TEXT.equals(id);
        boolean tooltipChanged = id == null || TOOL_TIP_TEXT.equals(id);
        boolean imageChanged = id == null || IMAGE.equals(id);
        boolean enabledChanged = id == null || ENABLED.equals(id);

        if (textChanged) {
            doSetText(handler == null ? oldText : handler.getText());
        }
        if (tooltipChanged) {
            doSetToolTipText(handler == null ? oldToolTipText : handler
                    .getToolTipText());
        }
        if (imageChanged) {
            doSetImageDescriptor(handler == null ? oldImage : handler
                    .getImageDescriptor());
            doSetDisabledImageDescriptor(handler == null ? oldDisabledImage
                    : handler.getDisabledImageDescriptor());
            doSetHoverImageDescriptor(handler == null ? oldHoverImage : handler
                    .getHoverImageDescriptor());
        }
        if (enabledChanged) {
            doSetEnabled(handler == null ? oldEnabled : handler.isEnabled());
        }
    }

    protected IAction getHandler() {
        return handler;
    }

    public void setSelection(ISelection selection) {
        setHandler(findHandler(selection));
    }

    private IAction findHandler(ISelection selection) {
        if (selection == null || selection.isEmpty())
            return null;

        if (selection instanceof IStructuredSelection) {
            IStructuredSelection ss = (IStructuredSelection) selection;
            if (ss.size() != 1)
                return null;

            Object o = ss.getFirstElement();
            if (o instanceof ITopic) {
                IGraphicalViewer viewer = getViewer();
                if (viewer == null)
                    return null;

                IPart part = viewer.findPart(o);
                if (part == null)
                    return null;

                IActionRegistry actionRegistry = (IActionRegistry) part
                        .getAdapter(IActionRegistry.class);
                if (actionRegistry == null)
                    return null;

                IAction action = actionRegistry.getAction(getHandlerId());
                if (action == this)
                    return null;
                return action;
            }
        }
        return null;
    }

    protected abstract String getHandlerId();

    public void propertyChange(PropertyChangeEvent event) {
        update(event.getProperty());
    }

}