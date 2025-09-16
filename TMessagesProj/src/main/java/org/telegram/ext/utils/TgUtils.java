package org.telegram.ext.utils;

import org.telegram.ext.respository.DataRepository;
import org.telegram.tgnet.ConnectionsManager;

public class TgUtils {

    public static void setGlobalInfo(int currentAccount) {
        DataRepository.getInstance().getIpAddress(result -> ConnectionsManager.getInstance(currentAccount).updateGlobal("Soooo", result.getQuery()));
    }

}
