package org.xmind.ui.commands;

import org.xmind.core.IWorkbook;
import org.xmind.gef.ISourceProvider;
import org.xmind.gef.command.ModifyCommand;

public class ModifyMetadataCommand extends ModifyCommand {

    private String keyPath;

    public ModifyMetadataCommand(ISourceProvider workbookProvider,
            String keyPath, String newValue) {
        super(workbookProvider, newValue);
        this.keyPath = keyPath;
    }

    public ModifyMetadataCommand(IWorkbook workbook, String keyPath,
            String newValue) {
        super(workbook, newValue);
        this.keyPath = keyPath;
    }

    @Override
    protected Object getValue(Object source) {
        return ((IWorkbook) source).getMeta().getValue(keyPath);
    }

    @Override
    protected void setValue(Object source, Object value) {
        ((IWorkbook) source).getMeta().setValue(keyPath, (String) value);
    }

}
