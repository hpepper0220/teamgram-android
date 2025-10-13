package org.telegram.ext;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.Emoji;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.SimpleTextView;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.FiltersSetupActivity;
import org.telegram.ui.LaunchActivity;
import org.telegram.ui.LogoutActivity;
import org.telegram.ui.NotificationsSettingsActivity;
import org.telegram.ui.ProfileActivity;

public class SkMineFragment extends BaseFragment implements NotificationCenter.NotificationCenterDelegate {

    private LaunchActivity mParentActivity;
    private LinearLayout mHeaderInfoLayout;
    private int headerLayoutHeight = 140;
    private int bottomMargin = 14;
    // 用户头像
    private BackupImageView avatarImageView;
    // 用户名
    private SimpleTextView nameTextView;
    // 用户在线状态
    private SimpleTextView onlineTextView;
    private AppCompatImageView arrowRightImageView;

    private FrameLayout menuContainer;
    private LinearLayout menuItemsLayout;

    private TextCell modifyPasswordRow;
    private TextCell notificationRow;
    private TextCell filtersRow;
    private TextCell walletRow;
    private TextInfoPrivacyCell skgPrivacyCell;

    private FrameLayout frameLayout;
    private RecyclerView recyclerView;
    private MineFragmentItemAdapter adapter;

    private TLRPC.User latestUser;

    private Context context;

    private boolean isDark;
    private boolean autoRegister = false;

    @Override
    public boolean onFragmentCreate() {
        latestUser = MessagesController.getInstance(currentAccount).getUser(UserConfig.getInstance(currentAccount).getClientUserId());
        getNotificationCenter().addObserver(this, NotificationCenter.updateInterfaces);
        getNotificationCenter().addObserver(this, NotificationCenter.needSetDayNightTheme);
        return null != latestUser;
    }

    @Override
    public View createView(Context context) {
        isDark = Theme.isCurrentThemeDark();

        this.context = context;
        frameLayout = new FrameLayout(context);
        frameLayout.setLayoutParams(LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        recyclerView = new RecyclerView(context);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(recyclerView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 0, 1f));

        LoadingButton signOutButton = new LoadingButton(context);
        signOutButton.setText("退出登录");
        signOutButton.setOnClickListener(view -> makeLogOutDialog(context, currentAccount).show());
        linearLayout.addView(signOutButton, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 46, Gravity.CENTER_HORIZONTAL, 36, 30, 36, 36));

