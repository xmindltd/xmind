package org.xmind.ui.internal.e4models;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelectionProvider;

public interface IModelPartContext extends IAdaptable {

    boolean registerContextMenu(Object menuParent, String menuId);

    boolean registerViewMenu(String viewMenuId);

    public void setSelectionProvider(ISelectionProvider selectionProvider);

}
