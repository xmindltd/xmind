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
package org.xmind.ui.internal.editor;

import java.io.File;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.xmind.core.IWorkbook;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.WorkbookFactory;

/**
 * This class is used to represent an existing workbook as an editor input.
 * 
 * @author Frank Shaka
 */
public class WorkbookEditorInput implements IEditorInput {

    private static int NUMBER = 0;

    private String name;

    private InputStream templateStream;

    private IWorkbook contents;

    /**
     */
    public WorkbookEditorInput() {
        this(WorkbookFactory.createEmptyWorkbook());
    }

    public WorkbookEditorInput(String name) {
        this.name = name;
    }

    public WorkbookEditorInput(String name, IWorkbook workbook) {
        this(name);
        this.contents = workbook;
    }

    public WorkbookEditorInput(String name, InputStream templateStream) {
        this(name);
        this.templateStream = templateStream;
    }

    /**
     * Create an editor input with a loaded workbook.
     * 
     * @param contents
     *            The loaded workbook
     */
    public WorkbookEditorInput(IWorkbook contents) {
        this((String) null);
        if (contents == null)
            throw new IllegalArgumentException();
        this.contents = contents;
    }

    /**
     * 
     * @param contents
     * @param targetFile
     * @deprecated Use {@link org.xmind.ui.internal.editor.FileEditorInput}
     */
    public WorkbookEditorInput(IWorkbook contents, String targetFile) {
        this(contents);
    }

    /**
     * 
     * @param contents
     * @param targetFile
     * @param initialDirty
     * @deprecated Use {@link org.xmind.ui.internal.editor.FileEditorInput}
     */
    public WorkbookEditorInput(IWorkbook contents, String targetFile,
            boolean initialDirty) {
        this(contents);
    }

    /**
     * 
     * @param contents
     * @param targetResourceFile
     * @deprecated Use
     *             {@link org.xmind.ui.internal.editor.FileEditorInputFactory#createResourceFileEditorInput(IFile)}
     */
    public WorkbookEditorInput(IWorkbook contents, IFile targetResourceFile) {
        this(contents);
    }

    public InputStream getTemplateStream() {
        return templateStream;
    }

    public boolean exists() {
        return false;
    }

    public ImageDescriptor getImageDescriptor() {
        return null;
    }

    public String getName() {
        if (name == null) {
            ++NUMBER;
            name = NLS.bind(MindMapMessages.WorkbookEditorInput_name, NUMBER);
        }
        return name;
    }

    public IPersistableElement getPersistable() {
        return null;
    }

    public String getToolTipText() {
        return getName();
    }

    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
        if (adapter == IWorkbook.class)
            return getContents();
        if (adapter == File.class) {
            if (contents != null) {
                String file = contents.getFile();
                if (file != null)
                    return new File(file);
            }
        }
        return Platform.getAdapterManager().getAdapter(this, adapter);
    }

    public IWorkbook getContents() {
        return contents;
    }

    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || !(obj instanceof WorkbookEditorInput))
            return false;
        WorkbookEditorInput that = (WorkbookEditorInput) obj;
        if (this.contents == null || that.contents == null)
            return false;
        return that.contents.equals(this.contents);
    }

}