package org.telegram.ext.utils;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
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

    /**
     * 判断触点是否落在某个 View 区域
     */
    public static boolean isTouchInsideView(MotionEvent e, View v) {
        if (v == null) return false;
        int[] loc = new int[2];
        v.getLocationOnScreen(loc);
        float x = e.getRawX();
        float y = e.getRawY();
        return x >= loc[0] && x <= loc[0] + v.getWidth()
                && y >= loc[1] && y <= loc[1] + v.getHeight();
    }

}
