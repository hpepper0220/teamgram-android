package org.telegram.ext;

import static org.telegram.messenger.LocaleController.getString;
import static org.telegram.messenger.NotificationsController.TYPE_CHANNEL;
import static org.telegram.messenger.NotificationsController.TYPE_GROUP;
import static org.telegram.messenger.NotificationsController.TYPE_PRIVATE;
import static org.telegram.messenger.NotificationsController.TYPE_REACTIONS_MESSAGES;
import static org.telegram.messenger.NotificationsController.TYPE_REACTIONS_STORIES;
import static org.telegram.messenger.NotificationsController.TYPE_STORIES;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import com.kyleduo.switchbutton.SwitchButton;

import net.csdn.roundview.RoundLinearLayout;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;
import org.telegram.tgnet.tl.TL_account;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AlertsCreator;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.LaunchActivity;
import org.telegram.ui.NotificationsSoundActivity;

import java.util.ArrayList;

public class NotificationAndSoundActivity extends BaseFragment implements NotificationCenter.NotificationCenterDelegate {

    private LinearLayout contentView;

    private LaunchActivity mParentActivity;
    private SwitchButton sb1;
    private SwitchButton sb2;
    private boolean enabled;
    private AppCompatTextView tv_sound;
    private SwitchButton sb_group;
    private SwitchButton sb_group_preview;
    private AppCompatTextView tv_sound_group;
    private SwitchButton sb_channel;
    private SwitchButton sb_channel_preview;
    private AppCompatTextView tv_sound_channel;
    private SwitchButton sb_app_tip;
    private SwitchButton sb_shock;
    private SwitchButton sb_app_msg_preview;
    private boolean reseting = false;

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

        ScrollView scrollView = new ScrollView(context);
        scrollView.setBackgroundColor(Color.parseColor("#F4F4F4"));
        scrollView.setLayoutParams(LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        contentView = new LinearLayout(context);
        contentView.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(contentView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        AppCompatTextView tv_notification = new AppCompatTextView(context);
        tv_notification.setText("消息通知");
        tv_notification.setGravity(Gravity.CENTER_VERTICAL);
        contentView.addView(tv_notification, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, AndroidUtilities.dp(16), 12, 0, 12, 0));

        RoundLinearLayout ll_notification = new RoundLinearLayout(context);
        ll_notification.setBackgroundColor(Color.WHITE);
        ll_notification.setRadius(6f);
        ll_notification.setOrientation(LinearLayout.VERTICAL);
        contentView.addView(ll_notification, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 12, 0, 12, 0));

