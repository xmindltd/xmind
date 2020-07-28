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
package org.xmind.ui.internal.editor;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorSupportProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.util.Util;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.ISaveablePart2;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.internal.util.BundleUtility;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.progress.IProgressService;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;
import org.eclipse.ui.services.IEvaluationService;
import org.eclipse.ui.statushandlers.IStatusAdapterConstants;
import org.eclipse.ui.statushandlers.StatusAdapter;
import org.eclipse.ui.statushandlers.StatusManager;
import org.xmind.core.Core;
import org.xmind.core.CoreException;
import org.xmind.core.IFileEntry;
import org.xmind.core.IManifest;
import org.xmind.core.IRelationship;
import org.xmind.core.IRelationshipEnd;
import org.xmind.core.ISheet;
import org.xmind.core.ISheetComponent;
import org.xmind.core.ISheetSettings;
import org.xmind.core.ITopic;
import org.xmind.core.ITopicComponent;
import org.xmind.core.IWorkbook;
import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.CoreEventRegister;
import org.xmind.core.event.ICoreEventListener;
import org.xmind.core.event.ICoreEventRegister;
import org.xmind.core.event.ICoreEventSource2;
import org.xmind.core.event.ICoreEventSupport;
import org.xmind.core.internal.UserDataConstants;
import org.xmind.core.util.FileUtils;
import org.xmind.gef.EditDomain;
import org.xmind.gef.IEditDomainListener;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.command.Command;
import org.xmind.gef.command.CommandStackEvent;
import org.xmind.gef.command.CompoundCommand;
import org.xmind.gef.command.ICommandStack;
import org.xmind.gef.image.ResizeConstants;
import org.xmind.gef.tool.ITool;
import org.xmind.gef.ui.actions.IActionRegistry;
import org.xmind.gef.ui.actions.RedoAction;
import org.xmind.gef.ui.actions.UndoAction;
import org.xmind.gef.ui.editor.GraphicalEditor;
import org.xmind.gef.ui.editor.IEditable;
import org.xmind.gef.ui.editor.IEditingContext;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.gef.ui.editor.IInteractiveMessage;
import org.xmind.ui.IWordContextProvider;
import org.xmind.ui.actions.MindMapActionFactory;
import org.xmind.ui.blackbox.BlackBox;
import org.xmind.ui.commands.ModifyFoldedCommand;
import org.xmind.ui.commands.MoveSheetCommand;
import org.xmind.ui.editor.EditorHistoryItem;
import org.xmind.ui.editor.IEditorHistory;
import org.xmind.ui.editor.PageTitleTabFolderRenderer;
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
import org.xmind.ui.internal.e4handlers.SaveWorkbookAsHandler;
import org.xmind.ui.internal.findreplace.IFindReplaceOperationProvider;
import org.xmind.ui.internal.mindmap.MindMapEditDomain;
import org.xmind.ui.internal.mindmap.MindMapState;
import org.xmind.ui.internal.mindmap.Overview;
import org.xmind.ui.mindmap.IMindMap;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.IMindMapViewer;
import org.xmind.ui.mindmap.IWorkbookRef;
import org.xmind.ui.mindmap.IWorkbookRefListener;
import org.xmind.ui.mindmap.MindMapImageExporter;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.mindmap.MindMapViewerExportSourceProvider;
import org.xmind.ui.prefs.PrefConstants;
import org.xmind.ui.resources.ColorUtils;
import org.xmind.ui.resources.FontUtils;
import org.xmind.ui.tabfolder.DelegatedSelectionProvider;
import org.xmind.ui.tabfolder.IPageMoveListener;
import org.xmind.ui.tabfolder.PageMoveHelper;
import org.xmind.ui.util.Logger;
import org.xmind.ui.wizards.ISaveContext;

