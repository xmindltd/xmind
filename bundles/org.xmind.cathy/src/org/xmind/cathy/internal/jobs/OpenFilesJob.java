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

package org.xmind.cathy.internal.jobs;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbench;
import org.xmind.cathy.internal.CathyPlugin;
import org.xmind.cathy.internal.WorkbenchMessages;
import org.xmind.core.Core;
import org.xmind.core.IWorkbook;
import org.xmind.core.command.Command;
import org.xmind.core.command.CommandJob;
import org.xmind.core.command.ICommand;
import org.xmind.core.util.FileUtils;
import org.xmind.ui.internal.MarkerImpExpUtils;
import org.xmind.ui.internal.imports.freemind.FreeMindImporter;
import org.xmind.ui.internal.imports.mm.MindManagerImporter;
import org.xmind.ui.internal.prefs.MarkerManagerPrefPage;
import org.xmind.ui.mindmap.LazyPasswordBasedEncryptor;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.PrefUtils;
import org.xmind.ui.wizards.MindMapImporter;

/**
 * @author Frank Shaka
 */
public class OpenFilesJob extends AbstractCheckFilesJob {

    private List<String> filesToOpen;

    private boolean markersImported = false;

    private List<String> commandFilesToOpen = new ArrayList<String>(1);

    public OpenFilesJob(IWorkbench workbench, String jobName) {
        super(workbench, jobName);
        this.filesToOpen = new ArrayList<String>();
    }

    /**
     * 
     */
    public OpenFilesJob(IWorkbench workbench, String jobName,
            Collection<String> files) {
        super(workbench, jobName);
        this.filesToOpen = new ArrayList<String>(files);
    }

    @Override
    protected void prepare() {
        markersImported = false;
        super.prepare();
    }

    @Override
    protected void finish() {
        filesToOpen.clear();
        markersImported = false;
        super.finish();
    }

    protected IStatus run(IProgressMonitor monitor) {
        monitor.beginTask(null, getTotalSteps());
        doCheckAndOpenFiles(monitor);
        monitor.done();
        return Status.OK_STATUS;
    }

    /**
     * Number of ticks that this job is going to consume.
     * 
     * @return
     */
    protected int getTotalSteps() {
        return 4;
    }

    /**
     * Opens files.
     * 
     * @param monitor
     *            the progress monitor
     */
    protected void doCheckAndOpenFiles(IProgressMonitor monitor) {
        filterFilesToOpen(filesToOpen, new SubProgressMonitor(monitor, 1));

        if (filesToOpen.isEmpty()) {
            monitor.worked(2);
        } else {
            addEditors(new SubProgressMonitor(monitor, 1));
            openEditors(monitor,
                    WorkbenchMessages.CheckOpenFilesJob_OpenFiles_name, 1,
                    true);
        }

        onFilsOpened(new SubProgressMonitor(monitor, 1));
    }

    /**
     * 
     */
    protected void onFilsOpened(IProgressMonitor monitor) {
        monitor.beginTask(null, 1);
        if (markersImported)
            showMarkersPrefPage();
        if (!commandFilesToOpen.isEmpty()) {
            openXMindCommandFiles();
        }
        monitor.done();
    }

    /**
     * Filters the files to open. Subclasses may add/remove files to/from the
     * `filesToOpen` list.
     * 
     * @param monitor
     */
    protected void filterFilesToOpen(List<String> filesToOpen,
            IProgressMonitor monitor) {
        monitor.beginTask(null, 1);
        monitor.done();
    }

    /**
     * Add editors to open.
     * 
     * @param monitor
     */
    protected void addEditors(final IProgressMonitor monitor) {
        monitor.beginTask(WorkbenchMessages.CheckOpenFilesJob_OpenFiles_name,
                filesToOpen.size());
        for (final String fileName : filesToOpen) {
            SafeRunner.run(new SafeRunnable(NLS.bind(
                    WorkbenchMessages.CheckOpenFilesJob_FailsToOpen_message,
                    fileName)) {
                public void run() throws Exception {
                    monitor.subTask(fileName);
                    IEditorInput input = createEditorInput(fileName, monitor);
                    if (input != null) {
                        addEditorToOpen(input);
                    }
                    monitor.worked(1);
                }
            });
        }
        monitor.done();
    }