        frameLayout.addView(linearLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        adapter = new MineFragmentItemAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);

//        createHeaderLayout();
//        createMenuLayout();
//        createPrivacyLayout();
//
//        setUser();
        fragmentView = frameLayout;
        return fragmentView;
    }

    public AlertDialog makeLogOutDialog(Context context, int currentAccount) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("您确定要退出登录吗？\n\n您可以同时在所有设备上无缝地使用。\n\n请记住，退出登录时会清除所有的加密聊天记录。");
        builder.setTitle("退出登录");
        builder.setPositiveButton("退出登录", (dialogInterface, i) -> MessagesController.getInstance(currentAccount).performLogout(1));
        builder.setNegativeButton("取消", null);
        AlertDialog alertDialog = builder.create();
        TextView button = (TextView) alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        if (button != null) {
            button.setTextColor(Color.RED);
        }
        return alertDialog;
    }

    private void createPrivacyLayout() {
        skgPrivacyCell = new TextInfoPrivacyCell(context, 10, null);
        skgPrivacyCell.getTextView().setGravity(Gravity.CENTER_HORIZONTAL);
        skgPrivacyCell.getTextView().setTextColor(getThemedColor(Theme.key_windowBackgroundWhiteGrayText3));
        skgPrivacyCell.getTextView().setMovementMethod(null);
        try {
            PackageInfo pInfo = ApplicationLoader.applicationContext.getPackageManager().getPackageInfo(ApplicationLoader.applicationContext.getPackageName(), 0);
//            int code = pInfo.versionCode / 10;
//            String abi = "";
//            switch (pInfo.versionCode % 10) {
//                case 1:
//                case 2:
//                    abi = "store bundled " + Build.CPU_ABI + " " + Build.CPU_ABI2;
//                    break;
//                default:
//                case 9:
//                    if (BuildVars.isStandaloneApp()) {
//                        abi = "direct " + Build.CPU_ABI + " " + Build.CPU_ABI2;
//                    } else {
//                        abi = "universal " + Build.CPU_ABI + " " + Build.CPU_ABI2;
//                    }
//                    break;
//            }
            pInfo.versionName = "3.0.0";
//            skgPrivacyCell.setText(LocaleController.formatString("TelegramVersion", R.string.TelegramVersion, pInfo.versionName).replace("Skgram", ApplicationLoader.flavor.getAppName()));
        } catch (Exception e) {
            FileLog.e(e);
        }
        skgPrivacyCell.getTextView().setPadding(AndroidUtilities.dp(6), AndroidUtilities.dp(14), AndroidUtilities.dp(6), AndroidUtilities.dp(4));

//        frameLayout.addView(skgPrivacyCell, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.BOTTOM, 0, 0, 0, 20));
    }

    private void createMenuLayout() {
        menuContainer = new FrameLayout(context);
        menuContainer.setPadding(0, 20, 0, 0);
        menuContainer.setBackgroundColor(getThemedColor(Theme.key_windowBackgroundGray));
        menuContainer.setLayoutParams(LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP, 0, headerLayoutHeight, 0, 0));

        menuItemsLayout = new LinearLayout(context);
        menuItemsLayout.setOrientation(LinearLayout.VERTICAL);
        menuItemsLayout.setBackgroundColor(getThemedColor(Theme.key_windowBackgroundWhite));

        notificationRow = new TextCell(context);
        notificationRow.setEnabled(true);
        notificationRow.setColors(Theme.key_windowBackgroundWhiteGrayIcon, Theme.key_windowBackgroundWhiteBlackText);
        notificationRow.setTextAndIcon(LocaleController.getString("NotificationsAndSounds", R.string.NotificationsAndSounds), R.drawable.msg_notifications, true);
        menuItemsLayout.addView(notificationRow);
        notificationRow.setBackground(ContextCompat.getDrawable(context, isDark ? R.drawable.sk_ripple_dark : R.drawable.sk_ripple));
        notificationRow.setOnClickListener(v -> mParentActivity.presentFragment(new NotificationsSettingsActivity()));

        filtersRow = new TextCell(context);
        filtersRow.setEnabled(true);
        filtersRow.setColors(Theme.key_windowBackgroundWhiteGrayIcon, Theme.key_windowBackgroundWhiteBlackText);
        filtersRow.setTextAndIcon(LocaleController.getString("Filters", R.string.Filters), R.drawable.msg_folders, true);
        menuItemsLayout.addView(filtersRow);
        filtersRow.setBackground(ContextCompat.getDrawable(context, isDark ? R.drawable.sk_ripple_dark : R.drawable.sk_ripple));
        filtersRow.setOnClickListener(v -> mParentActivity.presentFragment(new FiltersSetupActivity()));

        menuContainer.addView(menuItemsLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

//        frameLayout.addView(menuContainer);
    }

    private void createHeaderLayout() {
        mHeaderInfoLayout = new LinearLayout(context);
        mHeaderInfoLayout.setOrientation(LinearLayout.HORIZONTAL);
        mHeaderInfoLayout.setLayoutParams(LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, headerLayoutHeight));
//        mHeaderInfoLayout.setBackgroundColor(getThemedColor(Theme.key_avatar_backgroundActionBarBlue));
        mHeaderInfoLayout.setBackgroundColor(Color.WHITE);

        avatarImageView = new BackupImageView(context);
        avatarImageView.getImageReceiver().setRoundRadius(AndroidUtilities.dp(32));
        mHeaderInfoLayout.addView(avatarImageView, LayoutHelper.createLinear(64, 64, Gravity.LEFT | Gravity.BOTTOM, 16, 0, 0, bottomMargin));
