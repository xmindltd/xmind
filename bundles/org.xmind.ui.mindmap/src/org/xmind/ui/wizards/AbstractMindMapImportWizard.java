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
import java.io.InterruptedIOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.xmind.core.Core;
import org.xmind.core.ISheet;
import org.xmind.core.IWorkbook;
import org.xmind.core.event.ICoreEventListener;
import org.xmind.core.event.ICoreEventSource2;
import org.xmind.gef.command.Command;
import org.xmind.gef.command.CompoundCommand;
import org.xmind.gef.command.ICommandStack;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.ui.commands.AddSheetCommand;
import org.xmind.ui.internal.wizards.WizardMessages;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.Logger;

public abstract class AbstractMindMapImportWizard extends Wizard
        implements IImportWizard {

    protected static final String KEY_IMPORT_TARGET = "IMPORT_TARGET"; //$NON-NLS-1$

    protected static final String TARGET_CURRENT_WORKBOOK = "currentWorkbook"; //$NON-NLS-1$

    protected static final String TARGET_NEW_WORKBOOK = "newWorkbook"; //$NON-NLS-1$

    private IWorkbench workbench;

    private IGraphicalEditor targetEditor;

    private IWorkbook targetWorkbook;

    private String sourcePath;

    public AbstractMindMapImportWizard() {
        setNeedsProgressMonitor(true);
    }

    public void init(IWorkbench workbench, IStructuredSelection selection) {
        this.workbench = workbench;
        IEditorPart activeEditor = workbench.getActiveWorkbenchWindow()
                .getActivePage().getActiveEditor();
        if (activeEditor instanceof IGraphicalEditor) {
            this.targetEditor = (IGraphicalEditor) activeEditor;
            this.targetWorkbook = (IWorkbook) activeEditor
                    .getAdapter(IWorkbook.class);
        } else {
            this.targetEditor = null;
            this.targetWorkbook = null;
        }
    }

    public IWorkbench getWorkbench() {
        return workbench;
    }

    public IGraphicalEditor getTargetEditor() {
        return targetEditor;
    }

    public IWorkbook getTargetWorkbook() {
        return targetWorkbook;
    }

    public boolean hasTargetWorkbook() {
        return targetWorkbook != null;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    public boolean isToNewWorkbook() {
        if (!hasTargetWorkbook() || getDialogSettings() == null)
            return true;
        String target = getDialogSettings().get(KEY_IMPORT_TARGET);
        return target == null || TARGET_NEW_WORKBOOK.equals(target);
    }

    public void setToNewWorkbook(boolean newWorkbook) {
        if (getDialogSettings() != null) {
            if (newWorkbook) {
                getDialogSettings().put(KEY_IMPORT_TARGET, TARGET_NEW_WORKBOOK);
            } else {
                getDialogSettings().put(KEY_IMPORT_TARGET,
                        TARGET_CURRENT_WORKBOOK);
            }
        }
    }

    public boolean hasSourcePath() {
        return this.sourcePath != null && !"".equals(this.sourcePath) //$NON-NLS-1$
                && new File(this.sourcePath).exists()
                && new File(this.sourcePath).canRead();
    }

    public boolean performFinish() {
        if (!hasSourcePath())
            return false;

        return doImport();
    }

    protected boolean doImport() {
        final IWorkbook target = isToNewWorkbook() ? null : getTargetWorkbook();
        final MindMapImporter importer = createImporter(getSourcePath(),
                target);
        final Display display = workbench.getDisplay();

        try {
            getContainer().run(true, true, new IRunnableWithProgress() {
                public void run(IProgressMonitor monitor)
                        throws InvocationTargetException, InterruptedException {
                    monitor.beginTask(null, 100);
                    importer.setMonitor(new SubProgressMonitor(monitor, 99));
                    importer.build();

                    if (isToNewWorkbook()) {
                        monitor.subTask(WizardMessages.Import_OpeningWorkbook);

                        final IWorkbook workbook = importer.getTargetWorkbook();
                        if (workbook == null)
                            return;

                        final IEditorInput input = MindMapUI
                                .getEditorInputFactory()
                                .createEditorInputForPreLoadedWorkbook(workbook,
                                        null);
                        final Throwable[] exception = new Throwable[1];
                        display.syncExec(new Runnable() {
                            public void run() {
                                try {
                                    getWorkbench().getActiveWorkbenchWindow()
                                            .getActivePage().openEditor(input,
                                                    MindMapUI.MINDMAP_EDITOR_ID);
                                    // Forcely make editor saveable:
                                    if (workbook instanceof ICoreEventSource2) {
                                        ((ICoreEventSource2) workbook)
                                                .registerOnceCoreEventListener(
                                                        Core.WorkbookPreSaveOnce,
                                                        ICoreEventListener.NULL);
                                    }
                                } catch (Throwable e) {
                                    exception[0] = e;
                                }
                            }
                        });
                        if (exception[0] != null)
                            throw new InvocationTargetException(exception[0]);
                    } else {
                        monitor.subTask(WizardMessages.Import_AppendingSheet);
                        final List<ISheet> targetSheets = importer
                                .getTargetSheets();
                        if (targetSheets.isEmpty())
                            return;

                        final ICommandStack commandStack = getTargetEditor()
                                .getCommandStack();
                        if (commandStack == null)
                            return;

                        final Throwable[] exception = new Throwable[1];
                        display.syncExec(new Runnable() {
                            public void run() {
                                try {
                                    List<Command> commands = new ArrayList<Command>(
                                            targetSheets.size());
                                    for (ISheet sheet : targetSheets) {
                                        AddSheetCommand command = new AddSheetCommand(
                                                sheet, getTargetWorkbook());
                                        commands.add(command);
                                    }
                                    String label = NLS.bind(
                                            WizardMessages.Command_ImportFrom,
                                            new File(getSourcePath())
                                                    .getName());
                                    commandStack.execute(new CompoundCommand(
                                            label, commands));
                                } catch (Throwable e) {
                                    exception[0] = e;
                                }
                            }
                        });
                        if (exception[0] != null)
                            throw new InvocationTargetException(exception[0]);
                    }
                    monitor.done();
                }
            });
            return true;
        } catch (Throwable e) {
            if (e instanceof InterruptedException
                    || e instanceof InterruptedIOException) {
                return false;
            }
            if (e instanceof InvocationTargetException) {
                e = ((InvocationTargetException) e).getCause();
            }
            handleExportException(e);
        } finally {
            importer.dispose();
        }
        return false;
    }

    protected abstract MindMapImporter createImporter(String sourcePath,
            IWorkbook targetWorkbook);

    protected void handleExportException(Throwable e) {
        Logger.log(e, NLS.bind(WizardMessages.Import_FailedWhenImport,
                getApplicationId()));
    }

    protected abstract String getApplicationId();

}