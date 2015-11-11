package org.xmind.ui.commands;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xmind.core.ISheet;
import org.xmind.core.internal.dom.DOMConstants;
import org.xmind.gef.command.ModifyCommand;
import org.xmind.ui.internal.InfoItemContributorManager;
import org.xmind.ui.mindmap.IInfoItemContributor;

public class ModifyInfoItemVisibilityCommand extends ModifyCommand {

    private Map<String, String> defaultModeMap;

    private String type;

    public ModifyInfoItemVisibilityCommand(ISheet sheet, boolean visible,
            String type) {
        super(sheet, visible);
        this.type = type;
        init();
    }

    private void init() {
        if (defaultModeMap == null) {
            defaultModeMap = new HashMap<String, String>();
            List<IInfoItemContributor> contributors = InfoItemContributorManager
                    .getInstance().getBothContributors();
            for (IInfoItemContributor contributor : contributors)
                defaultModeMap.put(contributor.getId(),
                        contributor.getDefaultMode());
        }
    }

    @Override
    protected Object getValue(Object source) {
        if (source instanceof ISheet) {
            return Boolean.valueOf(((ISheet) source).getSetting()
                    .isInfoItemVisible(type, DOMConstants.ATTR_MODE,
                            defaultModeMap.get(type)));
        }
        return null;
    }

    @Override
    protected void setValue(Object source, Object value) {
        if (source instanceof ISheet && value instanceof Boolean) {
            setVisible((ISheet) source, (Boolean) value, type);
        }
    }

    private void setVisible(ISheet sheet, boolean visible, String type) {
        sheet.getSetting().setInfoItemVisible(type, DOMConstants.ATTR_MODE,
                defaultModeMap.get(type), visible);
    }

}
