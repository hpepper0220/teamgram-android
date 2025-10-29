package org.telegram.ext.widgets;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.view.ViewCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;

import java.lang.ref.WeakReference;

public class FloatingView {

    private SkFloatingView mFloatingView;
    private static volatile FloatingView mInstance;
    private WeakReference<FrameLayout> mContainer;

    private ViewGroup.LayoutParams mLayoutParams = getParams();

    private FloatingView() {
    }

    public static FloatingView get() {
        if (mInstance == null) {
            synchronized (FloatingView.class) {
                if (mInstance == null) {
                    mInstance = new FloatingView();
                }
            }
        }
        return mInstance;
    }

    public FloatingView add() {
        ensureFloatingView();
        return this;
    }

    public FloatingView attach(Activity activity) {
        attach(getActivityRoot(activity));
        return this;
    }

    public FloatingView attach(FrameLayout container) {
        if (container == null || mFloatingView == null) {
            mContainer = new WeakReference<>(container);
            return this;
        }
        if (mFloatingView.getParent() == container) {
            return this;
        }
        if (mFloatingView.getParent() != null) {
            ((ViewGroup) mFloatingView.getParent()).removeView(mFloatingView);
        }
        mContainer = new WeakReference<>(container);
        container.addView(mFloatingView);
        return this;
    }

    public FloatingView detach(Activity activity) {
        detach(getActivityRoot(activity));
        return this;
    }

    public FloatingView detach(FrameLayout container) {
        if (mFloatingView != null && container != null && ViewCompat.isAttachedToWindow(mFloatingView)) {
            container.removeView(mFloatingView);
        }
        if (getContainer() == container) {
            mContainer = null;
        }
        return this;
    }

    public SkFloatingView getView() {
        return mFloatingView;
    }

    public FloatingView layoutParams(ViewGroup.LayoutParams params) {
        mLayoutParams = params;
        if (mFloatingView != null) {
            mFloatingView.setLayoutParams(params);
        }
        return this;
    }

    private void ensureFloatingView() {
        synchronized (this) {
            if (mFloatingView != null) {
                return;
            }
            SkFloatingView floatingView = new SkFloatingView(ApplicationLoader.applicationContext);
            mFloatingView = floatingView;
            floatingView.setLayoutParams(mLayoutParams);
            addViewToWindow(floatingView);
        }
    }

    private void addViewToWindow(final View view) {
        if (getContainer() == null) {
            return;
        }
        getContainer().addView(view);
    }

    public FloatingView remove() {
        new Handler(Looper.getMainLooper()).post(() -> {
            if (mFloatingView == null) {
                return;
            }
            if (ViewCompat.isAttachedToWindow(mFloatingView) && getContainer() != null) {
                getContainer().removeView(mFloatingView);
            }
            mFloatingView = null;
        });
        return this;
    }

    private FrameLayout getContainer() {
        if (mContainer == null) {
            return null;
        }
        return mContainer.get();
    }

    private FrameLayout.LayoutParams getParams() {
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.BOTTOM | Gravity.END;
        params.setMargins(params.leftMargin, params.topMargin, 13, 500);
        return params;
    }

    private FrameLayout getActivityRoot(Activity activity) {
        if (activity == null) {
            return null;
        }
        try {
            return (FrameLayout) activity.getWindow().getDecorView().findViewById(android.R.id.content);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void clear() {
        FloatingView.get().remove();
    }

    public static void show(Context context, int currentAccount, Long id, String title, String url, String webUrl) {
        FloatingView.get().add();
        SkFloatingView floatingView = FloatingView.get().getView();
        if (null != floatingView) {
            AppCompatImageView imageView = floatingView.findViewById(R.id.image_view);
            Glide.with(context).load(url).diskCacheStrategy(DiskCacheStrategy.ALL).transform(new RoundedCorners(30)).transition(DrawableTransitionOptions.withCrossFade()).into(imageView);
            floatingView.setOnMagnetViewListener(new SkFloatingView.OnMagnetViewListener() {
                @Override
                public void onRemove() {
                    FloatingView.clear();
                }

                @Override
                public void onClick() {
                    NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.openWebView, id, title, url, webUrl);
                }
            });
        }
    }

}
