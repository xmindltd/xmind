package org.xmind.ui.evernote;

import static com.evernote.auth.EvernoteService.PRODUCTION;
import static com.evernote.auth.EvernoteService.YINXIANG;

import org.eclipse.core.runtime.Assert;
import org.xmind.ui.evernote.signin.IEvernoteAccount;

import com.evernote.auth.EvernoteAuth;
import com.evernote.auth.EvernoteService;
import com.evernote.clients.ClientFactory;
import com.evernote.clients.NoteStoreClient;
import com.evernote.clients.UserStoreClient;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.thrift.TException;
import com.evernote.thrift.transport.TTransportException;

/**
 * @author Jason Wong
 */
public class EvernoteStore {

    private ClientFactory clientFactory;

    private NoteStoreClient noteStore;

    private UserStoreClient userStore;

    private EvernoteService evernoteService;

    private IEvernoteAccount account;

    public EvernoteStore(IEvernoteAccount account) {
        this.account = account;
        Assert.isNotNull(account);

        this.clientFactory = new ClientFactory(new EvernoteAuth(
                getEvernoteService(), account.getAuthToken()));
    }

    public NoteStoreClient getNoteStore() throws EDAMUserException,
            EDAMSystemException, TException {
        if (noteStore == null) {
            noteStore = clientFactory.createNoteStoreClient();
        }
        return noteStore;
    }

    public UserStoreClient getUserStore() throws TTransportException {
        if (userStore == null) {
            userStore = clientFactory.createUserStoreClient();
        }
        return userStore;
    }

    public EvernoteService getEvernoteService() {
        if (evernoteService == null) {
            evernoteService = YINXIANG.name().equals(account.getServiceType()) ? YINXIANG
                    : PRODUCTION;
        }
        return evernoteService;
    }

}
