/* ******************************************************************************
 * Copyright (c) 2006-2016 XMind Ltd. and others.
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
/**
 * 
 */
package org.xmind.ui.internal.editor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.xmind.core.Core;
import org.xmind.core.CoreException;
import org.xmind.core.IEntryStreamNormalizer;
import org.xmind.core.IFileEntry;
import org.xmind.core.IWorkbook;
import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.ICoreEventSource;
import org.xmind.core.internal.security.PasswordProtectedNormalizer;

/**
 * @author Frank Shaka
 * @since 3.6.50
 */
public class WorkbookRefEncryptable implements IEncryptable {

    private static class LazyPasswordBasedEncryptor
            implements IEntryStreamNormalizer {

        private final AbstractWorkbookRef workbookRef;

        private IEntryStreamNormalizer delegate;

        /**
         * 
         */
        public LazyPasswordBasedEncryptor(AbstractWorkbookRef workbookRef) {
            this.workbookRef = workbookRef;
            this.delegate = IEntryStreamNormalizer.NULL;
        }

        /**
         * @return the delegate
         */
        public IEntryStreamNormalizer getDelegate() {
            return delegate;
        }

        /*
         * (non-Javadoc)
         * @see
         * org.xmind.core.IEntryStreamNormalizer#normalizeOutputStream(java.io.
         * OutputStream, org.xmind.core.IFileEntry)
         */
        @Override
        public OutputStream normalizeOutputStream(OutputStream stream,
                IFileEntry fileEntry) throws IOException, CoreException {
            return delegate.normalizeOutputStream(stream, fileEntry);
        }

        /*
         * (non-Javadoc)
         * @see
         * org.xmind.core.IEntryStreamNormalizer#normalizeInputStream(java.io.
         * InputStream, org.xmind.core.IFileEntry)
         */
        @Override
        public InputStream normalizeInputStream(InputStream stream,
                IFileEntry fileEntry) throws IOException, CoreException {
            if (fileEntry.getEncryptionData() != null
                    && delegate == IEntryStreamNormalizer.NULL) {
                /// encrypted, should ask for password
                IPasswordProvider passwordProvider = workbookRef
                        .getService(IPasswordProvider.class);
                if (passwordProvider != null) {
                    String password = passwordProvider
                            .askForPassword(workbookRef, null);
                    if (password == null)
                        throw new CoreException(Core.ERROR_CANCELLATION);
                    delegate = createEncryptor(password);
                }
            }

            return delegate.normalizeInputStream(stream, fileEntry);
        }

        /*
         * (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (obj == this)
                return true;
            if (obj == null || !(obj instanceof LazyPasswordBasedEncryptor))
                return false;
            LazyPasswordBasedEncryptor that = (LazyPasswordBasedEncryptor) obj;
            return this.delegate.equals(that.delegate);
        }

        /*
         * (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            return 37 ^ delegate.hashCode();
        }

    }

    private final AbstractWorkbookRef workbookRef;

    private IEntryStreamNormalizer encryptor;

    private String password;

    private String passwordHint;

    /**
     * 
     */
    public WorkbookRefEncryptable(AbstractWorkbookRef workbookRef) {
        super();
        this.workbookRef = workbookRef;
        this.encryptor = new LazyPasswordBasedEncryptor(workbookRef);
    }

    /*
     * (non-Javadoc)
     * @see
     * org.xmind.ui.internal.editor.IEncryptable#setPassword(java.lang.String)
     */
    @Override
    public void setPassword(String newPassword) {
        IEntryStreamNormalizer oldEncryptor = this.encryptor;
        IEntryStreamNormalizer newEncryptor = createEncryptor(newPassword);
        if (encryptorEquals(oldEncryptor, newEncryptor))
            return;

        this.encryptor = newEncryptor;

        IWorkbook workbook = workbookRef.getWorkbook();
        if (workbook != null && workbook instanceof ICoreEventSource) {
            ICoreEventSource eventSource = (ICoreEventSource) workbook;
            eventSource.getCoreEventSupport().dispatch(eventSource,
                    new CoreEvent(eventSource, Core.PasswordChange, null));
        }

        this.password = newPassword;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public void setPasswordHint(String passwordHint) {
        this.passwordHint = passwordHint;
    }

    public String getPasswordHint() {
        return passwordHint;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.xmind.ui.internal.editor.IEncryptable#isPasswordCorrect(java.lang.
     * String)
     */
    @Override
    public boolean testsPassword(String passwordToTest) {
        IEntryStreamNormalizer oldEncryptor = this.encryptor;
        IEntryStreamNormalizer newEncryptor = createEncryptor(passwordToTest);
        return encryptorEquals(oldEncryptor, newEncryptor);
    }

    private static boolean encryptorEquals(IEntryStreamNormalizer oldEncryptor,
            IEntryStreamNormalizer newEncryptor) {
        return oldEncryptor.equals(newEncryptor);
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.ui.internal.editor.IEncryptable#hasPassword()
     */
    @Override
    public boolean hasPassword() {
        IEntryStreamNormalizer e = this.encryptor;
        while (e instanceof LazyPasswordBasedEncryptor) {
            e = ((LazyPasswordBasedEncryptor) e).getDelegate();
        }
        return e != IEntryStreamNormalizer.NULL;
    }

    /**
     * @return the encryptor
     */
    public IEntryStreamNormalizer getEncryptor() {
        return encryptor;
    }

    protected void setEncryptor(IEntryStreamNormalizer encryptor) {
        this.encryptor = encryptor;
    }

    public void reset() {
        this.encryptor = new LazyPasswordBasedEncryptor(workbookRef);
    }

    /**
     * @param password
     * @return
     */
    private static IEntryStreamNormalizer createEncryptor(String password) {
        return password == null ? IEntryStreamNormalizer.NULL
                : new PasswordProtectedNormalizer(password);
    }

}
