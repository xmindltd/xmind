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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.xmind.core.util.FileUtils;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.ui.internal.dialogs.DialogUtils;
import org.xmind.ui.internal.wizards.UncompletablePage;
import org.xmind.ui.internal.wizards.WizardMessages;
import org.xmind.ui.io.MonitoredOutputStream;
import org.xmind.ui.mindmap.IMindMap;
import org.xmind.ui.mindmap.IMindMapViewer;
import org.xmind.ui.util.Logger;
import org.xmind.ui.util.MindMapUtils;

public abstract class AbstractMindMapExportWizard extends AbstractExportWizard {

    private static final int MAX_FILE_NAME_LENGTH = 120;

    private IGraphicalEditor sourceEditor;

    private IGraphicalEditorPage sourcePage;

    private IMindMapViewer sourceViewer;

    private IMindMap sourceMindMap;

    public AbstractMindMapExportWizard() {
        setNeedsProgressMonitor(true);
    }

    public void init(IWorkbench workbench, IStructuredSelection selection) {
        initSources(workbench);
        super.init(workbench, selection);
    }

    protected void initSources(IWorkbench workbench) {
        IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
        IEditorPart activeEditor = window.getActivePage().getActiveEditor();
        if (activeEditor instanceof IGraphicalEditor) {
            IGraphicalEditor editor = (IGraphicalEditor) activeEditor;
            IGraphicalEditorPage page = editor.getActivePageInstance();
            if (page != null) {
                IGraphicalViewer viewer = page.getViewer();
                if (viewer instanceof IMindMapViewer) {
                    IMindMapViewer mmv = (IMindMapViewer) viewer;
                    IMindMap mindMap = mmv.getMindMap();
                    if (mindMap != null) {
                        setSourceMindMap(mindMap);
                        setSourceViewer(mmv);
                        setSourcePage(page);
                        setSourceEditor(editor);
                        return;
                    }
                }
            }
        }
        setSourceViewer(null);
        setSourcePage(null);
        setSourceEditor(null);
    }

    public void dispose() {
        setSourceEditor(null);
        setSourcePage(null);
        setSourceViewer(null);
        super.dispose();
    }

    public void addPages() {
        if (hasSource()) {
            addValidPages();
        } else {
            UncompletablePage errorPage = new UncompletablePage(
                    WizardMessages.NoContentPage_title,
                    WizardMessages.NoContentPage_message);
            errorPage.setDescription(WizardMessages.NoContentPage_description);
            addPage(errorPage);
        }
    }

    protected abstract void addValidPages();

    public boolean canFinish() {
        return super.canFinish() && hasSource();
    }

    public boolean performFinish() {
        if (!hasSource() || !hasTargetPath())
            return false;
        if (!isLegalPath(getTargetPath())) {
            MessageDialog.openInformation(getShell(),
                    WizardMessages.ExportPage_FindFileFail_title,
                    WizardMessages.ExportPage_FindFileFail_message);
            return false;
        }
        if (!isExtensionCompatible(getTargetPath(),
                FileUtils.getExtension(getTargetPath()))) {
            String fileName = new File(getTargetPath()).getName();
            String formatName = getFormatName();
            String messages = NLS.bind(
                    WizardMessages.Export_UncompatibleFormat_message, fileName,
                    formatName);
            if (!MessageDialog.openConfirm(getShell(),
                    WizardMessages.Export_UncompatibleFormat_title, messages))
                return false;
        }
        setTargetPath(trimPath(getTargetPath()));

        if (!isOverwriteWithoutPrompt() && new File(getTargetPath()).exists()) {
            if (!DialogUtils.confirmOverwrite(getShell(), getTargetPath()))
                return false;
        }
        return doExport();
    }

    private boolean isLegalPath(String targetPath) {
        boolean isLagel = true;
        String path = targetPath.substring(0,
                targetPath.lastIndexOf(File.separator));
        File directory = new File(path);
        if (!directory.exists() || !directory.isDirectory()) {
            isLagel = false;
            return isLagel;
        }
        String fileName = targetPath
                .substring(targetPath.lastIndexOf(File.separator) + 1);
        @SuppressWarnings("nls")
        String[] letters = new String[] { "/", ":", "\\", "*", "?", "\"", "<",
                ">", "|" };
        for (int i = 0; i < letters.length; i++) {
            if (fileName.indexOf(letters[i]) != -1) {
                isLagel = false;
                return isLagel;
            }
        }
        return isLagel;
    }

