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
package org.xmind.ui.internal.editor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Layer;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.util.Util;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.ISaveablePart2;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.AbstractHyperlink;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.internal.util.BundleUtility;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.progress.IProgressService;
import org.eclipse.ui.statushandlers.IStatusAdapterConstants;
import org.eclipse.ui.statushandlers.StatusAdapter;
import org.xmind.core.Core;
import org.xmind.core.CoreException;
import org.xmind.core.IMeta;
import org.xmind.core.IRelationship;
import org.xmind.core.IRelationshipEnd;
import org.xmind.core.ISheet;
import org.xmind.core.ISheetComponent;
import org.xmind.core.ITopic;
import org.xmind.core.ITopicComponent;
import org.xmind.core.IWorkbook;
import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.CoreEventRegister;
import org.xmind.core.event.ICoreEventListener;
import org.xmind.core.event.ICoreEventRegister;
import org.xmind.core.event.ICoreEventSource2;
import org.xmind.core.internal.InternalCore;
import org.xmind.core.internal.dom.WorkbookImpl;
import org.xmind.core.util.FileUtils;
import org.xmind.gef.EditDomain;
import org.xmind.gef.GEF;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.command.Command;
import org.xmind.gef.command.CompoundCommand;
import org.xmind.gef.command.ICommandStack;
import org.xmind.gef.image.ResizeConstants;
import org.xmind.gef.ui.actions.IActionRegistry;
import org.xmind.gef.ui.actions.RedoAction;
import org.xmind.gef.ui.actions.UndoAction;
import org.xmind.gef.ui.editor.GraphicalEditor;
import org.xmind.gef.ui.editor.GraphicalEditorPagePopupPreviewHelper;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.ui.IPreSaveInteractiveFeedback;
import org.xmind.ui.IPreSaveInteractiveProvider;
import org.xmind.ui.IWordContextProvider;
import org.xmind.ui.actions.MindMapActionFactory;
import org.xmind.ui.blackbox.BlackBox;
import org.xmind.ui.blackbox.IBlackBoxMap;
import org.xmind.ui.commands.ModifyFoldedCommand;
import org.xmind.ui.commands.ModifyTitleTextCommand;
import org.xmind.ui.commands.MoveSheetCommand;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.internal.MindMapWordContextProvider;
import org.xmind.ui.internal.actions.CopySheetAction;
import org.xmind.ui.internal.actions.CreateSheetAction;
import org.xmind.ui.internal.actions.DeleteOtherSheetsAction;
import org.xmind.ui.internal.actions.DeleteSheetAction;
import org.xmind.ui.internal.actions.DuplicateSheetAction;
import org.xmind.ui.internal.actions.PasteSheetAction;
import org.xmind.ui.internal.actions.ShowPropertiesAction;
import org.xmind.ui.internal.dialogs.DialogMessages;
import org.xmind.ui.internal.dialogs.DialogUtils;
import org.xmind.ui.internal.findreplace.IFindReplaceOperationProvider;
import org.xmind.ui.internal.mindmap.MindMapEditDomain;
import org.xmind.ui.internal.mindmap.MindMapState;
import org.xmind.ui.internal.statushandlers.IRuntimeErrorDialogExtension;
import org.xmind.ui.internal.statushandlers.RuntimeErrorDialog;
import org.xmind.ui.internal.views.BlackBoxView;
import org.xmind.ui.mindmap.IMindMap;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.IWorkbookRef;
import org.xmind.ui.mindmap.MindMap;
import org.xmind.ui.mindmap.MindMapImageExporter;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.prefs.PrefConstants;
import org.xmind.ui.resources.ColorUtils;
import org.xmind.ui.resources.FontUtils;
import org.xmind.ui.tabfolder.IPageMoveListener;
import org.xmind.ui.tabfolder.IPageTitleChangedListener;
import org.xmind.ui.tabfolder.PageMoveHelper;
import org.xmind.ui.tabfolder.PageTitleEditor;
import org.xmind.ui.util.Logger;
import org.xmind.ui.util.MindMapUtils;

