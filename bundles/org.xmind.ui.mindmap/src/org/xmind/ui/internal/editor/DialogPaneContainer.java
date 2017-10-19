package org.xmind.ui.internal.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.services.IServiceLocator;

public class DialogPaneContainer implements IDialogPaneContainer {

    private Composite composite;

    private IDialogPane currentPane;

    private IServiceLocator serviceLocator;

    public void init(IServiceLocator serviceLocator) {
        this.serviceLocator = serviceLocator;
    }

    public Control getControl() {
        return composite;
    }

    public void createControl(Composite parent) {
        composite = new Composite(parent, SWT.NONE);

        Display display = parent.getDisplay();
        final Color background = new Color(display, 0x38, 0x38, 0x38);
        composite.setBackground(background);

        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        gridLayout.verticalSpacing = 0;
        gridLayout.horizontalSpacing = 0;
        composite.setLayout(gridLayout);

        composite.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                background.dispose();
                handleDispose();
            }
        });
    }

    protected void handleDispose() {
        hideCurrentDialog();
    }

    public void open(IDialogPane dialog) {
        if (composite == null || composite.isDisposed())
            return;
        showDialog(dialog);
    }

    protected void showDialog(IDialogPane dialog) {
        if (composite == null || composite.isDisposed())
            return;

        doHideCurrentDialog();
        currentPane = dialog;
        dialog.init(this);
        currentPane.createControl(composite);
        currentPane.setFocus();
        composite.layout(true);

        IContextActivation activation = null;
        IContextService contextService = serviceLocator == null ? null
                : (IContextService) serviceLocator
                        .getService(IContextService.class);
        if (contextService != null) {
            activation = contextService
                    .activateContext("org.xmind.ui.context.backcover"); //$NON-NLS-1$
        }

//        
//        
//        if (contextService != null && activation != null) {
//            contextService.deactivateContext(activation);
//        }
    }

    protected IDialogPane getCurrentDialog() {
        return currentPane;
    }

    protected void hideCurrentDialog() {
        doHideCurrentDialog();
        if (composite != null && !composite.isDisposed()) {
            composite.layout(true);
        }
    }

    private void doHideCurrentDialog() {
        if (currentPane != null) {
            Control pageControl = currentPane.getControl();
            currentPane.dispose();
            if (pageControl != null && !pageControl.isDisposed()) {
                pageControl.dispose();
            }
            currentPane = null;
        }
    }

    public void dispose() {
        close();
        if (composite != null) {
            composite.dispose();
        }
    }

    public void setFocus() {
        if (currentPane != null) {
            currentPane.setFocus();
        } else {
            composite.setFocus();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.ui.internal.editor.IDialogPaneContainer#close()
     */
    public boolean close() {
        hideCurrentDialog();
        return currentPane == null;
    }

    public void close(int returnCode) {
        if (currentPane != null) {
            currentPane.setReturnCode(returnCode);
        }
        close();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.ui.internal.editor.IDialogPaneContainer#isOpen()
     */
    public boolean isOpen() {
        return currentPane != null;
    }

}