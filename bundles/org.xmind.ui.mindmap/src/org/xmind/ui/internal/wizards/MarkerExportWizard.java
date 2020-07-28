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
package org.xmind.ui.internal.wizards;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.xmind.core.marker.IMarkerGroup;
import org.xmind.ui.internal.MarkerImpExpUtils;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.internal.dialogs.DialogMessages;
import org.xmind.ui.internal.dialogs.DialogUtils;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.wizards.AbstractExportPage;
import org.xmind.ui.wizards.AbstractExportWizard;

public class MarkerExportWizard extends AbstractExportWizard {

    private static final String PAGE_NAME = "org.xmind.ui.MarkerExportWizardPage"; //$NON-NLS-1$

    private static final String SECTION_NAME = "org.xmind.ui.markerExportWizard"; //$NON-NLS-1$

    private class MarkerExportWizardPage extends AbstractExportPage {

        private TableViewer viewer;

        protected MarkerExportWizardPage() {
            super(PAGE_NAME, WizardMessages.MarkerExportPage_title, null);
            setDescription(WizardMessages.MarkerExportPage_description);
        }

        public void createControl(Composite parent) {
            Composite composite = new Composite(parent, SWT.NONE);
            GridLayout gridLayout = new GridLayout(1, false);
            gridLayout.marginWidth = 5;
            gridLayout.marginHeight = 5;
            gridLayout.verticalSpacing = 5;
            gridLayout.horizontalSpacing = 5;
            composite.setLayout(gridLayout);
            setControl(composite);

            Label label = new Label(composite, SWT.NONE);
            label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false,
                    false));
            label.setText(WizardMessages.MarkerExportPage_ChooseMarkerGroups_label);

            viewer = new TableViewer(composite, SWT.BORDER | SWT.FULL_SELECTION
                    | SWT.MULTI);
            viewer.setContentProvider(new MarkerGroupContentProvider());
            viewer.setLabelProvider(new MarkerGroupLabelProvider());
            viewer.setInput(MindMapUI.getResourceManager().getUserMarkerSheet());
            viewer.getControl().setLayoutData(
                    new GridData(SWT.FILL, SWT.FILL, true, true));

            Control fileControl = createFileControls(composite);
            fileControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
                    false));

        }

        protected void setDialogFilters(FileDialog dialog,
                List<String> filterNames, List<String> filterExtensions) {
            filterNames.clear();
            String ext = "*" + MindMapUI.FILE_EXT_MARKER_PACKAGE; //$NON-NLS-1$
            filterNames.add(NLS.bind("{0} ({1})", //$NON-NLS-1$
                    DialogMessages.MarkerPackageFilterName, ext));

            filterExtensions.clear();
            filterExtensions.add(ext);
            super.setDialogFilters(dialog, filterNames, filterExtensions);
        }

        public List<IMarkerGroup> getSelection() {
            List<IMarkerGroup> list = new ArrayList<IMarkerGroup>();
            ISelection selection = viewer.getSelection();
            if (selection instanceof IStructuredSelection) {
                for (Object o : ((IStructuredSelection) selection).toList()) {
                    if (o instanceof IMarkerGroup)
                        list.add((IMarkerGroup) o);
                }
            }
            return list;
        }

    }

    private MarkerExportWizardPage page;

    public MarkerExportWizard() {
        setWindowTitle(WizardMessages.MarkerExportWizard_windowTitle);
        setDialogSettings(MindMapUIPlugin.getDefault().getDialogSettings(
                SECTION_NAME));
    }

    public void addPages() {
        addPage(page = new MarkerExportWizardPage());
    }

    public boolean performFinish() {
        if (hasTargetPath()) {
            if (!isOverwriteWithoutPrompt()
                    && new File(getTargetPath()).exists()) {
                if (!DialogUtils.confirmOverwrite(getShell(), getTargetPath()))
                    return false;
            }

            final List<IMarkerGroup> selection = page.getSelection();
            if (!selection.isEmpty()) {
                final boolean[] b = new boolean[1];
                SafeRunner.run(new SafeRunnable() {
                    public void run() throws Exception {
                        MarkerImpExpUtils.exportMarkerPackage(selection,
                                getTargetPath(), true);
                        String dir = new File(getTargetPath()).getParent();
                        Program.launch(dir);
                        b[0] = true;
                    }

                    public void handleException(Throwable e) {
                        b[0] = false;
                        super.handleException(e);
                    }
                });
                return b[0];
            }
        }
        return false;
    }

    protected boolean hasSource() {
        return true;
    }

    protected String getSuggestedFileName() {
        return "Package.xmp"; //$NON-NLS-1$
    }

}