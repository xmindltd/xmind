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

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.xmind.ui.internal.MindMapMessages;

/**
 * @author briansun
 * @deprecated The upgrade functionality has been moved to plugin
 *             <code>net.xmind.signin</code>. This plugin (
 *             <code>org.xmind.ui.mindmap</code>) no longer depends on Eclipse
 *             update plugins.
 */
public class TryProAction extends Action implements IWorkbenchAction {

    private IWorkbenchWindow window;

//    private static class SpecificFeatureFilter implements IUpdateSearchFilter {
//
//        private String featureId;
//
//        /**
//         * @param featureId
//         */
//        public SpecificFeatureFilter(String featureId) {
//            this.featureId = featureId;
//        }
//
//        /**
//         * @see org.eclipse.update.search.IUpdateSearchFilter#accept(org.eclipse.update.core.IFeature)
//         */
//        public boolean accept(IFeature match) {
//            return featureId.equals(match.getVersionedIdentifier()
//                    .getIdentifier());
//        }
//
//        /**
//         * @see org.eclipse.update.search.IUpdateSearchFilter#accept(org.eclipse.update.core.IFeatureReference)
//         */
//        public boolean accept(IFeatureReference match) {
//            try {
//                return featureId.equals(match.getVersionedIdentifier()
//                        .getIdentifier());
//            } catch (CoreException e) {
//                return false;
//            }
//        }
//
//    }
//
//    private static class UpdateSourceDialog extends Dialog {
//
//        private String url;
//
//        private Button onlineButton;
//
//        private Button localFileButton;
//
//        public UpdateSourceDialog(Shell parentShell) {
//            super(parentShell);
//            setBlockOnOpen(true);
//            setShellStyle(SWT.TITLE | SWT.CLOSE);
//        }
//
//        protected void configureShell(Shell newShell) {
//            super.configureShell(newShell);
//            newShell.setText(MindMapMessages.TryPro_UpdateSourceDialog_title);
//        }
//
//        protected Control createDialogArea(Composite parent) {
//            Composite composite = (Composite) super.createDialogArea(parent);
//
//            Label label = new Label(composite, SWT.NONE);
//            label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false,
//                    false));
//            label.setText(MindMapMessages.TryPro_UpdateSourceDialog_label);
//
//            onlineButton = new Button(composite, SWT.RADIO);
//            onlineButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
//                    false));
//            onlineButton
//                    .setText(MindMapMessages.TryPro_UpdateSourceDialog_Online);
//
//            localFileButton = new Button(composite, SWT.RADIO);
//            localFileButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
//                    true, false));
//            localFileButton
//                    .setText(MindMapMessages.TryPro_UpdateSourceDialog_LocalFile);
//
//            onlineButton.setSelection(true);
//            localFileButton.setSelection(false);
//
//            return composite;
//        }
//
//        protected void okPressed() {
//            boolean openFileDialog = localFileButton.getSelection();
//            if (openFileDialog) {
//                FileDialog fileDialog = new FileDialog(getShell(), SWT.OPEN);
//                fileDialog.setFilterExtensions(new String[] { "*.*" }); //$NON-NLS-1$
//                fileDialog.setFilterNames(new String[] { NLS.bind("{0} (*.*)", //$NON-NLS-1$
//                        DialogMessages.AllFilesFilterName) });
//                String path = fileDialog.open();
//                if (path == null) {
//                    return;
//                }
//                this.url = "jar:file:" + path + "!/"; //$NON-NLS-1$ //$NON-NLS-2$
//            } else {
//                this.url = "http://www.xmind.net/xmind/updates/xmindpro3/"; //$NON-NLS-1$
//            }
//            super.okPressed();
//        }
//
//        public String getURL() {
//            return url;
//        }
//    }
//
//    private UpdateSearchRequest searchRequest;
//
//    private List<IInstallFeatureOperation> updates;
//
//    private TryProJob job;
//
//    private class SearchResultCollector implements IUpdateSearchResultCollector {
//        public void accept(IFeature feature) {
//            IInstallFeatureOperation operation = OperationsManager
//                    .getOperationFactory().createInstallOperation(null,
//                            feature, null, null, null);
//            updates.add(operation);
//        }
//    }
//
//    private class TryProJob extends Job {
//
//        /**
//         * @param name
//         */
//        public TryProJob() {
//            super(MindMapMessages.TryPro_UpdateSourceDialog_jobName);
//        }
//
//        /**
//         * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
//         */
//        @Override
//        protected IStatus run(IProgressMonitor monitor) {
//            try {
//                searchRequest.performSearch(new SearchResultCollector(),
//                        monitor);
//                return Status.OK_STATUS;
//            } catch (OperationCanceledException e) {
//                return Status.CANCEL_STATUS;
//            } catch (CoreException e) {
//                return e.getStatus();
//            }
//        }
//
//    }