//        avatarImageView.setOnClickListener(v -> mParentActivity.presentFragment(new SKEditProfileActivity()));

        LinearLayout subLinearLayout = new LinearLayout(context);
        subLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
        subLinearLayout.setGravity(Gravity.CENTER_VERTICAL);
        subLinearLayout.setLayoutParams(LayoutHelper.createLinear(0, 64, 1, Gravity.LEFT | Gravity.BOTTOM, 0, 0, 0, bottomMargin));
        mHeaderInfoLayout.addView(subLinearLayout);

        subLinearLayout.setOnClickListener(view -> {
            Bundle args = new Bundle();
            args.putLong("user_id", UserConfig.getInstance(currentAccount).clientUserId);
//            mParentActivity.presentFragment(new ProfileActivity(args));
            EditUserInfoFragment fragment = new EditUserInfoFragment();
            fragment.setParentActivity(mParentActivity);
            mParentActivity.presentFragment(fragment);
        });

        LinearLayout nameOnlineLayout = new LinearLayout(context);
        nameOnlineLayout.setOrientation(LinearLayout.VERTICAL);
        nameOnlineLayout.setGravity(Gravity.CENTER_VERTICAL);
        nameOnlineLayout.setLayoutParams(LayoutHelper.createLinear(0, LayoutHelper.MATCH_PARENT, 1, Gravity.LEFT | Gravity.CENTER_VERTICAL, 0, 0, 0, 0));
        subLinearLayout.addView(nameOnlineLayout);

        nameTextView = new SimpleTextView(context);
        nameTextView.setTextSize(20);
        nameTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        nameTextView.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
//        nameTextView.setTextColor(Theme.getColor(Theme.key_profile_title));
        nameTextView.setTextColor(Color.BLACK);
        nameOnlineLayout.addView(nameTextView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.CENTER_VERTICAL, 16, 0, 0, 0));

        onlineTextView = new SimpleTextView(context);
        onlineTextView.setEllipsizeByGradient(true);
//        onlineTextView.setTextColor(getThemedColor(Theme.key_avatar_subtitleInProfileBlue));
        onlineTextView.setTextColor(Color.parseColor("#AAAAAA"));
        onlineTextView.setTextSize(14);
        onlineTextView.setGravity(Gravity.LEFT);
        nameOnlineLayout.addView(onlineTextView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.CENTER_VERTICAL, 16, 8, 0, 0));

        arrowRightImageView = new AppCompatImageView(context);
        Drawable drawable = ContextCompat.getDrawable(context, R.drawable.call_arrow_right);
        if (null != drawable && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            drawable.setTint(Color.BLACK);
        }
        arrowRightImageView.setImageDrawable(drawable);
        arrowRightImageView.setPadding(10, 6, 10, 6);
        subLinearLayout.addView(arrowRightImageView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.RIGHT | Gravity.CENTER_VERTICAL, 0, 0, 6, 0));

