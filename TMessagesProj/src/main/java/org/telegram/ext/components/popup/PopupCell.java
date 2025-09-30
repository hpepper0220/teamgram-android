package org.telegram.ext.components.popup;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

public class PopupCell extends LinearLayout {

    private Context mContext;
    private AppCompatImageView mIconView;
    private AppCompatTextView mTitleView;

    private int mIconSize = 20;
    private int titleLeftPadding = 10;

    public PopupCell(Context context) {
        super(context);
        this.mContext = context;

        createIconView();
        createTitleView();

        this.setOrientation(LinearLayout.HORIZONTAL);
        this.setGravity(Gravity.CENTER_VERTICAL);
    }

    public void setData(CharSequence title, int drawableResId) {
        mTitleView.setText(title);
        mIconView.setImageResource(drawableResId);
    }

    private void createTitleView() {
        mTitleView = new AppCompatTextView(mContext);
        mTitleView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        mTitleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        mTitleView.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
        mTitleView.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        mTitleView.setGravity(Gravity.CENTER_VERTICAL);
        mTitleView.setPadding(AndroidUtilities.dp(titleLeftPadding), 0, 0, 0);
        this.addView(mTitleView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.MATCH_PARENT));
    }

    private void createIconView() {
        mIconView = new AppCompatImageView(mContext);
        mIconView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        mIconView.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText), PorterDuff.Mode.MULTIPLY));
        this.addView(mIconView, LayoutHelper.createLinear(mIconSize, mIconSize));
    }

}
