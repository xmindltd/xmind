package org.xmind.gef.service;

public interface IViewerService2 {

    /**
     * Get essential data to preserve before this service is deactivated.
     * 
     * @return the data to preserve
     */
    Object preserveData();

    /**
     * Restore preserved data after this service is activated.
     * 
     * @param data
     *            the data to preserve
     */
    void restoreData(Object data);

}
