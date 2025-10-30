package org.telegram.ext.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import org.telegram.messenger.R;

public class SkFloatingView extends FrameLayout {

    private AppCompatImageView mImageView;
    private AppCompatImageView mBtnClose;

    private float mOriginalRawX;
    private float mOriginalRawY;
    private float mOriginalX;
    private float mOriginalY;

    public static final int MARGIN_EDGE = 13;
    private static final int TOUCH_TIME_THRESHOLD = 150;
    private static final int TOUCH_SLOP = 20; // 拖动阈值（像素）
    private long mLastTouchDownTime;
    protected MoveAnimator mMoveAnimator;
    protected int mScreenWidth;
    private int mScreenHeight;
    private int mStatusBarHeight;
    private boolean isNearestLeft = true;
    private float mPortraitY;
    private boolean mIsDragging = false; // 是否正在拖动
    private float mDownX; // 按下时的X坐标
    private float mDownY; // 按下时的Y坐标

    private OnMagnetViewListener mOnMagnetViewListener;

    public SkFloatingView(Context context) {
        this(context, null);
    }

    public SkFloatingView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SkFloatingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View rootView = LayoutInflater.from(context).inflate(R.layout.sk_floating_view, this);
        mImageView = rootView.findViewById(R.id.image_view);
        mBtnClose = rootView.findViewById(R.id.btn_close);

        mImageView.setOnClickListener(v -> {
            if (null != mOnMagnetViewListener) {
                mOnMagnetViewListener.onClick();
            }
        });

        mBtnClose.setOnClickListener(v -> {
            if (null != mOnMagnetViewListener) {
                mOnMagnetViewListener.onRemove();
            }
        });

