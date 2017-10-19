package org.xmind.cathy.internal.renderer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.e4.ui.internal.workbench.ContributionsAnalyzer;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuContribution;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.workbench.renderers.swt.MenuManagerRenderer;

public class XMenuManagerRenderer extends MenuManagerRenderer {

    private static final String[] retainedIconMenus = {
            "org.xmind.ui.toolbar.mindmap.topic.menu", //$NON-NLS-1$
            "org.xmind.ui.toolbar.export.export.menu", //$NON-NLS-1$
            "org.xmind.ui.toolbar.mindmap.insert.menu" //$NON-NLS-1$
    };

    @Inject
    private MApplication application;

    @Override
    public void processContributions(MMenu menuModel, String elementId,
            boolean isMenuBar, boolean isPopup) {
        if (elementId == null) {
            return;
        }

        List<MMenuElement> elements = menuModel.getChildren();
        for (MMenuElement menuItem : elements) {
            if (menuItem instanceof MMenuElement) {
                if (!checkRetainedIconMenu(menuModel)) {
                    ((MMenuElement) menuItem).setIconURI(null);
                }
                menuItem.setTooltip(""); //$NON-NLS-1$
            }
        }

        final ArrayList<MMenuContribution> toContribute = new ArrayList<MMenuContribution>();
        ContributionsAnalyzer.XXXgatherMenuContributions(menuModel,
                application.getMenuContributions(), elementId, toContribute,
                null, isPopup);
        for (MMenuContribution contri : toContribute) {
            List<MMenuElement> children = contri.getChildren();
            for (MMenuElement me : children) {
                if (!checkRetainedIconMenu(menuModel)) {
                    ((MMenuElement) me).setIconURI(null);
                }
                me.setTooltip(""); //$NON-NLS-1$
            }
        }
        super.processContributions(menuModel, elementId, isMenuBar, isPopup);
    }

    private boolean checkRetainedIconMenu(MMenu menuModel) {
        return Arrays.asList(retainedIconMenus)
                .contains(menuModel.getElementId());
    }

}