    private String trimPath(String path) {
        String dirPath = path.substring(0,
                path.lastIndexOf(File.separator) + 1);
        String fileName = FileUtils.getNoExtensionFileName(path);

        if (fileName.length() > MAX_FILE_NAME_LENGTH) {
            fileName = fileName.substring(0, MAX_FILE_NAME_LENGTH);
        }
        return dirPath + fileName + FileUtils.getExtension(path);
    }

    protected boolean doExport() {
        final Display display = Display.getCurrent();
        final Shell parentShell = findParentShell(display);

        try {
            getContainer().run(true, true, new IRunnableWithProgress() {
                public void run(IProgressMonitor monitor)
                        throws InvocationTargetException, InterruptedException {
                    try {
                        doExport(monitor, display, parentShell);
                    } catch (OutOfMemoryError e) {
                        try {
                            throw new Exception(
                                    WizardMessages.ImageTooLarge_Error, e);
                        } catch (Exception e2) {
                            throw new InvocationTargetException(e2);
                        }
                    }
                }
            });
            return true;
        } catch (Throwable e) {
            if (e instanceof InterruptedException
                    || e instanceof InterruptedIOException) {
                return false;
            }
            while (e instanceof InvocationTargetException) {
                Throwable t = ((InvocationTargetException) e).getCause();
                if (t == null)
                    break;
                e = t;
            }
            final Throwable ex = e;
            display.asyncExec(new Runnable() {

                public void run() {
                    handleExportException(ex);
                }

            });
        }
        return false;
    }

    protected void handleExportException(Throwable e) {
        Logger.log(e, NLS.bind(WizardMessages.Export_FailedWhenExport,
                getFormatName()));
        e.printStackTrace();

        if (e instanceof FileNotFoundException) {
            MessageDialog.openInformation(getShell(),
                    WizardMessages.ExportPage_FindFileFail_title,
                    WizardMessages.ExportPage_FindFileFail_message);
        }
    }

    private Shell findParentShell(Display display) {
        Shell shell = getContainer().getShell();
        if (shell != null) {
            Composite shellParent = shell.getParent();
            if (shellParent instanceof Shell) {
                Shell parentShell = (Shell) shellParent;
                if (!parentShell.isDisposed()
                        && parentShell.getDisplay() == display)
                    return parentShell;
            }
        }
        return null;
    }

    protected abstract void doExport(IProgressMonitor monitor, Display display,
            Shell parentShell)
            throws InvocationTargetException, InterruptedException;

    protected OutputStream wrapMonitor(OutputStream realStream,
            IProgressMonitor monitor) {
        return new MonitoredOutputStream(realStream, monitor);
    }

    public IGraphicalEditor getSourceEditor() {
        return sourceEditor;
    }

    public IGraphicalEditorPage getSourcePage() {
        return sourcePage;
    }

    public IMindMapViewer getSourceViewer() {
        return sourceViewer;
    }

    public void setSourceEditor(IGraphicalEditor sourceEditor) {
        this.sourceEditor = sourceEditor;
    }

    public void setSourcePage(IGraphicalEditorPage sourcePage) {
        this.sourcePage = sourcePage;
    }

    public void setSourceViewer(IMindMapViewer sourceViewer) {
        this.sourceViewer = sourceViewer;
    }

    public IMindMap getSourceMindMap() {
        return sourceMindMap;
    }

    public void setSourceMindMap(IMindMap sourceMindMap) {
        this.sourceMindMap = sourceMindMap;
    }

    public boolean hasSource() {
        return sourceEditor != null && sourcePage != null
                && sourceViewer != null && sourceViewer.getInput() != null
                && sourceMindMap != null;
    }

    protected abstract String getFormatName();

    protected boolean isExtensionCompatible(String path, String extension) {
        return true;
    }

    @Override
    protected String getSuggestedFileName() {
        IMindMap mindMap = getSourceMindMap();
        if (mindMap == null)
            return WizardMessages.ExportWizard_SuggestedFileName;
        String fileName = mindMap.getCentralTopic().getTitleText();
        String replacedFileName = MindMapUtils.trimFileName(fileName);
        if (replacedFileName.length() > MAX_FILE_NAME_LENGTH) {
            replacedFileName = replacedFileName.substring(0,
                    MAX_FILE_NAME_LENGTH);
        }
        return replacedFileName;
    }

}
