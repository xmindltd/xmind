package org.xmind.ui.internal.properties;

import static org.xmind.core.ISheetSettings.INFO_ITEM;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.xmind.core.Core;
import org.xmind.core.ISettingEntry;
import org.xmind.core.ISheet;
import org.xmind.core.ISheetSettings;
import org.xmind.core.event.ICoreEventRegister;
import org.xmind.core.internal.dom.DOMConstants;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.ui.color.ColorPicker;
import org.xmind.ui.color.IColorSelection;
import org.xmind.ui.color.PaletteContents;
import org.xmind.ui.commands.CommandMessages;
import org.xmind.ui.commands.ModifyInfoItemVisibilityCommand;
import org.xmind.ui.internal.InfoItemContributorManager;
import org.xmind.ui.mindmap.IInfoItemContributor;
import org.xmind.ui.properties.StyledPropertySectionPart;
import org.xmind.ui.style.Styles;

/**
 * @author Jason Wong
 */
public class SheetInfoCardPropertySectionPart
        extends StyledPropertySectionPart {

    private class BackgroundColorOpenListener implements IOpenListener {

        public void open(OpenEvent event) {
            changeBackgroundColor((IColorSelection) event.getSelection());
        }

    }

    private List<String> types;

    private Map<String, Button> checkMap;

    private Map<String, String> defaultModes;

    private Control bar;

    private ColorPicker bgColorPicker;

    @Override
    protected void createContent(Composite parent) {
        List<IInfoItemContributor> cs = InfoItemContributorManager.getInstance()
                .getBothContributors();
        for (IInfoItemContributor c : cs) {
            Button check = createCheck(parent, c);
            if (types == null)
                types = new ArrayList<String>();
            types.add(c.getId());

            if (checkMap == null)
                checkMap = new HashMap<String, Button>();
            checkMap.put(c.getId(), check);

            if (defaultModes == null)
                defaultModes = new HashMap<String, String>();
            defaultModes.put(c.getId(), c.getDefaultMode());
        }

        createBackgroundPart(parent);
    }

    private Button createCheck(Composite parent,
            final IInfoItemContributor contributor) {
        String cardLabel = contributor.getCardLabel();
        final Button check = new Button(parent, SWT.CHECK);
        check.setText(cardLabel);
        check.setSelection(
                DOMConstants.VAL_CARDMODE.equals(contributor.getDefaultMode()));
        check.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                infoItemVisibility(contributor.getId(), check.getSelection());
            }
        });
        return check;
    }

    private void createBackgroundPart(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(22, false);
        layout.horizontalSpacing = 7;
        composite.setLayout(layout);

        Label caption = new Label(composite, SWT.NONE);
        caption.setText(PropertyMessages.BackgroundColor_label);
        caption.setLayoutData(
                new GridData(GridData.FILL, GridData.CENTER, false, false));

        bgColorPicker = new ColorPicker(ColorPicker.AUTO | ColorPicker.CUSTOM,
                PaletteContents.getDefault());
        bgColorPicker.getAction()
                .setToolTipText(PropertyMessages.InfoCardBackground_toolTip);
        bgColorPicker.addOpenListener(new BackgroundColorOpenListener());
        ToolBarManager colorBar = new ToolBarManager(SWT.FLAT);
        colorBar.add(bgColorPicker);
        bar = colorBar.createControl(composite);
        bar.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER,
                false, false));
    }

    private void infoItemVisibility(String type, boolean visible) {
        IGraphicalEditor editor = getContributedEditor();
        if (editor == null)
            return;

        ISheet sheet = (ISheet) editor.getActivePageInstance()
                .getAdapter(ISheet.class);
        ModifyInfoItemVisibilityCommand command = new ModifyInfoItemVisibilityCommand(
                sheet, visible, type);

        command.setLabel(CommandMessages.Command_ShowOrHideInfoItem);
        editor.getCommandStack().execute(command);
    }

    private void changeBackgroundColor(IColorSelection selection) {
        changeColor(selection, Styles.YellowBoxFillColor,
                CommandMessages.Command_ModifyYellowBoxBackgroundColor);
    }

    protected void doRefresh() {
        for (String type : types)
            checkMap.get(type).setSelection(isVisible(type));
        updateColorPicker(bgColorPicker, Styles.YellowBoxFillColor, null);
    }

    private boolean isVisible(String type) {
        for (Object o : getSelectedElements()) {
            if (o instanceof ISheet) {
                ISettingEntry entry = findEntry((ISheet) o, type);
                if (entry == null)
                    break;
                return ISheetSettings.MODE_CARD
                        .equals(entry.getAttribute(ISheetSettings.ATTR_MODE));
            }
        }
        return ISheetSettings.MODE_CARD.equals(defaultModes.get(type));
    }

    private ISettingEntry findEntry(ISheet sheet, String type) {
        List<ISettingEntry> entries = sheet.getSettings().getEntries(INFO_ITEM);
        for (ISettingEntry entry : entries) {
            String t = entry.getAttribute(ISheetSettings.ATTR_TYPE);
            if (type.equals(t))
                return entry;
        }
        return null;
    }

    @Override
    protected void registerEventListener(Object source,
            ICoreEventRegister register) {
        super.registerEventListener(source, register);
        if (source instanceof ISheet)
            register.register(Core.SheetSettings);
    }

    @Override
    public void dispose() {
        super.dispose();
        types = null;
        checkMap = null;
        defaultModes = null;
        bar = null;
    }

    @Override
    public void setFocus() {
        if (types != null && !types.isEmpty() && checkMap != null
                && !checkMap.isEmpty()) {
            checkMap.get(types.get(0));
        }
    }
}
