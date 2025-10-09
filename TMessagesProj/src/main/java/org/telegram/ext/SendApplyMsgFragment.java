package org.telegram.ext;

import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatTextView;

import com.skg.lib.widget.ClearEditText;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.LaunchActivity;

import java.util.Objects;

public class SendApplyMsgFragment extends BaseFragment {

    private long user_id;
    private LinearLayout contentView;
    private LoadingButton button;
    private ClearEditText editText;
    private TLRPC.User latestUser;
    private LaunchActivity mParentActivity;

    public SendApplyMsgFragment() {
    }

    public SendApplyMsgFragment(Bundle args) {
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
        actionBar.setTitle("好友验证");
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
        contentView.setPadding(0, 0, 0, AndroidUtilities.dp(0));
        contentView.setLayoutParams(LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        AppCompatTextView tv_remark = new AppCompatTextView(context);
        tv_remark.setText("你需要发送好友申请，等待对方通过");
        tv_remark.setGravity(Gravity.CENTER_VERTICAL);
        tv_remark.setPadding(AndroidUtilities.dp(14), 0, 0, 0);
        tv_remark.setBackgroundColor(Color.parseColor("#F4F4F4"));
        contentView.addView(tv_remark, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 40, 0, 0, 0, 0));

        latestUser = getMessagesController().getUser(getUserConfig().getClientUserId());

        editText = new ClearEditText(context);
        editText.setTextSize(15);
        editText.setHint("你好，我是" + latestUser.first_name);
        editText.setTextColor(Color.parseColor("#000000"));
        editText.setHintTextColor(Color.parseColor("#999999"));
        editText.setBackgroundResource(R.drawable.transparent);
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
        contentView.addView(editText, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48, 10, 0, 12, 0));

        View viewLine = new View(context);
        viewLine.setBackgroundColor(Color.parseColor("#F4F4F4"));
        contentView.addView(viewLine, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 1));

        button = new LoadingButton(context);
        button.setText("发送");
        button.setBackgroundResource(R.drawable.button_circle_selector);
        button.setOnClickListener(view -> sendApplyRequest(context, getMessagesController().getUser(user_id)));
        contentView.addView(button, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 44, Gravity.CENTER_HORIZONTAL, 36, 40, 36, 0));

        return contentView;
    }

    private void sendApplyRequest(Context context, TLRPC.User target) {
        button.showLoading();

        TLRPC.TL_contacts_addContact req = new TLRPC.TL_contacts_addContact();
        req.id = getMessagesController().getInputUser(target);
        req.first_name = target.first_name;
        req.last_name = target.username;
        req.phone = target.phone;
        req.add_phone_privacy_exception = false;
        if (Objects.requireNonNull(editText.getText()).toString().isEmpty()) {
            req.message = "你好，我是" + latestUser.first_name;
        } else {
            req.message = editText.getText().toString();
        }
        if (req.phone == null) {
            req.phone = "";
        }
        ConnectionsManager.getInstance(currentAccount).sendRequest(req, (response, error) -> {
            AndroidUtilities.runOnUIThread(() -> {
                button.hideLoading();
                if (error != null) {
                    needShowAlert(getString(R.string.RestorePasswordNoEmailTitle), error.text);
                    return;
                }
                Toast.makeText(context, "申请已发送", Toast.LENGTH_SHORT).show();
                finishFragment();
            });
        });
    }

    private void needShowAlert(String title, String text) {
        if (text == null || mParentActivity == null) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
        builder.setTitle(title);
        builder.setMessage(text);
        builder.setPositiveButton(getString("OK", R.string.OK), null);
        showDialog(builder.create());
    }
}
