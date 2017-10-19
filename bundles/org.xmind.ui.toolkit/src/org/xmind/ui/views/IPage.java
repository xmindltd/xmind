package org.xmind.ui.views;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * A page is an area that's backed by one dedicated SWT control. A page is
 * responsible for creating the control and setting valid focus inside the
 * control when requested. A page should be regarded as <em>discarded</em> when
 * its control is disposed of, and it should dispose of all allocated resources
 * and revert other states when discarded.
 * 
 * @author Frank Shaka
 * @since 3.6.50
 */
public interface IPage extends IAdaptable {

    /**
     * Creates the control of this page inside the given composite control. This
     * method should be called only once during the life cycle of this page.
     * 
     * @param parent
     *            the parent composite control
     */
    void createControl(Composite parent);

    /**
     * Returns the control created by {@link #createControl()}.
     * 
     * @return the control of this page
     */
    Control getControl();

    /**
     * Sets valid focus inside the control of this page.
     */
    void setFocus();

}