public class MindMapEditor extends GraphicalEditor implements ISaveablePart2,
        ICoreEventListener, IPageMoveListener, IPropertyChangeListener,
        IMindMapPreviewGenerator, ISaveContext, IWorkbookRefListener {

    public static final int OVERVIEW_WIDTH = 300;
    public static final int OVERVIEW_HEIGHT = 180;

    private static final int DEFAULT_EXPORT_MARGIN = 5;

    private static final int MINIMUM_PREVIEW_WIDTH = 420;
    private static final int MAXIMUM_PREVIEW_WIDTH = MINIMUM_PREVIEW_WIDTH * 4;

    private static class EditorStatus {

        private static EditorStatus instance = null;

        private int activeIndex;

        private List<Double> zooms;

        private List<Point> scrollPositions;

        private List<String> indexPaths;

        private List<List<String>> topicTypeChains;

        private EditorStatus() {
        }

        public void saveStatus(IWorkbookRef workbookRef, int activeIndex,
                IGraphicalEditorPage[] pages) {
            init();
            if (workbookRef != null) {
                IWorkbook workbook = workbookRef.getWorkbook();
                if (workbook != null && pages != null && (pages.length != 0)) {
                    this.activeIndex = activeIndex;
                    for (IGraphicalEditorPage page : pages) {
                        IGraphicalViewer viewer = page.getViewer();
                        if (zooms == null)
                            zooms = new ArrayList<Double>();
                        double zoom = viewer.getZoomManager().getScale();
                        zooms.add(zoom);

                        if (scrollPositions == null)
                            scrollPositions = new ArrayList<Point>();
                        Point scrollPosition = viewer.getScrollPosition();
                        scrollPositions.add(scrollPosition);

                        if (indexPaths == null)
                            indexPaths = new ArrayList<String>();
                        if (topicTypeChains == null)
                            topicTypeChains = new ArrayList<List<String>>();
                        Object focused = viewer.getFocused();
                        if (focused == null || !(focused instanceof ITopic)) {
                            indexPaths.add(""); //$NON-NLS-1$
                            topicTypeChains
                                    .add(Collections.<String> emptyList());
                        } else {
                            List<String> typeChain = new ArrayList<String>();
                            String indexPath = getTopicIndexPath(
                                    (ITopic) focused, "", typeChain); //$NON-NLS-1$
                            indexPaths.add(indexPath);
                            Collections.reverse(typeChain);
                            topicTypeChains.add(typeChain);
                        }
                    }
                }
            }
        }

        private void init() {
            activeIndex = 0;

            if (zooms != null)
                zooms.clear();

            if (scrollPositions != null)
                scrollPositions.clear();

            if (indexPaths != null)
                indexPaths.clear();

            if (topicTypeChains != null)
                topicTypeChains.clear();
        }

        private String getTopicIndexPath(ITopic topic, String path,
                List<String> typeChain) {
            path = topic.getIndex() + "/" + path; //$NON-NLS-1$
            typeChain.add(topic.getType());
            if (topic.getParent() != null
                    && topic.getParent() instanceof ITopic)
                return getTopicIndexPath(topic.getParent(), path, typeChain);
            return path;
        }

        public int getActiveIndex() {
            return activeIndex;
        }

        public List<Double> getZooms() {
            return zooms;
        }

        public List<Point> getScrollPositions() {
            return scrollPositions;
        }

        public List<String> getIndexPaths() {
            return indexPaths;
        }

        public List<List<String>> getTopicTypeChains() {
            return topicTypeChains;
        }

        public static EditorStatus getInstance() {
            if (instance == null) {
                synchronized (EditorStatus.class) {
                    if (instance == null)
                        instance = new EditorStatus();
                }
            }
            return instance;
        }
    }

    private class MindMapEditorSelectionProvider
            extends MultiPageSelectionProvider {

        /*
         * (non-Javadoc)
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

    private IWorkbookRef workbookRef = null;

    private ICoreEventRegister workbookEventRegister = null;

    private ICoreEventRegister globalEventRegister = null;

    private MindMapPageTitleEditor pageTitleEditor = null;

    private PageMoveHelper pageMoveHelper = null;

    private MindMapFindReplaceOperationProvider findReplaceOperationProvider = null;

    private Composite messageContainer = null;

    private PageBook pageBook = null;

    private Composite pageContainer = null;

    private IEditingContext editingContext = new IEditingContext() {

        @Override
        public <T> T getAdapter(Class<T> adapter) {
            MindMapEditor editor = MindMapEditor.this;
            T result;

            result = editor.getSite().getService(adapter);
            if (result != null)
                return result;

            result = editor.getAdapter(adapter);
            if (result != null)
                return result;

            result = Platform.getAdapterManager().getAdapter(editor, adapter);
            if (result != null)
                return result;

            return null;
        }
    };

    private IWordContextProvider wordContextProvider = null;

    private boolean skipNextPreviewImage = false;

    private IContextActivation contextActivation = null;

    private IEditorHistory editorHistory = null;

    private boolean passwordTried = false;

    private IWindowListener windowListener;

    private ISelectionListener selectionListener;

    private boolean ignoreFileChanged = false;

    private IEditorLayoutManager layoutManager = null;

    private IContextActivation toolContextActivation = null;

    private IContextService contextService = null;

    public void init(IEditorSite site, IEditorInput input)
            throws PartInitException {
        this.editorHistory = site.getService(IEditorHistory.class);
        this.workbookRef = createWorkbookRefFromInput(input);
        if (this.workbookRef == null)
            throw new PartInitException(
                    "Failed to obtain workbook reference from editor input '" //$NON-NLS-1$
                            + input.toString() + "'"); //$NON-NLS-1$

        super.init(site, input);
        setMiniBarContributor(new MindMapMiniBarContributor());
    }

    private IWorkbookRef createWorkbookRefFromInput(IEditorInput input) {
        IWorkbookRef workbookRef = input.getAdapter(IWorkbookRef.class);
        if (workbookRef != null)
            return workbookRef;

        URI uri = input.getAdapter(URI.class);
        if (uri != null) {
            return MindMapUIPlugin.getDefault().getWorkbookRefFactory()
                    .createWorkbookRef(uri, null);
        }

        return null;
    }

    protected ISelectionProvider createSelectionProvider() {
        DelegatedSelectionProvider selectionProvider = new MindMapEditorSelectionProvider();
        selectionProvider.setDisplay(Display.getCurrent());
        return selectionProvider;
    }

    protected ICommandStack createCommandStack() {
        if (workbookRef != null)
            return workbookRef.getCommandStack();
        return super.createCommandStack();
    }

    protected void disposeCommandStack(ICommandStack commandStack) {
        // No need to dispose command stack here, because the workbook reference
        // manager will dispose unused command stacks automatically.
    }

    private void disposeData() {
        if (workbookRef != null && workbookRef.getURI() != null) {
            URI uri = workbookRef.getURI();
            if (URIUtil.isFileURI(uri)) {
                BlackBox.removeSavedMap(URIUtil.toFile(uri).getAbsolutePath());
            }
        }

        final IWorkbookRef theWorkbookRef = this.workbookRef;
        if (theWorkbookRef != null) {
            theWorkbookRef.removePropertyChangeListener(this);
            theWorkbookRef.removeWorkbookRefListener(this);
            MindMapState.getInstance().saveState(theWorkbookRef, getPages());
            if (theWorkbookRef.isDirty()) {
                theWorkbookRef.discardChanges();
            }
            safeRun(null, false, new IRunnableWithProgress() {

                @Override
                public void run(IProgressMonitor monitor)
                        throws InvocationTargetException, InterruptedException {
                    theWorkbookRef.close(monitor);
                }
            });
            IEditingContext activeContext = theWorkbookRef.getActiveContext();
            if (activeContext == this.editingContext) {
                theWorkbookRef.setActiveContext(null);
            }
        }

        deactivateToolContext();

        uninstallModelListener();
    }

    public void dispose() {
        if (contextActivation != null) {
            IContextService cs = getSite().getService(IContextService.class);
            cs.deactivateContext(contextActivation);
        }
        disposeData();
        super.dispose();
        IEditorInput editorInput = getEditorInput();
        if (editorInput instanceof MindMapEditorInput) {
            ((MindMapEditorInput) editorInput).dispose();
        }
        deactivateFileNotifier();
        workbookEventRegister = null;
        globalEventRegister = null;
        pageTitleEditor = null;
        pageMoveHelper = null;
        findReplaceOperationProvider = null;
        workbookRef = null;
        pageBook = null;
        pageContainer = null;

        // release reference to the workbook reference object
        this.workbookRef = null;
        layoutManager = null;
    }

    @Override
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);

        Composite container = getContainer();
        if (container instanceof CTabFolder) {
            ((CTabFolder) container).setRenderer(new PageTitleTabFolderRenderer(
                    (CTabFolder) container, this));
        }

        activateFileNotifier();
    }

    protected Composite createContainerParent(Composite parent) {
        FormLayout formLayout = new FormLayout();
        parent.setLayout(formLayout);

        messageContainer = new Composite(parent, SWT.BORDER);
        FormData messageFormData = new FormData();
        //no correct size data to set form data
        messageContainer.setLayoutData(messageFormData);
        messageContainer.setVisible(false);

        GridLayout messageLayout = new GridLayout(1, false);
        messageLayout.marginWidth = 0;
        messageLayout.marginHeight = 0;
        messageLayout.horizontalSpacing = 0;
        messageLayout.verticalSpacing = 0;
        messageContainer.setLayout(messageLayout);

        updateMessages(false);

        pageBook = new PageBook(parent, SWT.NONE);
        FormData pageBookData = new FormData();
        pageBookData.left = new FormAttachment(0, 0);
        pageBookData.top = new FormAttachment(0, 0);
        pageBookData.bottom = new FormAttachment(100, 0);
        pageBookData.right = new FormAttachment(100, 0);
        pageBook.setLayoutData(pageBookData);

        pageContainer = new Composite(pageBook, SWT.NONE);
        layoutManager = new EditorLayoutManager(pageContainer);
        layoutManager.setActiveLayout(new DefaultEditorLayout());
        IContextService cs = getSite().getService(IContextService.class);
        contextActivation = cs.activateContext(MindMapUI.CONTEXT_MINDMAP);
        return pageContainer;
    }

    @Override
    protected void createEditorContents() {
        super.createEditorContents();

        if (workbookRef != null) {
            workbookRef.setActiveContext(editingContext);
            workbookRef.addPropertyChangeListener(this);
            workbookRef.addWorkbookRefListener(this);
        }

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
            pageTitleEditor = new MindMapPageTitleEditor(tabFolder, this);
//            pageTitleEditor.addPageTitleChangedListener(this);
//            pageTitleEditor.setContextId(getSite(),
//                    MindMapUI.CONTEXT_PAGETITLE_EDIT);
            new MindMapEditorPagePopupPreviewHelper(this, tabFolder);

        }

        // Let 3rd-party plugins configure this editor:
        MindMapEditorConfigurerManager.getInstance().configureEditor(this);

        // Try loading workbook:
        if (getWorkbook() != null) {
            workbookLoaded();
        } else if (workbookRef != null) {
            final IWorkbookRef theWorkbookRef = workbookRef;
            Display.getCurrent().asyncExec(new Runnable() {
                @Override
                public void run() {
                    loadWorkbook(theWorkbookRef, 0);
                    collectUserData(theWorkbookRef.getURI());
                }
            });
        }
    }

    private static void collectUserData(URI uri) {
        if (uri == null || "".equals(uri)) { //$NON-NLS-1$
            return;
        }
        String scheme = uri.getScheme();
        if (scheme == null || "".equalsIgnoreCase(scheme)) //$NON-NLS-1$
            return;
        if ("file".equalsIgnoreCase(scheme)) { //$NON-NLS-1$
            MindMapUIPlugin.getDefault().getUsageDataCollector().trackEvent(
                    UserDataConstants.CATEGORY_WORKBOOK,
                    UserDataConstants.OPEN_LOCAL_WORKBOOK);
        }
    }

    private void loadWorkbook(final IWorkbookRef workbookRef, int times) {
        IEncryptable encryptable = workbookRef.getAdapter(IEncryptable.class);
        if (encryptable != null && !encryptable.hasPassword()) {
            /// make sure setPassword is called to prevent the default password dialog
            encryptable.setPassword(null);
        }

        IWorkbenchSiteProgressService context = getSite()
                .getService(IWorkbenchSiteProgressService.class);
        Assert.isTrue(context != null);
        try {
            context.run(true, true, new IRunnableWithProgress() {

                @Override
                public void run(IProgressMonitor monitor)
                        throws InvocationTargetException, InterruptedException {
                    workbookRef.open(monitor);
                }
            });
        } catch (final InvocationTargetException e) {
            CoreException coreEx = getCoreException(e);
            if (coreEx != null) {
                int errType = coreEx.getType();
                if (errType == Core.ERROR_WRONG_PASSWORD) {
                    // password error
                    String message = passwordTried
                            ? MindMapMessages.MindMapEditor_passwordPrompt_message1
                            : MindMapMessages.MindMapEditor_passwordPrompt_message2;
                    openDecryptionDialog(workbookRef, message, times);
                    passwordTried = true;
                    return;
                }
            }

            // failed
            Throwable cause = e.getTargetException();
            if (cause == null)
                cause = e;

            if (cause instanceof FileNotFoundException) {
                Display.getDefault().asyncExec(new Runnable() {
                    public void run() {
                        MessageDialog.openWarning(null,
                                MindMapMessages.OpenLocalFileHandler_MessageDialog_title,
                                MindMapMessages.WorkbookHistoryItem_FileMissingMessage);
                    }
                });
                asyncClose();
            } else {
                showError(cause,
                        workbookRef.getAdapter(ErrorSupportProvider.class));
            }
            return;
        } catch (InterruptedException e) {
            // canceled
            asyncClose();
            return;
        }

        workbookLoaded();
    }

    private void openDecryptionDialog(final IWorkbookRef workbookRef,
            String message, int times) {
        final int nextTime = times + 1;
        final IEncryptable encryptable = workbookRef
                .getAdapter(IEncryptable.class);

        new DecryptionDialog(Display.getCurrent().getActiveShell(),
                workbookRef.getName(), encryptable.getPasswordHint(), times) {
            protected void okPressed() {
                super.okPressed();

                encryptable.setPassword(getPassword());
                loadWorkbook(workbookRef, nextTime);
            };

            protected void handleShellCloseEvent() {
                super.handleShellCloseEvent();
                asyncClose();
            };

            protected void cancelPressed() {
                super.cancelPressed();
                asyncClose();
            };
        }.open();
    }

    private CoreException getCoreException(Throwable e) {
        if (e == null)
            return null;
        if (e instanceof CoreException)
            return (CoreException) e;
        return getCoreException(e.getCause());
    }

    private void asyncClose() {
        Display.getCurrent().asyncExec(new Runnable() {

            @Override
            public void run() {
                closeEditor();
            }
        });
    }

    private void closeEditor() {
        getSite().getPage().closeEditor(this, false);
    }

    private void showError(Throwable exception,
            ErrorSupportProvider supportProvider) {
        StatusAdapter statusAdapter = new StatusAdapter(new Status(
                IStatus.ERROR, MindMapUIPlugin.PLUGIN_ID,
                MindMapMessages.LoadWorkbookJob_errorDialog_title, exception));
        statusAdapter.setProperty(IStatusAdapterConstants.TIMESTAMP_PROPERTY,
                Long.valueOf(System.currentTimeMillis()));
        ErrorDialog errorDialog = new ErrorDialog(
                Display.getCurrent().getActiveShell(), statusAdapter,
                supportProvider);
        errorDialog.setCloseCallback(new Runnable() {
            @Override
            public void run() {
                asyncClose();
            }
        });
        errorDialog.open();
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

    private void addOverview() {
        final Composite container = getContainer();

        final Composite overviewControl = createOverview(container);
        overviewControl.moveAbove(null);
        overviewControl
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        new Overview(overviewControl, this);

        Rectangle containerClientArea = container.getClientArea();
        overviewControl.setBounds(containerClientArea.width - OVERVIEW_WIDTH,
                containerClientArea.height - OVERVIEW_HEIGHT, OVERVIEW_WIDTH,
                OVERVIEW_HEIGHT);

        container.addControlListener(new ControlAdapter() {
            public void controlResized(ControlEvent e) {
                Rectangle containerClientArea = container.getClientArea();
                overviewControl.setBounds(
                        containerClientArea.width - OVERVIEW_WIDTH,
                        containerClientArea.height - OVERVIEW_HEIGHT,
                        OVERVIEW_WIDTH, OVERVIEW_HEIGHT);
            }

            public void controlMoved(ControlEvent e) {
                Rectangle containerClientArea = container.getClientArea();
                overviewControl.setBounds(
                        containerClientArea.width - OVERVIEW_WIDTH,
                        containerClientArea.height - OVERVIEW_HEIGHT,
                        OVERVIEW_WIDTH, OVERVIEW_HEIGHT);
            }
        });

    }

    private Composite createOverview(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.horizontalSpacing = 0;
        layout.verticalSpacing = 0;
        composite.setLayout(layout);
        return composite;
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
        MindMapState.getInstance().loadState(getWorkbookRef(), page);
        return page;
    }

    protected EditDomain createEditDomain(IGraphicalEditorPage page) {
        MindMapEditDomain domain = new MindMapEditDomain();
        domain.addEditDomainListener(new IEditDomainListener() {

            @Override
            public void activeToolChanged(ITool oldTool, ITool newTool) {
                changeContext(newTool);
            }
        });
        domain.setCommandStack(getCommandStack());
        return domain;
    }

    protected void changeContext(ITool newTool) {
        deactivateToolContext();
        activateContext(newTool == null ? null : newTool.getContextId());
    }

    protected void changeContext(String contextId) {
        deactivateToolContext();
        activateContext(contextId);
    }

    private void activateContext(String contextId) {
        if (contextId == null)
            return;
        contextService = getSite().getService(IContextService.class);
        if (contextService != null) {
            toolContextActivation = contextService.activateContext(contextId);
        }
    }

    private void deactivateToolContext() {
        if (contextService != null && toolContextActivation != null) {
            contextService.deactivateContext(toolContextActivation);
        }
        contextService = null;
        toolContextActivation = null;
    }

    private void updateMessages(boolean layout) {
        for (Control messageItem : messageContainer.getChildren()) {
            messageItem.dispose();
        }
        List<IInteractiveMessage> messages = workbookRef.getMessages();
        if (messages != null && !messages.isEmpty()) {
            for (IInteractiveMessage message : new ArrayList<IInteractiveMessage>(
                    messages)) {

                Image icon;
                Color lineColor;
                Color textColor;
                int type = message.getMessageType();
                if (type == IInteractiveMessage.WARNING) {
                    ///TODO use a dedicated icon
                    icon = null;
                    lineColor = (Color) JFaceResources.getResources()
                            .get(ColorUtils.toDescriptor("#fff6ae")); //$NON-NLS-1$
                    textColor = (Color) JFaceResources.getResources()
                            .get(ColorUtils.toDescriptor("#1e1e1e")); //$NON-NLS-1$
                } else if (type == IInteractiveMessage.ERROR) {
                    ///TODO use a dedicated icon
                    icon = null;
                    lineColor = (Color) JFaceResources.getResources()
                            .get(ColorUtils.toDescriptor("#fff6ae")); //$NON-NLS-1$
                    textColor = (Color) JFaceResources.getResources()
                            .get(ColorUtils.toDescriptor("#1e1e1e")); //$NON-NLS-1$
                } else {
                    ///TODO use a dedicated icon
                    icon = Display.getCurrent()
                            .getSystemImage(SWT.ICON_INFORMATION);
                    lineColor = (Color) JFaceResources.getResources()
                            .get(ColorUtils.toDescriptor("#fff6ae")); //$NON-NLS-1$
                    textColor = (Color) JFaceResources.getResources()
                            .get(ColorUtils.toDescriptor("#1e1e1e")); //$NON-NLS-1$
                }

                String text = message.getMessage();
                if (text == null) {
                    text = ""; //$NON-NLS-1$
                }

                List<IAction> actions = message.getActions();

                final Composite line = new Composite(messageContainer,
                        SWT.NONE);
                line.setBackground(lineColor);
                GridData lineData = new GridData(SWT.CENTER, SWT.FILL, true,
                        false);
                lineData.widthHint = 500;
                line.setLayoutData(lineData);
                line.setLayout(new GridLayout(2, false));

                Composite leftComposite = new Composite(line, SWT.NONE);
                leftComposite.setBackground(line.getBackground());
                GridData lineBodyData = new GridData(SWT.CENTER, SWT.FILL, true,
                        false);
                leftComposite.setLayoutData(lineBodyData);
                int numColumns = 0;
                GridLayout leftCompositeLayout = new GridLayout(numColumns,
                        false);
                leftCompositeLayout.marginLeft = 16;
                leftCompositeLayout.marginHeight = 5;
                leftCompositeLayout.horizontalSpacing = 3;
                leftCompositeLayout.verticalSpacing = 0;
                leftComposite.setLayout(leftCompositeLayout);

                if (icon != null) {
                    numColumns++;
                    Label iconLabel = new Label(leftComposite, SWT.NONE);
                    iconLabel.setBackground(line.getBackground());
                    iconLabel.setImage(icon);
                    iconLabel.setLayoutData(
                            new GridData(SWT.CENTER, SWT.CENTER, false, true));
                }

                numColumns++;
                Label textLabel = new Label(leftComposite,
                        SWT.WRAP | SWT.CENTER);
                textLabel.setText(text);
                textLabel.setBackground(line.getBackground());
                textLabel.setForeground(textColor);
                textLabel.setLayoutData(
                        new GridData(SWT.CENTER, SWT.CENTER, true, true));
                Font textFont = textLabel.getFont();
                if (textFont != null) {
                    FontData[] fontData = textFont.getFontData();
                    FontData[] newFontData = FontUtils.newHeight(fontData,
                            Util.isMac() ? 12 : 10);
                    textLabel.setFont((Font) JFaceResources.getResources()
                            .get(FontDescriptor.createFrom(newFontData)));
                }

                if (!actions.isEmpty()) {
                    numColumns++;
                    Composite actionBar = new Composite(leftComposite,
                            SWT.NONE);
                    actionBar.setBackground(line.getBackground());
                    actionBar.setLayoutData(
                            new GridData(SWT.FILL, SWT.FILL, false, true));
                    GridLayout actionLayout = new GridLayout(actions.size(),
                            false);
                    actionLayout.marginWidth = 0;
                    actionLayout.marginHeight = 0;
                    actionLayout.horizontalSpacing = 3;
                    actionLayout.verticalSpacing = 0;
                    actionBar.setLayout(actionLayout);

                    for (final IAction action : actions) {
                        final Button actionButton = new Button(actionBar,
                                SWT.PUSH);
                        actionButton.setBackground(actionBar.getBackground());
                        actionButton.setLayoutData(new GridData(SWT.FILL,
                                SWT.CENTER, false, true));
                        String actionText = action.getText();
                        if (actionText == null) {
                            actionButton.setText(actionText);
                        } else {
                            ImageDescriptor actionImageDescriptor = action
                                    .getImageDescriptor();
                            if (actionImageDescriptor != null) {
                                final Image actionImage = actionImageDescriptor
                                        .createImage(Display.getCurrent());
                                actionButton.addDisposeListener(
                                        new DisposeListener() {

                                            @Override
                                            public void widgetDisposed(
                                                    DisposeEvent e) {
                                                actionImage.dispose();
                                            }
                                        });
                                actionButton.setImage(actionImage);
                            }
                        }
                        actionButton.setToolTipText(action.getToolTipText());
                        actionButton.addListener(SWT.Selection, new Listener() {

                            @Override
                            public void handleEvent(Event event) {
                                action.runWithEvent(event);
                            }
                        });
                    }
                }

                leftCompositeLayout.numColumns = numColumns;

                final Label closeImageLabel = new Label(line, SWT.NONE);
                closeImageLabel.setBackground(line.getBackground());
                closeImageLabel.setImage((Image) JFaceResources.getResources()
                        .get(MindMapUI.getImages().get("close.png", true))); //$NON-NLS-1$
                closeImageLabel.setLayoutData(
                        new GridData(SWT.END, SWT.BEGINNING, false, false));
                closeImageLabel.addMouseListener(new MouseAdapter() {

                    @Override
                    public void mouseDown(MouseEvent e) {
                        if (messageContainer != null
                                && !messageContainer.isDisposed()) {
                            if (line != null && !line.isDisposed()) {
                                line.dispose();
                            }
                            Control[] children = messageContainer.getChildren();
                            if (children == null || children.length == 0) {
                                messageContainer.setVisible(false);
                            }
                            messageContainer.getParent().layout(true);
                        }
                    }
                });
                closeImageLabel.addMouseTrackListener(new MouseTrackListener() {

                    @Override
                    public void mouseHover(MouseEvent e) {
                        closeImageLabel.setCursor(Display.getDefault()
                                .getSystemCursor(SWT.CURSOR_HAND));
                    }

                    @Override
                    public void mouseExit(MouseEvent e) {
                        closeImageLabel.setCursor(null);
                    }

                    @Override
                    public void mouseEnter(MouseEvent e) {
                        closeImageLabel.setCursor(Display.getDefault()
                                .getSystemCursor(SWT.CURSOR_HAND));
                    }
                });
            }
            org.eclipse.swt.graphics.Point size = messageContainer
                    .computeSize(SWT.DEFAULT, SWT.DEFAULT);
            FormData messageFormData = new FormData();
            messageFormData.left = new FormAttachment(50, -size.x / 2);
            messageFormData.right = new FormAttachment(50, size.x / 2);
            messageFormData.top = new FormAttachment(0, 0);
            //can't set bottom
//            messageFormData.bottom = new FormAttachment(100, -size.y);
            messageContainer.setLayoutData(messageFormData);
            messageContainer.setVisible(true);
        }

        if (layout) {
            messageContainer.getParent().layout(true, true);
        }
    }

    protected void updateNames() {
        setPartName(getEditorInput().getName());
        setTitleToolTip(getEditorInput().getToolTipText());
    }

    public IWorkbookRef getWorkbookRef() {
        return workbookRef;
    }

    public IWorkbook getWorkbook() {
        if (workbookRef == null)
            return null;
        return workbookRef.getWorkbook();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getAdapter(Class<T> adapter) {
        if (adapter == IEditorLayoutManager.class)
            return (T) layoutManager;
        return super.getAdapter(adapter);
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
        } else if (adapter == MindMapPageTitleEditor.class) {
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
//            return adapter.cast(backCover);
        }
        return super.getEditorAdapter(adapter);
    }

    protected void installModelListener() {
        IWorkbook workbook = getWorkbook();
        workbookEventRegister = new CoreEventRegister(workbook, this);
        workbookEventRegister.register(Core.SheetAdd);
        workbookEventRegister.register(Core.SheetRemove);
        workbookEventRegister.register(Core.SheetMove);
        workbookEventRegister.register(Core.PasswordChange);
        workbookEventRegister.register(Core.WorkbookPreSaveOnce);

        globalEventRegister = new CoreEventRegister(
                workbook.getAdapter(ICoreEventSupport.class), this);
        globalEventRegister.register(Core.SheetSettings);
    }

    protected void uninstallModelListener() {
        if (workbookEventRegister != null) {
            workbookEventRegister.unregisterAll();
            workbookEventRegister = null;
        }
        if (globalEventRegister != null) {
            globalEventRegister.unregisterAll();
            globalEventRegister = null;
        }
    }

    public void handleCoreEvent(final CoreEvent event) {
        if (pageBook == null || pageBook.isDisposed() || workbookRef == null)
            return;

        pageBook.getDisplay().syncExec(new Runnable() {

            public void run() {
                String type = event.getType();
                if (Core.WorkbookPreSaveOnce.equals(type)) {
                    firePropertyChange(IWorkbenchPartConstants.PROP_DIRTY);
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

                //update sheet tab colors
                if (Core.SheetSettings.equals(type)
                        && ISheetSettings.ATTR_RGB.equals(event.getTarget())) {
                    updateSheetTabColors();
                }
            }
        });
    }

    private void updateSheetTabColors() {
        getContainer().redraw();
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

//    public void pageTitleChanged(int pageIndex, String newValue) {
//        /// forbid changing sheet title to empty string
//        /// preventing saving errors (e.g. cloud syncing)
//        if (newValue == null || "".equals(newValue)) //$NON-NLS-1$
//            return;
//
//        IGraphicalEditorPage page = getPage(pageIndex);
//        if (page != null) {
//            Object pageInput = page.getInput();
//            if (pageInput instanceof ISheet) {
//                ModifyTitleTextCommand command = new ModifyTitleTextCommand(
//                        (ISheet) pageInput, newValue);
//                command.setLabel(""); //$NON-NLS-1$
//                saveAndRun(command);
//            }
//        }
//    }

    protected void createSheet() {
        IAction action = getActionRegistry()
                .getAction(MindMapActionFactory.NEW_SHEET.getId());
        if (action != null && action.isEnabled()) {
            action.run();
        }
    }

    @Override
    public void setFocus() {
//        if (workbookRef != null) {
//            workbookRef.setPrimaryReferrer(this);
//        }
        if (workbookRef != null) {
            workbookRef.setActiveContext(editingContext);
        }
//        if (backCover != null && backCover.isOpen()) {
//            backCover.setFocus();
//        } else {
        super.setFocus();
//        }
    }

    public void openEncryptionDialog() {
        if (pageBook == null || pageBook.isDisposed())
            return;

        final IWorkbookRef theWorkbookRef = workbookRef;

        new EncryptionDialog(Display.getCurrent().getActiveShell()) {

            @Override
            protected boolean hasPassword() {
                IEncryptable encryptable = theWorkbookRef
                        .getAdapter(IEncryptable.class);
                return encryptable != null && encryptable.hasPassword();
            }

            @Override
            protected boolean testsPassword(String password) {
                IEncryptable encryptable = theWorkbookRef
                        .getAdapter(IEncryptable.class);
                return encryptable != null
                        && encryptable.testsPassword(password);
            }

            @Override
            protected void okPressed() {
                boolean verified = verify();

                super.okPressed();

                if (verified) {
                    IEncryptable encryptable = theWorkbookRef
                            .getAdapter(IEncryptable.class);
                    if (encryptable != null) {
                        encryptable.setPassword(getPassword());
                        encryptable.setPasswordHint(getHintMessage());
                    }
                }
            }
        }.open();
    }

    public ISelectionProvider getSelectionProvider() {
        return getSite().getSelectionProvider();
    }

    public void reveal() {
        getSite().getPage().activate(this);
        setFocus();
    }

    /**
     * @param monitor
     * @deprecated
     */
    @Deprecated
    public void postSave(final IProgressMonitor monitor) {
        getSite().getShell().getDisplay().syncExec(new Runnable() {

            public void run() {
                postSave();
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
        passwordTried = false;
        if (pageBook == null || pageBook.isDisposed())
            return;

        // TODO
        pageBook.showPage(pageContainer);
        if (isEditorActive())
            setFocus();

        Assert.isTrue(getWorkbook() != null);
        createPages();
        addOverview();
        if (isEditorActive()) {
            setFocus();
        }
        installModelListener();
        firePropertyChange(PROP_INPUT);
        firePropertyChange(IWorkbenchPartConstants.PROP_DIRTY);

        recordEditorHistory();
    }

    private boolean isEditorActive() {
        return getSite().getPage().getActiveEditor() == this;
    }

    /*
     * (non-Javadoc)
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

    @Override
    public boolean isDirty() {
        return workbookRef != null && workbookRef.isDirty();
    }

    @Override
    public void doSave(IProgressMonitor monitor) {
        if (workbookRef == null)
            return;

        if (!workbookRef.canSave()) {
            doSaveAs();
            return;
        }

        safeRun(monitor, false, new IRunnableWithProgress() {

            @Override
            public void run(IProgressMonitor monitor)
                    throws InvocationTargetException, InterruptedException {
                workbookRef.save(monitor);
                if (MindMapUIPlugin.getDefault().getPreferenceStore()
                        .getBoolean(PrefConstants.AUTO_BACKUP_ENABLE)) {
                    URI uri = workbookRef.getURI();
                    if (URIUtil.isFileURI(uri)) {
                        BlackBox.doBackup(
                                URIUtil.toFile(uri).getAbsolutePath());
                    }
                }
                postSave();
                recordEditorHistory();
            }
        });
    }

    private void recordEditorHistory() {

        if (workbookRef.getURI() == null || workbookRef.getName() == null)
            return;
        URI uri = workbookRef.getURI();

        //only local file can record it history.
        if (!uri.getScheme().equalsIgnoreCase("file")) //$NON-NLS-1$
            return;

        InputStream input = null;
        try {
            try {
                if (uri.getScheme().equalsIgnoreCase("file")) { //$NON-NLS-1$
                    input = getThumbnailStreamForLocal();
                } else
                    input = getThumbnailStreamFor(workbookRef);
                if (input == null)
                    return;
                editorHistory.add(workbookRef.getURI(), new EditorHistoryItem(
                        workbookRef.getName(), System.currentTimeMillis()));
                editorHistory.saveThumbnailData(workbookRef.getURI(), input);
            } finally {
                if (input != null)
                    input.close();
            }
        } catch (IOException e) {
            MindMapUIPlugin.getDefault().getLog()
                    .log(new Status(IStatus.ERROR, MindMapUIPlugin.PLUGIN_ID,
                            "Failed to record preview image for editor history", //$NON-NLS-1$
                            e));
        }
    }

    private InputStream getThumbnailStreamForLocal() throws IOException {
        IManifest manifest = getWorkbook().getManifest();
        if (manifest != null) {
            IFileEntry thumbnailEntry = manifest
                    .getFileEntry("Thumbnails/thumbnail.png"); //$NON-NLS-1$
            if (thumbnailEntry != null)
                return thumbnailEntry.openInputStream();
        }
        return null;
    }

    private InputStream getThumbnailStreamFor(IWorkbookRef workbookRef) {
        if (workbookRef == null)
            return null;

        List<ISheet> sheets = workbookRef.getWorkbook().getSheets();
        if (sheets.isEmpty())
            return null;

        ISheet sheet = sheets.get(0);
        try {
            return workbookRef.getPreviewImageData(sheet.getId(), null);
        } catch (IOException e) {
            MindMapUIPlugin.log(e, String.format(
                    "Failed to load preview image for sheet 'workbooks/%s/sheets/%s'", //$NON-NLS-1$
                    sheet.getParent().toString(), sheet.getId()));
        } catch (SWTException e) {
            MindMapUIPlugin.log(e, String.format(
                    "Failed to load preview image for sheet 'workbooks/%s/sheets/%s'", //$NON-NLS-1$
                    sheet.getParent().toString(), sheet.getId()));
        }

        return null;
    }

    public void doSaveAs() {
        doSaveAs(false);
    }

    public void doSaveAs(boolean onlyToLocal) {
        final IWorkbookRef oldWorkbookRef = this.workbookRef;
        if (oldWorkbookRef == null || oldWorkbookRef
                .isInState(IWorkbookRef.CLOSED | IWorkbookRef.CLOSING))
            throw new IllegalStateException(
                    "This mind map editor is already closed."); //$NON-NLS-1$

        IProgressService runner = getSite().getService(IProgressService.class);
        final IWorkbookRef newWorkbookRef;
        try {
            newWorkbookRef = SaveWorkbookAsHandler.saveWorkbookAs(this,
                    oldWorkbookRef, runner, null, onlyToLocal);
            if (newWorkbookRef == null)
                // canceled
                return;
            if (newWorkbookRef.equals(oldWorkbookRef)) {
                // saved to old location
                postSave();
                recordEditorHistory();
                return;
            }

            try {
                runner.run(true, true, new IRunnableWithProgress() {

                    @Override
                    public void run(IProgressMonitor monitor)
                            throws InvocationTargetException,
                            InterruptedException {
                        newWorkbookRef.open(monitor);
                        newWorkbookRef.save(monitor);
                    }
                });
            } catch (InterruptedException e) {
                // canceled
                return;
            }
        } catch (Exception e) {
            StatusManager.getManager().handle(new Status(IStatus.ERROR,
                    MindMapUIPlugin.PLUGIN_ID, e.getMessage(), e),
                    StatusManager.LOG);
            return;
        }

        // TODO save editor state before changing editor input
        EditorStatus editorStatus = EditorStatus.getInstance();
        editorStatus.saveStatus(oldWorkbookRef, getActivePage(), getPages());

        disposeData();
        while (getPageCount() > 0) {
            removePage(0);
        }

        this.workbookRef = newWorkbookRef;
        if (newWorkbookRef != null) {
            newWorkbookRef.addPropertyChangeListener(this);
            newWorkbookRef.addWorkbookRefListener(this);
        }
        setInput(MindMapUI.getEditorInputFactory()
                .createEditorInput(newWorkbookRef));
        setCommandStack(newWorkbookRef.getCommandStack());
        updateNames();

        createPages();
        installModelListener();

        firePropertyChange(PROP_INPUT);
        postSave();

        // TODO restore editor state after changing editor input
        setActivePage(editorStatus.getActiveIndex());
        IGraphicalEditorPage[] pages = getPages();
        for (int index = 0; index < pages.length; index++) {
            IGraphicalViewer viewer = pages[index].getViewer();
            viewer.getProperties().set(
                    IMindMapViewer.VIEWER_SELECT_CENTRALTOPIC, Boolean.FALSE);
            Double scale = editorStatus.getZooms().get(index);
            if (scale != null)
                viewer.getZoomManager().setScale(scale);

            String indexPath = index < editorStatus.getIndexPaths().size()
                    ? editorStatus.getIndexPaths().get(index) : ""; //$NON-NLS-1$
            List<String> topicTypeChain = index < editorStatus
                    .getTopicTypeChains().size()
                            ? editorStatus.getTopicTypeChains().get(index)
                            : Collections.<String> emptyList();
            if (indexPath != null) {
                if ("".equals(indexPath)) //$NON-NLS-1$
                    viewer.setSelection(StructuredSelection.EMPTY, true);
                else {
                    Object input = viewer.getInput();
                    if (input instanceof IMindMap) {
                        ITopic topic = ((IMindMap) input).getCentralTopic();
                        String[] indexes = indexPath.split("/"); //$NON-NLS-1$
                        for (int i = 1; i < indexes.length; i++) {
                            String type = i < topicTypeChain.size()
                                    ? topicTypeChain.get(i) : ITopic.ATTACHED;
                            if (topic.getChildren(type).size() <= Integer
                                    .parseInt(indexes[i])) {
                                continue;
                            }
                            topic = topic.getChildren(type)
                                    .get(Integer.parseInt(indexes[i]));
                        }
                        viewer.setSelection(new StructuredSelection(topic),
                                true);
                    }

                }
            }

            Point scrollPosition = editorStatus.getScrollPositions().get(index);
            if (scrollPosition != null)
                viewer.scrollTo(scrollPosition);
        }

        recordEditorHistory();

        //Force update IEclipseContext
        IEclipseContext partContext = getSite()
                .getService(IEclipseContext.class);
        partContext.deactivate();
        partContext.activateBranch();
    }

    private void reload() {
        IWorkbookRef oldWorkbookRef = this.workbookRef;
        if (oldWorkbookRef == null || oldWorkbookRef
                .isInState(IWorkbookRef.CLOSED | IWorkbookRef.CLOSING)) {
            throw new IllegalStateException(
                    "This mind map editor is already closed."); //$NON-NLS-1$
        }

        // save editor state before changing editor input
        EditorStatus editorStatus = EditorStatus.getInstance();
        editorStatus.saveStatus(oldWorkbookRef, getActivePage(), getPages());

        IEditingContext context = oldWorkbookRef.getActiveContext();
        disposeData();
        while (getPageCount() > 0) {
            removePage(0);
        }

        final IWorkbookRef newWorkbookRef = MindMapUIPlugin.getDefault()
                .getWorkbookRefFactory()
                .createWorkbookRef(oldWorkbookRef.getURI(), null);
        if (newWorkbookRef == null) {
            return;
        }

        try {
            IProgressService runner = getSite()
                    .getService(IProgressService.class);
            runner.run(true, true, new IRunnableWithProgress() {

                @Override
                public void run(IProgressMonitor monitor)
                        throws InvocationTargetException, InterruptedException {
                    newWorkbookRef.open(monitor);
                }
            });
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return;
        }

        this.workbookRef = newWorkbookRef;
        if (newWorkbookRef != null) {
            newWorkbookRef.setActiveContext(context);
            newWorkbookRef.addPropertyChangeListener(this);
            newWorkbookRef.addWorkbookRefListener(this);
        }
        setInput(MindMapUI.getEditorInputFactory()
                .createEditorInput(newWorkbookRef));
        setCommandStack(newWorkbookRef.getCommandStack());
        updateNames();

        createPages();
        installModelListener();

        firePropertyChange(PROP_INPUT);
        firePropertyChange(IWorkbenchPartConstants.PROP_DIRTY);

        // restore editor state after changing editor input
        setActivePage(editorStatus.getActiveIndex());
        IGraphicalEditorPage[] pages = getPages();
        for (int index = 0; index < pages.length; index++) {
            IGraphicalViewer viewer = pages[index].getViewer();
            viewer.getProperties().set(
                    IMindMapViewer.VIEWER_SELECT_CENTRALTOPIC, Boolean.FALSE);
            Double scale = editorStatus.getZooms().size() > index
                    ? editorStatus.getZooms().get(index) : null;
            if (scale != null)
                viewer.getZoomManager().setScale(scale);

            String indexPath = editorStatus.getIndexPaths().size() > index
                    ? editorStatus.getIndexPaths().get(index) : ""; //$NON-NLS-1$
            List<String> topicTypeChain = editorStatus.getTopicTypeChains()
                    .size() > index
                            ? editorStatus.getTopicTypeChains().get(index)
                            : Collections.<String> emptyList();
            if (indexPath != null) {
                if ("".equals(indexPath)) //$NON-NLS-1$
                    viewer.setSelection(StructuredSelection.EMPTY, true);
                else {
                    Object input = viewer.getInput();
                    if (input instanceof IMindMap) {
                        ITopic topic = ((IMindMap) input).getCentralTopic();
                        String[] indexes = indexPath.split("/"); //$NON-NLS-1$
                        for (int i = 1; i < indexes.length; i++) {
                            String type = i < topicTypeChain.size()
                                    ? topicTypeChain.get(i) : ITopic.ATTACHED;
                            if (topic.getChildren(type).size() <= Integer
                                    .parseInt(indexes[i])) {
                                continue;
                            }
                            topic = topic.getChildren(type)
                                    .get(Integer.parseInt(indexes[i]));
                        }
                        viewer.setSelection(new StructuredSelection(topic),
                                true);
                    }

                }
            }

            Point scrollPosition = editorStatus.getScrollPositions()
                    .size() > index
                            ? editorStatus.getScrollPositions().get(index)
                            : null;
            if (scrollPosition != null)
                viewer.scrollTo(scrollPosition);
        }
    }

    private boolean safeRun(final IProgressMonitor monitor,
            final boolean promptError, final IRunnableWithProgress runnable) {
        final boolean[] successful = new boolean[1];
        successful[0] = false;
        SafeRunner.run(new SafeRunnable() {

            @Override
            public void run() throws Exception {
                if (monitor != null) {
                    runnable.run(monitor);
                } else {
                    IWorkbenchSiteProgressService context = getSite()
                            .getService(IWorkbenchSiteProgressService.class);
                    Assert.isTrue(context != null);
                    context.run(true, true, runnable);
                }
                successful[0] = true;
            }

            @Override
            public void handleException(Throwable e) {
                if (e instanceof InterruptedException)
                    // canceled, no error
                    return;

                if (e instanceof InvocationTargetException) {
                    Throwable cause = ((InvocationTargetException) e)
                            .getTargetException();
                    if (cause != null)
                        e = cause;
                }

                if (!promptError) {
                    // log only
                    Logger.log(e);
                    return;
                }

                super.handleException(e);
            }
        });

        return successful[0];
    }

    @Override
    public boolean isSaveAsAllowed() {
        return getWorkbook() != null;
    }

    public int promptToSaveOnClose() {
        //TODO
//        if (workbookRef != null && workbookRef.canSave()) {
//            NullProgressMonitor monitor = new NullProgressMonitor();
//            doSave(monitor);
//            if (monitor.isCanceled())
//                return CANCEL;
//            return NO;
//        }
        return DEFAULT;
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if (event.getSource() == workbookRef) {
            workbookRefPropertyChange(event);
        }
    }

    protected void workbookRefPropertyChange(PropertyChangeEvent event) {
        if (IEditable.PROP_NAME.equals(event.getProperty())
                || IEditable.PROP_DESCRIPTION.equals(event.getProperty())) {
            runInUI(new Runnable() {

                @Override
                public void run() {
                    updateNames();
                }
            });
        } else if (IEditable.PROP_DIRTY.equals(event.getProperty())) {
            runInUI(new Runnable() {

                @Override
                public void run() {
                    firePropertyChange(IWorkbenchPartConstants.PROP_DIRTY);
                }
            });
        } else if (IEditable.PROP_MESSAGES.equals(event.getProperty())) {
            runInUI(new Runnable() {

                @Override
                public void run() {
                    updateMessages(true);
                }
            });
        }

    }

    private void runInUI(final Runnable runnable) {
        final Control control = pageContainer;
        if (control == null || control.isDisposed())
            return;
        final Display display = control.getDisplay();
        if (display == null || display.isDisposed())
            return;
        display.asyncExec(runnable);
    }

    @Override
    public void handleCommandStackEvent(CommandStackEvent event) {
        // override to do nothing
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.ui.internal.editor.IMindMapPreviewGenerator#
     * generateMindMapPreview(org.xmind.ui.mindmap.IWorkbookRef,
     * java.io.OutputStream, org.xmind.ui.internal.editor.MindMapPreviewOptions)
     */
    @Override
    public Properties generateMindMapPreview(IWorkbookRef workbookRef,
            final ISheet sheet, final OutputStream output,
            MindMapPreviewOptions options) throws IOException {
        Assert.isLegal(output != null);

        final Properties properties = new Properties();
        if (sheet == null
                || MindMapUIPlugin.getDefault().getPreferenceStore()
                        .getBoolean(PrefConstants.PREVIEW_SKIPPED)
                || skipNextPreviewImage) {
            URL url = BundleUtility.find(MindMapUI.PLUGIN_ID,
                    IMindMapImages.DEFAULT_THUMBNAIL);
            if (url != null) {
                InputStream input = url.openStream();
                try {
                    FileUtils.transfer(input, output, false);
                } finally {
                    input.close();
                }
            }
            skipNextPreviewImage = false;
            return properties;
        }

        IEncryptable encryptable = workbookRef.getAdapter(IEncryptable.class);
        if (encryptable != null && encryptable.hasPassword()) {
            URL url = BundleUtility.find(MindMapUI.PLUGIN_ID,
                    IMindMapImages.ENCRYPTED_THUMBNAIL);
            if (url != null) {
                InputStream input = url.openStream();
                try {
                    FileUtils.transfer(input, output, false);
                } finally {
                    input.close();
                }
            }
            return properties;
        }

        Shell parentShell = getSite().getShell();
        final Display display = parentShell.getDisplay();
        final MindMapImageExporter exporter = new MindMapImageExporter(display);
        final Exception[] error = new Exception[1];
        display.syncExec(new Runnable() {

            public void run() {
                IGraphicalEditorPage page = findPage(sheet);
                if (page == null) {
                    page = getActivePageInstance();
                    if (page == null)
                        throw new IllegalArgumentException();
                }
//                if (page != null) {
                IGraphicalViewer sourceViewer = page.getViewer();
                MindMapViewerExportSourceProvider sourceProvider = new MindMapViewerExportSourceProvider(
                        sourceViewer, DEFAULT_EXPORT_MARGIN);
                org.eclipse.draw2d.geometry.Rectangle sourceArea = sourceProvider
                        .getSourceArea();

                int resizeWidth = Math
                        .max((sourceArea.width % 21 == 0) ? sourceArea.width
                                : (sourceArea.width + 21
                                        - sourceArea.width % 21),
                                (sourceArea.height % 13 == 0)
                                        ? sourceArea.height * 21 / 13
                                        : (sourceArea.height + 13
                                                - sourceArea.height % 13) * 21
                                                / 13);
                if (resizeWidth < MINIMUM_PREVIEW_WIDTH) {
                    resizeWidth = MINIMUM_PREVIEW_WIDTH;
                } else if (resizeWidth > MAXIMUM_PREVIEW_WIDTH) {
                    resizeWidth = MAXIMUM_PREVIEW_WIDTH;
                }
                int resizeHeight = resizeWidth * 13 / 21;
                exporter.setSourceProvider(sourceProvider);
                exporter.setResize(ResizeConstants.RESIZE_STRETCH, resizeWidth,
                        resizeHeight);
                exporter.setTargetStream(output);

                try {
                    exporter.export();
                } catch (SWTException e) {
                    error[0] = e;
                    return;
                }

                org.eclipse.draw2d.geometry.Point origin = exporter
                        .calcRelativeOrigin();

                properties.put(PREVIEW_ORIGIN_X, String.valueOf(origin.x));
                properties.put(PREVIEW_ORIGIN_Y, String.valueOf(origin.y));
                properties.put(PREVIEW_BACKGROUND,
                        exporter.getBackgroundColor());
            }
        });

        if (error[0] != null) {
            throw new IOException(error[0]);
        }

        return properties;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.xmind.ui.wizards.ISaveContext#getContextVariable(java.lang.Class)
     */
    @Override
    public <T> T getContextVariable(Class<T> key) {
        return getSite().getService(key);
    }

    /*
     * (non-Javadoc)
     * @see
     * org.xmind.ui.wizards.ISaveContext#getContextVariable(java.lang.String)
     */
    @Override
    public Object getContextVariable(String key) {
        IEvaluationService service = getSite()
                .getService(IEvaluationService.class);
        Assert.isNotNull(service);
        Object variable = service.getCurrentState().getVariable(key);
        return variable == IEvaluationContext.UNDEFINED_VARIABLE ? null
                : variable;
    }

    private void activateFileNotifier() {
        getEditorSite().getWorkbenchWindow().getWorkbench()
                .addWindowListener(getWindowListener());
        getEditorSite().getPage().addSelectionListener(getEditorSite().getId(),
                getSelectionListener());
    }

    private void deactivateFileNotifier() {
        getEditorSite().getWorkbenchWindow().getWorkbench()
                .removeWindowListener(getWindowListener());
        getEditorSite().getPage().removeSelectionListener(
                getEditorSite().getId(), getSelectionListener());
    }

    private IWindowListener getWindowListener() {
        if (windowListener == null) {
            windowListener = new IWindowListener() {

                @Override
                public void windowOpened(IWorkbenchWindow window) {
                }

                @Override
                public void windowDeactivated(IWorkbenchWindow window) {
                }

                @Override
                public void windowClosed(IWorkbenchWindow window) {
                }

                @Override
                public void windowActivated(IWorkbenchWindow window) {
                    IEditorPart activeEditor = getEditorSite().getPage()
                            .getActiveEditor();
                    if (activeEditor == MindMapEditor.this) {
                        if (workbookRef != null) {
                            workbookRef.activateNotifier();
                        }
                    }
                }
            };
        }
        return windowListener;
    }

    private ISelectionListener getSelectionListener() {
        if (selectionListener == null) {
            selectionListener = new ISelectionListener() {

                @Override
                public void selectionChanged(IWorkbenchPart part,
                        ISelection selection) {
                    if (workbookRef != null) {
                        workbookRef.activateNotifier();
                    }
                }
            };
        }
        return selectionListener;
    }

    @Override
    public void fileChanged(final String title, final String message,
            final String[] buttons) {
        if (workbookRef.getState() != IWorkbookRef.NORMAL) {
            return;
        }

        if (ignoreFileChanged) {
            return;
        }

        Display.getDefault().asyncExec(new Runnable() {

            @Override
            public void run() {
                deactivateFileNotifier();
                getEditorSite().getPage().activate(MindMapEditor.this);
                if (!isDirty()) {
                    reload();
                } else {
                    MessageDialog dialog = new MessageDialog(null, title, null,
                            NLS.bind(
                                    MindMapMessages.MindMapEditor_fileChangedDialog_message_prefix
                                            + message,
                                    workbookRef.getName()),
                            MessageDialog.CONFIRM, buttons, 0);

                    int code = dialog.open();
                    if (code == 0) {
                        reload();
                    } else if (code == 1 || code == -1) {
                        ignoreFileChanged = true;
                    }
                }

                activateFileNotifier();
            }
        });
    }

    @Override
    public void fileRemoved(final String title, final String message,
            final String[] buttons, boolean forceQuit) {
        if (workbookRef.getState() != IWorkbookRef.NORMAL) {
            return;
        }

        if (forceQuit) {
            Display.getDefault().asyncExec(new Runnable() {

                @Override
                public void run() {
                    closeEditor();
                }
            });
            return;
        }

        Display.getDefault().asyncExec(new Runnable() {

            @Override
            public void run() {
                deactivateFileNotifier();
                getEditorSite().getPage().activate(MindMapEditor.this);
                if (!isDirty()) {
                    closeEditor();
                } else {
                    MessageDialog dialog = new MessageDialog(null, title, null,
                            NLS.bind(
                                    MindMapMessages.MindMapEditor_fileRemovedDialog_message_prefix
                                            + message,
                                    workbookRef.getName()),
                            MessageDialog.CONFIRM, buttons, 0);

                    int code = dialog.open();
                    if (code == 0) {
                        doSaveAs();
                    } else if (code == 1 || code == -1) {
                        closeEditor();
                    }

                    activateFileNotifier();
                }
            }
        });
    }

}