        mMoveAnimator = new MoveAnimator();
        mStatusBarHeight = getStatusBarHeight(getContext());
        setClickable(true);
    }

    public void setOnMagnetViewListener(OnMagnetViewListener listener) {
        this.mOnMagnetViewListener = listener;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev == null) {
            return false;
        }
        
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownX = ev.getRawX();
                mDownY = ev.getRawY();
                mIsDragging = false;
                // 初始化拖动参数，以防事件被拦截后需要用到
                changeOriginalTouchParams(ev);
                updateSize();
                mMoveAnimator.stop();
                return false; // 不拦截 ACTION_DOWN，让子控件有机会处理点击
                
            case MotionEvent.ACTION_MOVE:
                if (!mIsDragging) {
                    float deltaX = Math.abs(ev.getRawX() - mDownX);
                    float deltaY = Math.abs(ev.getRawY() - mDownY);
                    // 如果移动距离超过阈值，则拦截事件，开始拖动
                    if (deltaX > TOUCH_SLOP || deltaY > TOUCH_SLOP) {
                        mIsDragging = true;
                        // 返回 true 拦截事件，系统会自动向子控件发送 ACTION_CANCEL
                        // 后续事件将传递到 onTouchEvent 处理拖动
                        return true;
                    }
                } else {
                    // 已经在拖动中，继续拦截
                    return true;
                }
                break;
                
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                boolean wasDragging = mIsDragging;
                // 如果之前是拖动状态，需要继续拦截，让 onTouchEvent 处理抬起事件
                // 否则让子控件处理点击
                return wasDragging;
        }
        
        return false; // 不拦截，让子控件处理
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event == null) {
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                changeOriginalTouchParams(event);
                updateSize();
                mMoveAnimator.stop();
                break;
            case MotionEvent.ACTION_MOVE:
                updateViewPosition(event);
                break;
            case MotionEvent.ACTION_UP:
                clearPortraitY();
                moveToEdge();
                // 如果事件被拦截（即发生了拖动），则不处理点击事件
                // 子控件的点击事件会在事件未被拦截时正常触发
                if (!mIsDragging && isOnClickEvent()) {
                    dealClickEvent();
                }
                mIsDragging = false;
                break;
        }
        return true;
    }

    protected void dealClickEvent() {
//        if (mMagnetViewListener != null) {
//            mMagnetViewListener.onClick(this);
//        }
    }

    protected boolean isOnClickEvent() {
        return System.currentTimeMillis() - mLastTouchDownTime < TOUCH_TIME_THRESHOLD;
    }

    private void updateViewPosition(MotionEvent event) {
        setX(mOriginalX + event.getRawX() - mOriginalRawX);
        // 限制不可超出屏幕高度
        float desY = mOriginalY + event.getRawY() - mOriginalRawY;
        if (desY < mStatusBarHeight) {
            desY = mStatusBarHeight;
        }
        if (desY > mScreenHeight - getHeight()) {
            desY = mScreenHeight - getHeight();
        }
        setY(desY);
    }

    private void changeOriginalTouchParams(MotionEvent event) {
        mOriginalX = getX();
        mOriginalY = getY();
        mOriginalRawX = event.getRawX();
        mOriginalRawY = event.getRawY();
        mLastTouchDownTime = System.currentTimeMillis();
    }

    protected class MoveAnimator implements Runnable {

        private Handler handler = new Handler(Looper.getMainLooper());
        private float destinationX;
        private float destinationY;
        private long startingTime;

        void start(float x, float y) {
            this.destinationX = x;
            this.destinationY = y;
            startingTime = System.currentTimeMillis();
            handler.post(this);
        }

        @Override
        public void run() {
            if (getRootView() == null || getRootView().getParent() == null) {
                return;
            }
            float progress = Math.min(1, (System.currentTimeMillis() - startingTime) / 400f);
            float deltaX = (destinationX - getX()) * progress;
            float deltaY = (destinationY - getY()) * progress;
            move(deltaX, deltaY);
            if (progress < 1) {
                handler.post(this);
            }
        }

        private void stop() {
            handler.removeCallbacks(this);
        }
    }

    private void move(float deltaX, float deltaY) {
        setX(getX() + deltaX);
        setY(getY() + deltaY);
    }

    protected void updateSize() {
        ViewGroup viewGroup = (ViewGroup) getParent();
        if (viewGroup != null) {
            mScreenWidth = viewGroup.getWidth() - getWidth();
            mScreenHeight = viewGroup.getHeight();
        }
//        mScreenWidth = (SystemUtils.getScreenWidth(getContext()) - this.getWidth());
//        mScreenHeight = SystemUtils.getScreenHeight(getContext());
    }

    public void moveToEdge() {
        moveToEdge(isNearestLeft(), false);
    }

    public void moveToEdge(boolean isLeft, boolean isLandscape) {
        float moveDistance = isLeft ? MARGIN_EDGE : mScreenWidth - MARGIN_EDGE;
        float y = getY();
        if (!isLandscape && mPortraitY != 0) {
            y = mPortraitY;
            clearPortraitY();
        }
        mMoveAnimator.start(moveDistance, Math.min(Math.max(0, y), mScreenHeight - getHeight()));
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (getParent() != null) {
            final boolean isLandscape = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE;
            markPortraitY(isLandscape);
            ((ViewGroup) getParent()).post(new Runnable() {
                @Override
                public void run() {
                    updateSize();
                    moveToEdge(isNearestLeft, isLandscape);
                }
            });
        }
    }

    private void markPortraitY(boolean isLandscape) {
        if (isLandscape) {
            mPortraitY = getY();
        }
    }

    private void clearPortraitY() {
        mPortraitY = 0;
    }

    protected boolean isNearestLeft() {
        int middle = mScreenWidth / 2;
        isNearestLeft = getX() < middle;
        return isNearestLeft;
    }

    public void onRemove() {
//        if (mMagnetViewListener != null) {
//            mMagnetViewListener.onRemove(this);
//        }
    }

    private int getStatusBarHeight(Context context) {
        int result = 0;
        @SuppressLint("InternalInsetResource") int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public interface OnMagnetViewListener {

        void onRemove();

        void onClick();

    }

}
