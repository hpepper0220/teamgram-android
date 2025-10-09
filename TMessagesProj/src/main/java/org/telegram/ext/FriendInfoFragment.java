package org.telegram.ext;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.widget.AppCompatTextView;

import net.csdn.roundview.RoundLinearLayout;

import org.checkerframework.checker.units.qual.A;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.UserCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.LaunchActivity;

public class FriendInfoFragment extends BaseFragment {

    private long user_id;
    private LinearLayout contentView;
    private LaunchActivity mParentActivity;
    private TLRPC.UserFull userInfo;
    private TLRPC.User user;
    private AppCompatTextView tv_remark_content;

    public FriendInfoFragment() {
    }

    public FriendInfoFragment(Bundle args) {
        super(args);
        user_id = args.getLong("user_id");
    }

    public void setParentActivity(LaunchActivity parentActivity) {
        this.mParentActivity = parentActivity;
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle("个人信息");
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

        userInfo = getMessagesController().getUserFull(user_id);

        contentView = new LinearLayout(context);
        contentView.setOrientation(LinearLayout.VERTICAL);
        contentView.setBackgroundColor(Color.parseColor("#F4F4F4"));
        contentView.setPadding(0, 0, 0, AndroidUtilities.dp(0));
        contentView.setLayoutParams(LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        View viewLine1 = new View(context);
        contentView.addView(viewLine1, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, AndroidUtilities.dp(4)));

        RoundLinearLayout headerLayout = new RoundLinearLayout(context);
        headerLayout.setRadius(4f);
        headerLayout.setBackgroundColor(Color.WHITE);
        headerLayout.setPadding(8, 20, 8, 20);
        contentView.addView(headerLayout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 12, 0, 12, 0));

        UserCell userCell = new UserCell(context, 1, 1, false);
        user = getMessagesController().getUser(user_id);
        userCell.setData(user, user.first_name, LocaleController.formatUserStatus(currentAccount, user), 0);
        headerLayout.addView(userCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        RoundLinearLayout bottomLayout = new RoundLinearLayout(context);
        bottomLayout.setRadius(4f);
        bottomLayout.setBackgroundColor(Color.WHITE);
        bottomLayout.setOrientation(LinearLayout.VERTICAL);
        contentView.addView(bottomLayout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 12, 16, 12, 0));

        LinearLayout remarkLayout = new LinearLayout(context);
        remarkLayout.setOrientation(LinearLayout.HORIZONTAL);
        bottomLayout.addView(remarkLayout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, AndroidUtilities.dp(16)));

        AppCompatTextView tv_remark = new AppCompatTextView(context);
        tv_remark.setText("个人简介");
        tv_remark.setGravity(Gravity.CENTER_VERTICAL);
        tv_remark.setPadding(12, 0, 12, 0);
        remarkLayout.addView(tv_remark, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.MATCH_PARENT, 8, 0, 8, 0));

        tv_remark_content = new AppCompatTextView(context);
        tv_remark_content.setMaxLines(1);
        tv_remark_content.setTextColor(Color.BLACK);
        tv_remark_content.setEllipsize(TextUtils.TruncateAt.END);
        tv_remark_content.setGravity(Gravity.CENTER_VERTICAL | Gravity.END);
        tv_remark_content.setPadding(0, 0, 12, 0);
        remarkLayout.addView(tv_remark_content, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.MATCH_PARENT, 1f, 0, 0, 8, 0));

        View viewLine = new View(context);
        viewLine.setBackgroundColor(Color.parseColor("#F4F4F4"));
        bottomLayout.addView(viewLine, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 1));

        LinearLayout signupRes = new LinearLayout(context);
        signupRes.setOrientation(LinearLayout.HORIZONTAL);
        bottomLayout.addView(signupRes, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, AndroidUtilities.dp(16)));

        AppCompatTextView tv_signup_res = new AppCompatTextView(context);
        tv_signup_res.setText("注册来源");
        tv_signup_res.setGravity(Gravity.CENTER_VERTICAL);
        tv_signup_res.setPadding(12, 0, 12, 0);
        signupRes.addView(tv_signup_res, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.MATCH_PARENT, 8, 0, 8, 0));

        AppCompatTextView tv_signup_res_value = new AppCompatTextView(context);
        tv_signup_res_value.setText("账号注册");
        tv_signup_res_value.setGravity(Gravity.CENTER_VERTICAL);
        tv_signup_res_value.setTextColor(Color.BLACK);
        tv_signup_res_value.setEllipsize(TextUtils.TruncateAt.END);
        tv_signup_res_value.setGravity(Gravity.CENTER_VERTICAL | Gravity.END);
        tv_signup_res_value.setPadding(0, 0, 12, 0);
        signupRes.addView(tv_signup_res_value, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.MATCH_PARENT, 1f, 0, 0, 8, 0));

        LoadingButton button = new LoadingButton(context);
        button.setText("添加好友");
        button.setBackgroundResource(R.drawable.button_circle_selector);
        button.setOnClickListener(view -> {
            Bundle bundle = new Bundle();
            bundle.putLong("user_id", user_id);

            SendApplyMsgFragment sendApplyMsgFragment = new SendApplyMsgFragment(bundle);
            sendApplyMsgFragment.setParentActivity(mParentActivity);

            mParentActivity.presentFragment(sendApplyMsgFragment);
        });
        contentView.addView(button, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 46, Gravity.CENTER_HORIZONTAL, 36, 40, 36, 0));

        loadUserFullInfo();

        return contentView;
    }

    private void loadUserFullInfo() {
        getMessagesController().loadFullUser(user, classGuid, true, arg -> AndroidUtilities.runOnUIThread(() -> {
            if (null == arg || null == arg.about || TextUtils.isEmpty(arg.about)) {
                tv_remark_content.setText("补充几句话介绍一下你自己");
            } else {
                tv_remark_content.setText(arg.about);
            }
        }));
    }
}
