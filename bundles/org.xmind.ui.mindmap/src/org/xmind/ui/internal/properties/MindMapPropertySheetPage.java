/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See https://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package org.xmind.ui.internal.properties;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.part.IPageSite;
import org.xmind.core.style.IStyled;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.gef.ui.properties.GraphicalPropertySheetPage;
import org.xmind.ui.commands.CommandMessages;
import org.xmind.ui.commands.ModifyStyleCommand;
import org.xmind.ui.forms.WidgetFactory;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.mindmap.ICategoryAnalyzation;
import org.xmind.ui.mindmap.ICategoryManager;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.resources.FontUtils;

public class MindMapPropertySheetPage extends GraphicalPropertySheetPage {

    private static PropertySectionContributorManager manager = PropertySectionContributorManager
            .getInstance();

    private Hyperlink resetStyleControl;

    public MindMapPropertySheetPage(IGraphicalEditor editor) {
        super(editor);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.gef.ui.properties.GraphicalPropertySheetPage#init(org.eclipse
     * .ui.part.IPageSite)
     */
    @Override
    public void init(IPageSite pageSite) {
        super.init(pageSite);
        IActionBars targetActionBars = pageSite.getActionBars();
        IActionBars sourceActionBars = getContributedEditor().getEditorSite()
                .getActionBars();
        retargetAction(sourceActionBars, targetActionBars,
                ActionFactory.UNDO.getId());
        retargetAction(sourceActionBars, targetActionBars,
                ActionFactory.REDO.getId());
    }

    /**
     * @param sourceActionBars
     * @param targetActionBars
     * @param id
     */
    private void retargetAction(IActionBars sourceActionBars,
            IActionBars targetActionBars, String actionId) {
        IAction handler = sourceActionBars.getGlobalActionHandler(actionId);
        if (handler != null) {
            targetActionBars.setGlobalActionHandler(actionId, handler);
        }
    }

    @Override
    protected void createExtendSectionControls(WidgetFactory widgetFactory,
            Composite parent) {
        super.createExtendSectionControls(widgetFactory, parent);
        createResetStyleControl(widgetFactory, parent);
    }

