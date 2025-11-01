package org.telegram.ext;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.LinearLayout;

import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.LaunchActivity;

public class NotificationAndSoundActivity extends BaseFragment {

    private LinearLayout contentView;

    private LaunchActivity mParentActivity;

    public void setParentActivity(LaunchActivity parentActivity) {
        this.mParentActivity = parentActivity;
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle("通知和声音");
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
        contentView.setLayoutParams(LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        return contentView;
    }
}
