package org.telegram.ext.components.dialog;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.widget.AppCompatTextView;

import com.skg.lib.BaseDialog;
import com.skg.lib.action.AnimAction;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.spoilers.SpoilersTextView;

public class SkActionDialog {

    public static class Builder extends BaseDialog.Builder<Builder> {
        private static final int radius = AndroidUtilities.dp(10);
        private static final int horizontalMargin = 20;
        private static final int titleTextColor = 0xFF333333;
        private static final int contentTextColor = 0xFF666666;
        private static final int negativeStateColor = 0xFFEEEEEE;
        private static final int negativeTextColor = 0xFF999999;
        private static final int positiveStateColor = 0xff65a9e0;
        private static final int positiveTextColor = 0xFFFFFFFF;
        private static final int actionTextSize = 14;

        private final Context mContext;

        private LinearLayout containerLayout;
        private AppCompatTextView mTitleView;
        private SpoilersTextView mContentView;
        private LinearLayout mActionLayout;
        private AppCompatTextView mNegativeButton;
        private AppCompatTextView mPositiveButton;

        public Builder(Context context) {
            super(context);
            this.mContext = context;
            createContainerLayout(context);

            setContentView(containerLayout);

            setAnimStyle(AnimAction.ANIM_DEFAULT);
            setGravity(Gravity.CENTER);
        }

        public LinearLayout getContainerLayout() {
            return containerLayout;
        }

        public AppCompatTextView getTitleView() {
            return mTitleView;
        }

        public SpoilersTextView getContentView() {
            return mContentView;
        }

        public LinearLayout getActionLayout() {
            return mActionLayout;
        }

        public AppCompatTextView getNegativeButton() {
            return mNegativeButton;
        }

        public AppCompatTextView getPositiveButton() {
            return mPositiveButton;
        }

        public Builder setOnDismissListener(BaseDialog.OnDismissListener dismissListener) {
            this.addOnDismissListener(dismissListener);
            return this;
        }

        public Builder setTitle(CharSequence text) {
            createTitleView(mContext);
            mTitleView.setText(text);
            return this;
        }

        public Builder setContent(CharSequence text) {
            createContentView(mContext);
            mContentView.setText(text);
            return this;
        }

        public Builder setNegativeButton(CharSequence text) {
            setNegativeButton(text, null);
            return this;
        }

        public Builder setNegativeButton(CharSequence text, final View.OnClickListener listener) {
            if (null == mActionLayout) {
                createActionLayout();
            }
            createNegativeButton();
            mNegativeButton.setText(text);
            mNegativeButton.setOnClickListener(v -> {
                if (null != listener) {
                    listener.onClick(v);
                }
                dismiss();
            });
            return this;
        }

        private void createNegativeButton() {
            mNegativeButton = new AppCompatTextView(mContext);
            mNegativeButton.setId(R.id.dialog_button_negative);
            mNegativeButton.setGravity(Gravity.CENTER);
            mNegativeButton.setTextColor(negativeTextColor);
            mNegativeButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, actionTextSize);
            mNegativeButton.setBackgroundDrawable(Theme.createSimpleSelectorRoundRectDrawable(AndroidUtilities.dp(20), negativeStateColor, negativeStateColor));
            mActionLayout.addView(mNegativeButton, LayoutHelper.createLinear(0, LayoutHelper.MATCH_PARENT, 1f));
        }

        public Builder setPositiveButton(CharSequence text) {
            setPositiveButton(text, null);
            return this;
        }

        public Builder setPositiveButton(CharSequence text, final View.OnClickListener listener) {
            if (null == mActionLayout) {
                createActionLayout();
            }
            createPositiveButton();
            mPositiveButton.setText(text);
            mPositiveButton.setOnClickListener(v -> {
                if (null != listener) {
                    listener.onClick(v);
                }
                dismiss();
            });
            return this;
        }

        private void createPositiveButton() {
            mPositiveButton = new AppCompatTextView(mContext);
            mPositiveButton.setId(R.id.dialog_button_positive);
            mPositiveButton.setGravity(Gravity.CENTER);
            mPositiveButton.setTextColor(positiveTextColor);
            mPositiveButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, actionTextSize);
            mPositiveButton.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            mPositiveButton.setBackgroundDrawable(Theme.createSimpleSelectorRoundRectDrawable(AndroidUtilities.dp(20), positiveStateColor, positiveStateColor));
            if (null != mNegativeButton) {
                mActionLayout.addView(mPositiveButton, LayoutHelper.createLinear(0, LayoutHelper.MATCH_PARENT, 1f, 25, 0, 0, 0));
            } else {
                mActionLayout.addView(mPositiveButton, LayoutHelper.createLinear(0, LayoutHelper.MATCH_PARENT, 1f));
            }
        }

        private void createActionLayout() {
            mActionLayout = new LinearLayout(mContext);
            mActionLayout.setOrientation(LinearLayout.HORIZONTAL);
            containerLayout.addView(mActionLayout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 40, Gravity.BOTTOM, horizontalMargin, 20, horizontalMargin, 16));
//            containerLayout.setPadding(AndroidUtilities.dp(20), 0, AndroidUtilities.dp(20), 0);
        }

        private void createContentView(Context context) {
            mContentView = new SpoilersTextView(context);
            mContentView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
            mContentView.setTextColor(contentTextColor);
            mContentView.setMovementMethod(new AndroidUtilities.LinkMovementMethodMy());
            mContentView.setLinkTextColor(Theme.getColor(Theme.key_dialogTextLink));
            mContentView.setGravity(Gravity.CENTER);
            mContentView.setMaxHeight(AndroidUtilities.dp(200));
            containerLayout.addView(mContentView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP, horizontalMargin, 16, horizontalMargin, 0));
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
            containerLayout.setLayoutParams(LayoutHelper.createLinear(265, LayoutHelper.WRAP_CONTENT));
            containerLayout.setBackgroundDrawable(Theme.createRoundRectDrawable(radius, radius, Color.WHITE));
        }
    }

}
