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
package org.xmind.ui.wizards;

import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.ui.mindmap.IMindMap;
import org.xmind.ui.mindmap.IMindMapViewer;

public abstract class AbstractMindMapExportPage extends AbstractExportPage {

    protected static final String FILTER_ALL_FILES = "*.*"; //$NON-NLS-1$

    public AbstractMindMapExportPage(String pageName, String title) {
        super(pageName, title, null);
    }

    protected AbstractMindMapExportWizard getCastedWizard() {
        return (AbstractMindMapExportWizard) super.getCastedWizard();
    }

    protected IGraphicalEditor getSourceEditor() {
        return getCastedWizard().getSourceEditor();
    }

    protected IGraphicalEditorPage getSourcePage() {
        return getCastedWizard().getSourcePage();
    }

    protected IMindMapViewer getSourceViewer() {
        return getCastedWizard().getSourceViewer();
    }

    protected IMindMap getSourceMindMap() {
        return getCastedWizard().getSourceMindMap();
    }

    protected boolean hasSource() {
        return getCastedWizard().hasSource();
    }

    @Override
    protected boolean isPageCompletable() {
        return super.isPageCompletable() && hasSource();
    }

//    protected String generateWarningMessage() {
//        if (hasTargetPath()) {
////            if (!getCastedWizard().isExtensionCompatible(getTargetPath(),
////                    FileUtils.getExtension(getTargetPath()))) {
////                return String.format(
////                        WizardMessages.ExportPage_UncompatibleFormat_warning,
////                        getCastedWizard().getFormatName());
////            }
//            if (!getCastedWizard().isOverwriteWithoutPrompt()
//                    && new File(getTargetPath()).exists())
//                return WizardMessages.ExportPage_FileExists_message;
//        }
//        return null;
//    }

}