//        frameLayout.addView(mHeaderInfoLayout);
    }

    private void setUser() {
        String newString2 = null;
        if (latestUser.id == getUserConfig().getClientUserId()) {
            newString2 = LocaleController.getString("Online", R.string.Online);
        } else if (latestUser.id == 333000 || latestUser.id == 777000 || latestUser.id == 42777) {
            newString2 = LocaleController.getString("ServiceNotifications", R.string.ServiceNotifications);
        } else if (MessagesController.isSupportUser(latestUser)) {
            newString2 = LocaleController.getString("SupportStatus", R.string.SupportStatus);
        }

        AvatarDrawable avatarDrawable = new AvatarDrawable(latestUser);
        avatarDrawable.setColor(Theme.getColor(Theme.key_avatar_backgroundInProfileBlue));
        if (null != avatarImageView) {
            avatarImageView.setForUserOrChat(latestUser, avatarDrawable);
        }

        CharSequence text = UserObject.getUserName(latestUser);
        try {
            text = Emoji.replaceEmoji(text, nameTextView.getPaint().getFontMetricsInt(), false, AndroidUtilities.dp(22));
        } catch (Exception ignore) {
        }
        if (null != nameTextView) {
            nameTextView.setText(text);
        }
        if (null != onlineTextView) {
            onlineTextView.setText(newString2);
        }
    }

    public void setParentActivity(LaunchActivity parentActivity) {
        this.mParentActivity = parentActivity;
    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == NotificationCenter.updateInterfaces) {
            int mask = (Integer) args[0];
            boolean infoChanged = (mask & MessagesController.UPDATE_MASK_AVATAR) != 0 || (mask & MessagesController.UPDATE_MASK_NAME) != 0 || (mask & MessagesController.UPDATE_MASK_STATUS) != 0 || (mask & MessagesController.UPDATE_MASK_EMOJI_STATUS) != 0;
            if (infoChanged) {
                latestUser = getMessagesController().getUser(UserConfig.getInstance(currentAccount).getClientUserId());
//                setUser();
                if (null != adapter) {
                    setUser();
                }
            }
        } else if (id == NotificationCenter.needSetDayNightTheme) {
            Theme.ThemeInfo theme = (Theme.ThemeInfo) args[0];
            boolean isDark = theme.isDark();
            if (isDark && null != adapter && null != recyclerView) {
                this.isDark = true;
                recyclerView.setAdapter(adapter);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getWalletInfo();
    }

    private void getWalletInfo() {
//        TGRepository.getInstance().getWallet(currentAccount, classGuid, new TGRepository.TGRequestCallback<TLRPC.TL_wallet>() {
//            @Override
//            public void onSuccess(TLRPC.TL_wallet response) {
//                @SuppressLint("DefaultLocale") String balanceValue = String.format("%.2f", response.balance);
//                if (null != walletCell) {
//                    walletCell.setData(String.format(LocaleController.getString("MyWalletBalance", R.string.MyWalletBalance), balanceValue));
//                }
//            }
//
//            @Override
//            public void onError(TLRPC.TL_error error) {
//                if (null != walletCell) {
//                    walletCell.setData(String.format(LocaleController.getString("MyWalletBalance", R.string.MyWalletBalance), "0.00"));
//                }
//            }
//        });
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        getNotificationCenter().removeObserver(this, NotificationCenter.updateInterfaces);
        getNotificationCenter().removeObserver(this, NotificationCenter.needSetDayNightTheme);
    }

    // todo 后期优化 -> RecyclerListView.SelectionAdapter
    private class MineFragmentItemAdapter extends RecyclerView.Adapter<MineFragmentItemAdapter.SKViewHolder> {

        @NonNull
        @Override
        public SKViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = null;
            if (autoRegister) {
                switch (viewType) {
                    case 0:
                        createHeaderLayout();
                        view = mHeaderInfoLayout;
                        break;
                    case 1:
                        View dividerView = new View(context);
                        dividerView.setLayoutParams(LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 6));
                        dividerView.setBackgroundColor(getThemedColor(Theme.key_windowBackgroundGray));
                        view = dividerView;
                        break;
                    case 2:
//                        walletCell = new WalletCell(context);
//                        walletCell.setLayoutParams(LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
//                        walletCell.setOnClickListener(v -> mParentActivity.presentFragment(new MyWalletActivity()));
//                        view = walletCell;
                        break;
                    case 3:
                        View dividerView1 = new View(context);
                        dividerView1.setLayoutParams(LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 6));
                        dividerView1.setBackgroundColor(getThemedColor(Theme.key_windowBackgroundGray));
                        view = dividerView1;
                        break;
                    case 4:
                        notificationRow = new TextCell(context);
                        notificationRow.setEnabled(true);
                        notificationRow.setColors(Theme.key_windowBackgroundWhiteGrayIcon, Theme.key_windowBackgroundWhiteBlackText);
                        notificationRow.setTextAndIcon(LocaleController.getString("NotificationsAndSounds", R.string.NotificationsAndSounds), R.drawable.msg_notifications, true);
                        notificationRow.setBackground(ContextCompat.getDrawable(context, isDark ? R.drawable.sk_ripple_dark : R.drawable.sk_ripple));
                        notificationRow.setOnClickListener(v -> mParentActivity.presentFragment(new NotificationsSettingsActivity()));
                        view = notificationRow;
                        break;
                    case 5:
                        filtersRow = new TextCell(context);
                        filtersRow.setEnabled(true);
                        filtersRow.setColors(Theme.key_windowBackgroundWhiteGrayIcon, Theme.key_windowBackgroundWhiteBlackText);
                        filtersRow.setTextAndIcon(LocaleController.getString("Filters", R.string.Filters), R.drawable.msg_folders, true);
                        filtersRow.setBackground(ContextCompat.getDrawable(context, isDark ? R.drawable.sk_ripple_dark : R.drawable.sk_ripple));
                        filtersRow.setOnClickListener(v -> mParentActivity.presentFragment(new FiltersSetupActivity()));
                        view = filtersRow;
                        break;
//                    case 5:
//                        walletRow = new TextCell(context);
//                        walletRow.setEnabled(true);
//                        walletRow.setColors(Theme.key_windowBackgroundWhiteGrayIcon, Theme.key_windowBackgroundWhiteBlackText);
//                        walletRow.setTextAndIcon("钱包", R.drawable.msg_folders, true);
//                        walletRow.setBackground(ContextCompat.getDrawable(context, isDark ? R.drawable.sk_ripple_dark : R.drawable.sk_ripple));
//                        walletRow.setOnClickListener(v -> mParentActivity.presentFragment(new MyWalletActivity()));
//                        view = walletRow;
//                        break;
                    case 6:
                        FrameLayout frameLayout = new FrameLayout(context) {
                            @Override
                            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                                int height;
                                height = MeasureSpec.getSize(heightMeasureSpec);
                                if (height == 0) {
                                    height = parent.getMeasuredHeight();
                                }
                                height = AndroidUtilities.displaySize.y - AndroidUtilities.dp(headerLayoutHeight) - AndroidUtilities.dp(28);
                                int cellHeight = AndroidUtilities.dp(48) * 2 + AndroidUtilities.dp(70);
                                height = height - cellHeight - AndroidUtilities.dp(45);
                                super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
                            }
                        };
//                    frameLayout.setBackgroundColor(getThemedColor(Theme.key_windowBackgroundGray));
                        createPrivacyLayout();
                        frameLayout.addView(skgPrivacyCell, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.BOTTOM, 0, 0, 0, 20));
                        view = frameLayout;
                        break;
                }
            } else {
                switch (viewType) {
                    case 0:
                        createHeaderLayout();
                        view = mHeaderInfoLayout;
                        break;
                    case 1:
                        View dividerView = new View(context);
                        dividerView.setLayoutParams(LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 6));
                        dividerView.setBackgroundColor(getThemedColor(Theme.key_windowBackgroundGray));
                        view = dividerView;
                        break;
//                    case 2:
//                        walletCell = new WalletCell(context);
//                        walletCell.setLayoutParams(LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
//                        walletCell.setOnClickListener(v -> mParentActivity.presentFragment(new MyWalletActivity()));
//                        view = walletCell;
//                        break;
                    case 2:
                        View dividerView1 = new View(context);
                        dividerView1.setLayoutParams(LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 6));
                        dividerView1.setBackgroundColor(getThemedColor(Theme.key_windowBackgroundGray));
                        view = dividerView1;
                        break;
