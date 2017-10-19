package org.xmind.ui.internal.dashboard.pages;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IEditorInput;

public interface IDashboardContext extends IAdaptable {

    /**
     * Hides the dashboard explicitly.
     */
    void hideDashboard();

    /**
     * Allows the command specified by the given id to be enabled via normal
     * mechanism. Commands that are not registered via this method will be
     * forcibly set to disabled, no matter whether there are active handlers
     * available.
     * 
     * @param commandId
     *            the command id to set enabled
     */
    void registerAvailableCommandId(String commandId);

    /**
     * Registers the popup menu specified by the given id within the dashboard.
     * Registered menus will be extended with menu contributions.
     * 
     * @param menuParent
     *            the parent for the context menu, e.g. a Control in SWT
     * @param menuId
     *            the id of the context menu to use
     * @return <code>true</code> if registration succeeded else
     *         <code>false</code>
     */
    boolean registerContextMenu(Object menuParent, String menuId);

    /**
     * Opens a new editor for the given input in the current window.
     * 
     * @param input
     *            the input to edit
     * @param editorId
     *            the editor id specifying which type of editor to use
     */
    boolean openEditor(IEditorInput input, String editorId);

    /**
     * Opens a new view in the current window.
     * 
     * @param viewId
     *            the view id specifying which type of view to open
     */
    boolean showView(String viewId);

    /**
     * Retrieves a dashboard state string associated with the given key that was
     * set by previous calls to {@link #setPersistedState(String, String)}.
     * Persisted states are preserved across program sessions.
     * 
     * @param key
     *            a string that should NEVER be <code>null</code>
     * @return a string associated with the key, or <code>null</code> if no such
     *         value is available
     * @throws IllegalArgumentException
     *             if the key is <code>null</code>
     */
    String getPersistedState(String key);

    /**
     * Sets a dashboard state string to be associated with the given key.
     * Persisted states are preserved across program sessions.
     * 
     * @param key
     *            a string that should NEVER be <code>null</code>
     * @param value
     *            a string, or <code>null</code> to clear previously set values
     *            associated with the given key
     * @throws IllegalArgumentException
     *             if the key is <code>null</code>
     */
    void setPersistedState(String key, String value);

    /**
     * @param key
     * @return
     */
    Object getContextVariable(String key);

    /**
     * @param key
     * @return
     */
    <T> T getContextVariable(Class<T> key);

    void setSelectionProvider(ISelectionProvider selectionProvider);

}
