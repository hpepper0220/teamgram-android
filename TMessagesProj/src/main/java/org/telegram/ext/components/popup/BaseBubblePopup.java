package org.telegram.ext.components.popup;

import android.content.Context;
import android.graphics.Color;
import android.view.View;

import com.xujiaji.happybubble.BubbleDialog;
import com.xujiaji.happybubble.BubbleLayout;

public abstract class BaseBubblePopup extends BubbleDialog {

    private Context mContext;
    private BubbleLayout mBubbleLayout;

    public BaseBubblePopup(Context context) {
        super(context);
        this.mContext = context;

        createBubbleLayout();

        setBubbleContentView(createContentView());
        setBubbleLayout(mBubbleLayout);

        setCancelable(true);
        setCanceledOnTouchOutside(true);
    }

    public void showAsDropDown(View anchor) {
        setClickedView(anchor);
        show();
    }

    public BubbleLayout getBubbleLayout() {
        return mBubbleLayout;
    }

    private void createBubbleLayout() {
        mBubbleLayout = new BubbleLayout(mContext);
        mBubbleLayout.setShadowColor(Color.TRANSPARENT);
        mBubbleLayout.setBubblePadding(0);
    }

    protected abstract View createContentView();

}
