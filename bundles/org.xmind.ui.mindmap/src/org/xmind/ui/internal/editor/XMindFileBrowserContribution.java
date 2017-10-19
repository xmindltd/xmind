package org.xmind.ui.internal.editor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.xmind.core.command.Command;
import org.xmind.core.command.CommandJob;
import org.xmind.core.command.ICommand;
import org.xmind.core.util.FileUtils;
import org.xmind.ui.browser.IBrowserViewer;
import org.xmind.ui.browser.IBrowserViewerContribution;
import org.xmind.ui.browser.IBrowserViewerContribution2;
import org.xmind.ui.browser.IPropertyChangingListener;
import org.xmind.ui.browser.PropertyChangingEvent;
import org.xmind.ui.internal.dialogs.DialogMessages;
import org.xmind.ui.io.DownloadJob;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.Logger;

public class XMindFileBrowserContribution
        implements IBrowserViewerContribution, IBrowserViewerContribution2 {

    protected static class XMindFileListener
            implements PropertyChangeListener, IPropertyChangingListener {

        private IBrowserViewer viewer;

        public XMindFileListener(IBrowserViewer viewer) {
            this.viewer = viewer;
            viewer.addPropertyChangeListener(IBrowserViewer.PROPERTY_LOCATION,
                    this);
        }

        /**
         * @return the viewer
         */
        public IBrowserViewer getViewer() {
            return viewer;
        }

        /*
         * (non-Javadoc)
         * 
         * @seejava.beans.PropertyChangeListener#propertyChange(java.beans.
         * PropertyChangeEvent)
         */
        public void propertyChange(PropertyChangeEvent evt) {
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.xmind.ui.browser.IPropertyChangingListener#propertyChanging(org
         * .xmind.ui.browser.PropertyChangingEvent)
         */
        public void propertyChanging(PropertyChangingEvent event) {
            if (!IBrowserViewer.PROPERTY_LOCATION
                    .equals(event.getPropertyName()))
                return;

            String location = (String) event.getNewValue();
            if (location == null || "about:blank".equals(location)) //$NON-NLS-1$
                return;

            final ICommand command = Command.parseURI(location);
            if (command != null) {
                new CommandJob(command, null).schedule();
            } else {
                try {
                    URI uri = new URI(location);
                    String uriPath = uri.getPath();
                    if (uriPath != null
                            && uriPath.endsWith(MindMapUI.FILE_EXT_XMIND)) {
                        downloadAndOpen(location,
                                FileUtils.getFileName(uriPath));
                        event.doit = false;
                    }
                } catch (Throwable e) {
                    Logger.log(e);
                }
            }
        }

        /**
         * @param location
         * @param url
         * @param connection
         */
        private void downloadAndOpen(String location, String suggestedName) {
            FileDialog dialog = new FileDialog(viewer.getControl().getShell(),
                    SWT.SAVE | SWT.SINGLE);
            String ext = "*" + MindMapUI.FILE_EXT_XMIND; //$NON-NLS-1$
            dialog.setFilterExtensions(new String[] { ext });
            dialog.setFilterNames(new String[] { NLS.bind("{0} ({1})", //$NON-NLS-1$
                    DialogMessages.WorkbookFilterName, ext) });
            dialog.setOverwrite(true);
            dialog.setText(DialogMessages.Save_title);
            dialog.setFileName(suggestedName);
            final String path = dialog.open();
            if (path == null)
                return;

            FileUtils.delete(new File(path));
            File file = new File(path);
            file.getParentFile().mkdirs();
            final String tempPath = path + ".downloading"; //$NON-NLS-1$
            DownloadJob job = new DownloadJob(file.getName(), location,
                    tempPath);
            job.addJobChangeListener(new JobChangeAdapter() {
                /*
                 * (non-Javadoc)
                 * 
                 * @see org.eclipse.core.runtime.jobs.JobChangeAdapter#done(org.
                 * eclipse .core.runtime.jobs.IJobChangeEvent)
                 */
                @Override
                public void done(IJobChangeEvent event) {
                    if (event.getResult().isOK()) {
                        if (rename(tempPath, path)) {
                            openFile(path);
                        }
                    }
                }

            });
            job.schedule();
        }

        private boolean rename(String tempPath, String path) {
            File tempFile = new File(tempPath);
            if (!tempFile.exists() || !tempFile.canRead())
                return false;
            return new File(tempPath).renameTo(new File(path));
        }

        private void openFile(String path) {
            final File file = new File(path);
            if (!file.exists() || !file.canRead())
                return;

            final IEditorInput[] input = new IEditorInput[1];
            SafeRunner.run(new SafeRunnable() {
                public void run() throws Exception {
                    input[0] = MindMapUI.getEditorInputFactory()
                            .createEditorInputForFile(file);
                    ;
                }
            });
            if (input[0] == null)
                return;

            final IWorkbench workbench = PlatformUI.getWorkbench();
            if (workbench != null) {
                Display display = workbench.getDisplay();
                if (display != null && !display.isDisposed()) {
                    display.asyncExec(new Runnable() {
                        public void run() {
                            SafeRunner.run(new SafeRunnable() {
                                public void run() throws Exception {
                                    openEditor(input[0], workbench);
                                }
                            });
                        }
                    });
                }
            }

//            IWorkbook contents;
//            try {
//                contents = Core.getWorkbookBuilder().loadFromPath(path);
//                final WorkbookEditorInput input = new WorkbookEditorInput(
//                        contents, path);
//                final IWorkbench workbench = PlatformUI.getWorkbench();
//                if (workbench != null) {
//                    workbench.getDisplay().asyncExec(new Runnable() {
//                        public void run() {
//                            openEditor(input, workbench);
//                        }
//
//                    });
//                }
//            } catch (Throwable e) {
//                Logger.log(e);
//            }
        }

        /**
         * @param input
         * @param workbench
         */
        private void openEditor(final IEditorInput input,
                final IWorkbench workbench) throws Exception {
            IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
            if (window != null) {
                IWorkbenchPage page = window.getActivePage();
                if (page != null) {
                    page.openEditor(input, MindMapUI.MINDMAP_EDITOR_ID);
                }
            }
        }

        public void dispose() {
            viewer.removePropertyChangeListener(this);
        }

    }

    private Map<IBrowserViewer, XMindFileListener> map = new HashMap<IBrowserViewer, XMindFileListener>();

    public void fillToolBar(IBrowserViewer viewer,
            IContributionManager toolBar) {
    }

    public void installBrowserListeners(IBrowserViewer viewer) {
        XMindFileListener listener = new XMindFileListener(viewer);
        map.put(viewer, listener);
    }

    public void uninstallBrowserListeners(IBrowserViewer viewer) {
        XMindFileListener listener = map.remove(viewer);
        if (listener != null) {
            listener.dispose();
        }
    }

}
