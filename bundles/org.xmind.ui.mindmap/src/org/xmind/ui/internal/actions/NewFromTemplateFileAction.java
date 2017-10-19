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
package org.xmind.ui.internal.actions;

import java.io.FileInputStream;
import java.io.InputStream;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.xmind.core.util.FileUtils;

public class NewFromTemplateFileAction extends BaseNewFromTemplateAction {

    private String templateFile;

    public NewFromTemplateFileAction(IWorkbenchWindow window,
            String templateFile) {
        super(window);
        if (templateFile == null)
            throw new IllegalArgumentException();
        this.templateFile = templateFile;
        setText(FileUtils.getNoExtensionFileName(templateFile));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.ui.internal.actions.BaseNewFromTemplateAction#getTemplateStream
     * (org.eclipse.swt.widgets.Shell)
     */
    protected InputStream getTemplateStream(Shell shell) throws Exception {
        return new FileInputStream(templateFile);
    }

}