    /**
     * Create an editor input from the file, or do anything to open the file.
     * 
     * @param fileName
     * @param monitor
     * @return
     * @throws Exception
     */
    protected IEditorInput createEditorInput(String fileName,
            IProgressMonitor monitor) throws Exception {
        final ICommand command = Command.parseURI(fileName);
        if (command != null) {
            new CommandJob(command, null).schedule();
            return null;
        }

        final String path = fileName;
        String extension = FileUtils.getExtension(path);

        if (CathyPlugin.COMMAND_FILE_EXT.equalsIgnoreCase(extension)) {
            return openXMindCommandFile(path);
        } else if (MindMapUI.FILE_EXT_TEMPLATE.equalsIgnoreCase(extension)) {
            return newFromTemplate(path, fileName);
        } else if (".mmap".equalsIgnoreCase(extension)) { //$NON-NLS-1$
            return importMindManagerFile(path, fileName);
        } else if (".mm".equalsIgnoreCase(extension)) { //$NON-NLS-1$
            return importFreeMindFile(path, fileName);
        } else if (MindMapUI.FILE_EXT_MARKER_PACKAGE
                .equalsIgnoreCase(extension)) {
            return importMarkers(path);
        } else if (new File(path).exists()) {
            return MindMapUI.getEditorInputFactory()
                    .createEditorInputForFile(new File(path));
        } else {
            // assumes we're opening xmind files
            Core.getWorkbookBuilder()
                    .setEntryStreamNormalizer(new LazyPasswordBasedEncryptor());
            IWorkbook workbook = Core.getWorkbookBuilder().loadFromPath(path);
            return workbook == null ? null
                    : MindMapUI.getEditorInputFactory()
                            .createEditorInputForPreLoadedWorkbook(workbook,
                                    fileName);
        }
    }

    protected IEditorInput newFromTemplate(String path, String fileName)
            throws Exception {
        Core.getWorkbookBuilder()
                .setEntryStreamNormalizer(new LazyPasswordBasedEncryptor());
        IWorkbook workbook = Core.getWorkbookBuilder().loadFromPath(path);
        return workbook == null ? null
                : MindMapUI.getEditorInputFactory()
                        .createEditorInputForPreLoadedWorkbook(workbook,
                                fileName);
    }

    protected IEditorInput importMindManagerFile(String path, String fileName)
            throws Exception {
        MindMapImporter importer = new MindManagerImporter(path);
        importer.build();
        IWorkbook workbook = importer.getTargetWorkbook();
        return workbook == null ? null
                : MindMapUI.getEditorInputFactory()
                        .createEditorInputForPreLoadedWorkbook(workbook,
                                fileName);
    }

    protected IEditorInput importFreeMindFile(String path, String fileName)
            throws Exception {
        FreeMindImporter importer = new FreeMindImporter(path);
        importer.build();
        IWorkbook workbook = importer.getTargetWorkbook();
        return workbook == null ? null
                : MindMapUI.getEditorInputFactory()
                        .createEditorInputForPreLoadedWorkbook(workbook,
                                fileName);
    }

    protected IEditorInput importMarkers(String path) throws Exception {
        MarkerImpExpUtils.importMarkerPackage(path);
        markersImported = true;
        return null;
    }

    private void showMarkersPrefPage() {
        Display display = getWorkbench().getDisplay();
        if (display == null || display.isDisposed())
            return;

        display.asyncExec(new Runnable() {
            public void run() {
                PrefUtils.openPrefDialog(null, MarkerManagerPrefPage.ID);
            }
        });
    }

    private IEditorInput openXMindCommandFile(String path) {
        commandFilesToOpen.add(path);
        return null;
    }

    private void openXMindCommandFiles() {
        for (String path : commandFilesToOpen) {
            new OpenXMindCommandFileJob(path).schedule(500);
        }
    }

}
