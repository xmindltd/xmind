package org.xmind.ui.internal.properties;

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
import org.xmind.core.ISheet;
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

public class SheetInfoCardPropertySectionPart extends StyledPropertySectionPart {

    private List<String> typeList;

    private Map<String, Button> checkMap;

    private Map<String, String> defaultModeMap;

    private Control bar;

    private ColorPicker backgroundColorPicker;

    private class BackgroundColorOpenListener implements IOpenListener {

        public void open(OpenEvent event) {
            changeBackgroundColor((IColorSelection) event.getSelection());
        }

    }

    @Override
    protected void createContent(Composite parent) {
        List<IInfoItemContributor> contributors = InfoItemContributorManager
                .getInstance().getBothContributors();

        for (final IInfoItemContributor contributor : contributors) {
            final Button check = createCheck(parent, contributor);
            if (typeList == null)
                typeList = new ArrayList<String>();
            typeList.add(contributor.getId());

            if (checkMap == null)
                checkMap = new HashMap<String, Button>();
            checkMap.put(contributor.getId(), check);

            if (defaultModeMap == null)
                defaultModeMap = new HashMap<String, String>();
            defaultModeMap.put(contributor.getId(),
                    contributor.getDefaultMode());
        }

        createBackgroundPart(parent);
    }

    private Button createCheck(Composite parent,
            final IInfoItemContributor contributor) {
        String cardLabel = contributor.getCardLabel();
        final Button check = new Button(parent, SWT.CHECK);
        check.setText(cardLabel);
        check.setSelection(DOMConstants.VAL_CARDMODE.equals(contributor
                .getDefaultMode()));
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
        caption.setLayoutData(new GridData(GridData.FILL, GridData.CENTER,
                false, false));

        backgroundColorPicker = new ColorPicker(ColorPicker.AUTO
                | ColorPicker.CUSTOM, PaletteContents.getDefault());
        backgroundColorPicker.getAction().setToolTipText(
                PropertyMessages.InfoCardBackground_toolTip);
        backgroundColorPicker
                .addOpenListener(new BackgroundColorOpenListener());
        ToolBarManager colorBar = new ToolBarManager(SWT.FLAT);
        colorBar.add(backgroundColorPicker);
        bar = colorBar.createControl(composite);
        bar.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER,
                false, false));
    }

    private void infoItemVisibility(String type, boolean visible) {
        IGraphicalEditor editor = getContributedEditor();
        if (editor == null)
            return;

        ISheet sheet = (ISheet) editor.getActivePageInstance().getAdapter(
                ISheet.class);
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
        for (String type : typeList) {
            Button check = checkMap.get(type);
            check.setSelection(isinfoItemVisible(type));
        }
        updateColorPicker(backgroundColorPicker, Styles.YellowBoxFillColor,
                null);
    }

    private boolean isinfoItemVisible(String type) {
        for (Object o : getSelectedElements()) {
            if (o instanceof ISheet) {
                String mode = getItemMode((ISheet) o, type);
                return mode != null && mode.equals(DOMConstants.VAL_CARDMODE);
            }
        }
        return false;
    }

    private String getItemMode(ISheet sheet, String type) {
        String mode = sheet.getSetting().getInfoItemMode(type,
                DOMConstants.ATTR_MODE);

        if (mode == null)
            mode = defaultModeMap.get(type);

        return mode;
    }

    @Override
    protected void registerEventListener(Object source,
            ICoreEventRegister register) {
        super.registerEventListener(source, register);
        if (source instanceof ISheet) {
            register.register(Core.Visibility);
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        typeList = null;
        checkMap = null;
        defaultModeMap = null;
        bar = null;
    }

    @Override
    public void setFocus() {
        if (typeList != null && !typeList.isEmpty() && checkMap != null
                && !checkMap.isEmpty()) {
            checkMap.get(typeList.get(0));
        }
    }
}
