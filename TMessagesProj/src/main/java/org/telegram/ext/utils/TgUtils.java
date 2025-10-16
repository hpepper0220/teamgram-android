package org.telegram.ext.utils;

import android.content.Context;
import android.util.Log;
import android.webkit.WebSettings;

import org.telegram.ext.config.SkConfig;
import org.telegram.ext.respository.DataRepository;
import org.telegram.tgnet.ConnectionsManager;

public class TgUtils {

    public static void setGlobalInfo(Context context, int currentAccount) {
        String userAgent = WebSettings.getDefaultUserAgent(context);
        DataRepository.getInstance().getIpAddress(result -> {
            SkConfig.ipAddress = result.getQuery();
            ConnectionsManager.getInstance(currentAccount).updateGlobal(SkConfig.merchantId, SkConfig.ipAddress, userAgent);
        });
    }

}
