package org.telegram.ext.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Build;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;

import org.telegram.ext.config.SkConfig;
import org.telegram.ext.respository.DataRepository;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.ImageLoader;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.WallpapersListActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class TgUtils {

    public static void setGlobalInfo(Context context, int currentAccount) {
//        String userAgent = WebSettings.getDefaultUserAgent(context);
        String brand = Build.BRAND;
        String model = Build.MODEL;
        String userAgent = brand + "-" + model;
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

    public static void setDefBackground(Context context, int currentAccount, int themeColor) {
        FileOutputStream stream = null;
        try {
            int dialogId = 0;

            File backgroundFile = drawableToFile(context, R.drawable.background_hd, "skg_background.png");
            Log.e("setDefBackground", "background file path ------> " + backgroundFile.getAbsolutePath());
            SendMessagesHelper.SendingMediaInfo info = new SendMessagesHelper.SendingMediaInfo();
            info.path = backgroundFile.getAbsolutePath();

            File currentWallpaperPath = new File(FileLoader.getDirectory(FileLoader.MEDIA_DIR_CACHE), Utilities.random.nextInt() + ".jpg");
            Point screenSize = AndroidUtilities.getRealScreenSize();
            Bitmap bitmap = ImageLoader.loadBitmap(info.path, null, screenSize.x, screenSize.y, true);
            stream = new FileOutputStream(currentWallpaperPath);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 87, stream);

            WallpapersListActivity.FileWallpaper wallpaper = new WallpapersListActivity.FileWallpaper("", currentWallpaperPath, currentWallpaperPath);

            boolean done;
            boolean sameFile = false;
            boolean isBlurred = false;
            Theme.ThemeInfo theme = Theme.getActiveTheme();
            String originalFileName = theme.generateWallpaperName(null, isBlurred);
            String fileName = isBlurred ? theme.generateWallpaperName(null, false) : originalFileName;
            File toFile = new File(ApplicationLoader.getFilesDirFixed(), originalFileName);

            File fromFile = wallpaper.originalPath != null ? wallpaper.originalPath : wallpaper.path;
            done = AndroidUtilities.copyFile(fromFile, toFile);

            String slug;
            int rotation = 45;
            int color = 0;
            int gradientColor1 = 0;
            int gradientColor2 = 0;
            int gradientColor3 = 0;
            File path = null;

            slug = wallpaper.slug;
            path = wallpaper.path;

            Theme.OverrideWallpaperInfo wallpaperInfo = new Theme.OverrideWallpaperInfo();
            wallpaperInfo.fileName = fileName;
            wallpaperInfo.originalFileName = originalFileName;
            wallpaperInfo.slug = slug;
            wallpaperInfo.isBlurred = isBlurred;
            wallpaperInfo.isMotion = false;
            wallpaperInfo.color = color;
            wallpaperInfo.gradientColor1 = gradientColor1;
            wallpaperInfo.gradientColor2 = gradientColor2;
            wallpaperInfo.gradientColor3 = gradientColor3;
            wallpaperInfo.rotation = rotation;
            wallpaperInfo.intensity = 0.5f;

            wallpaperInfo.forBoth = false;
            MessagesController.getInstance(currentAccount).saveWallpaperToServer(path, wallpaperInfo, slug != null && dialogId == 0, 0);

//            Theme.serviceMessageColorBackup = getThemedColor(Theme.key_chat_serviceBackground);
            Theme.serviceMessageColorBackup = themeColor;
            if (Theme.THEME_BACKGROUND_SLUG.equals(wallpaperInfo.slug)) {
                wallpaperInfo = null;
            }
            Theme.getActiveTheme().setOverrideWallpaper(wallpaperInfo);
            Theme.reloadWallpaper(true);
            if (!sameFile) {
                ImageLoader.getInstance().removeImage(ImageLoader.getHttpFileName(toFile.getAbsolutePath()) + "@100_100");
            }

            Log.e("TgUtils", "set background success");
        } catch (IOException e) {
            Log.e("TgUtils", "set background error -------> " + e.getMessage());
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static File drawableToFile(Context context, int resId, String fileName) throws IOException {
        InputStream inputStream = context.getResources().openRawResource(resId);
        File outFile = new File(context.getCacheDir(), fileName);
        FileOutputStream outputStream = new FileOutputStream(outFile);

        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }

        inputStream.close();
        outputStream.close();

        return outFile;
    }

}
