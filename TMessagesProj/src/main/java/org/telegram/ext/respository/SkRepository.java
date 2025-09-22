package org.telegram.ext.respository;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC.TL_ssgrams_getDiscoverPage;

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

    public void getDiscovery(int currentAccount, int classGuid) {
        TL_ssgrams_getDiscoverPage req = new TL_ssgrams_getDiscoverPage();
        int reqId = ConnectionsManager.getInstance(currentAccount).sendRequest(req, (response, error) -> {

        }, ConnectionsManager.RequestFlagFailOnServerErrors);
        ConnectionsManager.getInstance(currentAccount).bindRequestToGuid(reqId, classGuid);
    }

}
