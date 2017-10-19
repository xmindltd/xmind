package org.xmind.ui.tabfolder;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;

public class MTabItem extends MTabBarItem {

    private MTabFolder parent;

    private Control control = null;

    private String tooltipText;

    public MTabItem(MTabFolder parent, int style) {
        super(parent.getTabBar(), style);
        this.parent = parent;
        parent.createItem(this);
    }

    public Control getControl() {
        checkWidget();
        return control;
    }

    public void setControl(Control control) {
        checkWidget();
        if (control.getParent() != parent.getBody())
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        if (!isRadioButton())
            return;
        this.control = control;
        parent.updateItem(this);
    }

    @Override
    public void dispose() {
        super.dispose();
        if (isDisposed())
            return;
        parent.destroyItem(this);
        parent = null;
    }

    public void setTooltipText(String tooltipText) {
        this.tooltipText = tooltipText;
    }

    public String getTooltipText() {
        return tooltipText;
    }

}
