package org.telegram.ext;

import android.content.Context;
import android.graphics.Color;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;

import com.skg.lib.widget.ClearEditText;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;
import org.telegram.tgnet.tl.TL_account;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

import java.util.Objects;

public class EditNicknameFragment extends BaseFragment {

    private LinearLayout contentView;

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

        TLRPC.User latestUser = getMessagesController().getUser(getUserConfig().getClientUserId());

        ClearEditText editText = new ClearEditText(context);
        editText.setTextSize(15);
        editText.setHint(latestUser.first_name);
        editText.setTextColor(Color.parseColor("#000000"));
        editText.setHintTextColor(Color.parseColor("#999999"));
        editText.setBackgroundResource(R.drawable.transparent);
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
        contentView.addView(editText, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 40));

        View viewLine = new View(context);
        viewLine.setBackgroundColor(Color.parseColor("#F4F4F4"));
        contentView.addView(viewLine, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 1));

        LoadingButton button = new LoadingButton(context);
        button.setText("确定");
        button.setBackgroundResource(R.drawable.button_circle_selector);
        contentView.addView(button, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 44, Gravity.CENTER_HORIZONTAL, 36, 40, 36, 0));

        button.setOnClickListener(view -> {
            if (!TextUtils.isEmpty(editText.getText()) && (!TextUtils.equals(latestUser.first_name, editText.getText().toString()))) {
                TL_account.updateProfile req = new TL_account.updateProfile();

                req.flags |= 1;
                req.first_name = latestUser.first_name = Objects.requireNonNull(editText.getText()).toString();

                UserConfig.getInstance(currentAccount).saveConfig(true);
                NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.mainUserInfoChanged);
                NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.updateInterfaces, MessagesController.UPDATE_MASK_NAME);
                ConnectionsManager.getInstance(currentAccount).sendRequest(req, (response, error) -> {

                });

                finishFragment();
            }
        });

        return contentView;
    }
}
