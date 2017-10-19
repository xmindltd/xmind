package org.xmind.cathy.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbench;
import org.xmind.cathy.internal.jobs.OpenXMindCommandFileJob;
import org.xmind.core.IWorkbook;
import org.xmind.core.command.Command;
import org.xmind.core.command.CommandJob;
import org.xmind.core.command.ICommand;
import org.xmind.core.util.FileUtils;
import org.xmind.ui.internal.MarkerImpExpUtils;
import org.xmind.ui.internal.editor.ClonedWorkbookRef;
import org.xmind.ui.internal.imports.freemind.FreeMindImporter;
import org.xmind.ui.internal.imports.mm.MindManagerImporter;
import org.xmind.ui.internal.prefs.MarkerManagerPrefPage;
import org.xmind.ui.mindmap.IWorkbookRef;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.PrefUtils;
import org.xmind.ui.wizards.MindMapImporter;

public class OpenFilesProcess extends AbstractCheckFilesProcess {

    private List<String> filesToOpen;

    private boolean markersImported = false;

    private List<String> commandFilesToOpen = new ArrayList<String>(1);

    public OpenFilesProcess(IWorkbench workbench) {
        super(workbench);
        this.filesToOpen = new ArrayList<String>();
    }

    /**
     * 
     */
    public OpenFilesProcess(IWorkbench workbench, Collection<String> files) {
        super(workbench);
        this.filesToOpen = new ArrayList<String>(files);
    }

    public void doCheckAndOpenFiles() {
        filterFilesToOpen(filesToOpen);

        if (filesToOpen.isEmpty()) {

        } else {
            addEditors();
            openEditors(true);
        }

        onFilsOpened();
    }

    /**
     * 
     */
    protected void onFilsOpened() {
        if (markersImported)
            showMarkersPrefPage();
        if (!commandFilesToOpen.isEmpty()) {
            openXMindCommandFiles();
        }
    }

    /**
     * Filters the files to open. Subclasses may add/remove files to/from the
     * `filesToOpen` list.
     * 
     * @param monitor
     */
    protected void filterFilesToOpen(List<String> filesToOpen) {
    }

    /**
     * Add editors to open.
     * 
     * @param monitor
     */
    protected void addEditors() {
        for (final String fileName : filesToOpen) {
            SafeRunner.run(new SafeRunnable(NLS.bind(
                    WorkbenchMessages.CheckOpenFilesJob_FailsToOpen_message,
                    fileName)) {
                public void run() throws Exception {
                    IEditorInput input = createEditorInput(fileName);
                    if (input != null) {
                        addEditorToOpen(input);
                    }
                }
            });
        }
    }

    /**
     * Create an editor input from the file, or do anything to open the file.
     * 
     * @param fileName
     * @param monitor
     * @return
     * @throws Exception
     */
    protected IEditorInput createEditorInput(String fileName) throws Exception {
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
        } else {
            // assumes we're opening xmind files
            return MindMapUI.getEditorInputFactory()
                    .createEditorInputForFile(new File(path));
        }
    }

    protected IEditorInput newFromTemplate(String path, String fileName)
            throws Exception {
        IWorkbookRef ref = ClonedWorkbookRef
                .createFromSourceWorkbookURI(new File(path).toURI(), fileName);
        return MindMapUI.getEditorInputFactory().createEditorInput(ref);
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
