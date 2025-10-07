package org.telegram.ext.respository;
import android.util.Log;

import org.telegram.tgnet.ConnectionsManager;
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

    public void getApplyList(int currentAccount, int classGuid, SimpleCallback<TLRPC.TL_contacts_requestFriendContacts> callback) {
        TLRPC.TL_ssgrams_requestFriendList req = new TLRPC.TL_ssgrams_requestFriendList();
        req.offset = 1;
        req.limit = 10;
        int reqId = ConnectionsManager.getInstance(currentAccount).sendRequest(req, (response, error) -> {
            if (response instanceof TLRPC.TL_contacts_requestFriendContacts) {
                callback.onResp((TLRPC.TL_contacts_requestFriendContacts) response);
                Log.e("SkRepo", "TL_contacts_requestFriendContacts --------> ");
            }
        }, ConnectionsManager.RequestFlagFailOnServerErrors);
        ConnectionsManager.getInstance(currentAccount).bindRequestToGuid(reqId, classGuid);
    }

}
