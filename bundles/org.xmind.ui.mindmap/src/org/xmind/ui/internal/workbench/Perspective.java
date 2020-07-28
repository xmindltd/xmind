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
package org.xmind.ui.internal.workbench;

import java.lang.reflect.Field;

import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.internal.e4.compatibility.ModeledFolderLayout;
import org.xmind.ui.internal.e4models.IModelConstants;

/**
 * Mind Mapping Perspective
 * 
 * @author Brian Sun
 */
public class Perspective implements IPerspectiveFactory {

//    public static final String CONSOLE_VIEW_ID = "org.eclipse.ui.console.ConsoleView"; //$NON-NLS-1$
//    public static final String OUTLINE_VIEW_ID = "org.eclipse.ui.views.ContentOutline"; //$NON-NLS-1$
//    public static final String PROPERTIES_VIEW_ID = "org.eclipse.ui.views.PropertySheet"; //$NON-NLS-1$

//    public static final String STACK_NAVIGATORS = "org.xmind.ui.stack.navigators"; //$NON-NLS-1$
//    public static final String STACK_INSPECTORS = "org.xmind.ui.stack.inspectors"; //$NON-NLS-1$
//    public static final String STACK_ASSISTANTS = "org.xmind.ui.stack.assistants"; //$NON-NLS-1$
//    public static final String STACK_LIBRARIES = "org.xmind.ui.stack.libraries"; //$NON-NLS-1$

    public static final String STACK_RIGHT = "org.xmind.ui.stack.right"; //$NON-NLS-1$

    protected float getRatio(float width, boolean horizontal) {
        Point size = Util.getInitialWindowSize();
        return width / (horizontal ? size.x : size.y);
    }

    private Field getField(Class type, String name) {
        while (!type.equals(Object.class)) {
            try {
                return type.getDeclaredField(name);
            } catch (Exception exc) {
                type = type.getSuperclass();
            }
        }
        return null;
    }

    public void createInitialLayout(IPageLayout pageLayout) {

        IFolderLayout rightLayout = pageLayout.createFolder(STACK_RIGHT,
                IPageLayout.RIGHT, 1f, pageLayout.getEditorArea());
        if (rightLayout instanceof ModeledFolderLayout) {
            try {
                Field partStackField = getField(ModeledFolderLayout.class,
                        "folderModel");  //$NON-NLS-1$
                boolean oldAccessible = partStackField.isAccessible();
                partStackField.setAccessible(true);
                Object partStack = partStackField.get(rightLayout);
                partStackField.setAccessible(oldAccessible);
                if (partStack instanceof MPartStack) {
                    ((MPartStack) partStack).getTags()
                            .add(IModelConstants.TAG_X_STACK);
                }
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
//        rightLayout.addView(IModelConstants.PART_ID_PROPERTIES);

//        rightLayout.addPlaceholder(OUTLINE_VIEW_ID);
//        rightLayout.addPlaceholder(MindMapUI.VIEW_MARKER);
//        rightLayout.addPlaceholder(MindMapUI.VIEW_THEMES);
//        rightLayout.addPlaceholder(MindMapUI.VIEW_NOTES);
//        rightLayout.addPlaceholder(MindMapUI.VIEW_COMMENTS);

//        createLibrariesStack(pageLayout);
//        createNavigatorsStack(pageLayout);
//        createInspectorsStack(pageLayout);
//        createAssistantsStack(pageLayout);
//
//        pageLayout.addShowViewShortcut(OUTLINE_VIEW_ID);
//        pageLayout.addShowViewShortcut(MindMapUI.VIEW_OVERVIEW);
//        pageLayout.addShowViewShortcut(MindMapUI.VIEW_MARKER);
//        pageLayout.addShowViewShortcut(MindMapUI.VIEW_NOTES);
//        pageLayout.addShowViewShortcut(PROPERTIES_VIEW_ID);
//        pageLayout.addShowViewShortcut(MindMapUI.VIEW_THEMES);
//        pageLayout.addShowViewShortcut(MindMapUI.VIEW_REVISIONS);
//        pageLayout.addShowViewShortcut(MindMapUI.VIEW_INSPECTOR);
    }

//    private void createNavigatorsStack(IPageLayout pageLayout) {
//        IFolderLayout layout = pageLayout.createFolder(STACK_NAVIGATORS,
//                IPageLayout.RIGHT, 1f - getRatio(250, false),
//                pageLayout.getEditorArea());
//        layout.addView(OUTLINE_VIEW_ID);
//        layout.addView(MindMapUI.VIEW_OVERVIEW);
//        layout.addPlaceholder(MindMapUI.VIEW_REVISIONS);
//        layout.addPlaceholder(MindMapUI.VIEW_SPELLING);
//    }
//
//    private void createInspectorsStack(IPageLayout pageLayout) {
//        IFolderLayout layout = pageLayout.createFolder(STACK_INSPECTORS,
//                IPageLayout.RIGHT, 1f - getRatio(250, false),
//                pageLayout.getEditorArea());
//        layout.addPlaceholder(PROPERTIES_VIEW_ID);
//        layout.addView(MindMapUI.VIEW_MARKER);
//        layout.addPlaceholder(MindMapUI.VIEW_INSPECTOR);
//    }
//
//    private void createAssistantsStack(IPageLayout pageLayout) {
//        IPlaceholderFolderLayout layout = pageLayout.createPlaceholderFolder(
//                STACK_ASSISTANTS, IPageLayout.RIGHT, 1f - getRatio(250, false),
//                pageLayout.getEditorArea());
//        layout.addPlaceholder(MindMapUI.VIEW_NOTES);
//        layout.addPlaceholder(MindMapUI.VIEW_THEMES);
//        layout.addPlaceholder(MindMapUI.VIEW_COMMENTS);
//        layout.addPlaceholder(CONSOLE_VIEW_ID);
//    }
//
//    private void createLibrariesStack(IPageLayout pageLayout) {
//        IPlaceholderFolderLayout layout = pageLayout.createPlaceholderFolder(
//                STACK_LIBRARIES, IPageLayout.LEFT, getRatio(400, true),
//                pageLayout.getEditorArea());
//        layout.addPlaceholder(MindMapUI.VIEW_BROSWER);
//        layout.addPlaceholder(MindMapUI.VIEW_BLACKBOX);
//    }

}
