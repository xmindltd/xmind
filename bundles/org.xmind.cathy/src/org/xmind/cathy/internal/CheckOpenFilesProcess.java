package org.xmind.cathy.internal;

import java.io.File;
import java.util.List;

import org.eclipse.ui.IWorkbench;

public class CheckOpenFilesProcess extends OpenFilesProcess {

    public CheckOpenFilesProcess(IWorkbench workbench) {
        super(workbench);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.xmind.cathy.internal.jobs.OpenFilesJob#filterFilesToOpen(java.util
     * .List, org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    protected void filterFilesToOpen(List<String> filesToOpen) {
        Log opening = Log.get(Log.OPENING);
        if (opening.exists()) {
            String[] contents = opening.getContents();
            for (String line : contents) {
                if (line.startsWith("xmind:") || new File(line).exists()) { //$NON-NLS-1$
                    filesToOpen.add(line);
                }
            }
            opening.delete();
        }
    }

}
