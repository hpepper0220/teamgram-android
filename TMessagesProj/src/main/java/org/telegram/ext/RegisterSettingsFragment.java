package org.telegram.ext;

import static org.telegram.messenger.LocaleController.getString;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import com.blankj.utilcode.util.DeviceUtils;
import com.skg.lib.widget.ClearEditText;

import net.csdn.roundview.RoundLinearLayout;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.AuthTokensHelper;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.ImageLocation;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaDataController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarLayout;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.ImageUpdater;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RadialProgressView;
import org.telegram.ui.DialogsActivity;
import org.telegram.ui.LaunchActivity;
import org.telegram.ui.TwoStepVerificationSetupActivity;
import org.telegram.ui.RestrictedLanguagesSelectActivity;

import java.util.ArrayList;
import java.util.Objects;

public class RegisterSettingsFragment extends BaseFragment implements ImageUpdater.ImageUpdaterDelegate {

    private LinearLayout contentView;
    private LaunchActivity mParentActivity;

    private BackupImageView avatarImageView;

    private ImageUpdater imageUpdater;
    private RadialProgressView avatarProgressView;
    private TLRPC.FileLocation avatar;
    private TLRPC.FileLocation avatarBig;
    private AvatarDrawable avatarDrawable;
    private ClearEditText et_nickname;

    public void setParentActivity(LaunchActivity parentActivity) {
        this.mParentActivity = parentActivity;
    }

    private String account;
    private String password;

    public RegisterSettingsFragment() {
    }

