package org.telegram.ext;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.FrameLayout;

import org.telegram.ext.respository.SkRepository;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RadialProgressView;

public class SkDiscoveryFragment extends BaseFragment {

    private AnimatorSet animatorSet;

    private FrameLayout frameContainerView;
    private RadialProgressView progressView;

    @Override
    public View createView(Context context) {
        frameContainerView = new FrameLayout(context);
        frameContainerView.setLayoutParams(LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        createProgressView(context);

        showLoading();

        fetchData(currentAccount, classGuid);

        return frameContainerView;
    }

    public void fetchData(int currentCount, int classGuid) {
        SkRepository.getInstance().getDiscovery(currentCount, classGuid);
    }

    private void createProgressView(Context context) {
        progressView = new RadialProgressView(context);
        progressView.setSize(AndroidUtilities.dp(22));
        progressView.setAlpha(0.0f);
        progressView.setScaleX(0.1f);
        progressView.setScaleY(0.1f);
        progressView.setVisibility(View.INVISIBLE);
        progressView.setProgressColor(Color.BLACK);
        frameContainerView.addView(progressView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
    }

    public void showLoading() {
        if (null != animatorSet) {
            animatorSet.cancel();
        }
        animatorSet = new AnimatorSet();
        ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                progressView.setVisibility(View.VISIBLE);
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
            progressView.setScaleX(scale);
            progressView.setScaleY(scale);
            progressView.setAlpha(val);
        });
        animatorSet.playTogether(animator);
        animatorSet.setDuration(150);
        animatorSet.start();
    }

    public void hideLoading() {
        if (null != animatorSet) {
            animatorSet.cancel();
        }
        progressView.setVisibility(View.GONE);
    }
}
