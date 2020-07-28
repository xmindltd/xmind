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
/**
 * 
 */
package org.xmind.ui.internal.handlers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.internal.E4PartWrapper;
import org.eclipse.ui.part.IContributedContentsView;
import org.xmind.core.style.IStyle;
import org.xmind.core.style.IStyleSheet;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.mindmap.IResourceManager;
import org.xmind.ui.mindmap.MindMapUI;

/**
 * Utilities for handling mind mapping commands.
 * 
 * @author Frank Shaka
 */
public class MindMapHandlerUtil {

    public static final int MATCH_ALL = 0;

    public static final int MATCH_READ_ONLY = 1;

    public static final int MATCH_MODIFIABLE = 1 << 1;

    /**
     * 
     */
    private MindMapHandlerUtil() {
        throw new AssertionError();
    }

    public static final IEditorPart getActivePartAsEditor(
            ExecutionEvent event) {
        IWorkbenchPart part = HandlerUtil.getActivePart(event);
        return part instanceof IEditorPart ? (IEditorPart) part : null;
    }

    /**
     * Finds the editor currently contributing contents to the active part.
     * 
     * @param event
     *            the handler context
     * @return an {@link IEditorPart} object, or <code>null</code> if not found
     */
    public static final IEditorPart findContributingEditor(
            ExecutionEvent event) {
        IWorkbenchPart part = HandlerUtil.getActivePart(event);
        if (part == null)
            return null;

        if (part instanceof IEditorPart)
            return (IEditorPart) part;

        if (part instanceof E4PartWrapper) {
            IEditorPart p = HandlerUtil.getActiveEditor(event);
            if (p instanceof IEditorPart)
                return (IEditorPart) p;
        }

        IContributedContentsView contributedView = MindMapUIPlugin
                .getAdapter(part, IContributedContentsView.class);
        if (contributedView != null) {
            IWorkbenchPart contributingPart = contributedView
                    .getContributingPart();
            if (contributingPart instanceof IEditorPart)
                return (IEditorPart) contributingPart;
        }

        return null;
    }

    /**
     * Finds the applicable style in the specified context.
     * 
     * @param event
     *            the handler context
     * @return an {@link IStyle} object, or <code>null</code> if not found
     */
    public static final IStyle findStyle(ExecutionEvent event) {
        return findStyle(event, MATCH_ALL);
    }

    /**
     * Finds the applicable style in the specified context matching the
     * specified policy.
     * 
     * @param event
     *            the handler context
     * @param match
     *            the matching policy
     * @return an {@link IStyle} object, or <code>null</code> if not found
     * @see #MATCH_ALL
     * @see #MATCH_READ_ONLY
     * @see #MATCH_MODIFIABLE
     */
    public static final IStyle findStyle(ExecutionEvent event, int match) {
        // Look for style URI in command parameters:
        String uri = event.getParameter(IMindMapCommandConstants.RESOURCE_URI);
        if (uri != null) {
            // Multiple URIs (separated by spaces/commas) are allowed:
            String[] uris = uri.split("\\s+|,"); //$NON-NLS-1$
            for (int i = 0; i < uris.length; i++) {
                Object resource = MindMapUI.getResourceManager()
                        .findResource(uris[i]);
                if (resource != null && resource instanceof IStyle
                        && matchStyle((IStyle) resource, match))
                    return (IStyle) resource;
            }
            // Can't find style for specified URI:
            return null;
        }

        // Look for style selection:
        ISelection selection = HandlerUtil.getCurrentSelection(event);
        if (selection == null || selection.isEmpty()
                || !(selection instanceof IStructuredSelection))
            return null;

        // Find the first style in selection:
        for (Object element : ((IStructuredSelection) selection).toList()) {
            if (element instanceof IStyle
                    && matchStyle((IStyle) element, match)) {
                return (IStyle) element;
            }
        }
        return null;
    }

    /**
     * Finds applicable styles in the specified context.
     * 
     * @param event
     *            the handler context
     * @return a list of {@link IStyle} objects (may be empty but never
     *         <code>null</code>)
     */
    public static final List<IStyle> findStyles(ExecutionEvent event) {
        return findStyles(event, MATCH_ALL);
    }

    /**
     * Finds applicable styles in the specified context matching the specified
     * policy.
     * 
     * @param event
     *            the handler context
     * @param match
     *            the matching policy
     * @return a list of {@link IStyle} objects (may be empty but never
     *         <code>null</code>)
     * @see #MATCH_ALL
     * @see #MATCH_READ_ONLY
     * @see #MATCH_MODIFIABLE
     */
    public static final List<IStyle> findStyles(ExecutionEvent event,
            int match) {
        List<IStyle> styles = new ArrayList<IStyle>();

        // Look for style URI in command parameters:
        String uri = event.getParameter(IMindMapCommandConstants.RESOURCE_URI);
        if (uri != null) {
            // Multiple URIs (separated by spaces/commas) are allowed:
            String[] uris = uri.split("\\s+|,"); //$NON-NLS-1$
            for (int i = 0; i < uris.length; i++) {
                Object resource = MindMapUI.getResourceManager()
                        .findResource(uris[i]);
                if (resource != null && resource instanceof IStyle
                        && matchStyle((IStyle) resource, match)) {
                    styles.add((IStyle) resource);
                }
            }
        } else {
            // Look for style selection:
            ISelection selection = HandlerUtil.getCurrentSelection(event);
            if (selection != null && !selection.isEmpty()
                    && selection instanceof IStructuredSelection) {
                for (Object element : ((IStructuredSelection) selection)
                        .toList()) {
                    if (element instanceof IStyle
                            && matchStyle((IStyle) element, match)) {
                        styles.add((IStyle) element);
                    }
                }
            }
        }
        return styles;
    }

    private static final boolean matchStyle(IStyle style, int match) {
        if (match == MATCH_ALL)
            return true;
        IStyleSheet parentSheet = style.getOwnedStyleSheet();
        IResourceManager rm = MindMapUI.getResourceManager();
        if ((match & MATCH_READ_ONLY) != 0
                && (parentSheet == rm.getDefaultStyleSheet()
                        || parentSheet == rm.getSystemStyleSheet()
                        || parentSheet == rm.getSystemThemeSheet()))
            return true;
        if ((match & MATCH_MODIFIABLE) != 0
                && (parentSheet == rm.getUserStyleSheet()
                        || parentSheet == rm.getUserThemeSheet()))
            return true;
        return false;
    }

}
