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
package org.xmind.ui.richtext;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.xmind.ui.color.ColorPicker;
import org.xmind.ui.color.ColorSelection;
import org.xmind.ui.color.IColorSelection;
import org.xmind.ui.color.PaletteContents;
import org.xmind.ui.dialogs.Messages;
import org.xmind.ui.internal.ToolkitImages;
import org.xmind.ui.resources.ColorUtils;
import org.xmind.ui.resources.FontUtils;
import org.xmind.ui.viewers.MComboViewer;

public class FullRichTextActionBarContributor
        extends RichTextActionBarContributor {

    private static Set<Integer> FONT_SIZE_LIST = new TreeSet<Integer>();

    private IRichTextAction fontAction;

    private MComboViewer fontViewer;

    private MComboViewer sizeViewer;

    private IRichTextAction boldAction;

    private IRichTextAction italicAction;

    private IRichTextAction underlineAction;

    private IRichTextAction strikeoutAction;

    private IRichTextAction alignLeftAction;

    private IRichTextAction alignCenterAction;

    private IRichTextAction alignRightAction;

//    private IRichTextAction bulletAction;

//    private IRichTextAction numberAction;

//    private BulletActionGroup bulletGroup;

    private IRichTextAction indentAction;

    private IRichTextAction outdentAction;

    private AlignmentGroup alignGroup;

    private ColorPicker foregroundPicker;

    private ColorPicker backgroundPicker;

    private boolean refreshing = false;

    protected void makeActions(IRichTextEditViewer viewer) {
        fontAction = new FontAction(viewer);
        addRichTextAction(fontAction);

        boldAction = new BoldAction(viewer);
        addRichTextAction(boldAction);

        italicAction = new ItalicAction(viewer);
        addRichTextAction(italicAction);

        underlineAction = new UnderlineAction(viewer);
        addRichTextAction(underlineAction);

        strikeoutAction = new StrikeoutAction(viewer);
        addRichTextAction(strikeoutAction);

        alignLeftAction = new AlignLeftAction(viewer);
        addRichTextAction(alignLeftAction);

        alignCenterAction = new AlignCenterAction(viewer);
        addRichTextAction(alignCenterAction);

        alignRightAction = new AlignRightAction(viewer);
        addRichTextAction(alignRightAction);

//        numberAction = new NumberAction(viewer);
//        addRichTextAction(numberAction);

//        bulletAction = new BulletAction(viewer);
//        addRichTextAction(bulletAction);

//        bulletGroup = new BulletActionGroup();
//        bulletGroup.add(numberAction);
//        bulletGroup.add(bulletAction);

        indentAction = new IndentAction(viewer);
        addRichTextAction(indentAction);

        outdentAction = new OutdentAction(viewer);
        addRichTextAction(outdentAction);

        alignGroup = new AlignmentGroup();
        alignGroup.add(alignLeftAction);
        alignGroup.add(alignCenterAction);
        alignGroup.add(alignRightAction);

        int colorChooserStyle = ColorPicker.AUTO | ColorPicker.CUSTOM;
        foregroundPicker = new ColorPicker(colorChooserStyle,
                PaletteContents.getDefault(),
                RichTextMessages.ForegroundAction_text,
                ToolkitImages.get(ToolkitImages.FOREGROUND));
        foregroundPicker
                .setAutoColor(RichTextUtils.DEFAULT_FOREGROUND.getRGB());
        foregroundPicker
                .addSelectionChangedListener(new ISelectionChangedListener() {
                    public void selectionChanged(SelectionChangedEvent event) {
                        foregroundChanged(event);
                    }
                });
        backgroundPicker = new ColorPicker(colorChooserStyle,
                PaletteContents.getDefault(),
                RichTextMessages.BackgroundAction_text,
                ToolkitImages.get(ToolkitImages.BACKGROUND));
        backgroundPicker
                .setAutoColor(RichTextUtils.DEFAULT_BACKGROUND.getRGB());
        backgroundPicker
                .addSelectionChangedListener(new ISelectionChangedListener() {
                    public void selectionChanged(SelectionChangedEvent event) {
                        backgroundChanged(event);
                    }
                });
    }

    private void backgroundChanged(SelectionChangedEvent event) {
        IColorSelection selection = (IColorSelection) event.getSelection();
        Color c = selection.isAutomatic() ? null
                : ColorUtils.getColor(selection.getColor());
        getViewer().getRenderer().setSelectionBackground(c);
    }

    private void foregroundChanged(SelectionChangedEvent event) {
        IColorSelection selection = (IColorSelection) event.getSelection();
        Color c = selection.isAutomatic() ? null
                : ColorUtils.getColor(selection.getColor());
        getViewer().getRenderer().setSelectionForeground(c);
    }

    public void fillMenu(IMenuManager menu) {
        menu.add(fontAction);
        menu.add(boldAction);
        menu.add(italicAction);
        menu.add(underlineAction);
        menu.add(strikeoutAction);
        menu.add(new Separator());
        menu.add(alignLeftAction);
        menu.add(alignCenterAction);
        menu.add(alignRightAction);
        menu.add(new Separator());
        menu.add(indentAction);
        menu.add(outdentAction);
    }

    public void fillContextMenu(IMenuManager menu) {
        menu.add(fontAction);
        MenuManager fontMenu = new MenuManager(
                RichTextMessages.ACTIONBAR_FONT_MENU_TEXT);
        fontMenu.add(boldAction);
        fontMenu.add(italicAction);
        fontMenu.add(underlineAction);
        fontMenu.add(strikeoutAction);
        menu.add(fontMenu);
        MenuManager alignMenu = new MenuManager(
                RichTextMessages.ACTIONBAR_ALIGN_MENU_TEXT);
        alignMenu.add(alignLeftAction);
        alignMenu.add(alignCenterAction);
        alignMenu.add(alignRightAction);
        menu.add(alignMenu);
        menu.add(new Separator());
//        menu.add(bulletAction);
//        menu.add(numberAction);
        menu.add(new Separator());
        menu.add(indentAction);
        menu.add(outdentAction);
    }

    public void fillToolBar(IToolBarManager toolbar) {
        addFontFamilySelector(toolbar);
        addFontSizeSelector(toolbar);

//        toolbar.add(fontAction);
        toolbar.add(boldAction);
        toolbar.add(italicAction);
        toolbar.add(underlineAction);
        toolbar.add(strikeoutAction);
        toolbar.add(new Separator());
        toolbar.add(alignGroup);
//        toolbar.add(new Separator());
//        toolbar.add(numberAction);
//        toolbar.add(bulletAction);
        toolbar.add(new Separator());
        toolbar.add(indentAction);
        toolbar.add(outdentAction);
        toolbar.add(new Separator());
        toolbar.add(foregroundPicker);
        toolbar.add(backgroundPicker);
    }

    private void addFontFamilySelector(IToolBarManager toolbar) {
        toolbar.add(new ContributionItem() {
            public void fill(ToolBar parent, int index) {
                ToolItem ti;
                if (index < 0)
                    ti = new ToolItem(parent, SWT.SEPARATOR);
                else
                    ti = new ToolItem(parent, SWT.SEPARATOR, index++);

                fontViewer = new MComboViewer(parent, MComboViewer.FILTERED);
                GridDataFactory.fillDefaults().grab(true, false)
                        .applyTo(fontViewer.getControl());
                fontViewer.getControl().setToolTipText(
                        Messages.FullRichTextAction_FontViewer_toolTip);
                fontViewer.setContentProvider(new ArrayContentProvider());
                fontViewer.setLabelProvider(new LabelProvider());
                fontViewer.setInput(FontUtils.getAvailableFontNames());
                fontViewer.setSelection(new StructuredSelection(
                        RichTextUtils.DEFAULT_FONT.getFontData()[0].getName()));
                fontViewer.addSelectionChangedListener(
                        new ISelectionChangedListener() {
                            public void selectionChanged(
                                    SelectionChangedEvent event) {
                                if (refreshing)
                                    return;

                                handleFontSelectionChanged(event);
                            }
                        });
                ti.setWidth(105);

                ti.setControl(fontViewer.getControl());

                update();
            }
        });

    }

    private void addFontSizeSelector(IToolBarManager toolbar) {
        toolbar.add(new ContributionItem() {
            @Override
            public void fill(ToolBar parent, int index) {
                ToolItem ti;
                if (index < 0)
                    ti = new ToolItem(parent, SWT.SEPARATOR);
                else
                    ti = new ToolItem(parent, SWT.SEPARATOR, index++);

                sizeViewer = new MComboViewer(parent, MComboViewer.FILTERED);
                GridDataFactory.fillDefaults().grab(true, false)
                        .applyTo(sizeViewer.getControl());
                sizeViewer.getControl().setToolTipText(
                        Messages.FullRichTextAction_FontSizeViewer_toolTip);
                sizeViewer.setContentProvider(new ArrayContentProvider());
                sizeViewer.setLabelProvider(new LabelProvider());
                sizeViewer.setPermitsUnprovidedElement(true);
                if (FONT_SIZE_LIST.isEmpty()) {
                    FONT_SIZE_LIST.addAll(Arrays.asList(8, 9, 10, 11, 12, 13,
                            14, 16, 18, 20, 22, 24, 36, 48, 56));
                }
                sizeViewer.setInput(FONT_SIZE_LIST);

                sizeViewer.addSelectionChangedListener(
                        new ISelectionChangedListener() {
                            public void selectionChanged(
                                    SelectionChangedEvent event) {
                                if (refreshing)
                                    return;

                                handleFontSelectionChanged(event);
                            }
                        });

                ti.setWidth(45);

                ti.setControl(sizeViewer.getControl());

                update();
            }
        });
    }

    public void selectionChanged(ISelection selection, boolean enabled) {
        super.selectionChanged(selection, enabled);
        updateColorChoosers(enabled);
        updateFontFamilyViewer(enabled);
        updateFontSizeViewer(enabled);
    }

    private void updateFontFamilyViewer(boolean enabled) {
        if (fontViewer == null || fontViewer.getControl().isDisposed())
            return;
        refreshing = true;
        IRichTextRenderer renderer = getViewer().getRenderer();
        fontViewer.setSelection(
                new StructuredSelection(renderer.getSelectionFontFace()));
        fontViewer.setEnabled(enabled);
        refreshing = false;
    }

    private void updateFontSizeViewer(boolean enabled) {
        if (sizeViewer == null || sizeViewer.getControl().isDisposed())
            return;

        refreshing = true;
        IRichTextRenderer renderer = getViewer().getRenderer();
        sizeViewer.setSelection(
                new StructuredSelection(renderer.getSelectionFontSize()));
        sizeViewer.setEnabled(enabled);
        refreshing = false;
    }

    private void updateColorChoosers(boolean enabled) {
        IRichTextRenderer renderer = getViewer().getRenderer();
        TextStyle style = (renderer instanceof RichTextRenderer)
                ? ((RichTextRenderer) renderer).getSelectionTextStyle() : null;
        int foregroundType = (style == null || style.foreground == null)
                ? ColorSelection.AUTO : ColorSelection.CUSTOM;
        foregroundPicker.setSelection(new ColorSelection(foregroundType,
                renderer.getSelectionForeground().getRGB()));
        foregroundPicker.getAction().setEnabled(enabled);
        int backgroundType = (style == null || style.background == null)
                ? ColorSelection.AUTO : ColorSelection.CUSTOM;
        backgroundPicker.setSelection(new ColorSelection(backgroundType,
                renderer.getSelectionBackground().getRGB()));
        backgroundPicker.getAction().setEnabled(enabled);
    }

    public void dispose() {
        if (foregroundPicker != null) {
            foregroundPicker.dispose();
        }
        if (backgroundPicker != null) {
            backgroundPicker.dispose();
        }
        if (alignGroup != null) {
            alignGroup.dispose();
        }
        if (fontViewer != null)
            fontViewer = null;
        if (sizeViewer != null)
            sizeViewer = null;
        super.dispose();
    }

    protected void handleFontSelectionChanged(SelectionChangedEvent event) {
        IRichTextEditViewer textViewer = getViewer();
        if (textViewer == null || textViewer.getControl().isDisposed())
            return;

        IRichTextRenderer renderer = textViewer.getRenderer();
        Font selectionFont = renderer.getSelectionFont();

        ResourceManager resources = new LocalResourceManager(
                JFaceResources.getResources());

        Object o = ((IStructuredSelection) event.getSelection())
                .getFirstElement();
        if (o instanceof String) {
            renderer.setSelectionFont((Font) resources
                    .get(FontDescriptor.createFrom(FontUtils.newName(
                            selectionFont.getFontData(), (String) o))));
        } else if (o instanceof Integer) {
            int size = (Integer) o;
            if (size > 0) {
                renderer.setSelectionFont((Font) resources
                        .get(FontDescriptor.createFrom(FontUtils.newHeight(
                                selectionFont.getFontData(), size))));
            }
        }
    }

}
