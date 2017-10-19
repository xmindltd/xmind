package org.xmind.ui.internal.print.multipage;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.eclipse.jface.dialogs.IDialogSettings;

/**
 * Add set dirty state function for {@link IDialogSettings}.
 * 
 * @author Shawn
 *
 */
public class DialogSettingsDecorator implements IDialogSettings {

    private IDialogSettings settings;

    private boolean dirty;

    public DialogSettingsDecorator(IDialogSettings settings) {
        this.settings = settings;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public boolean isDirty() {
        return dirty;
    }

    public IDialogSettings addNewSection(String name) {
        return settings.addNewSection(name);
    }

    public void addSection(IDialogSettings section) {
        settings.addSection(section);
    }

    public String get(String key) {
        return settings.get(key);
    }

    public String[] getArray(String key) {
        return settings.getArray(key);
    }

    public boolean getBoolean(String key) {
        return settings.getBoolean(key);
    }

    public double getDouble(String key) throws NumberFormatException {
        return settings.getDouble(key);
    }

    public float getFloat(String key) throws NumberFormatException {
        return settings.getFloat(key);
    }

    public int getInt(String key) throws NumberFormatException {
        return settings.getInt(key);
    }

    public long getLong(String key) throws NumberFormatException {
        return settings.getLong(key);
    }

    public String getName() {
        return settings.getName();
    }

    public IDialogSettings getSection(String sectionName) {
        return settings.getSection(sectionName);
    }

    public IDialogSettings[] getSections() {
        return settings.getSections();
    }

    public void load(Reader reader) throws IOException {
        settings.load(reader);
    }

    public void load(String fileName) throws IOException {
        settings.load(fileName);
    }

    public void put(String key, String[] value) {
        settings.put(key, value);
        setDirty(true);
    }

    public void put(String key, double value) {
        settings.put(key, value);
        setDirty(true);
    }

    public void put(String key, float value) {
        settings.put(key, value);
        setDirty(true);
    }

    public void put(String key, int value) {
        settings.put(key, value);
        setDirty(true);
    }

    public void put(String key, long value) {
        settings.put(key, value);
        setDirty(true);
    }

    public void put(String key, String value) {
        settings.put(key, value);
        setDirty(true);
    }

    public void put(String key, boolean value) {
        settings.put(key, value);
        setDirty(true);
    }

    public void save(Writer writer) throws IOException {
        settings.save(writer);
    }

    public void save(String fileName) throws IOException {
        settings.save(fileName);
    }

}
