package org.xmind.cathy.internal;

import java.net.MalformedURLException;
import java.net.URL;

import javax.annotation.PostConstruct;

import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.SideValue;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MToolControl;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;

public class UndoRedoActionToolControl {

    private static class HandledAction extends Action
            implements IPropertyChangeListener {

        private IAction handler;

        public HandledAction(IAction handler) {
            super(handler.getText(), handler.getStyle());
            this.handler = handler;
            setId(handler.getId());
            setEnabled(handler.isEnabled());
            handler.addPropertyChangeListener(this);
        }

        @Override
        public void runWithEvent(Event event) {
            handler.runWithEvent(event);
        }

        @Override
        public void run() {
            handler.run();
        }

        public void propertyChange(PropertyChangeEvent event) {
            if (event.getProperty().equals(IAction.ENABLED)) {
                Boolean bool = (Boolean) event.getNewValue();
                setEnabled(bool.booleanValue());
            } else if (event.getProperty().equals(IAction.CHECKED)) {
                Boolean bool = (Boolean) event.getNewValue();
                setChecked(bool.booleanValue());
            } else if (event.getProperty().equals(IAction.TOOL_TIP_TEXT)) {
                String str = (String) event.getNewValue();
                setToolTipText(str);
            }
        }

    }

    @PostConstruct
    public void createWidget(Composite parent, MToolControl toolControl,
            MWindow window) {
        int orientation = getOrientation(toolControl.getParent());
        int contributionItemMode = toolControl.getTags()
                .contains(ICathyConstants.TAG_FORCE_TEXT)
                        ? ActionContributionItem.MODE_FORCE_TEXT : 0;

        ToolBarManager toolBarManager = new ToolBarManager(
                orientation | SWT.RIGHT | SWT.WRAP | SWT.FLAT);

        IWorkbenchWindow ww = window.getContext().get(IWorkbenchWindow.class);
        if (ww != null) {
            IAction undoAction = new HandledAction(
                    ActionFactory.UNDO.create(ww));
            undoAction.setImageDescriptor(imageDescriptorFor("undo.png")); //$NON-NLS-1$
            ActionContributionItem undoItem = new ActionContributionItem(
                    undoAction);
            undoItem.setMode(contributionItemMode);
            toolBarManager.add(undoItem);

            IAction redoAction = new HandledAction(
                    ActionFactory.REDO.create(ww));
            redoAction.setImageDescriptor(imageDescriptorFor("redo.png")); //$NON-NLS-1$
            ActionContributionItem redoItem = new ActionContributionItem(
                    redoAction);
            redoItem.setMode(contributionItemMode);
            toolBarManager.add(redoItem);
        }

        toolBarManager.createControl(parent);
    }

    private ImageDescriptor imageDescriptorFor(String iconName) {
        try {
            return ImageDescriptor.createFromURL(
                    new URL("platform:/plugin/org.xmind.cathy/icons/toolbar/e/" //$NON-NLS-1$
                            + iconName));
        } catch (MalformedURLException e) {
            return null;
        }
    }

    private int getOrientation(final MUIElement element) {
        MTrimBar trimContainer = findTrimBar(element);
        if (trimContainer != null) {
            SideValue side = trimContainer.getSide();
            if (side.getValue() == SideValue.LEFT_VALUE
                    || side.getValue() == SideValue.RIGHT_VALUE) {
                return SWT.VERTICAL;
            }
        }
        return SWT.HORIZONTAL;
    }

    private MTrimBar findTrimBar(MUIElement element) {
        if (element == null)
            return null;
        if (element instanceof MTrimBar)
            return (MTrimBar) element;
        return findTrimBar(element.getParent());
    }
}