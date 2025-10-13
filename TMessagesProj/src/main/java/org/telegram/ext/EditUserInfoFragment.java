package org.telegram.ext;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import net.csdn.roundview.RoundLinearLayout;

import org.telegram.PhoneFormat.PhoneFormat;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.Emoji;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.ImageLoader;
import org.telegram.messenger.ImageLocation;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.ImageUpdater;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RadialProgressView;
import org.telegram.ui.LaunchActivity;
import org.telegram.ui.PhotoViewer;

import java.io.File;
import java.util.ArrayList;

public class EditUserInfoFragment extends BaseFragment implements ImageUpdater.ImageUpdaterDelegate, NotificationCenter.NotificationCenterDelegate {

    private LinearLayout contentView;
    private LaunchActivity mParentActivity;

    private BackupImageView avatarImageView;

    private TLRPC.User latestUser;
    private ImageUpdater imageUpdater;
    private RadialProgressView avatarProgressView;
    private TLRPC.FileLocation avatar;
    private TLRPC.FileLocation avatarBig;
    private AvatarDrawable avatarDrawable;
    private AppCompatTextView tv_nickname;

    public void setParentActivity(LaunchActivity parentActivity) {
        this.mParentActivity = parentActivity;
    }

    @Override
    public boolean onFragmentCreate() {
        getNotificationCenter().addObserver(this, NotificationCenter.updateInterfaces);
        getNotificationCenter().addObserver(this, NotificationCenter.needSetDayNightTheme);
        return super.onFragmentCreate();
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle("编辑资料");
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

        latestUser = getMessagesController().getUser(getUserConfig().getClientUserId());

        contentView = new LinearLayout(context);
        contentView.setOrientation(LinearLayout.VERTICAL);
        contentView.setGravity(Gravity.CENTER_HORIZONTAL);
        contentView.setPadding(AndroidUtilities.dp(12), AndroidUtilities.dp(10), AndroidUtilities.dp(12), AndroidUtilities.dp(0));
        contentView.setLayoutParams(LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        avatarImageView = new BackupImageView(context);
        avatarImageView.getImageReceiver().setRoundRadius(AndroidUtilities.dp(32));

        avatarDrawable = new AvatarDrawable();
        avatarDrawable.setProfile(true);
        avatarDrawable.setInfo(latestUser);

        AvatarDrawable avatarDrawable = new AvatarDrawable(latestUser);
        avatarDrawable.setColor(Theme.getColor(Theme.key_avatar_backgroundInProfileBlue));
        avatarImageView.setForUserOrChat(latestUser, avatarDrawable);

        FrameLayout avatarFrame = new FrameLayout(context);
        contentView.addView(avatarFrame, LayoutHelper.createFrame(64, 84));
        avatarFrame.addView(avatarImageView, LayoutHelper.createFrame(64, 64, Gravity.TOP | Gravity.CENTER_HORIZONTAL));
        avatarFrame.setOnClickListener(view -> uploadAvatar());

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
        contentView.addView(nickname_layout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, AndroidUtilities.dp(16), 0, 16, 0, 0));

        nickname_layout.setOnClickListener(view -> mParentActivity.presentFragment(new EditNicknameFragment()));

        AppCompatTextView tv_nickname_title = new AppCompatTextView(context);
        tv_nickname_title.setText("昵称");
        tv_nickname_title.setGravity(Gravity.CENTER_VERTICAL);
        tv_nickname_title.setPadding(12, 0, 12, 0);
        nickname_layout.addView(tv_nickname_title, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.MATCH_PARENT, 8, 0, 8, 0));

