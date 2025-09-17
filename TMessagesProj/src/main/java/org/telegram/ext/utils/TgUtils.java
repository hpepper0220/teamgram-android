package org.telegram.ext.utils;

import org.telegram.ext.config.SkConfig;
import org.telegram.ext.respository.DataRepository;
import org.telegram.tgnet.ConnectionsManager;

public class TgUtils {

    public static void setGlobalInfo(int currentAccount) {
        DataRepository.getInstance().getIpAddress(result -> {
            SkConfig.ipAddress = result.getQuery();
            ConnectionsManager.getInstance(currentAccount).updateGlobal(SkConfig.merchantId, SkConfig.ipAddress);
        });
    }

}
