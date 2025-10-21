package org.telegram.ext.components;

import android.content.Context;
import android.view.Gravity;
import android.view.View;

import com.skg.lib.BaseDialog;

import org.telegram.ext.components.dialog.SkActionDialog;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
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

}
