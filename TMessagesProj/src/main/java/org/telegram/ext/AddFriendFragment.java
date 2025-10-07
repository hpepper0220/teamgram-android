package org.telegram.ext;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.LaunchActivity;

public class AddFriendFragment extends BaseFragment {

    private LinearLayout contentView;
    private LaunchActivity mParentActivity;
    private Rect mRect;

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle("添加朋友");
        actionBar.setTitleColor(Color.BLACK);
        actionBar.setItemsColor(getThemedColor(Theme.key_actionBarActionModeDefaultIcon), false);
        actionBar.setBackgroundColor(Color.WHITE);
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        contentView = new LinearLayout(context);
        contentView.setOrientation(LinearLayout.VERTICAL);
        contentView.setPadding(AndroidUtilities.dp(12), AndroidUtilities.dp(10), AndroidUtilities.dp(12), AndroidUtilities.dp(0));
        contentView.setLayoutParams(LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        LinearLayout searchLayout = new LinearLayout(context);
        searchLayout.setPadding(AndroidUtilities.dp(12), 0, AndroidUtilities.dp(12), 0);
        Drawable drawable = Theme.createRoundRectDrawable(AndroidUtilities.dp(8), Color.parseColor("#F4F4F4"));
        searchLayout.setBackground(drawable);
        searchLayout.setOnClickListener(view -> {
            mParentActivity.presentFragment(new SearchFriendFragment());
        });
        contentView.addView(searchLayout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 40));

        AppCompatImageView iconSearch = new AppCompatImageView(context);
        iconSearch.setImageResource(R.mipmap.ic_index_search);
        searchLayout.addView(iconSearch, LayoutHelper.createLinear(AndroidUtilities.dp(6), AndroidUtilities.dp(6), Gravity.CENTER_VERTICAL, 0, 0, 0, 0));

        AppCompatTextView textView = new AppCompatTextView(context);
        textView.setText("搜索账号");
        textView.setTextSize(13f);
        textView.setTextColor(Color.parseColor("#666666"));
        searchLayout.addView(textView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL, 6, 0, 0, 0));

        return contentView;
    }

    public void setParentActivity(LaunchActivity parentActivity) {
        this.mParentActivity = parentActivity;
    }
}
