package org.xmind.core;

public interface IFileEntryFilter {

    /**
     * Returns <code>true</code> to indicate the path passes the filter.
     * 
     * @param path
     * @param mediaType
     * @param isDirectory
     * @return
     */
    boolean select(String path, String mediaType, boolean isDirectory);

}