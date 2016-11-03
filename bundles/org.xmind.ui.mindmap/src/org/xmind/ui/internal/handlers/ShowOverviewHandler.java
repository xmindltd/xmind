package org.xmind.ui.internal.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.prefs.PrefConstants;

public class ShowOverviewHandler extends AbstractHandler {

    private IPreferenceStore ps;

    public Object execute(ExecutionEvent event) throws ExecutionException {
        ps = MindMapUIPlugin.getDefault().getPreferenceStore();

        ps.setValue(PrefConstants.SHOW_OVERVIEW, true);

        return null;
    }

}
