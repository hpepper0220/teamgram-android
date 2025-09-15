package org.telegram.ext;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RadialProgressView;

public class LoadingButton extends FrameLayout {
    private static final int buttonHeight = 44;

    private AppCompatTextView mTextView;
    private FrameLayout mContainerLayout;
    private RadialProgressView mProgressView;

    private AnimatorSet animatorSet;

    public LoadingButton(@NonNull Context context) {
        this(context, buttonHeight, Theme.getColor(Theme.key_chats_actionBackground), Theme.getColor(Theme.key_chats_actionPressedBackground));
    }

    public LoadingButton(@NonNull Context context, int height, int defaultColor, int pressedColor) {
        super(context);
        createContainerLayout(context, height, defaultColor, pressedColor);
        createTextView(context);
        createProgressView(context);
    }

    public void setText(CharSequence text) {
        mTextView.setText(text);
    }

    public AppCompatTextView getTextView() {
        return mTextView;
    }

    public void showLoading() {
        this.setEnabled(false);
        if (null != animatorSet) {
            animatorSet.cancel();
        }
        animatorSet = new AnimatorSet();
        ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mTextView.setVisibility(View.GONE);
                mProgressView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (animatorSet != null && animatorSet.equals(animation)) {
                    animatorSet = null;
                }
            }
        });
        animator.addUpdateListener(animation -> {
            float val = (float) animation.getAnimatedValue();

            float scale = 0.1f + 0.9f * val;
            mProgressView.setScaleX(scale);
            mProgressView.setScaleY(scale);
            mProgressView.setAlpha(val);
        });
        animatorSet.playTogether(animator);
        animatorSet.setDuration(150);
        animatorSet.start();
        mContainerLayout.setAlpha(0.5f);
    }

    public void hideLoading() {
        this.setEnabled(true);
        if (null != animatorSet) {
            animatorSet.cancel();
        }
        mTextView.setVisibility(View.VISIBLE);
        mProgressView.setVisibility(View.GONE);
        mContainerLayout.setAlpha(1.0f);
    }

    private void createProgressView(Context context) {
        mProgressView = new RadialProgressView(context);
        mProgressView.setSize(AndroidUtilities.dp(22));
        mProgressView.setAlpha(0.0f);
        mProgressView.setScaleX(0.1f);
        mProgressView.setScaleY(0.1f);
        mProgressView.setVisibility(View.INVISIBLE);
        mProgressView.setProgressColor(Color.WHITE);
        this.addView(mProgressView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
    }

    private void createTextView(Context context) {
        mTextView = new AppCompatTextView(context);
        mTextView.setTextColor(Color.WHITE);
        mTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        mTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        mContainerLayout.addView(mTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER));
    }

    private void createContainerLayout(Context context, int height, int defaultColor, int pressedColor) {
        mContainerLayout = new FrameLayout(context);
        this.addView(mContainerLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        Drawable drawable = Theme.createSimpleSelectorRoundRectDrawable(AndroidUtilities.dp(buttonHeight / 2), defaultColor, pressedColor);
        mContainerLayout.setBackground(drawable);
    }

}