        tv_nickname = new AppCompatTextView(context);
        tv_nickname.setMaxLines(1);
        tv_nickname.setTextColor(Color.BLACK);
        tv_nickname.setEllipsize(TextUtils.TruncateAt.END);
        tv_nickname.setGravity(Gravity.CENTER_VERTICAL | Gravity.END);
        tv_nickname.setPadding(0, 0, 12, 0);
        nickname_layout.addView(tv_nickname, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.MATCH_PARENT, 1f, 0, 0, 0, 0));

        AppCompatImageView ic_arrow_next = new AppCompatImageView(context);
        ic_arrow_next.setImageResource(R.drawable.arrow_newchat);
        ic_arrow_next.setColorFilter(Color.BLACK);
        nickname_layout.addView(ic_arrow_next, LayoutHelper.createLinear(12, 12, Gravity.CENTER_VERTICAL, 0, 0, 8, 0));

        CharSequence text = UserObject.getUserName(latestUser);
        try {
            text = Emoji.replaceEmoji(text, tv_nickname.getPaint().getFontMetricsInt(), false, AndroidUtilities.dp(22));
        } catch (Exception ignore) {
        }
        tv_nickname.setText(text);

        View viewLine = new View(context);
        viewLine.setBackgroundColor(Color.parseColor("#F4F4F4"));
        contentView.addView(viewLine, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 1, 8, 0, 8, 0));

        // 账号
        LinearLayout account_layout = new LinearLayout(context);
        account_layout.setOrientation(LinearLayout.HORIZONTAL);
        contentView.addView(account_layout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, AndroidUtilities.dp(16)));

        AppCompatTextView tv_account_title = new AppCompatTextView(context);
        tv_account_title.setText("账号");
        tv_account_title.setGravity(Gravity.CENTER_VERTICAL);
        tv_account_title.setPadding(12, 0, 12, 0);
        account_layout.addView(tv_account_title, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.MATCH_PARENT, 8, 0, 8, 0));

        AppCompatTextView tv_account = new AppCompatTextView(context);
        tv_account.setMaxLines(1);
        tv_account.setTextColor(Color.BLACK);
        tv_account.setEllipsize(TextUtils.TruncateAt.END);
        tv_account.setGravity(Gravity.CENTER_VERTICAL | Gravity.END);
        tv_account.setPadding(0, 0, 12, 0);
        account_layout.addView(tv_account, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.MATCH_PARENT, 1f, 0, 0, 8, 0));

        String phoneNumber;
        if (latestUser != null && !TextUtils.isEmpty(latestUser.phone)) {
            text = PhoneFormat.getInstance().format("+" + latestUser.phone);
            phoneNumber = latestUser.phone;
        } else {
            text = LocaleController.getString(R.string.PhoneHidden);
            phoneNumber = null;
        }

        tv_account.setText(text);

        View viewLine2 = new View(context);
        viewLine2.setBackgroundColor(Color.parseColor("#F4F4F4"));
        contentView.addView(viewLine2, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 1, 8, 0, 8, 0));

        // 用户名
        LinearLayout username_layout = new LinearLayout(context);
        username_layout.setOrientation(LinearLayout.HORIZONTAL);
        contentView.addView(username_layout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, AndroidUtilities.dp(16)));

        AppCompatTextView tv_username_title = new AppCompatTextView(context);
        tv_username_title.setText("用户名");
        tv_username_title.setGravity(Gravity.CENTER_VERTICAL);
        tv_username_title.setPadding(12, 0, 12, 0);
        username_layout.addView(tv_username_title, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.MATCH_PARENT, 8, 0, 8, 0));

        AppCompatTextView tv_username = new AppCompatTextView(context);
        tv_username.setMaxLines(1);
        tv_username.setTextColor(Color.BLACK);
        tv_username.setEllipsize(TextUtils.TruncateAt.END);
        tv_username.setGravity(Gravity.CENTER_VERTICAL | Gravity.END);
        tv_username.setPadding(0, 0, 12, 0);
        username_layout.addView(tv_username, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.MATCH_PARENT, 1f, 0, 0, 8, 0));

        String username = UserObject.getPublicUsername(latestUser);
        if (null != username && !username.isEmpty()) {
            tv_username.setText("@" + username);
        }

        View viewLine3 = new View(context);
        viewLine3.setBackgroundColor(Color.parseColor("#F4F4F4"));
        contentView.addView(viewLine3, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 1, 8, 0, 8, 0));

        fragmentView = contentView;

        return fragmentView;
    }

    private void updateUserInfo() {
        avatarDrawable.setColor(Theme.getColor(Theme.key_avatar_backgroundInProfileBlue));
        avatarImageView.setForUserOrChat(latestUser, avatarDrawable);

        CharSequence text = UserObject.getUserName(latestUser);
        try {
            text = Emoji.replaceEmoji(text, tv_nickname.getPaint().getFontMetricsInt(), false, AndroidUtilities.dp(22));
        } catch (Exception ignore) {
        }
        tv_nickname.setText(text);
    }

    private void uploadAvatar() {
        TLRPC.User user = getMessagesController().getUser(getUserConfig().getClientUserId());
        if (user == null) {
            user = UserConfig.getInstance(currentAccount).getCurrentUser();
        }
        if (user == null) {
            return;
        }
        imageUpdater.openMenu(user.photo != null && user.photo.photo_big != null && !(user.photo instanceof TLRPC.TL_userProfilePhotoEmpty), () -> {
            MessagesController.getInstance(currentAccount).deleteUserPhoto(null);
        }, dialog -> {
        }, 0);
    }

    int avatarUploadingRequest;

    @Override
    public void didUploadPhoto(TLRPC.InputFile photo, TLRPC.InputFile video, double videoStartTimestamp, String videoPath, TLRPC.PhotoSize bigSize, TLRPC.PhotoSize smallSize, boolean isVideo, TLRPC.VideoSize emojiMarkup) {
        AndroidUtilities.runOnUIThread(() -> {
            if (photo != null || video != null || emojiMarkup != null) {
                if (avatar == null) {
                    return;
                }
                TLRPC.TL_photos_uploadProfilePhoto req = new TLRPC.TL_photos_uploadProfilePhoto();
                if (photo != null) {
                    req.file = photo;
                    req.flags |= 1;
                }
                if (video != null) {
                    req.video = video;
                    req.flags |= 2;
                    req.video_start_ts = videoStartTimestamp;
                    req.flags |= 4;
                }
                if (emojiMarkup != null) {
                    req.video_emoji_markup = emojiMarkup;
                    req.flags |= 16;
                }
                avatarUploadingRequest = getConnectionsManager().sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
                    if (error == null) {
                        TLRPC.User user = getMessagesController().getUser(getUserConfig().getClientUserId());
                        if (user == null) {
                            user = getUserConfig().getCurrentUser();
                            if (user == null) {
                                return;
                            }
                            getMessagesController().putUser(user, false);
                        } else {
                            getUserConfig().setCurrentUser(user);
                        }

                        TLRPC.TL_photos_photo photos_photo = (TLRPC.TL_photos_photo) response;
                        ArrayList<TLRPC.PhotoSize> sizes = photos_photo.photo.sizes;
                        TLRPC.PhotoSize small = FileLoader.getClosestPhotoSizeWithSize(sizes, 150);
                        TLRPC.PhotoSize big = FileLoader.getClosestPhotoSizeWithSize(sizes, 800);
                        TLRPC.VideoSize videoSize = photos_photo.photo.video_sizes.isEmpty() ? null : FileLoader.getClosestVideoSizeWithSize(photos_photo.photo.video_sizes, 1000);
                        user.photo = new TLRPC.TL_userProfilePhoto();
                        user.photo.photo_id = photos_photo.photo.id;
                        if (small != null) {
                            user.photo.photo_small = small.location;
                        }
                        if (big != null) {
                            user.photo.photo_big = big.location;
                        }

                        if (small != null && avatar != null) {
                            File destFile = FileLoader.getInstance(currentAccount).getPathToAttach(small, true);
                            File src = FileLoader.getInstance(currentAccount).getPathToAttach(avatar, true);
                            src.renameTo(destFile);
                            String oldKey = avatar.volume_id + "_" + avatar.local_id + "@50_50";
                            String newKey = small.location.volume_id + "_" + small.location.local_id + "@50_50";
                            ImageLoader.getInstance().replaceImageInCache(oldKey, newKey, ImageLocation.getForUserOrChat(user, ImageLocation.TYPE_SMALL), false);
                        }

                        if (videoSize != null && videoPath != null) {
                            File destFile = FileLoader.getInstance(currentAccount).getPathToAttach(videoSize, "mp4", true);
                            File src = new File(videoPath);
                            src.renameTo(destFile);
                        } else if (big != null && avatarBig != null) {
                            File destFile = FileLoader.getInstance(currentAccount).getPathToAttach(big, true);
                            File src = FileLoader.getInstance(currentAccount).getPathToAttach(avatarBig, true);
                            src.renameTo(destFile);
                        }
                        getMessagesController().getDialogPhotos(user.id).addPhotoAtStart(((TLRPC.TL_photos_photo) response).photo);
                        ArrayList<TLRPC.User> users = new ArrayList<>();
                        users.add(user);
                        getMessagesStorage().putUsersAndChats(users, null, false, true);
                        TLRPC.UserFull userFull = getMessagesController().getUserFull(getUserConfig().getClientUserId());
                        if (userFull != null) {
                            userFull.profile_photo = photos_photo.photo;
                            getMessagesStorage().updateUserInfo(userFull, false);
                        }
                    }

//                    allowPullingDown = !AndroidUtilities.isTablet() && !isInLandscapeMode && avatarImage.getImageReceiver().hasNotThumb() && !AndroidUtilities.isAccessibilityScreenReaderEnabled();
                    avatar = null;
                    avatarBig = null;
//                    avatarsViewPager.setCreateThumbFromParent(false);
//                    updateProfileData(true);
//                    updateUserInfo();
                    showAvatarProgress(false, true);
                    getNotificationCenter().postNotificationName(NotificationCenter.updateInterfaces, MessagesController.UPDATE_MASK_ALL);
                    getNotificationCenter().postNotificationName(NotificationCenter.mainUserInfoChanged);
                    getUserConfig().saveConfig(true);
                }));
            } else {
                avatar = smallSize.location;
                avatarBig = bigSize.location;
                avatarImageView.setImage(ImageLocation.getForLocal(avatar), "50_50", avatarDrawable, null);
//                if (setAvatarRow != -1) {
//                    updateRowsIds();
//                    if (listAdapter != null) {
//                        listAdapter.notifyDataSetChanged();
//                    }
//                    needLayout(true);
//                }
//                avatarsViewPager.addUploadingImage(uploadingImageLocation = ImageLocation.getForLocal(avatarBig), ImageLocation.getForLocal(avatar));
                showAvatarProgress(true, false);
            }
//            actionBar.createMenu().requestLayout();
        });
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

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == NotificationCenter.updateInterfaces) {
            int mask = (Integer) args[0];
            boolean infoChanged = (mask & MessagesController.UPDATE_MASK_AVATAR) != 0 || (mask & MessagesController.UPDATE_MASK_NAME) != 0 || (mask & MessagesController.UPDATE_MASK_STATUS) != 0 || (mask & MessagesController.UPDATE_MASK_EMOJI_STATUS) != 0;
            if (infoChanged) {
                latestUser = getMessagesController().getUser(UserConfig.getInstance(currentAccount).getClientUserId());
                updateUserInfo();
            }
        }
    }
}