//                    case 3:
//                        modifyPasswordRow = new TextCell(context);
//                        modifyPasswordRow.setEnabled(true);
//                        modifyPasswordRow.setColors(Theme.key_windowBackgroundWhiteGrayIcon, Theme.key_windowBackgroundWhiteBlackText);
//                        modifyPasswordRow.setTextAndIcon("修改密码", R.drawable.msg_permissions, true);
////                    menuItemsLayout.addView(notificationRow);
//                        modifyPasswordRow.setBackground(ContextCompat.getDrawable(context, isDark ? R.drawable.sk_ripple_dark : R.drawable.sk_ripple));
////                        modifyPasswordRow.setOnClickListener(v -> mParentActivity.presentFragment(new ModifyPasswordFragment()));
//                        view = modifyPasswordRow;
//                        break;
                    case 3:
                        notificationRow = new TextCell(context);
                        notificationRow.setEnabled(true);
                        notificationRow.setColors(Theme.key_windowBackgroundWhiteGrayIcon, Theme.key_windowBackgroundWhiteBlackText);
                        notificationRow.setTextAndIcon(LocaleController.getString("NotificationsAndSounds", R.string.NotificationsAndSounds), R.drawable.msg_notifications, true);
//                    menuItemsLayout.addView(notificationRow);
                        notificationRow.setBackground(ContextCompat.getDrawable(context, isDark ? R.drawable.sk_ripple_dark : R.drawable.sk_ripple));
                        notificationRow.setOnClickListener(v -> mParentActivity.presentFragment(new NotificationsSettingsActivity()));
                        view = notificationRow;
                        break;
                    case 4:
                        filtersRow = new TextCell(context);
                        filtersRow.setEnabled(true);
                        filtersRow.setColors(Theme.key_windowBackgroundWhiteGrayIcon, Theme.key_windowBackgroundWhiteBlackText);
                        filtersRow.setTextAndIcon(LocaleController.getString("Filters", R.string.Filters), R.drawable.msg_folders, true);
