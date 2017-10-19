package org.xmind.ui.views;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * 
 * @author Frank Shaka
 * @since 3.6.50
 */
public abstract class Page implements IPage {

    private Control control;

    public void createControl(Composite parent) {
        Assert.isTrue(this.control == null);
        Control control = doCreateControl(parent);
        Assert.isTrue(control != null);
        this.control = control;
    }

    protected abstract Control doCreateControl(Composite parent);

    public <T> T getAdapter(Class<T> adapter) {
        if (Control.class.equals(adapter))
            return adapter.cast(control);
        return null;
    }

    public Control getControl() {
        return control;
    }

    public void setFocus() {
        setFocus(control);
    }

    protected boolean setFocus(Control c) {
        if (c != null && !c.isDisposed()) {
            return c.setFocus();
        }
        return false;
    }

}
