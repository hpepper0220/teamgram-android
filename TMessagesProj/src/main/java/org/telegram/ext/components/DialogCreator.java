package org.telegram.ext.components;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import com.skg.lib.BaseDialog;

import org.checkerframework.checker.units.qual.A;
import org.telegram.ext.components.dialog.SkActionDialog;
import org.telegram.ext.components.dialog.SkBottomDialog;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.OneUIUtilities;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

public class DialogCreator {

    public static void createDeleteMessagesDialog(Context context, String title, String message, View customView, String actionText, final BaseDialog.OnDismissListener onDismissListener, final View.OnClickListener listener) {
        SkActionDialog.Builder builder = new SkActionDialog.Builder(context);
        builder.setTitle(title).setContent(message);
        if (null != customView) {
            builder.getContainerLayout().addView(customView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 2, 6, 2, 0));
        }
        builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel));
        builder.setPositiveButton(actionText, listener);
        builder.setOnDismissListener(dialog -> {
            if (onDismissListener != null) {
                onDismissListener.onDismiss(dialog);
            }
        });
        builder.show();
        builder.getContentView().setLineSpacing(20, 1);
        if (null != customView) {
            builder.getContentView().setGravity(Gravity.START | Gravity.TOP);
            builder.getActionLayout().setLayoutParams(LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 40, Gravity.BOTTOM, 20, 10, 20, 16));
        }
        builder.getPositiveButton().setBackgroundDrawable(Theme.createSimpleSelectorRoundRectDrawable(AndroidUtilities.dp(20), 0xFFFE456A, 0xFFFE456A));
    }

    public static void createClearOrDeleteDialogAlert(Context context, CharSequence title, CharSequence message, View customView, CharSequence actionText, final View.OnClickListener listener) {
        SkActionDialog.Builder builder = new SkActionDialog.Builder(context);
        builder.setTitle(title).setContent(message);
        if (null != customView) {
            builder.getContainerLayout().addView(customView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 2, 6, 2, 0));
        }
        builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel));
        builder.setPositiveButton(actionText, listener);
        builder.show();
        builder.getContentView().setLineSpacing(20, 1);
        if (null != customView) {
            builder.getContentView().setGravity(Gravity.START | Gravity.TOP);
            builder.getActionLayout().setLayoutParams(LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 40, Gravity.BOTTOM, 20, 10, 20, 16));
        }
        builder.getPositiveButton().setBackgroundDrawable(Theme.createSimpleSelectorRoundRectDrawable(AndroidUtilities.dp(20), 0xFFFE456A, 0xFFFE456A));
    }

    public static void createBottomDialog(Context context, CharSequence title, final View.OnClickListener listener) {
        SkBottomDialog.Builder builder = new SkBottomDialog.Builder(context);
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setId(R.id.dialog_hide);

        layout.setOnClickListener(view -> {
            listener.onClick(layout);
            builder.dismiss();
        });

        AppCompatImageView aiv1 = new AppCompatImageView(context);
        aiv1.setImageResource(R.mipmap.ic_folder);
        layout.addView(aiv1, LayoutHelper.createLinear(60, 60));

        AppCompatTextView atv1 = new AppCompatTextView(context);
        atv1.setText("收起");
        atv1.setTextColor(Color.parseColor("#666666"));
        atv1.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
        atv1.setGravity(Gravity.CENTER);
        layout.addView(atv1, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));

        builder.getCustomView().addView(layout, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 0, 0, 6, 0));

        LinearLayout layout2 = new LinearLayout(context);
        layout2.setOrientation(LinearLayout.VERTICAL);
        layout2.setGravity(Gravity.CENTER);
        layout2.setId(R.id.dialog_refresh);

        layout2.setOnClickListener(view -> {
            listener.onClick(layout2);
            builder.dismiss();
        });

        AppCompatImageView aiv2 = new AppCompatImageView(context);
        aiv2.setImageResource(R.mipmap.ic_refresh);
        layout2.addView(aiv2, LayoutHelper.createLinear(60, 60));

        AppCompatTextView atv2 = new AppCompatTextView(context);
        atv2.setText("刷新");
        atv2.setTextColor(Color.parseColor("#666666"));
        atv2.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
        atv2.setGravity(Gravity.CENTER);
        layout2.addView(atv2, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));

        builder.getCustomView().addView(layout2, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));
        builder.show();
    }

    public static void createBackgroundActivityDialog(Context context) {
        SkActionDialog.Builder builder = new SkActionDialog.Builder(context);
        builder.setTitle(context.getResources().getString(R.string.AllowBackgroundActivity)).setContent(AndroidUtilities.replaceTags(context.getResources().getString(OneUIUtilities.isOneUI() ? Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ? R.string.AllowBackgroundActivityInfoOneUIAboveS : R.string.AllowBackgroundActivityInfoOneUIBelowS : R.string.AllowBackgroundActivityInfo))).setNegativeButton(context.getResources().getString(R.string.ContactsPermissionAlertNotNow)).setPositiveButton(context.getResources().getString(R.string.PermissionOpenSettings), v -> {
            try {
                Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + ApplicationLoader.applicationContext.getPackageName()));
                context.startActivity(intent);
            } catch (Exception e) {
                FileLog.e(e);
            }
        });
        builder.show();
        builder.getContentView().setLineSpacing(20, 1);
        builder.getContentView().setGravity(Gravity.START | Gravity.TOP);
    }

}
