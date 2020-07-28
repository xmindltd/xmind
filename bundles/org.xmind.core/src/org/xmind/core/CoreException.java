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
package org.xmind.core;

/**
 *
 * @see Core
 * @author Frank Shaka
 */
public class CoreException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = -5654915714687349526L;

    private int type;

    private String localizedMessage;

    private String codeInfo;

    /**
     * 
     * @param type
     */
    public CoreException(int type) {
        super(getErrorMessage(type, null));
        this.type = type;
    }

    /**
     * 
     * @param type
     * @param codeInfo
     */
    public CoreException(int type, String codeInfo) {
        super(getErrorMessage(type, codeInfo));
        this.type = type;
        this.codeInfo = codeInfo;
    }

    /**
     * 
     * @param type
     * @param cause
     */
    public CoreException(int type, Throwable cause) {
        super(getErrorMessage(type, null), cause);
        this.type = type;
    }

    /**
     * 
     * @param type
     * @param codeInfo
     * @param cause
     */
    public CoreException(int type, String codeInfo, Throwable cause) {
        super(getErrorMessage(type, codeInfo), cause);
        this.type = type;
        this.codeInfo = codeInfo;
    }

    public int getType() {
        return type;
    }

    public void setLocalizedMessage(String localizedMessage) {
        this.localizedMessage = localizedMessage;
    }

    public boolean isMessageLocalized() {
        return localizedMessage != null;
    }

    void setCodeInfo(String info) {
        this.codeInfo = info;
    }

    public String getCodeInfo() {
        return codeInfo;
    }

    public String getLocalizedMessage() {
        if (isMessageLocalized())
            return localizedMessage;
        return super.getLocalizedMessage();
    }

    public String getMessage() {
        if (isMessageLocalized())
            return localizedMessage;
        return super.getMessage();
    }

    private static String getErrorMessage(int type, String codeInfo) {
        String err;
        if (type == Core.ERROR_NULL_ARGUMENT) {
            err = "Null argument"; //$NON-NLS-1$
        } else if (type == Core.ERROR_INVALID_ARGUMENT) {
            err = "Invalid argument"; //$NON-NLS-1$
        } else if (type == Core.ERROR_INVALID_FILE) {
            err = "Invalid file"; //$NON-NLS-1$
        } else if (type == Core.ERROR_NO_SUCH_ENTRY) {
            err = "No such entry"; //$NON-NLS-1$
        } else if (type == Core.ERROR_FAIL_ACCESS_XML_PARSER) {
            err = "Failed to access XML parser"; //$NON-NLS-1$
        } else if (type == Core.ERROR_FAIL_PARSING_XML) {
            err = "Failed to parse XML"; //$NON-NLS-1$
        } else if (type == Core.ERROR_NO_WORKBOOK_CONTENT) {
            err = "No workbook content"; //$NON-NLS-1$
        } else if (type == Core.ERROR_FAIL_ACCESS_XML_TRANSFORMER) {
            err = "Failed to access XML transformer"; //$NON-NLS-1$
        } else if (type == Core.ERROR_FAIL_INIT_CRYPTOGRAM) {
            err = "Failed to initialize cryptogram engine"; //$NON-NLS-1$
        } else if (type == Core.ERROR_WRONG_PASSWORD) {
            err = "Wrong password"; //$NON-NLS-1$
        } else if (type == Core.ERROR_FAIL_SERIALIZING_XML) {
            err = "Failed to serialize XML"; //$NON-NLS-1$
        } else if (type == Core.ERROR_CANCELLATION) {
            err = "Operation canceled"; //$NON-NLS-1$
        } else {
            err = "Unexpected error"; //$NON-NLS-1$
        }
        if (codeInfo != null)
            return String.format("%s: %s", err, codeInfo); //$NON-NLS-1$
        return err;
    }
}