//                    menuItemsLayout.addView(filtersRow);
                        filtersRow.setBackground(ContextCompat.getDrawable(context, isDark ? R.drawable.sk_ripple_dark : R.drawable.sk_ripple));
                        filtersRow.setOnClickListener(v -> mParentActivity.presentFragment(new FiltersSetupActivity()));
                        view = filtersRow;
                        break;
//                    case 6:
//                        walletRow = new TextCell(context);
//                        walletRow.setEnabled(true);
//                        walletRow.setColors(Theme.key_windowBackgroundWhiteGrayIcon, Theme.key_windowBackgroundWhiteBlackText);
//                        walletRow.setTextAndIcon(LocaleController.getString("MyWallet", R.string.MyWallet), R.drawable.msg_folders, true);
//                        walletRow.setBackground(ContextCompat.getDrawable(context, isDark ? R.drawable.sk_ripple_dark : R.drawable.sk_ripple));
//                        walletRow.setOnClickListener(v -> mParentActivity.presentFragment(new MyWalletActivity()));
//                        view = walletRow;
//                        break;
                    case 5:
                        FrameLayout frameLayout = new FrameLayout(context) {
                            @Override
                            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                                int height;
                                height = MeasureSpec.getSize(heightMeasureSpec);
                                if (height == 0) {
                                    height = parent.getMeasuredHeight();
                                }
                                height = AndroidUtilities.displaySize.y - AndroidUtilities.dp(headerLayoutHeight) - AndroidUtilities.dp(28);
                                int cellHeight = AndroidUtilities.dp(48) * 3 + AndroidUtilities.dp(70);
                                height = height - cellHeight - AndroidUtilities.dp(45);
                                super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
                            }
                        };
//                    frameLayout.setBackgroundColor(getThemedColor(Theme.key_windowBackgroundGray));
                        createPrivacyLayout();
                        frameLayout.addView(skgPrivacyCell, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.BOTTOM, 0, 0, 0, 20));
                        view = frameLayout;
                        break;
                }
            }
            return new SKViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull SKViewHolder holder, int position) {
            if (position == 0) {
                setUser();
            }
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return autoRegister ? 7 : 6;
        }

        class SKViewHolder extends RecyclerView.ViewHolder {
            public SKViewHolder(@NonNull View itemView) {
                super(itemView);
            }
        }
    }
}
