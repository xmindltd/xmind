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
package org.xmind.ui.internal;

import org.eclipse.core.runtime.Platform;
import org.xmind.gef.dnd.IDndSupport;
import org.xmind.gef.part.IPartFactory;
import org.xmind.gef.service.IPlaybackProvider;
import org.xmind.ui.IEditorHistory;
import org.xmind.ui.branch.IBranchPolicyManager;
import org.xmind.ui.decorations.IDecorationFactory;
import org.xmind.ui.decorations.IDecorationManager;
import org.xmind.ui.internal.branch.BranchPolicyManager;
import org.xmind.ui.internal.decorations.DecorationManager;
import org.xmind.ui.internal.editor.WorkbookRefManager;
import org.xmind.ui.internal.mindmap.EditorInputFactory;
import org.xmind.ui.internal.protocols.ProtocolManager;
import org.xmind.ui.mindmap.ICategoryManager;
import org.xmind.ui.mindmap.IEditPolicyManager;
import org.xmind.ui.mindmap.IEditorInputFactory;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.INumberFormatManager;
import org.xmind.ui.mindmap.INumberSeparatorManager;
import org.xmind.ui.mindmap.IProtocolManager;
import org.xmind.ui.mindmap.IResourceManager;
import org.xmind.ui.mindmap.IWorkbookRefManager;
import org.xmind.ui.prefs.PrefConstants;

/**
 * 
 * @author Frank Shaka
 */
@SuppressWarnings("deprecation")
public class InternalMindMapUI {

    private static InternalMindMapUI instance = null;

    private IPartFactory mindMapPartFactory = null;

    private IPartFactory mindMapTreePartFactory = null;

    private IDndSupport mindMapDndSupport = null;

    private IProtocolManager protocolManager = null;

    private IBranchPolicyManager branchPolicyManager = null;

    private IPlaybackProvider playbackProvider = null;

    private IResourceManager resourceManager = null;

    private DecorationManager decorationManager;

    private ICategoryManager categoryManager;

    private IEditPolicyManager editPolicyManager;

    private IMindMapImages images;

    private INumberFormatManager numberFormatManager;

    private INumberSeparatorManager numberSeparatorManager;

    private IEditorHistory editorHistory;

    /**
     * @deprecated
     */
    @Deprecated
    private IWorkbookRefManager workbookRefManager;

    private EditorInputFactory editorInputFactory;

    private InternalMindMapUI() {
    }

    public static InternalMindMapUI getDefault() {
        if (instance == null)
            instance = new InternalMindMapUI();
        return instance;
    }

    public IPartFactory getMindMapPartFactory() {
        if (mindMapPartFactory == null)
            mindMapPartFactory = new MindMapPartFactory();
        return mindMapPartFactory;
    }

    public IPartFactory getMindMapTreePartFactory() {
        if (mindMapTreePartFactory == null) {
            mindMapTreePartFactory = new MindMapTreePartFactory();
        }
        return mindMapTreePartFactory;
    }

    public IDndSupport getMindMapDndSupport() {
        if (mindMapDndSupport == null) {
            mindMapDndSupport = new MindMapDndSupport();
        }
        return mindMapDndSupport;
    }

    public IProtocolManager getProtocolManager() {
        if (protocolManager == null) {
            protocolManager = new ProtocolManager();
        }
        return protocolManager;
    }

    public IBranchPolicyManager getBranchPolicyManager() {
        if (branchPolicyManager == null) {
            branchPolicyManager = new BranchPolicyManager();
        }
        return branchPolicyManager;
    }

    public IPlaybackProvider getPlaybackProvider() {
        if (playbackProvider == null) {
            playbackProvider = new MindMapPlaybackProvider();
        }
        return playbackProvider;
    }

    public boolean isAnimationEnabled() {
        if (Platform.isRunning()) {
            return MindMapUIPlugin.getDefault().getPreferenceStore()
                    .getBoolean(PrefConstants.ANIMATION_ENABLED);
        }
        return false;
    }

    public boolean isGradientColorEnabled() {
        if (Platform.isRunning()) {
            return MindMapUIPlugin.getDefault().getPreferenceStore()
                    .getBoolean(PrefConstants.GRADIENT_COLOR);
        }
        return false;
    }

    public boolean isOverlapsAllowed() {
        if (Platform.isRunning()) {
            return MindMapUIPlugin.getDefault().getPreferenceStore()
                    .getBoolean(PrefConstants.OVERLAPS_ALLOWED);
        }
        return false;
    }

    public boolean isFreePositionMoveAllowed() {
        if (Platform.isRunning()) {
            return MindMapUIPlugin.getDefault().getPreferenceStore()
                    .getBoolean(PrefConstants.FREE_POSITION_ALLOWED);
        }
        return false;
    }

    public IResourceManager getResourceManager() {
        if (resourceManager == null) {
            resourceManager = new MindMapResourceManager();
        }
        return resourceManager;
    }

    public IDecorationManager getDecorationManager() {
        if (decorationManager == null) {
            decorationManager = new DecorationManager();
        }
        return decorationManager;
    }

    public IDecorationFactory getMindMapDecorationFactory() {
        if (decorationManager == null) {
            decorationManager = new DecorationManager();
        }
        return decorationManager;
    }

    public ICategoryManager getCategoryManager() {
        if (categoryManager == null) {
            categoryManager = new CategoryManager();
        }
        return categoryManager;
    }

    public IEditPolicyManager getEditPolicyManager() {
        if (editPolicyManager == null) {
            editPolicyManager = new EditPolicyManager();
        }
        return editPolicyManager;
    }

    /**
     * @deprecated
     */
    @Deprecated
    public IWorkbookRefManager getWorkbookRefManager() {
        if (workbookRefManager == null) {
            workbookRefManager = new WorkbookRefManager();
        }
        return workbookRefManager;
    }

    public IMindMapImages getImages() {
        if (images == null) {
            images = new MindMapImages();
        }
        return images;
    }

    public INumberFormatManager getNumberFormatManager() {
        if (numberFormatManager == null)
            numberFormatManager = new NumberFormatExtensionManager();
        return numberFormatManager;
    }

    public INumberSeparatorManager getNumberSeparatorManager() {
        if (numberSeparatorManager == null)
            numberSeparatorManager = new NumberSeparatorExtensionManager();
        return numberSeparatorManager;
    }

    public synchronized IEditorHistory getEditorHistory() {
        if (editorHistory == null)
            editorHistory = new EditorHistory();
        return editorHistory;
    }

    public IEditorInputFactory getEditorInputFactory() {
        if (editorInputFactory == null) {
            editorInputFactory = new EditorInputFactory();
        }
        return editorInputFactory;
    }

}