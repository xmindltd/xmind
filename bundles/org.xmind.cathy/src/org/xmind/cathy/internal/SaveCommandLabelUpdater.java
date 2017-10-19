
package org.xmind.cathy.internal;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.EventTopic;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MItem;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.osgi.service.event.Event;
import org.xmind.core.Core;
import org.xmind.core.IMeta;
import org.xmind.core.IWorkbook;
import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.ICoreEventListener;
import org.xmind.core.event.ICoreEventRegistration;
import org.xmind.core.event.ICoreEventSource;
import org.xmind.ui.internal.MindMapMessages;

public class SaveCommandLabelUpdater
        implements IPartListener, IPropertyListener, ICoreEventListener {

    private static final String DATA_ORIGINAL_TEXT = "SaveCommandLabelUpdater:OriginalText"; //$NON-NLS-1$
    private static final String DATA_ORIGINAL_TOOLTIP = "SaveCommandLabelUpdater:OriginalTooltip"; //$NON-NLS-1$

    @Inject
    private EModelService modelService;

    @Inject
    private Display display;

    private MWindow activeWindow = null;
    private IEditorPart activeEditor = null;
    private IWorkbook editingWorkbook = null;
    private ICoreEventRegistration metadataEventRegistration = null;

    private void updateSaveCommandLabel() {
        if (this.activeWindow == null)
            return;

        String text;
        String tooltip;
        if (this.editingWorkbook != null
                && isAutoGeneratingRevision(this.editingWorkbook)) {
            text = MindMapMessages.SaveNewRevision_text;
            tooltip = MindMapMessages.SaveNewRevision_tooltip;
        } else {
            text = null;
            tooltip = null;
        }

        MItem item = findItem(this.activeWindow.getMainMenu(),
                ICathyConstants.ID_MENU_ITEM_SAVE);
        if (item != null) {
            if (text == null) {
                if (item.getTransientData().containsKey(DATA_ORIGINAL_TEXT)) {
                    item.setLabel((String) item.getTransientData()
                            .get(DATA_ORIGINAL_TEXT));
                }
            } else {
                if (!item.getTransientData().containsKey(DATA_ORIGINAL_TEXT)) {
                    item.getTransientData().put(DATA_ORIGINAL_TEXT,
                            item.getLabel());
                }
                item.setLabel(text);
            }
        }

        item = findItem(this.activeWindow, ICathyConstants.ID_TOOL_ITEM_SAVE);
        if (item != null) {
            if (tooltip == null) {
                if (item.getTransientData()
                        .containsKey(DATA_ORIGINAL_TOOLTIP)) {
                    item.setTooltip((String) item.getTransientData()
                            .get(DATA_ORIGINAL_TOOLTIP));
                }
            } else {
                if (!item.getTransientData()
                        .containsKey(DATA_ORIGINAL_TOOLTIP)) {
                    item.getTransientData().put(DATA_ORIGINAL_TOOLTIP,
                            item.getTooltip());
                }
                item.setTooltip(tooltip);
            }
        }
    }

    @Inject
    @Optional
    public void applicationStarted(
            @EventTopic(UIEvents.UILifeCycle.APP_STARTUP_COMPLETE) Event event,
            @Optional final MApplication app) {
        if (app == null)
            return;

        if (display == null || display.isDisposed())
            return;

        display.syncExec(new Runnable() {
            public void run() {
                MWindow window = app.getSelectedElement();
                setActiveWindow(window);
                updateSaveCommandLabel();
            }
        });
    }

    @Inject
    @Optional
    public void windowChanged(
            @EventTopic(UIEvents.ElementContainer.TOPIC_SELECTEDELEMENT) Event event) {
        Object selectedElement = event
                .getProperty(UIEvents.ElementContainer.SELECTEDELEMENT);
        if (!(selectedElement instanceof MWindow))
            return;

        setActiveWindow((MWindow) selectedElement);
        updateSaveCommandLabel();
    }

    public void partActivated(IWorkbenchPart part) {
        setActiveEditor(findActiveEditorFrom(part));
        updateSaveCommandLabel();
    }

    public void partBroughtToTop(IWorkbenchPart part) {
    }

    public void partClosed(IWorkbenchPart part) {
        setActiveEditor(findActiveEditorFrom(part));
        updateSaveCommandLabel();
    }

    public void partDeactivated(IWorkbenchPart part) {
    }

    public void partOpened(IWorkbenchPart part) {
    }

    public void propertyChanged(Object source, int propId) {
        if (propId == IEditorPart.PROP_INPUT) {
            setEditingWorkbook(findWorkbookIn(this.activeEditor));
            updateSaveCommandLabel();
        }
    }

    public void handleCoreEvent(CoreEvent event) {
        if (IMeta.CONFIG_AUTO_REVISION_GENERATION.equals(event.getTarget())) {
            if (display != null) {
                display.syncExec(new Runnable() {
                    public void run() {
                        updateSaveCommandLabel();
                    }
                });
            }
        }
    }

    private void setActiveWindow(MWindow window) {
        if (window != this.activeWindow) {
            if (this.activeWindow != null) {
                IWorkbenchWindow wbWindow = this.activeWindow.getContext()
                        .get(IWorkbenchWindow.class);
                if (wbWindow != null)
                    wbWindow.getPartService().removePartListener(this);
            }
            this.activeWindow = window;
            if (this.activeWindow != null) {
                IWorkbenchWindow wbWindow = this.activeWindow.getContext()
                        .get(IWorkbenchWindow.class);
                if (wbWindow != null)
                    wbWindow.getPartService().addPartListener(this);
            }
        }
        setActiveEditor(findActiveEditorIn(this.activeWindow));
    }

    private void setActiveEditor(IEditorPart editor) {
        if (editor != this.activeEditor) {
            if (this.activeEditor != null)
                this.activeEditor.removePropertyListener(this);
            this.activeEditor = editor;
            if (this.activeEditor != null)
                this.activeEditor.addPropertyListener(this);
        }
        setEditingWorkbook(findWorkbookIn(editor));
    }

    private void setEditingWorkbook(IWorkbook workbook) {
        if (workbook != this.editingWorkbook) {
            if (this.metadataEventRegistration != null) {
                this.metadataEventRegistration.unregister();
                this.metadataEventRegistration = null;
            }
            this.editingWorkbook = workbook;
            if (this.editingWorkbook != null && this.editingWorkbook
                    .getMeta() instanceof ICoreEventSource) {
                this.metadataEventRegistration = ((ICoreEventSource) this.editingWorkbook
                        .getMeta()).registerCoreEventListener(Core.Metadata,
                                this);
            }
        }
    }

    private IEditorPart findActiveEditorFrom(IWorkbenchPart referencePart) {
        if (referencePart == null)
            return null;

        IWorkbenchWindow window = referencePart.getSite().getWorkbenchWindow();
        return findActiveEditorIn(window);
    }

    private IEditorPart findActiveEditorIn(MWindow window) {
        if (window == null)
            return null;
        IWorkbenchWindow wbWindow = window.getContext()
                .get(IWorkbenchWindow.class);
        return findActiveEditorIn(wbWindow);
    }

    private IEditorPart findActiveEditorIn(IWorkbenchWindow window) {
        if (window == null)
            return null;
        IWorkbenchPage page = window.getActivePage();
        if (page == null)
            return null;
        return page.getActiveEditor();
    }

    private IWorkbook findWorkbookIn(IEditorPart editor) {
        if (editor == null)
            return null;
        return editor.getAdapter(IWorkbook.class);
    }

    private MItem findItem(MUIElement rootElement, String id) {
        if (this.modelService == null || rootElement == null)
            return null;
        MUIElement element = this.modelService.find(id, rootElement);
        if (!(element instanceof MItem))
            return null;
        return (MItem) element;
    }

    private static final boolean isAutoGeneratingRevision(IWorkbook workbook) {
        String value = workbook.getMeta()
                .getValue(IMeta.CONFIG_AUTO_REVISION_GENERATION);
        return value == null || IMeta.V_YES.equalsIgnoreCase(value);
    }

}
