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
package org.xmind.ui.internal.views;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.xmind.core.style.IStyle;
import org.xmind.gef.EditDomain;
import org.xmind.gef.GEF;
import org.xmind.gef.part.IPart;
import org.xmind.gef.tool.ITool;
import org.xmind.gef.util.Properties;
import org.xmind.ui.gallery.GalleryEditTool;
import org.xmind.ui.gallery.GalleryLayout;
import org.xmind.ui.gallery.GallerySelectTool;
import org.xmind.ui.gallery.GalleryViewer;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.texteditor.FloatingTextEditor;

public class ThemesViewer extends GalleryViewer {

    private static class ThemeNameEditTool extends GalleryEditTool {

        protected IDocument getTextContents(IPart source) {
            return new Document(((IStyle) source.getModel()).getName());
        }

        protected void handleTextModified(IPart source, IDocument document) {
            ((IStyle) source.getModel()).setName(document.get());
            MindMapUI.getResourceManager().saveUserThemeSheet();
        }

        protected void hookEditor(FloatingTextEditor editor) {
            super.hookEditor(editor);
            getHelper().setPrefWidth(130);
        }

    }

    private IStyle defaultTheme = null;

    private Image defaultImage = null;

    public ThemesViewer(Composite parent) {
        super();
        init();
        createControl(parent);

        final Display display = parent.getDisplay();
        getControl().setBackground(
                display.getSystemColor(SWT.COLOR_LIST_BACKGROUND));

        getControl().addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                if (defaultImage != null) {
                    defaultImage.dispose();
                    defaultImage = null;
                }
            }
        });
    }

    protected void init() {
//        setPartFactory(new ThemePartFactory(getPartFactory()));
        setLabelProvider(new ThemeLabelProvider());

        EditDomain editDomain = new EditDomain();
        editDomain.installTool(GEF.TOOL_SELECT, new GallerySelectTool());
        editDomain.installTool(GEF.TOOL_EDIT, new ThemeNameEditTool());
        setEditDomain(editDomain);

        Properties properties = getProperties();
        properties.set(GalleryViewer.Horizontal, Boolean.TRUE);
        properties.set(GalleryViewer.Wrap, Boolean.TRUE);
        properties.set(GalleryViewer.Layout,
                new GalleryLayout(GalleryLayout.ALIGN_CENTER,
                        GalleryLayout.ALIGN_FILL, 1, 1, new Insets(5)));
        properties.set(GalleryViewer.FrameContentSize, new Dimension(128, 64));
        properties.set(GalleryViewer.TitlePlacement,
                GalleryViewer.TITLE_BOTTOM);
        properties.set(GalleryViewer.SingleClickToOpen, Boolean.FALSE);
        properties.set(GalleryViewer.SolidFrames, true);
        properties.set(GalleryViewer.FlatFrames, true);
        properties.set(GalleryViewer.ImageConstrained, true);
//        properties.set(GalleryViewer.ImageStretched, true);

        properties.set(GalleryViewer.CustomContentPaneDecorator, true);
    }

    protected boolean isTitleEditable(IPart p) {
        Object model = p.getModel();
        if (!(model instanceof IStyle))
            return false;

        IStyle theme = (IStyle) model;
        return theme.getOwnedStyleSheet() == MindMapUI.getResourceManager()
                .getUserThemeSheet();
    }

    public void setSelection(ISelection selection) {
        super.setSelection(selection, true);
    }

    public IStyle getDefaultTheme() {
        return defaultTheme;
    }

    public void setDefaultTheme(IStyle defaultTheme) {
        IStyle oldTheme = this.defaultTheme;
        this.defaultTheme = defaultTheme;

        updateThemePart(oldTheme);
        updateThemePart(defaultTheme);
    }

    private void updateThemePart(IStyle theme) {
        update(new Object[] { theme });
    }

    public void startEditing(IStyle theme) {
        EditDomain domain = getEditDomain();
        ITool tool = domain.getDefaultTool();
        ((GallerySelectTool) tool).getStatus().setStatus(GEF.ST_ACTIVE, true);
        domain.handleRequest(GEF.REQ_EDIT, this);
    }

}