package org.xmind.ui.internal.resourcemanager;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.xmind.core.IAdaptable;

public interface IResourceManagerDialogPage extends IAdaptable {

    public Image getImage();

    public String getTitle();

    public String getId();

    public Control getControl();

    public void setImageDescriptor(ImageDescriptor image);

    public void setTitle(String title);

    public void setId(String id);

    public void createControl(Composite parent);

    public void dispose();

    public void refresh();

}