public class MindMapEditor extends GraphicalEditor implements ISaveablePart2,
        ICoreEventListener, IPageMoveListener, IPageTitleChangedListener,
        IWorkbookReferrer, IRuntimeErrorDialogExtension {

    private static class MindMapEditorPagePopupPreviewHelper
            extends GraphicalEditorPagePopupPreviewHelper {

        private static final int MIN_PREVIEW_WIDTH = 600;

        private static final int MIN_PREVIEW_HEIGHT = 600;

        public MindMapEditorPagePopupPreviewHelper(IGraphicalEditor editor,
                CTabFolder tabFolder) {
            super(editor, tabFolder);
        }

        protected Rectangle calcContentsBounds(IFigure contents,
                IGraphicalViewer viewer) {
            Rectangle bounds = super.calcContentsBounds(contents, viewer);
            int max = Math.max(bounds.width, bounds.height) + 50;

            int newWidth = bounds.width;
            if (newWidth < MIN_PREVIEW_WIDTH) {
                newWidth = MIN_PREVIEW_WIDTH;
            }
            if (newWidth < max) {
                newWidth = max;
            }

            if (newWidth != bounds.width) {
                int ex = (newWidth - bounds.width) / 2;
                Rectangle b = contents.getBounds();
                int right = bounds.x + bounds.width;
                bounds.x = Math.max(b.x, bounds.x - ex);
                bounds.width = Math.min(b.x + b.width, right + ex) - bounds.x;
            }

            int newHeight = bounds.height;
            if (newHeight < MIN_PREVIEW_HEIGHT) {
                newHeight = MIN_PREVIEW_HEIGHT;
            }
            if (newHeight < max) {
                newHeight = max;
            }
            if (newHeight != bounds.height) {
                int ex = (newHeight - bounds.height) / 2;
                Rectangle b = contents.getBounds();
                int bottom = bounds.y + bounds.height;
                bounds.y = Math.max(b.y, bounds.y - ex);
                bounds.height = Math.min(b.y + b.height, bottom + ex)
                        - bounds.y;
            }
            return bounds;
        }

    }

    protected class MindMapEditorBackCover extends DialogPaneContainer {

        private Font bigFont;

        @Override
        public void createControl(Composite parent) {
            super.createControl(parent);
            createBigFont(parent.getDisplay());
        }

        private void createBigFont(Display display) {
            Font base = display.getSystemFont();
            FontData[] fontData = base.getFontData();
            int increment;
            if ((Util.isMac()) && System.getProperty(
                    "org.eclipse.swt.internal.carbon.smallFonts") != null) { //$NON-NLS-1$
                increment = 3;
            } else {
                increment = 1;
            }
            for (FontData fd : fontData) {
                fd.setHeight(fd.getHeight() + increment);
            }
            this.bigFont = new Font(display, fontData);
        }

        @Override
        protected void handleDispose() {
            if (bigFont != null) {
                bigFont.dispose();
                bigFont = null;
            }
            super.handleDispose();
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xmind.ui.internal.editor.DialogPaneContainer#close()
         */
        @Override
        public boolean close() {
            boolean ret = super.close();
            if (ret) {
                if (pageBook != null && !pageBook.isDisposed()) {
                    pageBook.showPage(pageContainer);
                    if (isEditorActive()) {
                        MindMapEditor.this.setFocus();
                    }
                }
            }
            return ret;
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.xmind.ui.internal.editor.DialogPaneContainer#showDialog(org.xmind
         * .ui.internal.editor.IDialogPane)
         */
        @Override
        protected void showDialog(IDialogPane dialog) {
            pageBook.showPage(getControl());
            if (isEditorActive()) {
                MindMapEditor.this.setFocus();
            }
            super.showDialog(dialog);
        }

    }

    private class MindMapEditorSelectionProvider
            extends MultiPageSelectionProvider {
        /*
         * (non-Javadoc)
         * 
         * @see
         * org.xmind.ui.tabfolder.DelegatedSelectionProvider#setSelection(org
         * .eclipse.jface.viewers.ISelection)
         */
        @Override
        public void setSelection(ISelection selection) {
            if (selection instanceof IStructuredSelection) {
                for (Object element : ((IStructuredSelection) selection)
                        .toList()) {
                    if (element instanceof ITopicComponent) {
                        setSelectionAndUnfold(element);
                    } else if (element instanceof IRelationship) {
                        IRelationship r = (IRelationship) element;
                        IRelationshipEnd e1 = r.getEnd1();
                        IRelationshipEnd e2 = r.getEnd2();
                        if (e1 instanceof ITopicComponent) {
                            setSelectionAndUnfold(e1);
                        }
                        if (e2 instanceof ITopicComponent) {
                            setSelectionAndUnfold(e2);

                        }
                    }
                }
            }

            super.setSelection(selection);
        }
    }

    private WorkbookRef workbookRef = null;

    private ICoreEventRegister eventRegister = null;

    private PageTitleEditor pageTitleEditor = null;

    private PageMoveHelper pageMoveHelper = null;

    private MindMapFindReplaceOperationProvider findReplaceOperationProvider = null;

    private EditorInputMonitor inputMonitor = null;

    private PageBook pageBook = null;

    private Composite pageContainer = null;

    private LoadWorkbookJob loadWorkbookJob = null;

    private MindMapEditorBackCover backCover = null;

    private IWordContextProvider wordContextProvider = null;

    private boolean skipNextPreviewImage = false;

    private IContextActivation contextActivation;

    public void init(IEditorSite site, IEditorInput input)
            throws PartInitException {
        if (this.workbookRef == null) {
            try {
                this.workbookRef = WorkbookRefManager.getInstance()
                        .addReferrer(input, this);
            } catch (org.eclipse.core.runtime.CoreException e) {
                throw new PartInitException(NLS.bind(
                        MindMapMessages.MindMapEditor_partInitException_message,
                        input), e);
            }
        }
        super.init(site, input);
        setMiniBarContributor(new MindMapMiniBarContributor());
//        MindMapUI.getEditorHistory().add(MME.getURIFromEditorInput(input));
    }

    protected ISelectionProvider createSelectionProvider() {
        return new MindMapEditorSelectionProvider();
    }

    protected ICommandStack createCommandStack() {
        return workbookRef.getCommandStack();
    }

    protected void disposeCommandStack(ICommandStack commandStack) {
        // No need to dispose command stack here, because the workbook reference
        // manager will dispose unused command stacks automatically.
    }

    public void dispose() {
        MindMapState.getInstance().saveState(getPages());

        if (contextActivation != null) {
            IContextService cs = getSite().getService(IContextService.class);
            cs.deactivateContext(contextActivation);
        }

        uninstallModelListener();

        if (getWorkbook() != null)
            BlackBox.removeSavedMap(getWorkbook().getFile());

        WorkbookRefManager.getInstance().removeReferrer(getEditorInput(), this);
        if (inputMonitor != null) {
            inputMonitor.dispose();
            inputMonitor = null;
        }
        if (loadWorkbookJob != null) {
            loadWorkbookJob.cancel();
            loadWorkbookJob = null;
        }
        if (backCover != null) {
            backCover.dispose();
            backCover = null;
        }
        super.dispose();
        eventRegister = null;
        pageTitleEditor = null;
        pageMoveHelper = null;
        findReplaceOperationProvider = null;
        workbookRef = null;
        pageBook = null;
        pageContainer = null;
    }

    protected Composite createContainerParent(Composite parent) {
        StackLayout layout = new StackLayout();
        parent.setLayout(layout);

        pageBook = new PageBook(parent, SWT.NONE);
        layout.topControl = pageBook;

        backCover = new MindMapEditorBackCover();
        backCover.init(getSite());
        backCover.createControl(pageBook);

        pageContainer = new Composite(pageBook, SWT.NONE);
        IContextService cs = getSite().getService(IContextService.class);
        contextActivation = cs.activateContext(MindMapUI.CONTEXT_MINDMAP);
        return pageContainer;
    }

    @Override
    protected void createEditorContents() {
        super.createEditorContents();

        // Make editor actions:
        createActions(getActionRegistry());

        // Update editor pane title:
        updateNames();

        // Add helpers to handle moving pages, editing page title, showing
        // page popup preview, creating new page, etc.:
        if (getContainer() instanceof CTabFolder) {
            final CTabFolder tabFolder = (CTabFolder) getContainer();
            pageMoveHelper = new PageMoveHelper(tabFolder);
            pageMoveHelper.addListener(this);
            pageTitleEditor = new PageTitleEditor(tabFolder);
            pageTitleEditor.addPageTitleChangedListener(this);
            pageTitleEditor.setContextId(getSite(),
                    "org.xmind.ui.context.mindmap.textEdit"); //$NON-NLS-1$
            new MindMapEditorPagePopupPreviewHelper(this, tabFolder);

        }

        // Let 3rd-party plugins configure this editor:
        MindMapEditorConfigurerManager.getInstance().configureEditor(this);

        // Start monitoring changes to this editor's input source:
        inputMonitor = new EditorInputMonitor(this);

        // Try loading workbook:
        if (getWorkbook() != null) {
            workbookLoaded();
        } else if (loadWorkbookJob == null) {
            loadWorkbookJob = new LoadWorkbookJob(getEditorInput().getName(),
                    workbookRef, backCover, pageBook.getDisplay());
            loadWorkbookJob.addJobChangeListener(new JobChangeAdapter() {
                public void done(IJobChangeEvent event) {
                    loadWorkbookJob = null;
                    if (pageBook == null || pageBook.isDisposed())
                        return;

                    IStatus result = event.getResult();
                    if (result.getSeverity() == IStatus.OK) {
                        pageBook.getDisplay().asyncExec(new Runnable() {
                            public void run() {
                                workbookLoaded();
                            }
                        });
                    } else if (result.getSeverity() == IStatus.CANCEL) {
                        pageBook.getDisplay().asyncExec(new Runnable() {
                            public void run() {
                                closeEditor();
                            }
                        });
                    } else {
                        Throwable error = result.getException();
                        if (error == null) {
                            try {
                                throw new org.eclipse.core.runtime.CoreException(
                                        new Status(IStatus.ERROR,
                                                MindMapUI.PLUGIN_ID,
                                                MindMapMessages.UnexpectedWorkbookLoadFailure_error));
                            } catch (Throwable e) {
                                error = e;
                            }
                        }
                        final Throwable err = error;
                        pageBook.getDisplay().asyncExec(new Runnable() {
                            public void run() {
                                showError(err);
                            }
                        });
                    }
                }
            });
            loadWorkbookJob.schedule();
            fireDirty();
        }
    }

    private void recordEditorHistory(IWorkbook workbook, boolean recordInputURI,
            boolean recordThumbnail) {
        if (workbook != null) {
            String thumbnailSourcePath = workbook.getTempLocation()
                    + File.separator + "Thumbnails/thumbnail.png"; //$NON-NLS-1$
            URI inputURI = MME.getURIFromEditorInput(getEditorInput());

            if (recordInputURI)
                MindMapUI.getEditorHistory().add(inputURI);

            if (recordThumbnail)
                MindMapUI.getEditorHistory().addThumbnail(inputURI,
                        thumbnailSourcePath);
        }
    }

    private void closeEditor() {
        getSite().getPage().closeEditor(this, false);
    }

    private void showError(Throwable exception) {
        StatusAdapter statusAdapter = new StatusAdapter(new Status(
                IStatus.ERROR, MindMapUIPlugin.PLUGIN_ID,
                MindMapMessages.LoadWorkbookJob_errorDialog_title, exception));
        statusAdapter.setProperty(IStatusAdapterConstants.TIMESTAMP_PROPERTY,
                Long.valueOf(System.currentTimeMillis()));
        statusAdapter.setProperty(RuntimeErrorDialog.DIALOG_EXTENSION, this);
//        StatusManager.getManager().handle(statusAdapter,
//                StatusManager.BLOCK | StatusManager.SHOW);
        ErrorDialogPane2 pane = new ErrorDialogPane2(statusAdapter);
        backCover.open(pane);
        closeEditor();
    }

    protected void createActions(IActionRegistry actionRegistry) {
        UndoAction undoAction = new UndoAction(this);
        actionRegistry.addAction(undoAction);
        addCommandStackAction(undoAction);

        RedoAction redoAction = new RedoAction(this);
        actionRegistry.addAction(redoAction);
        addCommandStackAction(redoAction);

        CreateSheetAction createSheetAction = new CreateSheetAction(this);
        actionRegistry.addAction(createSheetAction);

        DeleteSheetAction deleteSheetAction = new DeleteSheetAction(this);
        actionRegistry.addAction(deleteSheetAction);

        DeleteOtherSheetsAction deleteOtherSheetAction = new DeleteOtherSheetsAction(
                this);
        actionRegistry.addAction(deleteOtherSheetAction);

        DuplicateSheetAction duplicateSheetAction = new DuplicateSheetAction(
                this);
        actionRegistry.addAction(duplicateSheetAction);

        CopySheetAction copySheetAction = new CopySheetAction(this);
        actionRegistry.addAction(copySheetAction);

        PasteSheetAction pasteSheetAction = new PasteSheetAction(this);
        actionRegistry.addAction(pasteSheetAction);

        ShowPropertiesAction showPropertiesAction = new ShowPropertiesAction(
                getSite().getWorkbenchWindow());
        actionRegistry.addAction(showPropertiesAction);
    }

    private void configurePage(IGraphicalEditorPage page) {
        MindMapEditorConfigurerManager.getInstance().configurePage(page);
    }

    protected void createPages() {
        if (getWorkbook() == null)
            return;

        for (ISheet sheet : getWorkbook().getSheets()) {
            IGraphicalEditorPage page = createSheetPage(sheet, -1);
            configurePage(page);
        }
        if (getPageCount() > 0) {
            setActivePage(0);
        }
    }

    protected IGraphicalEditorPage createSheetPage(ISheet sheet, int index) {
        IGraphicalEditorPage page = new MindMapEditorPage();
        page.init(this, sheet);
        addPage(page);
        if (index >= 0 && index < getPageCount()) {
            movePageTo(findPage(page), index);
        }
        index = findPage(page);
        if (getActivePage() != index) {
            setActivePage(index);
        }
        page.updatePageTitle();
        MindMapState.getInstance().loadState(page);
        return page;
    }

    protected EditDomain createEditDomain(IGraphicalEditorPage page) {
        MindMapEditDomain domain = new MindMapEditDomain();
        domain.setCommandStack(getCommandStack());
        return domain;
    }

    protected void updateNames() {
        setPartName(getEditorInput().getName());
        setTitleToolTip(getEditorInput().getToolTipText());
    }

    public int promptToSaveOnClose() {
        if (BackgroundWorkbookSaver.getInstance().isRunning()
                && workbookRef != null && workbookRef.willOverwriteTarget()) {
            NullProgressMonitor monitor = new NullProgressMonitor();
            doSave(monitor, true);
            if (monitor.isCanceled())
                return CANCEL;
            return NO;
        }
        return DEFAULT;
    }

    private void saveWorkbook(IProgressMonitor monitor,
            boolean useProgressDialog, final boolean skipNewRevisions) {
        final IWorkbookReferrer previewSaver = this;
        saveWithProgress(new IRunnableWithProgress() {
            public void run(IProgressMonitor monitor)
                    throws InvocationTargetException, InterruptedException {
                try {
                    if (InternalCore.DEBUG_WORKBOOK_SAVE)
                        Logger.log("MindMapEditor: About to save workbook: " //$NON-NLS-1$
                                + getEditorInput().toString());
                    workbookRef.saveWorkbook(monitor, previewSaver,
                            skipNewRevisions);
                    if (InternalCore.DEBUG_WORKBOOK_SAVE)
                        Logger.log("MindMapEditor: Finished saving workbook: " //$NON-NLS-1$
                                + getEditorInput().toString());

                    recordEditorHistory(getWorkbook(), false, true);

                } catch (Exception e) {
                    throw new InvocationTargetException(e,
                            e.getLocalizedMessage());
                }
            }
        }, monitor, useProgressDialog);
    }

    private void saveWorkbookAs(final IEditorInput newInput,
            IProgressMonitor monitor, boolean useProgressDialog,
            final boolean skipNewRevisions) {
        final IWorkbookReferrer previewSaver = this;
        saveWithProgress(new IRunnableWithProgress() {
            public void run(IProgressMonitor monitor)
                    throws InvocationTargetException, InterruptedException {
                try {
                    if (InternalCore.DEBUG_WORKBOOK_SAVE)
                        Logger.log(
                                "MindMapEditor: About to save workbook to a new location: " //$NON-NLS-1$
                                        + getEditorInput().toString() + " -> " //$NON-NLS-1$
                                        + newInput.toString());
                    workbookRef.saveWorkbookAs(newInput, monitor, previewSaver,
                            skipNewRevisions);
                    if (InternalCore.DEBUG_WORKBOOK_SAVE)
                        Logger.log(
                                "MindMapEditor: Finished saving workbook to a new location: " //$NON-NLS-1$
                                        + getEditorInput().toString() + " -> " //$NON-NLS-1$
                                        + newInput.toString());
                    recordEditorHistory(getWorkbook(), true, true);
                } catch (Exception e) {
                    throw new InvocationTargetException(e,
                            e.getLocalizedMessage());
                }
            }
        }, monitor, useProgressDialog);
    }

    private void saveWithProgress(final IRunnableWithProgress runnable,
            final IProgressMonitor monitor, final boolean useProgressDialog) {
        final IProgressService ps = (IProgressService) getSite()
                .getService(IProgressService.class);
        if (ps != null) {
            SafeRunner.run(new SafeRunnable() {
                public void run() throws Exception {
                    try {
                        ps.run(true, true, runnable);
                    } catch (InterruptedException e) {
                        // save canceled.
                    }
                }
            });
        }
    }

    /**
     * 
     * @param fileName
     * @return 0 for Save As, 1 for Overwrite, other for Cancel
     */
    private int promptWorkbookVersion(String fileName) {
        IWorkbook workbook = getWorkbook();
        String oldVersion = workbook.getVersion();
        if (Core.getCurrentVersion().equals(oldVersion))
            return 1;

        String messages = NLS.bind(
                MindMapMessages.MindMapEditor_CompatibilityWarning_OverwritingHigherVersion_message,
                oldVersion);

        MessageDialog dialog = new MessageDialog(getSite().getShell(),
                MindMapMessages.MindMapEditor_CompatibilityWarning_dialogTitle,
                null, messages, MessageDialog.QUESTION,
                new String[] {
                        MindMapMessages.MindMapEditor_CompatibilityWarning_SaveAs_button,
                        MindMapMessages.MindMapEditor_CompatibilityWarning_Overwrite_button,
                        IDialogConstants.CANCEL_LABEL },
                0);
        return dialog.open();
    }

    public void doSave(final IProgressMonitor monitor) {
        doSave(monitor, false);
    }

    private String workbookNameByPreSaveInteractiveProvider;

    private IPreSaveInteractiveFeedback preSaveInteractive(
            int interactiveType) {
        Class<? extends IPreSaveInteractiveProvider> preSaveInteractiveProviderClass = MindMapUIPlugin
                .getDefault().getPreSaveInteractiveProvider();
        if (preSaveInteractiveProviderClass != null) {
            try {
                IPreSaveInteractiveProvider preSaveInteractiveProvider = preSaveInteractiveProviderClass
                        .newInstance();
                return preSaveInteractiveProvider.executeInteractive(this,
                        interactiveType);
            } catch (InstantiationException e) {
            } catch (IllegalAccessException e) {
            }
        }
        return null;
    }

    private void doSave(IProgressMonitor monitor, boolean useProgressDialog) {
        if (!workbookRef.willOverwriteTarget()) {
            String interactiveResult = IPreSaveInteractiveProvider.INSTRUCTION_PROMOTE;

            IPreSaveInteractiveFeedback feedback = preSaveInteractive(
                    IPreSaveInteractiveProvider.TYPE_PRE_SAVE);
            if (feedback != null) {
                interactiveResult = feedback.interactiveFeedback().getProperty(
                        IPreSaveInteractiveFeedback.INTERACTIVE_RESULT);
                workbookNameByPreSaveInteractiveProvider = feedback
                        .interactiveFeedback().getProperty(
                                IPreSaveInteractiveFeedback.INTERACTIVE_FEEDBACK_1);
            }
            if (IPreSaveInteractiveProvider.INSTRUCTION_END == interactiveResult) {
                return;
            } else if (IPreSaveInteractiveProvider.INSTRUCTION_PROMOTE == interactiveResult) {
            }
            doSaveAs(monitor, useProgressDialog);
        } else {
            int ret = promptWorkbookVersion(getPartName());
            if (ret == 0) {
                String interactiveResult = IPreSaveInteractiveProvider.INSTRUCTION_PROMOTE;

                IPreSaveInteractiveFeedback feedback = preSaveInteractive(
                        IPreSaveInteractiveProvider.TYPE_PRE_SAVE_AS);
                if (feedback != null) {
                    interactiveResult = feedback.interactiveFeedback()
                            .getProperty(
                                    IPreSaveInteractiveFeedback.INTERACTIVE_RESULT);
                    workbookNameByPreSaveInteractiveProvider = feedback
                            .interactiveFeedback().getProperty(
                                    IPreSaveInteractiveFeedback.INTERACTIVE_FEEDBACK_1);
                }
                if (IPreSaveInteractiveProvider.INSTRUCTION_END == interactiveResult) {
                    return;
                } else if (IPreSaveInteractiveProvider.INSTRUCTION_PROMOTE == interactiveResult) {
                }
                doSaveAs(monitor, useProgressDialog);
            } else if (ret == 1) {
                BlackBox.doBackup(getWorkbook().getFile());
                saveWorkbook(monitor, useProgressDialog, false);
            }
        }
    }

    public void doSaveAs(IProgressMonitor monitor, String filterExtension,
            String filterName) {
        doSaveAs(monitor, false, filterExtension, filterName);
    }

    public void doSaveAs(final IProgressMonitor monitor,
            final boolean useProgressDialog, final String filterExtension,
            final String filterName) {
        if (getWorkbook() == null)
            return;

        SafeRunner.run(new SafeRunnable() {
            public void run() throws Exception {
                final IEditorInput newInput = createNewEditorInput(monitor,
                        filterExtension, filterName);
                if (newInput == null || monitor.isCanceled())
                    return;
                boolean isTemplate = MindMapUI.FILE_EXT_TEMPLATE
                        .equals(filterExtension);
                ((WorkbookImpl) getWorkbook())
                        .setSkipRevisionsWhenSaving(isTemplate);
                saveWorkbookAs(newInput, monitor, useProgressDialog,
                        isTemplate);
            }
        });
    }

    protected IEditorInput createNewEditorInput(final IProgressMonitor monitor,
            String filterExtension, String filterName)
                    throws org.eclipse.core.runtime.CoreException {
        //TODO Save workbook as workspace resource if installed as plugin.
        return saveAsFile(monitor, filterExtension, filterName);
    }

    private IEditorInput saveAsFile(final IProgressMonitor monitor,
            String filterExtension, String filterName)
                    throws org.eclipse.core.runtime.CoreException {
        String path;
        String extension = filterExtension;
        String proposalName;
        File oldFile = MME.getFile(getEditorInput());
        if (oldFile != null) {
            proposalName = FileUtils.getNoExtensionFileName(oldFile.getName());
            path = oldFile.getParent();
        } else {
            String name = getWorkbook().getPrimarySheet().getRootTopic()
                    .getTitleText();
            proposalName = MindMapUtils.trimFileName(name);
            path = null;
        }

        if (workbookNameByPreSaveInteractiveProvider != null) {
            proposalName = workbookNameByPreSaveInteractiveProvider;
        }

        // Hide busy cursor
        Display display = getSite().getShell().getDisplay();
        Shell[] shells = display.getShells();
        Cursor cursor = display.getSystemCursor(SWT.CURSOR_WAIT);
        for (Shell shell : shells) {
            Cursor cursor2 = shell.getCursor();
            if (cursor2 != null && cursor2.equals(cursor)) {
                shell.setCursor(null);
            }
        }

        // Show save dialog
        String extensionFullName = "*" + extension; //$NON-NLS-1$
        String filterFullName;
        if (Platform.OS_MACOSX.equals(Platform.getOS())) {
            filterFullName = NLS.bind("{0} ({1})", filterName, //$NON-NLS-1$
                    extensionFullName);
        } else {
            filterFullName = filterName;
        }
        String result = DialogUtils.save(getSite().getShell(), proposalName,
                new String[] { extensionFullName },
                new String[] { filterFullName }, 0, path);
        if (result == null) {
            monitor.setCanceled(true);
            return null;
        }

        if ("win32".equals(SWT.getPlatform())) { //$NON-NLS-1$
            if (!result.endsWith(filterExtension)) {
                result = result + filterExtension;
            }
        }

        String lastPath = new File(result).getParent();
        if (lastPath != null) {
            MindMapUIPlugin.getDefault()
                    .getDialogSettings(
                            MindMapUIPlugin.SECTION_IPRESAVEINTERACTIVEPROVIDER)
                    .put(MindMapUIPlugin.IPRESAVEINTERACTIVEPROVIDER_LASTPATH,
                            lastPath);
        }

        return MME.createFileEditorInput(result);
    }

    protected void doSaveAs(final IProgressMonitor monitor,
            boolean useProgressDialog) {
        doSaveAs(monitor, useProgressDialog, MindMapUI.FILE_EXT_XMIND,
                DialogMessages.WorkbookFilterName);
    }

    public void doSaveAs() {
        String interactiveResult = IPreSaveInteractiveProvider.INSTRUCTION_PROMOTE;

        IPreSaveInteractiveFeedback feedback = preSaveInteractive(
                IPreSaveInteractiveProvider.TYPE_PRE_SAVE_AS);
        if (feedback != null) {
            interactiveResult = feedback.interactiveFeedback().getProperty(
                    IPreSaveInteractiveFeedback.INTERACTIVE_RESULT);
            workbookNameByPreSaveInteractiveProvider = feedback
                    .interactiveFeedback().getProperty(
                            IPreSaveInteractiveFeedback.INTERACTIVE_FEEDBACK_1);
        }
        if (IPreSaveInteractiveProvider.INSTRUCTION_END == interactiveResult) {
            return;
        } else if (IPreSaveInteractiveProvider.INSTRUCTION_PROMOTE == interactiveResult) {
        }
        doSaveAs(new NullProgressMonitor(), false);
    }

    public boolean isSaveAsAllowed() {
        return getWorkbook() != null;
    }

    public IWorkbookRef getWorkbookRef() {
        return workbookRef;
    }

    public IWorkbook getWorkbook() {
        if (workbookRef == null)
            return null;
        return workbookRef.getWorkbook();
    }

    @Override
    protected <T> T getEditorAdapter(Class<T> adapter) {
        if (workbookRef != null) {
            T result = workbookRef.getAdapter(adapter);
            if (result != null)
                return result;
        }

        if (adapter == IWorkbookRef.class) {
            return adapter.cast(getWorkbookRef());
        } else if (adapter == IWorkbook.class) {
            return adapter.cast(getWorkbook());
        } else if (adapter == PageTitleEditor.class) {
            return adapter.cast(pageTitleEditor);
        } else if (adapter == PageMoveHelper.class) {
            return adapter.cast(pageMoveHelper);
        } else if (adapter == IFindReplaceOperationProvider.class) {
            if (findReplaceOperationProvider == null) {
                findReplaceOperationProvider = new MindMapFindReplaceOperationProvider(
                        this);
            }
            return adapter.cast(findReplaceOperationProvider);
        } else if (adapter == IWordContextProvider.class) {
            if (wordContextProvider == null) {
                wordContextProvider = new MindMapWordContextProvider(this);
            }
            return adapter.cast(wordContextProvider);
        } else if (adapter == IDialogPaneContainer.class) {
            return adapter.cast(backCover);
        } else if (adapter == LoadWorkbookJob.class) {
            return adapter.cast(loadWorkbookJob);
        }
        return super.getEditorAdapter(adapter);
    }

    protected void installModelListener() {
        IWorkbook workbook = getWorkbook();
        eventRegister = new CoreEventRegister(workbook, this);
        eventRegister.register(Core.SheetAdd);
        eventRegister.register(Core.SheetRemove);
        eventRegister.register(Core.SheetMove);
        eventRegister.register(Core.PasswordChange);
        eventRegister.register(Core.WorkbookPreSaveOnce);
    }

    protected void uninstallModelListener() {
        if (eventRegister != null) {
            eventRegister.unregisterAll();
            eventRegister = null;
        }
    }

    public void handleCoreEvent(final CoreEvent event) {
        PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
            public void run() {
                String type = event.getType();
                if (Core.WorkbookPreSaveOnce.equals(type)) {
                    fireDirty();
                    firePropertyChange(PROP_INPUT);
                } else if (Core.SheetAdd.equals(type)) {
                    ISheet sheet = (ISheet) event.getTarget();
                    int index = event.getIndex();
                    IGraphicalEditorPage page = createSheetPage(sheet, index);
                    configurePage(page);
                } else if (Core.SheetRemove.equals(type)) {
                    ISheet sheet = (ISheet) event.getTarget();
                    IGraphicalEditorPage page = findPage(sheet);
                    if (page != null) {
                        removePage(page);
                    }
                } else if (Core.SheetMove.equals(type)) {
                    int oldIndex = event.getIndex();
                    int newIndex = ((ISheet) event.getTarget()).getIndex();
                    movePageTo(oldIndex, newIndex);
                } else if (Core.PasswordChange.equals(type)) {
                    IWorkbook workbook = getWorkbook();
                    if (workbook instanceof ICoreEventSource2) {
                        ((ICoreEventSource2) workbook)
                                .registerOnceCoreEventListener(
                                        Core.WorkbookPreSaveOnce,
                                        ICoreEventListener.NULL);
                    }
                }
            }
        });
    }

    public boolean isDirty() {
        return workbookRef != null && workbookRef.isDirty();
    }

    protected void saveAndRun(Command command) {
        ICommandStack cs = getCommandStack();
        if (cs != null)
            cs.execute(command);
    }

    public void pageMoved(int fromIndex, int toIndex) {
        IWorkbook workbook = getWorkbook();
        MoveSheetCommand command = new MoveSheetCommand(workbook, fromIndex,
                toIndex);
        command.setLabel(""); //$NON-NLS-1$
        saveAndRun(command);
    }

    public void pageTitleChanged(int pageIndex, String newValue) {
        IGraphicalEditorPage page = getPage(pageIndex);
        if (page != null) {
            Object pageInput = page.getInput();
            if (pageInput instanceof ISheet) {
                ModifyTitleTextCommand command = new ModifyTitleTextCommand(
                        (ISheet) pageInput, newValue);
                command.setLabel(""); //$NON-NLS-1$
                saveAndRun(command);
            }
        }
    }

    protected void createSheet() {
        IAction action = getActionRegistry()
                .getAction(MindMapActionFactory.NEW_SHEET.getId());
        if (action != null && action.isEnabled()) {
            action.run();
        }
    }

    @Override
    public void setFocus() {
        if (workbookRef != null) {
            workbookRef.setPrimaryReferrer(this);
        }
        if (backCover != null && backCover.isOpen()) {
            backCover.setFocus();
        } else {
            super.setFocus();
        }
    }

    public void openEncryptionDialog() {
        if (pageBook == null || pageBook.isDisposed())
            return;
        backCover.open(new EncryptionDailogPane(getWorkbookRef()));
    }

    public ISelectionProvider getSelectionProvider() {
        return getSite().getSelectionProvider();
    }

    public void reveal() {
        getSite().getPage().activate(this);
        setFocus();
    }

    public void savePreivew(final IWorkbook workbook,
            final IProgressMonitor monitor) throws IOException, CoreException {
        if (workbook == null)
            throw new IllegalArgumentException();

        if (workbook.getPassword() != null) {
            URL url = BundleUtility.find(MindMapUI.PLUGIN_ID,
                    IMindMapImages.ENCRYPTED_THUMBNAIL);
            if (url != null) {
                savePreviewFromURL(url, workbook);
            }
        } else if (MindMapUIPlugin.getDefault().getPreferenceStore().getBoolean(
                PrefConstants.PREVIEW_SKIPPED) || skipNextPreviewImage) {
            URL url = BundleUtility.find(MindMapUI.PLUGIN_ID,
                    IMindMapImages.DEFAULT_THUMBNAIL);
            if (url != null) {
                savePreviewFromURL(url, workbook);
            }
            skipNextPreviewImage = false;
        } else if (getPageCount() > 0) {
            Shell parentShell = getSite().getShell();
            final Display display = parentShell.getDisplay();
            display.syncExec(new Runnable() {
                public void run() {
                    MindMapImageExporter exporter = new MindMapImageExporter(
                            display);
                    IGraphicalViewer sourceViewer = getPage(0).getViewer();
                    IMindMap map = (IMindMap) sourceViewer
                            .getAdapter(IMindMap.class);
                    if (map == null || map.getCentralTopic()
                            .equals(map.getSheet().getRootTopic())) {
                        exporter.setSourceViewer(sourceViewer, null, null,
                                new Insets(MindMapUI.DEFAULT_EXPORT_MARGIN));
                    } else {
                        exporter.setSource(new MindMap(map.getSheet()), null,
                                new Insets(MindMapUI.DEFAULT_EXPORT_MARGIN));
                    }
                    exporter.setResize(ResizeConstants.RESIZE_MAXPIXELS, 1280,
                            1024);
                    exporter.setTargetWorkbook(workbook);

                    exporter.export();
                    org.eclipse.draw2d.geometry.Point origin = exporter
                            .calcRelativeOrigin();
                    workbook.getMeta().setValue(IMeta.ORIGIN_X,
                            String.valueOf(origin.x));
                    workbook.getMeta().setValue(IMeta.ORIGIN_Y,
                            String.valueOf(origin.y));
                    workbook.getMeta().setValue(IMeta.BACKGROUND_COLOR,
                            getBackgroundColor(sourceViewer));
                }
            });

        }
    }

    private String getBackgroundColor(IGraphicalViewer sourceViewer) {
        Layer layer = sourceViewer.getLayer(GEF.LAYER_BACKGROUND);
        if (layer != null) {
            Color color = layer.getBackgroundColor();
            if (color != null)
                return ColorUtils.toString(color);
        }
        return "#ffffff"; //$NON-NLS-1$
    }

    /**
     * @param url
     */
    private void savePreviewFromURL(URL url, IWorkbook workbook)
            throws IOException {
        MindMapImageExporter exporter = new MindMapImageExporter(
                getSite().getShell().getDisplay());
        InputStream stream = url.openStream();
        exporter.setTargetWorkbook(workbook);
        exporter.export(stream);
    }

    public void postSave(final IProgressMonitor monitor) {
        getSite().getShell().getDisplay().syncExec(new Runnable() {
            public void run() {
                superDoSave(monitor);
            }
        });
    }

    private void superDoSave(IProgressMonitor monitor) {
        super.doSave(monitor);
    }

    public void postSaveAs(final Object newKey,
            final IProgressMonitor monitor) {
        getSite().getShell().getDisplay().syncExec(new Runnable() {
            public void run() {
                if (newKey instanceof IEditorInput) {
                    setInput((IEditorInput) newKey);
                    firePropertyChange(PROP_INPUT);
                }
                superDoSave(monitor);
                updateNames();
            }
        });
    }

    public void setSelection(ISelection selection, boolean reveal,
            boolean forceFocus) {
        ISelectionProvider selectionProvider = getSite().getSelectionProvider();
        if (selectionProvider != null) {
            selectionProvider.setSelection(selection);
        }
        if (forceFocus) {
            getSite().getPage().activate(this);
            Shell shell = getSite().getShell();
            if (shell != null && !shell.isDisposed()) {
                shell.setActive();
            }
        } else if (reveal) {
            getSite().getPage().bringToTop(this);
        }
    }

    public IGraphicalEditorPage findPage(Object input) {
        if (input instanceof IMindMap) {
            input = ((IMindMap) input).getSheet();
        }
        return super.findPage(input);
    }

    private void workbookLoaded() {
        if (pageBook == null || pageBook.isDisposed())
            return;
        backCover.close();
        Assert.isTrue(getWorkbook() != null);
        createPages();
        if (isEditorActive()) {
            setFocus();
        }
        installModelListener();
        firePropertyChange(PROP_INPUT);
        fireDirty();
        WorkbookRefManager.getInstance().hibernateAll();

        recordEditorHistory(getWorkbook(), true, true);
    }

    private boolean isEditorActive() {
        return getSite().getPage().getActiveEditor() == this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.gef.ui.editor.GraphicalEditor#findOwnedInput(org.eclipse.jface
     * .viewers.ISelection)
     */
    @Override
    protected Object findOwnedInput(ISelection selection) {
        if (selection instanceof IStructuredSelection) {
            Object[] elements = ((IStructuredSelection) selection).toArray();
            for (Object element : elements) {
                if (element instanceof ISheetComponent)
                    return ((ISheetComponent) element).getOwnedSheet();
                if (element instanceof ISheet)
                    return (ISheet) element;
            }
        }
        return super.findOwnedInput(selection);
    }

    public void skipNextPreviewImage() {
        this.skipNextPreviewImage = true;
    }

    private void setSelectionAndUnfold(Object element) {
        List<Command> showElementsCommands = new ArrayList<Command>(1);
        ITopic parent = ((ITopicComponent) element).getParent();
        while (parent != null) {
            if (parent.isFolded()) {
                showElementsCommands
                        .add(new ModifyFoldedCommand(parent, false));
            }
            parent = parent.getParent();
        }
        if (!showElementsCommands.isEmpty()) {
            Command command = new CompoundCommand(
                    showElementsCommands.get(0).getLabel(),
                    showElementsCommands);
            saveAndRun(command);
        }
    }

    public Control createDialogExtension(Composite parent) {
        IBlackBoxMap[] blackBoxMaps = BlackBox.getMaps();
        if (!MindMapUIPlugin.getDefault().getPreferenceStore()
                .getBoolean(PrefConstants.AUTO_BACKUP_ENABLE)
                || (blackBoxMaps == null || blackBoxMaps.length == 0))
            return null;
        Composite hyperParent = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        hyperParent.setLayout(layout);
        hyperParent.setBackground(parent.getBackground());

        Label preLink = new Label(hyperParent, SWT.NONE);
        preLink.setText(
                MindMapMessages.LoadWorkbookJob_errorDialog_Pre_message);
        preLink.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        preLink.setBackground(parent.getBackground());

        Hyperlink hyperlink = new Hyperlink(hyperParent, SWT.NONE);
        hyperlink.setBackground(parent.getBackground());
        hyperlink.setUnderlined(true);
        hyperlink.setText(
                MindMapMessages.LoadWorkbookJob_errorDialog_GoToBackup_message);
        hyperlink
                .setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        hyperlink.addHyperlinkListener(new HyperlinkAdapter() {
            @Override
            public void linkActivated(HyperlinkEvent e) {
                showBlackBoxView();
            }
        });
        hyperlink.setFont(
                FontUtils.getBoldRelative(JFaceResources.DEFAULT_FONT, 0));
        /* Prevent focus box from being painted: */
        try {
            Field fPaintFocus = AbstractHyperlink.class
                    .getDeclaredField("paintFocus"); //$NON-NLS-1$
            fPaintFocus.setAccessible(true);
            fPaintFocus.set(hyperlink, false);
        } catch (Throwable e) {
            // ignore
        }

        return hyperParent;
    }

    private void showBlackBoxView() {
        if (PlatformUI.isWorkbenchRunning()) {
            IWorkbenchWindow window = PlatformUI.getWorkbench()
                    .getActiveWorkbenchWindow();
            if (window != null) {
                final IWorkbenchPage page = window.getActivePage();
                if (page != null) {
                    SafeRunner.run(new SafeRunnable() {
                        public void run() throws Exception {
                            BlackBoxView blackBoxView = (BlackBoxView) page
                                    .showView(MindMapUI.VIEW_BLACKBOX);
                            File damagedFile = MME.getFile(getEditorInput());
                            blackBoxView.setDamagedFile(damagedFile);
                        }
                    });
                }
            }
        }
    }

}