    public RegisterSettingsFragment(Bundle args) {
        super(args);
        account = args.getString("account");
        password = args.getString("password");
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle("注册");
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
        contentView.setGravity(Gravity.CENTER_HORIZONTAL);
        contentView.setPadding(AndroidUtilities.dp(12), AndroidUtilities.dp(10), AndroidUtilities.dp(12), AndroidUtilities.dp(0));
        contentView.setLayoutParams(LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        avatarImageView = new BackupImageView(context);
        avatarImageView.getImageReceiver().setRoundRadius(AndroidUtilities.dp(32));

        avatarDrawable = new AvatarDrawable();
        avatarDrawable.setAvatarType(AvatarDrawable.AVATAR_TYPE_REGISTER);
        avatarDrawable.setInfo(5, account, null);
        avatarImageView.setImageDrawable(avatarDrawable);

        FrameLayout avatarFrame = new FrameLayout(context);
        contentView.addView(avatarFrame, LayoutHelper.createFrame(64, 84));
        avatarFrame.addView(avatarImageView, LayoutHelper.createFrame(64, 64, Gravity.TOP | Gravity.CENTER_HORIZONTAL));
        avatarFrame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageUpdater.openMenu(avatar != null, () -> {
                    avatar = null;
                    avatarBig = null;
                    showAvatarProgress(false, true);
                    avatarImageView.setImage(null, null, avatarDrawable, null);
                }, dialog -> {
                }, 0);
            }
        });

        RoundLinearLayout cameraLayout = new RoundLinearLayout(context);
        cameraLayout.setRadius(20f);
        cameraLayout.setGravity(Gravity.CENTER);
        cameraLayout.setBackgroundColor(Color.parseColor("#80000000"));

        AppCompatImageView cameraImage = new AppCompatImageView(context);
        cameraImage.setImageResource(R.mipmap.sk_ic_avatar_camera);
        cameraLayout.addView(cameraImage, LayoutHelper.createFrame(12, 12));

        avatarFrame.addView(cameraLayout, LayoutHelper.createFrame(20, 20, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0, 0, 12));

        imageUpdater = new ImageUpdater(false, ImageUpdater.FOR_TYPE_USER, true);
        imageUpdater.setOpenWithFrontfaceCamera(true);
        imageUpdater.setSearchAvailable(false);
        imageUpdater.setUploadAfterSelect(false);
        imageUpdater.parentFragment = this;
        imageUpdater.setDelegate(this);

        avatarProgressView = new RadialProgressView(context) {
            private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

            {
                paint.setColor(0x55000000);
            }

            @Override
            protected void onDraw(Canvas canvas) {
                if (avatarImageView != null && avatarImageView.getImageReceiver().hasNotThumb()) {
                    paint.setAlpha((int) (0x55 * avatarImageView.getImageReceiver().getCurrentAlpha()));
                    canvas.drawCircle(getMeasuredWidth() / 2.0f, getMeasuredHeight() / 2.0f, getMeasuredWidth() / 2.0f, paint);
                }
                super.onDraw(canvas);
            }
        };
        avatarProgressView.setSize(AndroidUtilities.dp(26));
        avatarProgressView.setProgressColor(0xffffffff);
        avatarProgressView.setNoProgress(false);
        avatarFrame.addView(avatarProgressView, LayoutHelper.createFrame(64, 64));
        showAvatarProgress(false, false);

        // 昵称
        LinearLayout nickname_layout = new LinearLayout(context);
        nickname_layout.setOrientation(LinearLayout.HORIZONTAL);
        contentView.addView(nickname_layout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, AndroidUtilities.dp(14), 0, 16, 0, 0));

        nickname_layout.setOnClickListener(view -> mParentActivity.presentFragment(new EditNicknameFragment()));

        AppCompatTextView tv_nickname_title = new AppCompatTextView(context);
        tv_nickname_title.setText("昵称");
        tv_nickname_title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f);
        tv_nickname_title.setGravity(Gravity.CENTER_VERTICAL);
        tv_nickname_title.setPadding(12, 0, 12, 0);
        nickname_layout.addView(tv_nickname_title, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.MATCH_PARENT, 8, 0, 8, 0));

        et_nickname = new ClearEditText(context);
        et_nickname.setMaxLines(1);
        et_nickname.setHint("请输入昵称");
        et_nickname.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f);
        et_nickname.setTextColor(Color.parseColor("#000000"));
        et_nickname.setHintTextColor(Color.parseColor("#999999"));
        et_nickname.setEllipsize(TextUtils.TruncateAt.END);
        et_nickname.setGravity(Gravity.CENTER_VERTICAL | Gravity.END);
        et_nickname.setPadding(0, 0, 12, 0);
        et_nickname.setBackgroundColor(Color.TRANSPARENT);
        et_nickname.setInputType(InputType.TYPE_CLASS_TEXT);
        nickname_layout.addView(et_nickname, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.MATCH_PARENT, 1f, 0, 0, 0, 0));

        View viewLine = new View(context);
        viewLine.setBackgroundColor(Color.parseColor("#F4F4F4"));
        contentView.addView(viewLine, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 1, 8, 0, 8, 0));

        LoadingButton button = new LoadingButton(context);
        button.setText("确定");
        button.setBackgroundResource(R.drawable.button_circle_selector);
        contentView.addView(button, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 44, Gravity.CENTER_HORIZONTAL, 36, 40, 36, 0));

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(et_nickname.getText())) {
                    return;
                }
                button.showLoading();
                getConnectionsManager().sendRequest(createSignupReqBody(), new RequestDelegate() {
                    @Override
                    public void run(TLObject response, TLRPC.TL_error error) {
                        AndroidUtilities.runOnUIThread(() -> {
                            button.hideLoading();
                            if (response instanceof TLRPC.TL_auth_authorization) {
                                contentView.postDelayed(() -> {
                                    AndroidUtilities.hideKeyboard(contentView.findFocus());
                                    onAuthSuccess((TLRPC.TL_auth_authorization) response, true);
                                }, 150);
                            } else {
                                if (error.text.contains("PHONE_NUMBER_INVALID")) {
                                    needShowAlert(getString(R.string.RestorePasswordNoEmailTitle), getString("InvalidPhoneNumber", R.string.InvalidPhoneNumber));
                                } else if (error.text.contains("PHONE_CODE_EMPTY") || error.text.contains("PHONE_CODE_INVALID")) {
                                    needShowAlert(getString(R.string.RestorePasswordNoEmailTitle), getString("InvalidCode", R.string.InvalidCode));
                                } else if (error.text.contains("PHONE_CODE_EXPIRED")) {
                                    needShowAlert(getString(R.string.RestorePasswordNoEmailTitle), getString("CodeExpired", R.string.CodeExpired));
                                } else if (error.text.contains("FIRSTNAME_INVALID")) {
                                    needShowAlert(getString(R.string.RestorePasswordNoEmailTitle), getString("InvalidFirstName", R.string.InvalidFirstName));
                                } else if (error.text.contains("LASTNAME_INVALID")) {
                                    needShowAlert(getString(R.string.RestorePasswordNoEmailTitle), getString("InvalidLastName", R.string.InvalidLastName));
                                } else {
                                    needShowAlert(getString(R.string.RestorePasswordNoEmailTitle), error.text);
                                }
                            }
                        });
                    }
                }, ConnectionsManager.RequestFlagWithoutLogin | ConnectionsManager.RequestFlagFailOnServerErrors);
            }
        });

        return contentView;
    }

    private void onAuthSuccess(TLRPC.TL_auth_authorization res) {
        onAuthSuccess(res, false);
    }

    private void onAuthSuccess(TLRPC.TL_auth_authorization res, boolean afterSignup) {
        MessagesController.getInstance(currentAccount).cleanup();
        ConnectionsManager.getInstance(currentAccount).setUserId(res.user.id);
        UserConfig.getInstance(currentAccount).clearConfig();
        MessagesController.getInstance(currentAccount).cleanup();
        UserConfig.getInstance(currentAccount).syncContacts = false;
        UserConfig.getInstance(currentAccount).setCurrentUser(res.user);
        UserConfig.getInstance(currentAccount).saveConfig(true);
        MessagesStorage.getInstance(currentAccount).cleanup(true);
        ArrayList<TLRPC.User> users = new ArrayList<>();
        users.add(res.user);
        MessagesStorage.getInstance(currentAccount).putUsersAndChats(users, null, true, true);
        MessagesController.getInstance(currentAccount).putUser(res.user, false);
        ContactsController.getInstance(currentAccount).checkAppAccount();
        MessagesController.getInstance(currentAccount).checkPromoInfo(true);
        ConnectionsManager.getInstance(currentAccount).updateDcSettings();
        MessagesController.getInstance(currentAccount).loadAppConfig();
        MessagesController.getInstance(currentAccount).checkPeerColors(false);

        if (res.future_auth_token != null) {
            AuthTokensHelper.saveLogInToken(res);
        } else {
            FileLog.d("onAuthSuccess future_auth_token is empty");
        }

        if (afterSignup) {
            MessagesController.getInstance(currentAccount).putDialogsEndReachedAfterRegistration();
        }
        MediaDataController.getInstance(currentAccount).loadStickersByEmojiOrName(AndroidUtilities.STICKERS_PLACEHOLDER_PACK_NAME, false, true);

        // 将之前的页面清除
        getParentLayout().removeFragmentFromStack(0);

        needFinishActivity(afterSignup, res.setup_password_required, res.otherwise_relogin_days);
    }

    private void needFinishActivity(boolean afterSignup, boolean showSetPasswordConfirm, int otherwiseRelogin) {
        if (getParentActivity() != null) {
            AndroidUtilities.setLightStatusBar(getParentActivity().getWindow(), false);
        }
        clearCurrentState();
        if (afterSignup && showSetPasswordConfirm) {
            TwoStepVerificationSetupActivity twoStepVerification = new TwoStepVerificationSetupActivity(TwoStepVerificationSetupActivity.TYPE_INTRO, null);
            twoStepVerification.setBlockingAlert(otherwiseRelogin);
            twoStepVerification.setFromRegistration(true);
            presentFragment(twoStepVerification, true);
        } else {
            Bundle args = new Bundle();
            args.putBoolean("afterSignup", afterSignup);
            DialogsActivity dialogsActivity = new DialogsActivity(args);
            presentFragment(dialogsActivity, true);
        }

        NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.mainUserInfoChanged);
        LocaleController.getInstance().loadRemoteLanguages(currentAccount);
        RestrictedLanguagesSelectActivity.checkRestrictedLanguages(true);
    }

    private void clearCurrentState() {
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("logininfo2" + (""), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();
    }

    private void needShowAlert(String title, String text) {
        if (text == null || getParentActivity() == null) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
        builder.setTitle(title);
        builder.setMessage(text);
        builder.setPositiveButton(getString("OK", R.string.OK), null);
        showDialog(builder.create());
    }

    private TLRPC.TL_ssgrams_signUp createSignupReqBody() {
        TLRPC.TL_ssgrams_signUp req = new TLRPC.TL_ssgrams_signUp();
        req.account = account;
        req.password = password;

        req.first_name = Objects.requireNonNull(et_nickname.getText()).toString();
        req.last_name = "";
        req.device = DeviceUtils.getUniqueDeviceId();
        req.version = BuildVars.BUILD_VERSION_STRING;
        req.invite_code = "";
        req.auto_register = false;
        req.download_source = "";
        return req;
    }

    private AnimatorSet avatarAnimation;

    public void showAvatarProgress(boolean show, boolean animated) {
        if (avatarProgressView == null) {
            return;
        }
        if (avatarAnimation != null) {
            avatarAnimation.cancel();
            avatarAnimation = null;
        }
        if (animated) {
            avatarAnimation = new AnimatorSet();
            if (show) {
                avatarProgressView.setVisibility(View.VISIBLE);
                avatarAnimation.playTogether(ObjectAnimator.ofFloat(avatarProgressView, View.ALPHA, 1.0f));
            } else {
                avatarAnimation.playTogether(ObjectAnimator.ofFloat(avatarProgressView, View.ALPHA, 0.0f));
            }
            avatarAnimation.setDuration(180);
            avatarAnimation.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (avatarAnimation == null || avatarProgressView == null) {
                        return;
                    }
                    if (!show) {
                        avatarProgressView.setVisibility(View.INVISIBLE);
                    }
                    avatarAnimation = null;
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    avatarAnimation = null;
                }
            });
            avatarAnimation.start();
        } else {
            if (show) {
                avatarProgressView.setAlpha(1.0f);
                avatarProgressView.setVisibility(View.VISIBLE);
            } else {
                avatarProgressView.setAlpha(0.0f);
                avatarProgressView.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public void didUploadPhoto(TLRPC.InputFile photo, TLRPC.InputFile video, double videoStartTimestamp, String videoPath, TLRPC.PhotoSize bigSize, TLRPC.PhotoSize smallSize, boolean isVideo, TLRPC.VideoSize emojiMarkup) {
        AndroidUtilities.runOnUIThread(() -> {
            avatar = smallSize.location;
            avatarBig = bigSize.location;
            avatarImageView.setImage(ImageLocation.getForLocal(avatar), "50_50", avatarDrawable, null);
        });
    }

    @Override
    public void onUploadProgressChanged(float progress) {
        if (avatarProgressView == null) {
            return;
        }
        avatarProgressView.setProgress(progress);
    }

    @Override
    public void didStartUpload(boolean fromAvatarConstructor, boolean isVideo) {
        if (avatarProgressView == null) {
            return;
        }
        avatarProgressView.setProgress(0.0f);
    }
}
