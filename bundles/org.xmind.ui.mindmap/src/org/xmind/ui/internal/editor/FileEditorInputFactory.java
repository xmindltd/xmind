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
package org.xmind.ui.internal.editor;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.Logger;

/**
 * 
 * @author Frank Shaka
 * @deprecated See {@link MindMapEditorInputFactory}
 */
public class FileEditorInputFactory implements IElementFactory {

    private static final String ID = "org.xmind.ui.WorkbookEditorInputFactory"; //$NON-NLS-1$

    private static final String TAG_PATH = "path"; //$NON-NLS-1$

    /**
     * For backward compatability with 3.0.0/1
     */
    private static final String TAG_RESOURCE_PATH = "resourcePath"; //$NON-NLS-1$

    public IAdaptable createElement(IMemento memento) {
        String path = memento.getString(TAG_PATH);
        if (path != null) {
            return MindMapUI.getEditorInputFactory()
                    .createEditorInputForFile(new File(path));
        }

        // For backward compatability
        path = memento.getString(TAG_RESOURCE_PATH);
        if (path != null) {
            try {
                return createResourceFileEditorInput(path);
            } catch (CoreException e) {
                Logger.log(e);
            }
        }
        return null;
    }

    /**
     * The 'resource path' is stored by XMind 3.0.0/1 for the sake of
     * representing an IFile object and create an old WorkbookEditorInput. Now
     * we take into account the creation of IFile and its corresponding editor
     * input ONLY when org.eclipse.ui.ide plugin exists in the runtime
     * environment.
     * 
     * @throws CoreException
     */
    private IEditorInput createResourceFileEditorInput(String resourcePath)
            throws CoreException {
        IFile file = ResourcesPlugin.getWorkspace().getRoot()
                .getFile(new Path(resourcePath));
        return MindMapUI.getEditorInputFactory()
                .createEditorInput(file.getLocationURI());
    }
}