    private void createResetStyleControl(WidgetFactory widgetFactory,
            Composite parent) {
        resetStyleControl = widgetFactory.createHyperlink(parent,
                MindMapMessages.MindMapPropertySheetPage_ResetStyle_text,
                SWT.NONE);
        resetStyleControl.setUnderlined(false);
        resetStyleControl.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER,
                true, false));
        resetStyleControl.addHyperlinkListener(new IHyperlinkListener() {

            public void linkExited(HyperlinkEvent e) {
                resetStyleControl.setUnderlined(false);
            }

            public void linkEntered(HyperlinkEvent e) {
                resetStyleControl.setUnderlined(true);
            }

            public void linkActivated(HyperlinkEvent e) {
                resetStyles();
            }
        });
        resetStyleControl.setFont(FontUtils.getRelativeHeight(
                JFaceResources.DEFAULT_FONT, -1));
        resetStyleControl.setForeground(parent.getDisplay().getSystemColor(
                SWT.COLOR_BLUE));
    }

    private boolean shouldHasResetStyleControl(ISelection selection) {
        if (!(selection instanceof StructuredSelection))
            return false;

        boolean result = true;

        Object[] resetedStyleds = ((StructuredSelection) selection).toArray();
        if (resetedStyleds != null) {
            for (Object styled : resetedStyleds) {
                if (!(styled instanceof IStyled)) {
                    result = false;
                }
            }
        }
        return result;
    }

    protected void resetStyles() {
        IGraphicalEditor editor = getContributedEditor();
        if (editor == null)
            return;

        IGraphicalEditorPage activePageInstance = editor
                .getActivePageInstance();
        if (activePageInstance == null)
            return;

        ISelectionProvider selectionProvider = activePageInstance
                .getSelectionProvider();
        if (selectionProvider == null)
            return;

        ISelection selection = selectionProvider.getSelection();
        if (!(selection instanceof StructuredSelection))
            return;

        Object[] resetedStyleds = ((StructuredSelection) selection).toArray();
        if (resetedStyleds != null) {
            for (Object styled : resetedStyleds) {
                if (styled instanceof IStyled) {
                    IStyled resetedStyled = (IStyled) styled;
                    ModifyStyleCommand modifyStyleCommand = new ModifyStyleCommand(
                            resetedStyled, (String) null);
                    modifyStyleCommand
                            .setLabel(CommandMessages.Command_ModifyStyle);
                    editor.getCommandStack().execute(modifyStyleCommand);
                }
            }
        }
    }

    protected void selectionChanged(ISelection selection) {
        if (getControl() != null && !getControl().isDisposed())
            getControl().setRedraw(false);

        if (resetStyleControl != null && !resetStyleControl.isDisposed()) {
            boolean resetStyleControlVisible = shouldHasResetStyleControl(selection);
            GridData gd = (GridData) resetStyleControl.getLayoutData();
            gd.exclude = !resetStyleControlVisible;
            resetStyleControl.setVisible(resetStyleControlVisible);
        }

        if (selection instanceof IStructuredSelection) {
            IStructuredSelection ss = (IStructuredSelection) selection;
            List<String> newVisibleSectionIds = manager
                    .getApplicableSectionIds(ss.toArray());
            List<String> oldVisibleSectionIds = getVisibleSectionIds();
            if (!equalsList(oldVisibleSectionIds, newVisibleSectionIds)) {
                List<String> oldSectionIds = getSectionIds();
                List<String> toAdd = new ArrayList<String>(newVisibleSectionIds);
                toAdd.removeAll(oldSectionIds);
                for (String id : toAdd) {
                    addSection(id, newVisibleSectionIds, oldSectionIds);
                    oldSectionIds = getSectionIds();
                }

                List<String> toHide = new ArrayList<String>(oldSectionIds);
                toHide.removeAll(newVisibleSectionIds);
                for (String id : oldSectionIds) {
                    setSectionVisible(id, !toHide.contains(id));
                }

                reflow();
            }
            setTitle(calcTitle(ss.toArray()));
        } else {
            setTitle(null);
        }
        if (getControl() != null && !getControl().isDisposed())
            getControl().setRedraw(true);
    }

    private void addSection(String id, List<String> newVisibleSectionIds,
            List<String> oldSectionIds) {
        addSection(id, manager.createSection(id));
        String aboveId = findAboveId(id, oldSectionIds, newVisibleSectionIds);
        moveSectionAfter(id, aboveId);
    }

    private String findAboveId(String id, List<String> oldSectionIds,
            List<String> newSectionIds) {
        int index = newSectionIds.indexOf(id);
        for (int i = index - 1; i >= 0; i--) {
            String aboveId = newSectionIds.get(i);
            if (oldSectionIds.contains(aboveId))
                return aboveId;
        }
        return null;
    }

    private static boolean equalsList(List<String> list1, List<String> list2) {
        if (list1.size() != list2.size())
            return false;
        for (int i = 0; i < list1.size(); i++) {
            String s1 = list1.get(i);
            String s2 = list2.get(i);
            if (!s1.equals(s2))
                return false;
        }
        return true;
    }

    private String calcTitle(Object[] objects) {
        if (objects == null || objects.length == 0)
            return null;
        String category = getCategoryName(objects);
        return category;
//        String names = join(getObjectNames(objects));
//        if ("".equals(names)) //$NON-NLS-1$
//            return category;
//        return NLS.bind("{0} ({1})", category, names); //$NON-NLS-1$
    }

//    private String[] getObjectNames(Object[] objects) {
//        String[] names = new String[objects.length];
//        for (int i = 0; i < objects.length; i++) {
//            Object obj = objects[i];
//            names[i] = trim(getObjectName(obj));
//        }
//        return names;
//    }

//    private String getObjectName(Object obj) {
//        if (obj instanceof ITitled)
//            return ((ITitled) obj).getTitleText();
//        if (obj instanceof INamed)
//            return ((INamed) obj).getName();
//        if (obj instanceof IAdaptable) {
//            ITitled titled = (ITitled) ((IAdaptable) obj)
//                    .getAdapter(ITitled.class);
//            if (titled != null)
//                return titled.getTitleText();
//            INamed named = (INamed) ((IAdaptable) obj).getAdapter(INamed.class);
//            if (named != null)
//                return named.getName();
//        }
//
//        if (obj != null)
//            return obj.toString();
//        return ""; //$NON-NLS-1$
//    }

    private String getCategoryName(Object[] objects) {
        ICategoryManager typeManager = MindMapUI.getCategoryManager();
        ICategoryAnalyzation result = typeManager.analyze(objects);
        return typeManager.getCategoryName(result.getMainCategory());
    }

//    private static String join(String[] strs) {
//        StringBuilder sb = new StringBuilder(strs.length * 15);
//        for (String s : strs) {
//            if (sb.length() > 0) {
//                sb.append(", "); //$NON-NLS-1$
//            }
//            sb.append(s);
//        }
//        return sb.toString();
//    }

//    private static String trim(String name) {
//        name = name.replaceAll("\\s+", " ").trim(); //$NON-NLS-1$ //$NON-NLS-2$
//        return name.length() > 100 ? name.substring(0, 97) + "..." : name; //$NON-NLS-1$
//    }

}