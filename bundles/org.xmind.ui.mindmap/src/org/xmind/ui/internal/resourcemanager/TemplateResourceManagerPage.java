package org.xmind.ui.internal.resourcemanager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.e4models.IContextRunnable;
import org.xmind.ui.internal.e4models.IModelConstants;
import org.xmind.ui.internal.resourcemanager.TemplateResourceManagerViewer.TemplateGalleryCore;
import org.xmind.ui.internal.utils.ResourceUtils;
import org.xmind.ui.mindmap.IResourceManagerListener;
import org.xmind.ui.mindmap.ITemplate;
import org.xmind.ui.mindmap.ITemplateGroup;
import org.xmind.ui.mindmap.MindMapUI;

public class TemplateResourceManagerPage extends ResourceManagerDialogPage
        implements IResourceManagerListener {

    private static final int IMPORT_BUTTON_ID = IDialogConstants.CLIENT_ID + 1;
    private static final String IMPORT_RESOURCE_BUNDLE_COMMAND_ID = "org.xmind.ui.command.importXMindResourceBundle"; //$NON-NLS-1$
    private TemplateResourceManagerViewer viewer;

    @Override
    protected ResourceManagerViewer createViewer() {
        viewer = new TemplateResourceManagerViewer();
        MindMapUI.getResourceManager().addResourceManagerListener(this);
        return viewer;
    }

    @Override
    public void dispose() {
        MindMapUI.getResourceManager().removeResourceManagerListener(this);
        super.dispose();
    }

    public void userTemplateAdded(ITemplate template) {
        if (!(template instanceof ITemplate))
            return;
        if (viewer == null || viewer.getControl() == null
                || viewer.getControl().isDisposed())
            return;

        viewer.refresh();
        viewer.reveal(TemplateGalleryCore.getInstance()
                .getGroupByName(TemplateGalleryCore.USER_GROUP_NAME));
        viewer.setSelection(new StructuredSelection(template));
    }

    public void userTemplateRemoved(ITemplate template) {
        if (template instanceof ITemplate) {
            if (viewer == null || viewer.getControl() == null
                    || viewer.getControl().isDisposed())
                return;
            viewer.refresh();
        }
    }

    @Override
    protected void createButtonsForButtonBar(Composite composite) {
        Button importButton = createButton(composite, IMPORT_BUTTON_ID,
                MindMapMessages.TemplateResourceManagerPage_Import_button,
                false);

        final IAction addTemplateAction = getAddTemplateAction();
        importButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                addTemplateAction.run();
            }
        });
    }

    private IAction getAddTemplateAction() {
        Action addTemplateAction = new Action(
                MindMapMessages.TemplateResourceManagerPage_AddTemplates_label) {
            @Override
            public void run() {
                FileDialog dialog = new FileDialog(
                        Display.getCurrent().getActiveShell(), SWT.OPEN);
                String ext = "*" + MindMapUI.FILE_EXT_TEMPLATE; //$NON-NLS-1$
                dialog.setFilterExtensions(new String[] { ext });
                dialog.setFilterNames(new String[] { NLS.bind("{0} ({1})", //$NON-NLS-1$
                        MindMapMessages.TemplateResourceManagerPage_TemplateFilterName_label,
                        ext) });
                String path = dialog.open();
                if (path == null)
                    return;

                final File templateFile = new File(path);
                if (templateFile != null && templateFile.exists()) {
                    SafeRunner.run(new SafeRunnable() {
                        public void run() throws Exception {
                            MindMapUI.getResourceManager()
                                    .addUserTemplateFromWorkbookURI(
                                            templateFile.toURI());
                        }
                    });
                }

            }
        };
        addTemplateAction.setToolTipText(
                MindMapMessages.TemplateResourceManagerPage_AddTemplates_tooltip);

        return addTemplateAction;
    }

    @Override
    protected void registerRunnable(IEclipseContext eclipseContext) {
        super.registerRunnable(eclipseContext);
        eclipseContext.set(
                getId() + "/" + IModelConstants.KEY_MODEL_PART_DELETE, //$NON-NLS-1$
                new IContextRunnable() {

                    @Override
                    public void run() {
                        if (viewer != null && viewer.getControl() != null
                                && !viewer.getControl().isDisposed()) {
                            List<ITemplate> templates = getSelectedTemplates();
                            if (templates.isEmpty()) {
                                return;
                            }
                            StringBuilder sb = new StringBuilder(
                                    templates.size() * 10);
                            for (ITemplate template : templates) {
                                if (sb.length() > 0) {
                                    sb.append(',');
                                    sb.append(' ');
                                }
                                sb.append('\'');
                                sb.append(template.getName());
                                sb.append('\'');
                            }
                            if (!MessageDialog.openConfirm(
                                    viewer.getControl().getShell(),
                                    MindMapMessages.TemplateResourceManagerPage_Delete_ConfirmDialog_title,
                                    NLS.bind(
                                            MindMapMessages.TemplateResourceManagerPage_Delete_ConfirmDialog_message,
                                            sb.toString()))) {
                                return;
                            }
                            ResourceUtils.deleteTemplates(templates);
                        }
                    }

                    @Override
                    public boolean canExecute(IEclipseContext context,
                            String contextKey) {
                        List<ITemplate> templates = getSelectedTemplates();
                        List<ITemplate> sysTemplates = MindMapUI
                                .getResourceManager().getSystemTemplates();
                        boolean canExecute = !templates.isEmpty();
                        for (ITemplate template : templates) {
                            canExecute = canExecute
                                    && !sysTemplates.contains(template);
                        }

                        List<ITemplateGroup> sysGroups = MindMapUI
                                .getResourceManager().getSystemTemplateGroups();
                        for (ITemplateGroup group : sysGroups) {
                            List<ITemplate> gTemplates = group.getTemplates();
                            for (ITemplate template : templates)
                                canExecute = canExecute
                                        && !gTemplates.contains(template);
                        }
                        return canExecute;
                    }
                });

        eclipseContext.set(
                getId() + "/" + IModelConstants.KEY_MODEL_PART_DUPLICATE, //$NON-NLS-1$
                new IContextRunnable() {
                    @Override
                    public void run() {
                        if (viewer != null && viewer.getControl() != null
                                && !viewer.getControl().isDisposed()) {
                            List<ITemplate> selectedTemplates = getSelectedTemplates();
                            if (!selectedTemplates.isEmpty()) {
                                ResourceUtils
                                        .duplicateTemplates(selectedTemplates);
                            }
                        }
                    }

                    @Override
                    public boolean canExecute(IEclipseContext context,
                            String contextKey) {
                        List<ITemplate> templates = getSelectedTemplates();
                        boolean canExecute = !templates.isEmpty();
                        return canExecute;
                    }
                });
        eclipseContext.set(
                getId() + "/" + IModelConstants.KEY_MODEL_PART_RENAME, //$NON-NLS-1$
                new IContextRunnable() {
                    @Override
                    public void run() {
                        if (viewer != null && viewer.getControl() != null
                                && !viewer.getControl().isDisposed()) {
                            List<ITemplate> selectedTemplates = getSelectedTemplates();
                            if (selectedTemplates.size() == 1) {
                                ITemplate template = selectedTemplates.get(0);
                                viewer.startEditing(template);
                            }
                        }
                    }

                    @Override
                    public boolean canExecute(IEclipseContext context,
                            String contextKey) {
                        List<ITemplate> selectedTemplates = getSelectedTemplates();
                        List<ITemplate> systemTemplates = MindMapUI
                                .getResourceManager().getSystemTemplates();
                        boolean canExecute = selectedTemplates.size() == 1;
                        for (ITemplate template : selectedTemplates) {
                            canExecute = canExecute
                                    && !systemTemplates.contains(template);
                        }
                        List<ITemplateGroup> sysGroups = MindMapUI
                                .getResourceManager().getSystemTemplateGroups();
                        for (ITemplateGroup group : sysGroups) {
                            List<ITemplate> gTemplates = group.getTemplates();
                            for (ITemplate template : selectedTemplates)
                                canExecute = canExecute
                                        && !gTemplates.contains(template);
                        }
                        return canExecute;
                    }
                });
    }

    private List<ITemplate> getSelectedTemplates() {
        ArrayList<ITemplate> templates = new ArrayList<ITemplate>();
        if (viewer != null && viewer.getControl() != null
                && !viewer.getControl().isDisposed()) {
            ISelection selection = viewer.getStructuredSelection();
            if (selection instanceof IStructuredSelection) {
                for (Object element : ((IStructuredSelection) selection)
                        .toList()) {
                    templates.add((ITemplate) element);
                }
            }
        }
        return templates;
    }

    @Override
    protected String getContextMenuId() {
        return IModelConstants.POPUPMENU_ID_RESOURCEMANAGER_TEMPLATE;
    }

    @Override
    public String getModelPageId() {
        return IModelConstants.PAGE_ID_RESOURCE_MANAGER_TEMPLATE;
    }

    @Override
    public String getModelPageTitle() {
        return null;
    }

}
