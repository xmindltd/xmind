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
package org.xmind.ui.internal.prefs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.xmind.core.Core;
import org.xmind.core.marker.IMarker;
import org.xmind.core.marker.IMarkerGroup;
import org.xmind.core.marker.IMarkerResource;
import org.xmind.core.marker.IMarkerSheet;
import org.xmind.core.util.FileUtils;
import org.xmind.ui.internal.dialogs.DialogUtils;
import org.xmind.ui.internal.wizards.MarkerExportWizard;
import org.xmind.ui.internal.wizards.MarkerGroupContentProvider;
import org.xmind.ui.internal.wizards.MarkerGroupLabelProvider;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.Logger;

public class MarkerManagerPrefPage extends PreferencePage
        implements IWorkbenchPreferencePage, Listener {

    public static final String ID = "org.xmind.ui.MarkersPrefPage"; //$NON-NLS-1$

    private TableViewer groupViewer;

    private TableViewer markerViewer;

    private Button addGroupButton;

    private Button removeGroupButton;

    private Button renameGroupButton;

    private Button exportGroupButton;

    private Button addMarkerButton;

    private Button removeMarkerButton;

    private Button renameMarkerButton;

    public MarkerManagerPrefPage() {
        super(PrefMessages.MarkersPage_title);
    }

    protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = 5;
        gridLayout.marginHeight = 5;
        gridLayout.verticalSpacing = 20;
        gridLayout.horizontalSpacing = 0;
        composite.setLayout(gridLayout);

        createGroupGroup(composite);
        createMarkerGroup(composite);

        parent.getDisplay().asyncExec(new Runnable() {
            public void run() {
                IMarkerSheet sheet = getSheet();
                groupViewer.setInput(sheet);
                List<IMarkerGroup> groups = sheet.getMarkerGroups();
                if (!groups.isEmpty()) {
                    groupViewer.setSelection(
                            new StructuredSelection(groups.get(0)));
                }
                refreshButtons();
            }
        });

        return composite;
    }

    private void createGroupGroup(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        gridLayout.verticalSpacing = 5;
        gridLayout.horizontalSpacing = 5;
        composite.setLayout(gridLayout);

        Label label = new Label(composite, SWT.NONE);
        label.setLayoutData(
                new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        label.setText(PrefMessages.MarkersPage_Groups_label);

        Composite groupContainer = new Composite(composite, SWT.NONE);
        groupContainer
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        GridLayout gridLayout2 = new GridLayout(2, false);
        gridLayout2.marginWidth = 0;
        gridLayout2.marginHeight = 0;
        gridLayout2.verticalSpacing = 5;
        gridLayout2.horizontalSpacing = 5;
        groupContainer.setLayout(gridLayout2);

        createGroupViewer(groupContainer);
        createGroupControls(groupContainer);
    }

    private void createGroupViewer(Composite parent) {
        groupViewer = new TableViewer(parent, SWT.BORDER | SWT.SINGLE);
        groupViewer.getTable().setLinesVisible(true);
        groupViewer.setContentProvider(new MarkerGroupContentProvider());
        final MarkerGroupLabelProvider labelProvider = new MarkerGroupLabelProvider();
        groupViewer.setLabelProvider(labelProvider);
        groupViewer.getControl()
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        groupViewer
                .addSelectionChangedListener(new ISelectionChangedListener() {
                    public void selectionChanged(SelectionChangedEvent event) {
                        markerViewer.setInput(
                                ((IStructuredSelection) event.getSelection())
                                        .getFirstElement());
                        refreshButtons();
                    }
                });
        groupViewer.setColumnProperties(new String[] { "NAME" }); //$NON-NLS-1$
        groupViewer.setCellEditors(new CellEditor[] {
                new TextCellEditor(groupViewer.getTable(), SWT.SINGLE) });
        groupViewer.setCellModifier(new ICellModifier() {
            public void modify(Object element, String property, Object value) {
                if (element instanceof Widget) {
                    element = ((Widget) element).getData();
                }
                if (element instanceof IMarkerGroup) {
                    changeGroupName((IMarkerGroup) element, value.toString());
                }
            }

            public Object getValue(Object element, String property) {
                return labelProvider.getText(element);
            }

            public boolean canModify(Object element, String property) {
                return true;
            }
        });
        groupViewer.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent event) {
                renameGroup();
            }
        });
    }

    private void createGroupControls(Composite parent) {
        Composite outer = new Composite(parent, SWT.NONE);
        outer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        gridLayout.verticalSpacing = 0;
        gridLayout.horizontalSpacing = 0;
        outer.setLayout(gridLayout);

        Composite inner = new Composite(outer, SWT.NONE);
        inner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
        GridLayout gridLayout2 = new GridLayout(1, false);
        gridLayout2.marginWidth = 0;
        gridLayout2.marginHeight = 0;
        gridLayout2.verticalSpacing = 5;
        gridLayout2.horizontalSpacing = 0;
        inner.setLayout(gridLayout2);

        addGroupButton = new Button(inner, SWT.PUSH);
        addGroupButton.setText(PrefMessages.MarkersPage_AddGroup_text);
        addGroupButton
                .setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        hookWidget(addGroupButton, SWT.Selection);

        removeGroupButton = new Button(inner, SWT.PUSH);
        removeGroupButton.setText(PrefMessages.MarkersPage_RemoveGroup_text);
        removeGroupButton
                .setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        hookWidget(removeGroupButton, SWT.Selection);

        renameGroupButton = new Button(inner, SWT.PUSH);
        renameGroupButton.setText(PrefMessages.MarkersPage_RenameGroup_text);
        renameGroupButton
                .setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        hookWidget(renameGroupButton, SWT.Selection);

        exportGroupButton = new Button(inner, SWT.PUSH);
        exportGroupButton.setText(PrefMessages.MarkersPage_ExportGroup_text);
        exportGroupButton
                .setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        hookWidget(exportGroupButton, SWT.Selection);
    }

    private void createMarkerGroup(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        gridLayout.verticalSpacing = 5;
        gridLayout.horizontalSpacing = 5;
        composite.setLayout(gridLayout);

        Label label = new Label(composite, SWT.NONE);
        label.setLayoutData(
                new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        label.setText(PrefMessages.MarkersPage_Markers_label);

        Composite markerContainer = new Composite(composite, SWT.NONE);
        markerContainer
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        GridLayout gridLayout2 = new GridLayout(2, false);
        gridLayout2.marginWidth = 0;
        gridLayout2.marginHeight = 0;
        gridLayout2.verticalSpacing = 5;
        gridLayout2.horizontalSpacing = 5;
        markerContainer.setLayout(gridLayout2);

        createMarkerViewer(markerContainer);
        createMarkerControls(markerContainer);
    }

    /**
     * @param markerContainer
     */
    private void createMarkerViewer(Composite parent) {
        markerViewer = new TableViewer(parent, SWT.SINGLE | SWT.BORDER);
        markerViewer.setContentProvider(new MarkerGroupContentProvider());
        final MarkerGroupLabelProvider labelProvider = new MarkerGroupLabelProvider();
        markerViewer.setLabelProvider(labelProvider);
        markerViewer.getControl()
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        markerViewer
                .addSelectionChangedListener(new ISelectionChangedListener() {
                    public void selectionChanged(SelectionChangedEvent event) {
                        refreshButtons();
                    }
                });
        markerViewer.setColumnProperties(new String[] { "NAME" }); //$NON-NLS-1$
        markerViewer.setCellEditors(new CellEditor[] {
                new TextCellEditor(markerViewer.getTable(), SWT.SINGLE) });
        markerViewer.setCellModifier(new ICellModifier() {
            public void modify(Object element, String property, Object value) {
                if (element instanceof Widget) {
                    element = ((Widget) element).getData();
                }
                if (element instanceof IMarker) {
                    changeMarkerName((IMarker) element, value.toString());
                }
            }

            public Object getValue(Object element, String property) {
                return labelProvider.getText(element);
            }

            public boolean canModify(Object element, String property) {
                return true;
            }
        });
        markerViewer.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent event) {
                renameMarker();
            }
        });
    }

    /**
     * @param markerContainer
     */
    private void createMarkerControls(Composite parent) {
        Composite outer = new Composite(parent, SWT.NONE);
        outer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        gridLayout.verticalSpacing = 0;
        gridLayout.horizontalSpacing = 0;
        outer.setLayout(gridLayout);

        Composite inner = new Composite(outer, SWT.NONE);
        inner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
        GridLayout gridLayout2 = new GridLayout(1, false);
        gridLayout2.marginWidth = 0;
        gridLayout2.marginHeight = 0;
        gridLayout2.verticalSpacing = 5;
        gridLayout2.horizontalSpacing = 0;
        inner.setLayout(gridLayout2);

        addMarkerButton = new Button(inner, SWT.PUSH);
        addMarkerButton.setText(PrefMessages.MarkersPage_AddMarker_text);
        addMarkerButton
                .setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        hookWidget(addMarkerButton, SWT.Selection);

        removeMarkerButton = new Button(inner, SWT.PUSH);
        removeMarkerButton.setText(PrefMessages.MarkersPage_RemoveMarker_text);
        removeMarkerButton
                .setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        hookWidget(removeMarkerButton, SWT.Selection);

        renameMarkerButton = new Button(inner, SWT.PUSH);
        renameMarkerButton.setText(PrefMessages.MarkersPage_RenameMarker_text);
        renameMarkerButton
                .setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        hookWidget(renameMarkerButton, SWT.Selection);
    }

    private void hookWidget(Widget widget, int eventType) {
        widget.addListener(eventType, this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    public boolean performOk() {
        saveSheet();
        return super.performOk();
    }

    public void init(IWorkbench workbench) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.
     * Event)
     */
    public void handleEvent(Event event) {
        if (event.widget == addGroupButton) {
            addGroup();
        } else if (event.widget == removeGroupButton) {
            removeGroup();
        } else if (event.widget == renameGroupButton) {
            renameGroup();
        } else if (event.widget == exportGroupButton) {
            exportGroup();
        } else if (event.widget == addMarkerButton) {
            addMarker();
        } else if (event.widget == removeMarkerButton) {
            removeMarker();
        } else if (event.widget == renameMarkerButton) {
            renameMarker();
        }
    }

    /**
     * 
     */
    private void addGroup() {
        String name = createGroupName();
        IMarkerGroup group = getSheet().createMarkerGroup(false);
        group.setName(name);
        getSheet().addMarkerGroup(group);
        groupViewer.refresh();
        groupViewer.setSelection(new StructuredSelection(group), true);
        groupViewer.editElement(group, 0);
    }

    private String createGroupName() {
        int index = getSheet().getMarkerGroups().size() + 1;
        String name = NLS.bind(PrefMessages.MarkersPage_DefaultGroupName,
                index);
        while (isNameExisting(name)) {
            index++;
            name = NLS.bind(PrefMessages.MarkersPage_DefaultGroupName, index);
        }
        return name;
    }

    private boolean isNameExisting(String name) {
        for (IMarkerGroup group : getSheet().getMarkerGroups()) {
            if (name.equals(group.getName()))
                return true;
        }
        return false;
    }

    /**
     * 
     */
    private void removeGroup() {
        IStructuredSelection selection = (IStructuredSelection) groupViewer
                .getSelection();
        if (selection.isEmpty())
            return;

        List<IMarkerGroup> markerGroups = ((IMarkerSheet) groupViewer
                .getInput()).getMarkerGroups();
        int index = markerGroups.indexOf(selection.getFirstElement());

        IMarkerSheet sheet = getSheet();
        for (Object group : selection.toList()) {
            List<IMarker> markers = ((IMarkerGroup) group).getMarkers();
            for (IMarker marker : markers) {
                if (MindMapUI.getResourceManager().getRecentMarkerGroup()
                        .getMarker(marker.getId()) != null)
                    MindMapUI.getResourceManager().getRecentMarkerGroup()
                            .removeMarker(marker);
                removeMarkerFile(marker);
            }
            sheet.removeMarkerGroup((IMarkerGroup) group);
        }
        groupViewer.refresh();

        //select the next one
        markerGroups = ((IMarkerSheet) groupViewer.getInput())
                .getMarkerGroups();
        if (index >= markerGroups.size()) {
            index = markerGroups.size() - 1;
        }
        if (index >= 0) {
            if (!markerGroups.isEmpty()) {
                groupViewer.setSelection(
                        new StructuredSelection(markerGroups.get(index)));
            }
        }
    }

    /**
     * 
     */
    private void renameGroup() {
        IStructuredSelection selection = (IStructuredSelection) groupViewer
                .getSelection();
        if (selection.isEmpty())
            return;
        groupViewer.editElement(selection.getFirstElement(), 0);
    }

    private void exportGroup() {
        MarkerExportWizard wizard = new MarkerExportWizard();
        wizard.init(PlatformUI.getWorkbench(), null);
        WizardDialog dialog = new WizardDialog(getShell(), wizard);
        dialog.create();
        dialog.open();
    }

    /**
     * 
     */
    private void addMarker() {
        IStructuredSelection groupSelection = (IStructuredSelection) groupViewer
                .getSelection();
        if (groupSelection.isEmpty())
            return;

        IMarkerGroup targetGroup = (IMarkerGroup) groupSelection
                .getFirstElement();

        String[] sourcePaths = selectImageFile();
        if (sourcePaths == null)
            return;

        List<IMarker> newMarkers = new ArrayList<IMarker>(sourcePaths.length);
        for (String sourcePath : sourcePaths) {
            if (imageValid(sourcePath)) {
                String path = Core.getIdFactory().createId()
                        + FileUtils.getExtension(sourcePath);
                IMarker marker = getSheet().createMarker(path);
                marker.setName(FileUtils.getFileName(sourcePath));
                IMarkerResource resource = marker.getResource();
                if (resource != null) {
                    OutputStream os = resource.getOutputStream();
                    if (os != null) {
                        try {
                            FileInputStream is = new FileInputStream(
                                    sourcePath);
                            FileUtils.transfer(is, os, true);
                        } catch (IOException e) {
                            Logger.log(e);
                        }
                    }
                }
                targetGroup.addMarker(marker);
                newMarkers.add(marker);
            }
        }

        markerViewer.refresh();
        markerViewer.setSelection(new StructuredSelection(newMarkers), true);
    }

    private boolean imageValid(String sourcePath) {
        try {
            new Image(Display.getCurrent(), sourcePath).dispose();
            return true;
        } catch (Throwable e) {
        }
        return false;
    }

    /**
     * @return
     */
    private String[] selectImageFile() {
        FileDialog dialog = new FileDialog(getShell(), SWT.OPEN | SWT.MULTI);
        DialogUtils.makeDefaultImageSelectorDialog(dialog, true);
        String open = dialog.open();
        if (open == null)
            return null;
        String parent = dialog.getFilterPath();
        String[] fileNames = dialog.getFileNames();
        String[] paths = new String[fileNames.length];
        for (int i = 0; i < fileNames.length; i++) {
            paths[i] = new File(parent, fileNames[i]).getAbsolutePath();
        }
        return paths;
    }

    /**
     * 
     */
    private void removeMarker() {
        IStructuredSelection selection = (IStructuredSelection) markerViewer
                .getSelection();
        if (selection.isEmpty())
            return;

        IStructuredSelection groupSelection = (IStructuredSelection) groupViewer
                .getSelection();
        if (groupSelection.isEmpty())
            return;

        List<IMarker> markers = ((IMarkerGroup) markerViewer.getInput())
                .getMarkers();
        int index = markers.indexOf(selection.getFirstElement());

        IMarkerGroup group = (IMarkerGroup) groupSelection.getFirstElement();
        for (Object marker : selection.toList()) {
            group.removeMarker((IMarker) marker);
            if (MindMapUI.getResourceManager().getRecentMarkerGroup()
                    .getMarker(((IMarker) marker).getId()) != null)
                MindMapUI.getResourceManager().getRecentMarkerGroup()
                        .removeMarker((IMarker) marker);
            removeMarkerFile((IMarker) marker);
        }
        markerViewer.refresh();

        //select the next one
        markers = ((IMarkerGroup) markerViewer.getInput()).getMarkers();
        if (index >= markers.size()) {
            index = markers.size() - 1;
        }
        if (index >= 0) {
            if (!markers.isEmpty()) {
                markerViewer.setSelection(
                        new StructuredSelection(markers.get(index)));
            }
        }
    }

    private void removeMarkerFile(IMarker marker) {
        if (marker == null)
            return;
        String path = Core.getWorkspace()
                .getAbsolutePath("markers/" + marker.getResourcePath()); //$NON-NLS-1$
        new File(path).delete();
    }

    /**
     * 
     */
    private void renameMarker() {
        IStructuredSelection selection = (IStructuredSelection) markerViewer
                .getSelection();
        if (selection.isEmpty())
            return;
        markerViewer.editElement(selection.getFirstElement(), 0);
    }

    private void changeGroupName(IMarkerGroup group, String newName) {
        group.setName(newName);
        groupViewer.update(group, null);
    }

    private void refreshButtons() {
        IStructuredSelection groupSelection = (IStructuredSelection) groupViewer
                .getSelection();
        removeGroupButton.setEnabled(!groupSelection.isEmpty());
        renameGroupButton.setEnabled(!groupSelection.isEmpty());
        exportGroupButton.setEnabled(!groupSelection.isEmpty());

        addMarkerButton.setEnabled(!groupSelection.isEmpty());

        IStructuredSelection markerSelection = (IStructuredSelection) markerViewer
                .getSelection();
        removeMarkerButton.setEnabled(!markerSelection.isEmpty());
        renameMarkerButton.setEnabled(!markerSelection.isEmpty());
    }

    private void changeMarkerName(IMarker marker, String newName) {
        marker.setName(newName);
        markerViewer.update(marker, null);
    }

    private static IMarkerSheet getSheet() {
        return MindMapUI.getResourceManager().getUserMarkerSheet();
    }

    private static void saveSheet() {
        MindMapUI.getResourceManager().saveUserMarkerSheet();
    }

}