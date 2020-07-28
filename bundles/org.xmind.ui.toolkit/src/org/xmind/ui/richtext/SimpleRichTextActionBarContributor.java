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

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.TextStyle;
import org.xmind.ui.color.ColorPicker;
import org.xmind.ui.color.ColorSelection;
import org.xmind.ui.color.IColorSelection;
import org.xmind.ui.color.PaletteContents;
import org.xmind.ui.internal.ToolkitImages;
import org.xmind.ui.resources.ColorUtils;

public class SimpleRichTextActionBarContributor extends
        RichTextActionBarContributor {

    private IRichTextAction boldAction;

    private IRichTextAction italicAction;

    private IRichTextAction underlineAction;

    private IRichTextAction strikeoutAction;

    private IRichTextAction alignLeftAction;

    private IRichTextAction alignCenterAction;

    private IRichTextAction alignRightAction;

    private IRichTextAction indentAction;

    private IRichTextAction outdentAction;

    private AlignmentGroup alignGroup;

    private ColorPicker foregroundPicker;

    private ColorPicker backgroundPicker;

    protected void makeActions(IRichTextEditViewer viewer) {
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

        indentAction = new IndentAction(viewer);
        addRichTextAction(indentAction);

        outdentAction = new OutdentAction(viewer);
        addRichTextAction(outdentAction);

        alignGroup = new AlignmentGroup();
        alignGroup.add(alignLeftAction);
        alignGroup.add(alignCenterAction);
        alignGroup.add(alignRightAction);

        int colorChooserStyle = ColorPicker.AUTO;
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
        Color c = selection.isAutomatic() ? null : ColorUtils
                .getColor(selection.getColor());
        getViewer().getRenderer().setSelectionBackground(c);
    }

    private void foregroundChanged(SelectionChangedEvent event) {
        ISelection selection = event.getSelection();
        if (selection instanceof IColorSelection) {
            IColorSelection colorSelection = (IColorSelection) selection;
            Color c = colorSelection.isAutomatic() ? null : ColorUtils
                    .getColor(colorSelection.getColor());
            getViewer().getRenderer().setSelectionForeground(c);
        }
    }

    public void fillMenu(IMenuManager menu) {
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

    @Override
    public void fillContextMenu(IMenuManager menu) {
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
        menu.add(indentAction);
        menu.add(outdentAction);
    }

    public void fillToolBar(IToolBarManager toolbar) {
        toolbar.add(boldAction);
        toolbar.add(italicAction);
        toolbar.add(underlineAction);
        toolbar.add(strikeoutAction);
        toolbar.add(new Separator());
        toolbar.add(alignGroup);
        toolbar.add(new Separator());
        toolbar.add(indentAction);
        toolbar.add(outdentAction);
        toolbar.add(new Separator());
        toolbar.add(foregroundPicker);
        toolbar.add(backgroundPicker);
    }

    public void selectionChanged(ISelection selection, boolean enabled) {
        super.selectionChanged(selection, enabled);
        updateColorChoosers(enabled);
    }

    private void updateColorChoosers(boolean enabled) {
        foregroundPicker.getAction().setEnabled(enabled);
        backgroundPicker.getAction().setEnabled(enabled);

        IRichTextRenderer renderer = getViewer().getRenderer();
        if (renderer == null) {
            return;
        }
        TextStyle style = (renderer instanceof RichTextRenderer) ? ((RichTextRenderer) renderer)
                .getSelectionTextStyle() : null;
        int foregroundType = (style == null || style.foreground == null) ? ColorSelection.AUTO
                : ColorSelection.CUSTOM;
        foregroundPicker.setSelection(new ColorSelection(foregroundType,
                renderer.getSelectionForeground().getRGB()));
        
        int backgroundType = (style == null || style.background == null) ? ColorSelection.AUTO
                : ColorSelection.CUSTOM;
        backgroundPicker.setSelection(new ColorSelection(backgroundType,
                renderer.getSelectionBackground().getRGB()));
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
        super.dispose();
    }

}