    public TryProAction(IWorkbenchWindow window) {
        this("org.xmind.ui.upgradeXMind", window); //$NON-NLS-1$
    }

    /**
     * @param id
     * @param window
     */
    public TryProAction(String id, IWorkbenchWindow window) {
        super(MindMapMessages.TryPro_text);
        setId(id);
        setToolTipText(MindMapMessages.TryPro_toolTip);
        if (window == null)
            throw new IllegalArgumentException();
        this.window = window;
    }

    /**
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        if (window == null)
            return;

//        UpdateSourceDialog dialog = new UpdateSourceDialog(window.getShell());
//        int retCode = dialog.open();
//        if (retCode != UpdateSourceDialog.OK)
//            return;
//
//        String url = dialog.getURL();
//        UpdateSearchScope scope = new UpdateSearchScope();
//        try {
//            scope.addSearchSite("XMind Pro", new URL(url), null); //$NON-NLS-1$
//        } catch (MalformedURLException e) {
//            Logger.log(e);
//        }
//        scope.setFeatureProvidedSitesEnabled(false);
//        searchRequest = new UpdateSearchRequest(UpdateSearchRequest
//                .createDefaultSiteSearchCategory(), scope);
//        searchRequest.addFilter(new EnvironmentFilter());
//        searchRequest.addFilter(new BackLevelFilter());
//        searchRequest.addFilter(new SpecificFeatureFilter(
//                "org.xmind.meggy.feature")); //$NON-NLS-1$

//        UpdateJob job = new UpdateJob("Try Pro", searchRequest);
//        UpdateManagerUI.openInstaller(window.getShell(), job);

        searchFeature();
    }

    /**
     * 
     */
    private void searchFeature() {
//        updates = new ArrayList<IInstallFeatureOperation>();
//        job = new TryProJob();
//        job.addJobChangeListener(new JobChangeAdapter() {
//
//            /**
//             * @see org.eclipse.core.runtime.jobs.JobChangeAdapter#done(org.eclipse.core.runtime.jobs.IJobChangeEvent)
//             */
//            @Override
//            public void done(IJobChangeEvent event) {
//                afterSearch(event.getResult());
//            }
//
//        });
//        job.schedule();
    }

//    /**
//     * @param result
//     */
//    private void afterSearch(final IStatus result) {
//        if (result == Status.CANCEL_STATUS)
//            return;
//        final Shell shell = window.getShell();
//        if (result != Status.OK_STATUS)
//            shell.getDisplay().syncExec(new Runnable() {
//                public void run() {
//                    UpdateUI.log(result, true);
//                }
//            });
//        shell.getDisplay().asyncExec(new Runnable() {
//            public void run() {
//                BusyIndicator.showWhile(shell.getDisplay(), new Runnable() {
//                    public void run() {
//                        openInstallWizard();
//                    }
//                });
//            }
//        });
//    }

//    /**
//     * 
//     */
//    private void openInstallWizard() {
//        Shell shell = window.getShell();
//        if (InstallWizard2.isRunning()) {
//            MessageDialog.openInformation(shell,
//                    UpdateUIMessages.InstallWizard_isRunningTitle,
//                    UpdateUIMessages.InstallWizard_isRunningInfo);
//            return;
//        }
//        if (updates == null || updates.isEmpty()) {
//            MessageDialog.openInformation(shell,
//                    MindMapMessages.TryPro_ErrorDialog_title,
//                    MindMapMessages.TryPro_ErrorDialog_message);
//            return;
//        }
//        InstallWizard2 wizard = new InstallWizard2(searchRequest, updates
//                .toArray(new IInstallFeatureOperation[0]), true);
//        final WizardDialog dialog = new ResizableInstallWizardDialog(shell,
//                wizard, MindMapMessages.TryPro_InstallDialog_title);
//        dialog.create();
//        dialog.open();
//    }

    /**
     * @see org.eclipse.ui.actions.ActionFactory.IWorkbenchAction#dispose()
     */
    public void dispose() {
        window = null;
    }

}