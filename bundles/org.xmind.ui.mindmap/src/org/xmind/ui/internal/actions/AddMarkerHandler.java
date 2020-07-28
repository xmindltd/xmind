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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.HandlerEvent;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.xmind.gef.ui.actions.IActionRegistry;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;

/**
 * 
 * @author frankshaka
 * @deprecated Use {@link org.xmind.ui.internal.handlers.AddMarkerHandler}
 *             instead
 */
@Deprecated
public class AddMarkerHandler extends AbstractHandler {

    private static final String COMMAND_ID = "org.xmind.ui.command.addMarker"; //$NON-NLS-1$

    private static final String PARAM_MARKER_ID = "org.xmind.ui.mindmap.markerId"; //$NON-NLS-1$

    private AddMarkerAction action;

    private IHandlerActivation activation;

    /**
     * The property change listener hooked on to the action. This is initialized
     * when the action is set on this handler, and is removed when the handler
     * is disposed or the action is no longer used by this handler.
     */
    private IPropertyChangeListener propertyChangeListener;

    public Object execute(ExecutionEvent event) throws ExecutionException {
        if (action != null) {
            String markerId = event.getParameter(PARAM_MARKER_ID);
            if (markerId != null) {
                try {
                    action.runWithMarkerId(markerId);
                } catch (Throwable e) {
                    throw new ExecutionException(
                            "Error occured while executing Add Marker command.", //$NON-NLS-1$
                            e);
                }
            }
        }
        return null;
    }

    public void activate(IHandlerService service) {
        if (activation == null) {
            activation = service.activateHandler(COMMAND_ID, this);
        }
    }

    public void deactivate(IHandlerService service) {
        if (activation != null) {
            service.deactivateHandler(activation);
            activation = null;
        }
    }

    public void setActivatePage(IGraphicalEditorPage page) {
        if (page != null) {
            IActionRegistry actionRegistry = (IActionRegistry) page
                    .getAdapter(IActionRegistry.class);
            if (actionRegistry != null) {
                IAction action = actionRegistry
                        .getAction(ActionConstants.ADD_MARKER_ACTION_ID);
                if (action instanceof AddMarkerAction) {
                    setAction((AddMarkerAction) action);
                    return;
                }
            }
        }
        setAction(null);
    }

    public boolean isEnabled() {
        return action != null && action.isEnabled();
    }

    public boolean isHandled() {
        return action != null;
    }

    private void setAction(AddMarkerAction action) {
        if (action == this.action)
            return;

        unhookAction();
        boolean oldEnabled = isEnabled();
        boolean oldHandled = isHandled();

        this.action = action;

        boolean newEnabled = isEnabled();
        boolean newHandled = isHandled();
        hookAction();

        boolean enabledChanged = oldEnabled != newEnabled;
        boolean handledChanged = oldHandled != newHandled;
        if (enabledChanged || handledChanged) {
            fireHandlerChanged(
                    new HandlerEvent(this, enabledChanged, handledChanged));
        }
    }

    public void dispose() {
        unhookAction();
        super.dispose();
    }

    private void hookAction() {
        if (action == null)
            return;

        if (propertyChangeListener == null) {
            propertyChangeListener = new IPropertyChangeListener() {
                public final void propertyChange(
                        final PropertyChangeEvent propertyChangeEvent) {
                    final String property = propertyChangeEvent.getProperty();
                    fireHandlerChanged(new HandlerEvent(AddMarkerHandler.this,
                            IAction.ENABLED.equals(property),
                            IAction.HANDLED.equals(property)));
                }
            };
        }
        action.addPropertyChangeListener(propertyChangeListener);
    }

    private void unhookAction() {
        if (action == null)
            return;

        if (propertyChangeListener != null) {
            action.removePropertyChangeListener(propertyChangeListener);
            propertyChangeListener = null;
        }
    }

}