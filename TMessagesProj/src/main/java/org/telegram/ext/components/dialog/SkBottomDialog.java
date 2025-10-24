package org.telegram.ext.components.dialog;

import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;

import com.skg.lib.BaseDialog;
import com.skg.lib.action.AnimAction;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

public class SkBottomDialog {

    public static class Builder extends BaseDialog.Builder<SkBottomDialog.Builder> {
        private static final int radius = AndroidUtilities.dp(10);
        private static final int horizontalMargin = 20;
        private static final int titleTextColor = 0xFF333333;
        private static final int editTextColor = 0xFF333333;
        private static final int editHintColor = 0xFFAAAAAA;

        private final Context mContext;

        private LinearLayout containerLayout;
        private AppCompatTextView mTitleView;
        private LinearLayout customView;

        public Builder(Context context) {
            super(context);
            this.mContext = context;
            createContainerLayout(context);

            setContentView(containerLayout);

            createCustomView(context);
            createCancelButton(context);

            setAnimStyle(AnimAction.ANIM_DEFAULT);
            setGravity(Gravity.BOTTOM);
        }

        public LinearLayout getContainerLayout() {
            return containerLayout;
        }

        public LinearLayout getCustomView() {
            return customView;
        }

        public AppCompatTextView getTitleView() {
            return mTitleView;
        }

        private void createCustomView(Context context) {
            customView = new LinearLayout(context);
            customView.setOrientation(LinearLayout.HORIZONTAL);
            containerLayout.addView(customView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 0, 1f, 12, 16, 12, 0));
        }

        private void createCancelButton(Context context) {
            AppCompatTextView cancelButton = new AppCompatTextView(context);
            cancelButton.setText("取消");
            cancelButton.setTextColor(titleTextColor);
            cancelButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
            cancelButton.setGravity(Gravity.CENTER);
            cancelButton.setBackgroundColor(Color.WHITE);
            cancelButton.setOnClickListener(view -> dismiss());
            containerLayout.addView(cancelButton, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48));
        }

        private void createTitleView(Context context) {
            mTitleView = new AppCompatTextView(context);
            mTitleView.setTextColor(titleTextColor);
            mTitleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            mTitleView.setGravity(Gravity.CENTER);
            mTitleView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            containerLayout.addView(mTitleView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP, horizontalMargin, 16, horizontalMargin, 0));
        }

        private void createContainerLayout(Context context) {
            containerLayout = new LinearLayout(context);
            containerLayout.setOrientation(LinearLayout.VERTICAL);
            containerLayout.setLayoutParams(LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 200));
            containerLayout.setBackground(Theme.createRoundRectDrawable(radius, 0, Color.parseColor("#F4F4F4")));
            containerLayout.setPadding(0, 0, 0, 0);
        }

    }

}