        // 消息通知
        LinearLayout ll_notification_1 = new LinearLayout(context);
        ll_notification_1.setOrientation(LinearLayout.HORIZONTAL);
        ll_notification.addView(ll_notification_1, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48));

        AppCompatTextView tv_1 = new AppCompatTextView(context);
        tv_1.setText("显示通知");
        tv_1.setTextSize(15f);
        tv_1.setTextColor(Color.BLACK);
        tv_1.setGravity(Gravity.CENTER_VERTICAL);
        ll_notification_1.addView(tv_1, LayoutHelper.createLinear(0, LayoutHelper.MATCH_PARENT, 1f, Gravity.CENTER_VERTICAL, 12, 0, 0, 0));

        sb1 = new SwitchButton(context);
        sb1.setBackDrawableRes(R.drawable.miui_back_drawable);
        sb1.setThumbColorRes(R.drawable.custom_thumb_color);
        sb1.setClickable(false);
        ll_notification_1.addView(sb1, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL, 0, 8, 12, 8));

        ll_notification_1.setOnClickListener(view -> {
            if (sb1.isChecked()) {
                AlertsCreator.showCustomNotificationsDialog(NotificationAndSoundActivity.this, 0, 0, 1, new ArrayList<>(), null, currentAccount, param -> {
                    sb1.setChecked(getNotificationsController().isGlobalNotificationsEnabled(1));
                });
            } else {
                getNotificationsController().setGlobalNotificationsEnabled(1, 0);
                updateView();
            }
        });

        View view_line1 = new View(context);
        view_line1.setBackgroundColor(Color.parseColor("#F4F4F4"));
        ll_notification.addView(view_line1, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 1, 12, 0, 12, 0));

        // 消息预览
        LinearLayout ll_notification_2 = new LinearLayout(context);
        ll_notification_2.setOrientation(LinearLayout.HORIZONTAL);
        ll_notification.addView(ll_notification_2, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48));

        AppCompatTextView tv_2 = new AppCompatTextView(context);
        tv_2.setText("消息预览");
        tv_2.setTextSize(15f);
        tv_2.setTextColor(Color.BLACK);
        tv_2.setGravity(Gravity.CENTER_VERTICAL);
        ll_notification_2.addView(tv_2, LayoutHelper.createLinear(0, LayoutHelper.MATCH_PARENT, 1f, Gravity.CENTER_VERTICAL, 12, 0, 0, 0));

        sb2 = new SwitchButton(context);
        sb2.setBackDrawableRes(R.drawable.miui_back_drawable);
        sb2.setThumbColorRes(R.drawable.custom_thumb_color);
        sb2.setClickable(false);
        ll_notification_2.addView(sb2, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL, 0, 8, 12, 8));
        ll_notification_2.setOnClickListener(view -> {
            SharedPreferences preferences = getNotificationsSettings();
            SharedPreferences.Editor editor = preferences.edit();
            enabled = preferences.getBoolean("EnablePreviewAll", true);
            editor.putBoolean("EnablePreviewAll", !enabled);

            editor.commit();
            getNotificationsController().updateServerNotificationsSettings(1);

            sb2.setChecked(!enabled);
        });

        View view_line2 = new View(context);
        view_line2.setBackgroundColor(Color.parseColor("#F4F4F4"));
        ll_notification.addView(view_line2, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 1, 12, 0, 12, 0));

        // 声音
        LinearLayout ll_notification_3 = new LinearLayout(context);
        ll_notification_3.setOrientation(LinearLayout.HORIZONTAL);
        ll_notification.addView(ll_notification_3, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48));

        AppCompatTextView tv_3 = new AppCompatTextView(context);
        tv_3.setText("声音");
        tv_3.setTextSize(15f);
        tv_3.setTextColor(Color.BLACK);
        tv_3.setGravity(Gravity.CENTER_VERTICAL);
        ll_notification_3.addView(tv_3, LayoutHelper.createLinear(0, LayoutHelper.MATCH_PARENT, 1f, Gravity.CENTER_VERTICAL, 12, 0, 0, 0));

        tv_sound = new AppCompatTextView(context);
        tv_sound.setTextSize(15f);
        tv_sound.setTextColor(Color.BLACK);
        tv_sound.setGravity(Gravity.CENTER_VERTICAL);
        ll_notification_3.addView(tv_sound, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.MATCH_PARENT, Gravity.CENTER_VERTICAL, 0, 0, 0, 0));

        AppCompatImageView imageView = new AppCompatImageView(context);
        imageView.setImageResource(R.drawable.call_arrow_right);
        imageView.setColorFilter(Color.BLACK);
        ll_notification_3.addView(imageView, LayoutHelper.createLinear(18, 18, Gravity.CENTER_VERTICAL, 0, 0, 12, 0));

        ll_notification_3.setOnClickListener(view -> {
            Bundle bundle = new Bundle();
            bundle.putInt("type", 1);
            presentFragment(new NotificationsSoundActivity(bundle, getResourceProvider()));
        });

        createGroup(context);
        createChannel(context);
        createAppNotification(context);

        LoadingButton reset_button = new LoadingButton(context);
        reset_button.setText("重设所有通知选项");
        contentView.addView(reset_button, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 44, 12, 20, 12, 20));
        reset_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                builder.setTitle(getString("ResetNotificationsAlertTitle", R.string.ResetNotificationsAlertTitle));
                builder.setMessage(getString("ResetNotificationsAlert", R.string.ResetNotificationsAlert));
                builder.setPositiveButton(getString("Reset", R.string.Reset), (dialogInterface, i) -> {
                    if (reseting) {
                        return;
                    }
                    reseting = true;
                    TL_account.resetNotifySettings req = new TL_account.resetNotifySettings();
                    ConnectionsManager.getInstance(currentAccount).sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
                        getMessagesController().enableJoined = true;
                        reseting = false;
                        SharedPreferences preferences = MessagesController.getNotificationsSettings(currentAccount);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.clear();
                        editor.commit();
                        updateView();
                        if (getParentActivity() != null) {
                            Toast toast = Toast.makeText(getParentActivity(), getString("ResetNotificationsText", R.string.ResetNotificationsText), Toast.LENGTH_SHORT);
                            toast.show();
                        }
                        getMessagesStorage().updateMutedDialogsFiltersCounters();
                    }));
                });
                builder.setNegativeButton(getString("Cancel", R.string.Cancel), null);
                AlertDialog alertDialog = builder.create();
                showDialog(alertDialog);
                TextView button = (TextView) alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                if (button != null) {
                    button.setTextColor(Theme.getColor(Theme.key_text_RedBold));
                }
            }
        });

        updateView();

        return scrollView;
    }

    private void createAppNotification(Context context) {
        AppCompatTextView tv_notification = new AppCompatTextView(context);
        tv_notification.setText("应用内通知");
        tv_notification.setGravity(Gravity.CENTER_VERTICAL);
        contentView.addView(tv_notification, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, AndroidUtilities.dp(16), 12, 0, 12, 0));

        RoundLinearLayout ll_notification = new RoundLinearLayout(context);
        ll_notification.setBackgroundColor(Color.WHITE);
        ll_notification.setRadius(6f);
        ll_notification.setOrientation(LinearLayout.VERTICAL);
        contentView.addView(ll_notification, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 12, 0, 12, 0));

        // 消息通知
        LinearLayout ll_notification_1 = new LinearLayout(context);
        ll_notification_1.setOrientation(LinearLayout.HORIZONTAL);
        ll_notification.addView(ll_notification_1, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48));

        AppCompatTextView tv_1 = new AppCompatTextView(context);
        tv_1.setText("应用内播放提醒音");
        tv_1.setTextSize(15f);
        tv_1.setTextColor(Color.BLACK);
        tv_1.setGravity(Gravity.CENTER_VERTICAL);
        ll_notification_1.addView(tv_1, LayoutHelper.createLinear(0, LayoutHelper.MATCH_PARENT, 1f, Gravity.CENTER_VERTICAL, 12, 0, 0, 0));

        sb_app_tip = new SwitchButton(context);
        sb_app_tip.setBackDrawableRes(R.drawable.miui_back_drawable);
        sb_app_tip.setThumbColorRes(R.drawable.custom_thumb_color);
        sb_app_tip.setClickable(false);
        ll_notification_1.addView(sb_app_tip, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL, 0, 8, 12, 8));

        ll_notification_1.setOnClickListener(view -> {
            SharedPreferences preferences = MessagesController.getNotificationsSettings(currentAccount);
            SharedPreferences.Editor editor = preferences.edit();
            enabled = preferences.getBoolean("EnableInAppSounds", true);
            editor.putBoolean("EnableInAppSounds", !enabled);
            editor.commit();

            sb_app_tip.setChecked(!enabled);
        });

        View view_line1 = new View(context);
        view_line1.setBackgroundColor(Color.parseColor("#F4F4F4"));
        ll_notification.addView(view_line1, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 1, 12, 0, 12, 0));

        // 消息预览
        LinearLayout ll_notification_2 = new LinearLayout(context);
        ll_notification_2.setOrientation(LinearLayout.HORIZONTAL);
        ll_notification.addView(ll_notification_2, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48));

        AppCompatTextView tv_2 = new AppCompatTextView(context);
        tv_2.setText("应用内振动提醒");
        tv_2.setTextSize(15f);
        tv_2.setTextColor(Color.BLACK);
        tv_2.setGravity(Gravity.CENTER_VERTICAL);
        ll_notification_2.addView(tv_2, LayoutHelper.createLinear(0, LayoutHelper.MATCH_PARENT, 1f, Gravity.CENTER_VERTICAL, 12, 0, 0, 0));

        sb_shock = new SwitchButton(context);
        sb_shock.setBackDrawableRes(R.drawable.miui_back_drawable);
        sb_shock.setThumbColorRes(R.drawable.custom_thumb_color);
        sb_shock.setClickable(false);
        ll_notification_2.addView(sb_shock, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL, 0, 8, 12, 8));
        ll_notification_2.setOnClickListener(view -> {
            SharedPreferences preferences = MessagesController.getNotificationsSettings(currentAccount);
            SharedPreferences.Editor editor = preferences.edit();
            enabled = preferences.getBoolean("EnableInAppVibrate", true);
            editor.putBoolean("EnableInAppVibrate", !enabled);
            editor.commit();

            sb_shock.setChecked(!enabled);
        });

        View view_line2 = new View(context);
        view_line2.setBackgroundColor(Color.parseColor("#F4F4F4"));
        ll_notification.addView(view_line2, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 1, 12, 0, 12, 0));

        // 消息预览
        LinearLayout ll_notification_3 = new LinearLayout(context);
        ll_notification_3.setOrientation(LinearLayout.HORIZONTAL);
        ll_notification.addView(ll_notification_3, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48));

        AppCompatTextView tv_3 = new AppCompatTextView(context);
        tv_3.setText("应用内消息预览");
        tv_3.setTextSize(15f);
        tv_3.setTextColor(Color.BLACK);
        tv_3.setGravity(Gravity.CENTER_VERTICAL);
        ll_notification_3.addView(tv_3, LayoutHelper.createLinear(0, LayoutHelper.MATCH_PARENT, 1f, Gravity.CENTER_VERTICAL, 12, 0, 0, 0));

        sb_app_msg_preview = new SwitchButton(context);
        sb_app_msg_preview.setBackDrawableRes(R.drawable.miui_back_drawable);
        sb_app_msg_preview.setThumbColorRes(R.drawable.custom_thumb_color);
        sb_app_msg_preview.setClickable(false);
        ll_notification_3.addView(sb_app_msg_preview, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL, 0, 8, 12, 8));
        ll_notification_3.setOnClickListener(view -> {
            SharedPreferences preferences = MessagesController.getNotificationsSettings(currentAccount);
            SharedPreferences.Editor editor = preferences.edit();
            enabled = preferences.getBoolean("EnableInAppPreview", true);
            editor.putBoolean("EnableInAppPreview", !enabled);
            editor.commit();

            sb_app_msg_preview.setChecked(!enabled);
        });
    }

    private void createChannel(Context context) {
        AppCompatTextView tv_notification = new AppCompatTextView(context);
        tv_notification.setText("频道通知");
        tv_notification.setGravity(Gravity.CENTER_VERTICAL);
        contentView.addView(tv_notification, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, AndroidUtilities.dp(16), 12, 0, 12, 0));

        RoundLinearLayout ll_notification = new RoundLinearLayout(context);
        ll_notification.setBackgroundColor(Color.WHITE);
        ll_notification.setRadius(6f);
        ll_notification.setOrientation(LinearLayout.VERTICAL);
        contentView.addView(ll_notification, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 12, 0, 12, 0));

        // 消息通知
        LinearLayout ll_notification_1 = new LinearLayout(context);
        ll_notification_1.setOrientation(LinearLayout.HORIZONTAL);
        ll_notification.addView(ll_notification_1, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48));

        AppCompatTextView tv_1 = new AppCompatTextView(context);
        tv_1.setText("显示通知");
        tv_1.setTextSize(15f);
        tv_1.setTextColor(Color.BLACK);
        tv_1.setGravity(Gravity.CENTER_VERTICAL);
        ll_notification_1.addView(tv_1, LayoutHelper.createLinear(0, LayoutHelper.MATCH_PARENT, 1f, Gravity.CENTER_VERTICAL, 12, 0, 0, 0));

        sb_channel = new SwitchButton(context);
        sb_channel.setBackDrawableRes(R.drawable.miui_back_drawable);
        sb_channel.setThumbColorRes(R.drawable.custom_thumb_color);
        sb_channel.setClickable(false);
        ll_notification_1.addView(sb_channel, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL, 0, 8, 12, 8));

        ll_notification_1.setOnClickListener(view -> {
            if (sb_channel.isChecked()) {
                AlertsCreator.showCustomNotificationsDialog(NotificationAndSoundActivity.this, 0, 0, 2, new ArrayList<>(), null, currentAccount, param -> {
                    sb_channel.setChecked(getNotificationsController().isGlobalNotificationsEnabled(2));
                });
            } else {
                getNotificationsController().setGlobalNotificationsEnabled(2, 0);
                updateView();
            }
        });

        View view_line1 = new View(context);
        view_line1.setBackgroundColor(Color.parseColor("#F4F4F4"));
        ll_notification.addView(view_line1, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 1, 12, 0, 12, 0));

        // 消息预览
        LinearLayout ll_notification_2 = new LinearLayout(context);
        ll_notification_2.setOrientation(LinearLayout.HORIZONTAL);
        ll_notification.addView(ll_notification_2, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48));

        AppCompatTextView tv_2 = new AppCompatTextView(context);
        tv_2.setText("消息预览");
        tv_2.setTextSize(15f);
        tv_2.setTextColor(Color.BLACK);
        tv_2.setGravity(Gravity.CENTER_VERTICAL);
        ll_notification_2.addView(tv_2, LayoutHelper.createLinear(0, LayoutHelper.MATCH_PARENT, 1f, Gravity.CENTER_VERTICAL, 12, 0, 0, 0));

        sb_channel_preview = new SwitchButton(context);
        sb_channel_preview.setBackDrawableRes(R.drawable.miui_back_drawable);
        sb_channel_preview.setThumbColorRes(R.drawable.custom_thumb_color);
        sb_channel_preview.setClickable(false);
        ll_notification_2.addView(sb_channel_preview, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL, 0, 8, 12, 8));
        ll_notification_2.setOnClickListener(view -> {
            SharedPreferences preferences = getNotificationsSettings();
            SharedPreferences.Editor editor = preferences.edit();
            enabled = preferences.getBoolean("EnablePreviewChannel", true);
            editor.putBoolean("EnablePreviewChannel", !enabled);

            editor.commit();
            getNotificationsController().updateServerNotificationsSettings(2);

            sb_channel_preview.setChecked(!enabled);
        });

        View view_line2 = new View(context);
        view_line2.setBackgroundColor(Color.parseColor("#F4F4F4"));
        ll_notification.addView(view_line2, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 1, 12, 0, 12, 0));

        // 声音
        LinearLayout ll_notification_3 = new LinearLayout(context);
        ll_notification_3.setOrientation(LinearLayout.HORIZONTAL);
        ll_notification.addView(ll_notification_3, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48));

        AppCompatTextView tv_3 = new AppCompatTextView(context);
        tv_3.setText("声音");
        tv_3.setTextSize(15f);
        tv_3.setTextColor(Color.BLACK);
        tv_3.setGravity(Gravity.CENTER_VERTICAL);
        ll_notification_3.addView(tv_3, LayoutHelper.createLinear(0, LayoutHelper.MATCH_PARENT, 1f, Gravity.CENTER_VERTICAL, 12, 0, 0, 0));

        tv_sound_channel = new AppCompatTextView(context);
        tv_sound_channel.setTextSize(15f);
        tv_sound_channel.setTextColor(Color.BLACK);
        tv_sound_channel.setGravity(Gravity.CENTER_VERTICAL);
        ll_notification_3.addView(tv_sound_channel, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.MATCH_PARENT, Gravity.CENTER_VERTICAL, 0, 0, 0, 0));

        AppCompatImageView imageView = new AppCompatImageView(context);
        imageView.setImageResource(R.drawable.call_arrow_right);
        imageView.setColorFilter(Color.BLACK);
        ll_notification_3.addView(imageView, LayoutHelper.createLinear(18, 18, Gravity.CENTER_VERTICAL, 0, 0, 12, 0));

        ll_notification_3.setOnClickListener(view -> {
            Bundle bundle = new Bundle();
            bundle.putInt("type", 2);
            presentFragment(new NotificationsSoundActivity(bundle, getResourceProvider()));
        });
    }

    private void createGroup(Context context) {
        AppCompatTextView tv_notification = new AppCompatTextView(context);
        tv_notification.setText("群消息通知");
        tv_notification.setGravity(Gravity.CENTER_VERTICAL);
        contentView.addView(tv_notification, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, AndroidUtilities.dp(16), 12, 0, 12, 0));

        RoundLinearLayout ll_notification = new RoundLinearLayout(context);
        ll_notification.setBackgroundColor(Color.WHITE);
        ll_notification.setRadius(6f);
        ll_notification.setOrientation(LinearLayout.VERTICAL);
        contentView.addView(ll_notification, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 12, 0, 12, 0));

        // 消息通知
        LinearLayout ll_notification_1 = new LinearLayout(context);
        ll_notification_1.setOrientation(LinearLayout.HORIZONTAL);
        ll_notification.addView(ll_notification_1, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48));

        AppCompatTextView tv_1 = new AppCompatTextView(context);
        tv_1.setText("显示通知");
        tv_1.setTextSize(15f);
        tv_1.setTextColor(Color.BLACK);
        tv_1.setGravity(Gravity.CENTER_VERTICAL);
        ll_notification_1.addView(tv_1, LayoutHelper.createLinear(0, LayoutHelper.MATCH_PARENT, 1f, Gravity.CENTER_VERTICAL, 12, 0, 0, 0));

        sb_group = new SwitchButton(context);
        sb_group.setBackDrawableRes(R.drawable.miui_back_drawable);
        sb_group.setThumbColorRes(R.drawable.custom_thumb_color);
        sb_group.setClickable(false);
        ll_notification_1.addView(sb_group, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL, 0, 8, 12, 8));

        ll_notification_1.setOnClickListener(view -> {
            if (sb_group.isChecked()) {
                AlertsCreator.showCustomNotificationsDialog(NotificationAndSoundActivity.this, 0, 0, 0, new ArrayList<>(), null, currentAccount, param -> {
                    sb_group.setChecked(getNotificationsController().isGlobalNotificationsEnabled(0));
                });
            } else {
                getNotificationsController().setGlobalNotificationsEnabled(0, 0);
                updateView();
            }
        });

        View view_line1 = new View(context);
        view_line1.setBackgroundColor(Color.parseColor("#F4F4F4"));
        ll_notification.addView(view_line1, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 1, 12, 0, 12, 0));

        // 消息预览
        LinearLayout ll_notification_2 = new LinearLayout(context);
        ll_notification_2.setOrientation(LinearLayout.HORIZONTAL);
        ll_notification.addView(ll_notification_2, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48));

        AppCompatTextView tv_2 = new AppCompatTextView(context);
        tv_2.setText("消息预览");
        tv_2.setTextSize(15f);
        tv_2.setTextColor(Color.BLACK);
        tv_2.setGravity(Gravity.CENTER_VERTICAL);
        ll_notification_2.addView(tv_2, LayoutHelper.createLinear(0, LayoutHelper.MATCH_PARENT, 1f, Gravity.CENTER_VERTICAL, 12, 0, 0, 0));

        sb_group_preview = new SwitchButton(context);
        sb_group_preview.setBackDrawableRes(R.drawable.miui_back_drawable);
        sb_group_preview.setThumbColorRes(R.drawable.custom_thumb_color);
        sb_group_preview.setClickable(false);
        ll_notification_2.addView(sb_group_preview, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL, 0, 8, 12, 8));
        ll_notification_2.setOnClickListener(view -> {
            SharedPreferences preferences = getNotificationsSettings();
            SharedPreferences.Editor editor = preferences.edit();
            enabled = preferences.getBoolean("EnablePreviewGroup", true);
            editor.putBoolean("EnablePreviewGroup", !enabled);

            editor.commit();
            getNotificationsController().updateServerNotificationsSettings(1);

            sb_group_preview.setChecked(!enabled);
        });

        View view_line2 = new View(context);
        view_line2.setBackgroundColor(Color.parseColor("#F4F4F4"));
        ll_notification.addView(view_line2, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 1, 12, 0, 12, 0));

        // 声音
        LinearLayout ll_notification_3 = new LinearLayout(context);
        ll_notification_3.setOrientation(LinearLayout.HORIZONTAL);
        ll_notification.addView(ll_notification_3, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48));

        AppCompatTextView tv_3 = new AppCompatTextView(context);
        tv_3.setText("声音");
        tv_3.setTextSize(15f);
        tv_3.setTextColor(Color.BLACK);
        tv_3.setGravity(Gravity.CENTER_VERTICAL);
        ll_notification_3.addView(tv_3, LayoutHelper.createLinear(0, LayoutHelper.MATCH_PARENT, 1f, Gravity.CENTER_VERTICAL, 12, 0, 0, 0));

        tv_sound_group = new AppCompatTextView(context);
        tv_sound_group.setTextSize(15f);
        tv_sound_group.setTextColor(Color.BLACK);
        tv_sound_group.setGravity(Gravity.CENTER_VERTICAL);
        ll_notification_3.addView(tv_sound_group, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.MATCH_PARENT, Gravity.CENTER_VERTICAL, 0, 0, 0, 0));

        AppCompatImageView imageView = new AppCompatImageView(context);
        imageView.setImageResource(R.drawable.call_arrow_right);
        imageView.setColorFilter(Color.BLACK);
        ll_notification_3.addView(imageView, LayoutHelper.createLinear(18, 18, Gravity.CENTER_VERTICAL, 0, 0, 12, 0));

        ll_notification_3.setOnClickListener(view -> {
            Bundle bundle = new Bundle();
            bundle.putInt("type", 0);
            presentFragment(new NotificationsSoundActivity(bundle, getResourceProvider()));
        });
    }

    private String getSound(int currentType) {
        final SharedPreferences prefs = getNotificationsSettings();
        String value = getString("SoundDefault", R.string.SoundDefault);
        long documentId;
        switch (currentType) {
            case TYPE_PRIVATE:
                value = prefs.getString("GlobalSound", value);
                documentId = prefs.getLong("GlobalSoundDocId", 0);
                break;
            case TYPE_GROUP:
                value = prefs.getString("GroupSound", value);
                documentId = prefs.getLong("GroupSoundDocId", 0);
                break;
            case TYPE_REACTIONS_MESSAGES:
            case TYPE_REACTIONS_STORIES:
                value = prefs.getString("ReactionSound", value);
                documentId = prefs.getLong("ReactionSoundDocId", 0);
                break;
            case TYPE_STORIES:
                value = prefs.getString("StoriesSound", value);
                documentId = prefs.getLong("StoriesSoundDocId", 0);
                break;
            case TYPE_CHANNEL:
            default:
                value = prefs.getString("ChannelSound", value);
                documentId = prefs.getLong("ChannelDocId", 0);
        }
        if (documentId != 0) {
            TLRPC.Document document = getMediaDataController().ringtoneDataStore.getDocument(documentId);
            if (document == null) {
                return getString("CustomSound", R.string.CustomSound);
            } else {
                return NotificationsSoundActivity.trimTitle(document, FileLoader.getDocumentFileName(document));
            }
        } else if (value.equals("NoSound")) {
            return getString("NoSound", R.string.NoSound);
        } else if (value.equals("Default")) {
            return getString("SoundDefault", R.string.SoundDefault);
        }
        return value;
    }

    private void updateView() {
        int currentTime = ConnectionsManager.getInstance(currentAccount).getCurrentTime();
        SharedPreferences prefs = MessagesController.getNotificationsSettings(currentAccount);

        int offUntil = prefs.getInt("EnableAll2", 0);
        enabled = offUntil < currentTime;
        sb1.setChecked(enabled);

        enabled = prefs.getBoolean("EnablePreviewAll", true);
        sb2.setChecked(enabled);

        offUntil = prefs.getInt("EnableGroup2", 0);
        enabled = offUntil < currentTime;
        sb_group.setChecked(enabled);

        enabled = prefs.getBoolean("EnablePreviewGroup", true);
        sb_group_preview.setChecked(enabled);

        offUntil = prefs.getInt("EnableChannel2", 0);
        enabled = offUntil < currentTime;
        sb_channel.setChecked(enabled);

        enabled = prefs.getBoolean("EnablePreviewChannel", true);
        sb_channel_preview.setChecked(enabled);

        enabled = prefs.getBoolean("EnableInAppSounds", true);
        sb_app_tip.setChecked(enabled);

        enabled = prefs.getBoolean("EnableInAppVibrate", true);
        sb_shock.setChecked(enabled);

        enabled = prefs.getBoolean("EnableInAppPreview", true);
        sb_app_msg_preview.setChecked(enabled);

        tv_sound.setText(getSound(1));
        tv_sound_group.setText(getSound(0));
        tv_sound_channel.setText(getSound(2));
    }

    @Override
    public void onResume() {
        super.onResume();
        getNotificationCenter().addObserver(this, NotificationCenter.notificationsSettingsUpdated);
    }

    @Override
    public void onPause() {
        super.onPause();
        getNotificationCenter().removeObserver(this, NotificationCenter.notificationsSettingsUpdated);
    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == NotificationCenter.notificationsSettingsUpdated) {
            updateView();
        }
    }
}
