package org.telegram.ext.respository;
import android.util.Log;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.tgnet.TLRPC.TL_ssgrams_getDiscoverPage;

import java.util.List;

public class SkRepository {

    public SkRepository() {}

    private static SkRepository sInstance;

    public static synchronized SkRepository getInstance() {
        if (sInstance == null) {
            synchronized (SkRepository.class) {
                if (sInstance == null) {
                    sInstance = new SkRepository();
                }
                return sInstance;
            }
        }
        return sInstance;
    }

    public void getExplore(int currentAccount) {
        TLRPC.TL_ssgrams_getExplorePage req = new TLRPC.TL_ssgrams_getExplorePage();
        ConnectionsManager.getInstance(currentAccount).sendRequest(req, (response, error) -> {
            if (response instanceof TLRPC.TL_discoverList) {

            }
        }, ConnectionsManager.RequestFlagFailOnServerErrors);
    }

    public void getDiscovery(int currentAccount, int classGuid, SimpleCallback<List<TLRPC.TL_discoverPage>> callback) {
        TL_ssgrams_getDiscoverPage req = new TL_ssgrams_getDiscoverPage();
        int reqId = ConnectionsManager.getInstance(currentAccount).sendRequest(req, (response, error) -> {
            if (response instanceof TLRPC.TL_discoverList) {
                List<TLRPC.TL_discoverPage> dataList = ((TLRPC.TL_discoverList) response).list;
                callback.onResp(dataList);
            }
        }, ConnectionsManager.RequestFlagFailOnServerErrors);
        ConnectionsManager.getInstance(currentAccount).bindRequestToGuid(reqId, classGuid);
    }

    public void getApplyList(int currentAccount, int classGuid, int page, SimpleCallback<TLRPC.TL_contacts_requestFriendContacts> callback) {
        TLRPC.TL_ssgrams_requestFriendList req = new TLRPC.TL_ssgrams_requestFriendList();
        req.offset = page;
        req.limit = 100;
        int reqId = ConnectionsManager.getInstance(currentAccount).sendRequest(req, (response, error) -> {
            AndroidUtilities.runOnUIThread(() -> {
                if (response instanceof TLRPC.TL_contacts_requestFriendContacts) {
                    callback.onResp((TLRPC.TL_contacts_requestFriendContacts) response);
                    Log.e("SkRepo", "TL_contacts_requestFriendContacts --------> ");
                }
            });
        }, ConnectionsManager.RequestFlagFailOnServerErrors);
        ConnectionsManager.getInstance(currentAccount).bindRequestToGuid(reqId, classGuid);
    }

    public void getApplyCount(int currentAccount, int classGuid, SimpleCallback<Integer> callback) {
        TLRPC.TL_ssgrams_getcontactRequestCount req = new TLRPC.TL_ssgrams_getcontactRequestCount();
        int reqId = ConnectionsManager.getInstance(currentAccount).sendRequest(req, (response, error) -> {
            AndroidUtilities.runOnUIThread(() -> {
                if (response instanceof TLRPC.TL_contactRequestUnreadCount) {
                    callback.onResp(((TLRPC.TL_contactRequestUnreadCount) response).count);
                }
            });
        }, ConnectionsManager.RequestFlagFailOnServerErrors);
        ConnectionsManager.getInstance(currentAccount).bindRequestToGuid(reqId, classGuid);
    }

    public void getChannels(int currentAccount, int classGuid) {
        TLRPC.TL_channels_getInactiveChannels req = new TLRPC.TL_channels_getInactiveChannels();
        ConnectionsManager.getInstance(currentAccount).sendRequest(req, new RequestDelegate() {
            @Override
            public void run(TLObject response, TLRPC.TL_error error) {

            }
        });
    }

}
