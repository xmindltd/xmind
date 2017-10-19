package org.xmind.core.internal.dom;

import org.w3c.dom.Element;
import org.xmind.core.Core;
import org.xmind.core.ISheet;
import org.xmind.core.ISheetSetting;
import org.xmind.core.event.ICoreEventSource;
import org.xmind.core.event.ICoreEventSupport;
import org.xmind.core.util.DOMUtils;

public class SheetSetting implements ISheetSetting {

    private Element sheetEle;

    private SheetImpl ownedSheet;

    public SheetSetting(Element sheetEle, SheetImpl ownedSheet) {
        this.sheetEle = sheetEle;
        this.ownedSheet = ownedSheet;
    }

    public boolean isInfoItemVisible(String type, String key, String defaultMode) {
        String mode = getInfoItemMode(type, DOMConstants.ATTR_MODE);

        if (mode == null)
            mode = defaultMode;

        return mode != null && mode.equals(DOMConstants.VAL_CARDMODE);
    }

    public void setInfoItemVisible(String type, String key, String defaultMode,
            boolean visible) {
        if (visible) {
            if (!isInfoItemVisible(type, key, defaultMode)) {
                setInfoItemMode(type, key, DOMConstants.VAL_CARDMODE);
                fireValueChange(ownedSheet, Core.Visibility, Boolean.FALSE,
                        Boolean.TRUE);
            }
        } else {
            if (isInfoItemVisible(type, key, defaultMode)) {
                setInfoItemMode(type, key, DOMConstants.VAL_ICONMODE);
                fireValueChange(ownedSheet, Core.Visibility, Boolean.TRUE,
                        Boolean.FALSE);
            }
        }
    }

    public String getInfoItemMode(String type, String key) {
        Element infoItemEle = getInfoItemEle(type);
        if (infoItemEle == null)
            return null;
        return DOMUtils.getAttribute(infoItemEle, DOMConstants.ATTR_MODE);
    }

    public void setInfoItemMode(String type, String key, String value) {
        Element infoItemEle = ensureInfoItemEle(type);
        DOMUtils.setAttribute(infoItemEle, key, value);
    }

    public Element getSheetEle() {
        return sheetEle;
    }

    public SheetImpl getOwnedSheet() {
        return ownedSheet;
    }

    private Element getInfoItemEle(String type) {
        if (sheetEle != null) {
            Element settingsEle = DOMUtils.getFirstChildElementByTag(sheetEle,
                    DOMConstants.TAG_SHEET_SETTINGS);
            if (settingsEle == null)
                return null;
            Element infoItemsEle = DOMUtils.getFirstChildElementByTag(
                    settingsEle, DOMConstants.TAG_INFO_ITEMS);
            if (infoItemsEle == null)
                return null;
            Element[] infoItemEles = DOMUtils.getChildElementsByTag(
                    infoItemsEle, DOMConstants.TAG_INFO_ITEM);
            if (infoItemEles == null || infoItemEles.length == 0)
                return null;

            for (Element infoItemEle : infoItemEles) {
                String eleType = DOMUtils.getAttribute(infoItemEle,
                        DOMConstants.ATTR_TYPE);
                if (type.equals(eleType))
                    return infoItemEle;
            }
        }
        return null;
    }

    private Element ensureInfoItemEle(String type) {
        Element sheetSettingsEle = DOMUtils.ensureChildElement(sheetEle,
                DOMConstants.TAG_SHEET_SETTINGS);
        Element infoItemsEle = DOMUtils.ensureChildElement(sheetSettingsEle,
                DOMConstants.TAG_INFO_ITEMS);
        Element infoItemEle = getInfoItemEle(type);
        if (infoItemEle == null) {
            infoItemEle = DOMUtils.createElement(infoItemsEle,
                    DOMConstants.TAG_INFO_ITEM);
            DOMUtils.setAttribute(infoItemEle, DOMConstants.ATTR_TYPE, type);
        }

        return infoItemEle;
    }

    private void fireValueChange(ISheet sheet, String visibility,
            Boolean oldValue, Boolean newValue) {
        if (sheet instanceof ICoreEventSource)
            getCoreEventSupport(sheet).dispatchValueChange(
                    (ICoreEventSource) sheet, visibility, oldValue, newValue);
    }

    private ICoreEventSupport getCoreEventSupport(ISheet sheet) {
        return (ICoreEventSupport) sheet.getAdapter(ICoreEventSupport.class);
    }

}
