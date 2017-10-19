package org.xmind.ui.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPluginContribution;
import org.eclipse.ui.dialogs.PreferencesUtil;

public class PrefUtils {

    public static final String GENERAL_PREF_PAGE_ID = "org.xmind.ui.prefPage.General"; //$NON-NLS-1$

    private static final Set<String> HIDDEN_PAGE_IDS = new HashSet<String>(
            Arrays.<String> asList(

                    "org.eclipse.equinox.security.ui.category", //$NON-NLS-1$
                    "org.eclipse.equinox.security.ui.storage" //$NON-NLS-1$

            ));

    public static void openPrefDialog(Shell shell, String prefPageId,
            Object data) {

        PreferenceDialog dialog = PreferencesUtil
                .createPreferenceDialogOn(shell, prefPageId, null, data);
        if (dialog == null)
            return;

        IProduct product = Platform.getProduct();
        if (product != null && "org.xmind.cathy.application" //$NON-NLS-1$
                .equals(product.getApplication())) {
            configTreeViewerFilter(dialog);
            dialog.getTreeViewer().setExpandPreCheckFilters(true);
            dialog.getTreeViewer().expandAll();
        }
        dialog.open();
    }

    private static void configTreeViewerFilter(PreferenceDialog dialog) {
        dialog.getTreeViewer().addFilter(new ViewerFilter() {

            @Override
            public boolean select(Viewer viewer, Object parentElement,
                    Object element) {
                if (element instanceof IPluginContribution) {
                    String id = ((IPluginContribution) element).getLocalId();
                    if (HIDDEN_PAGE_IDS.contains(id))
                        return false;
                }
                return true;
            }
        });
    }

    public static void openPrefDialog(Shell shell, String prefPageId) {
        openPrefDialog(shell, prefPageId, null);
    }

}
