package org.telegram.ext;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.LinearLayout;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import com.blankj.utilcode.util.DeviceUtils;
import com.skg.lib.utils.KeyboardWatcher;
import com.skg.lib.widget.ClearEditText;
import com.skg.lib.widget.InputTextManager;
import com.skg.lib.widget.SubmitButton;

import org.telegram.messenger.BuildVars;
import org.telegram.messenger.R;
import org.telegram.ui.Components.LayoutHelper;

import org.telegram.tgnet.TLRPC;

import java.util.Objects;

public class LoginView extends LinearLayout {

    private final AppCompatImageView logoView;
    private final ClearEditText usernameEt;
    private final ClearEditText passwordEt;
    public final LoadingButton signUpButton;
    public final LoadingButton signInButton;
    private final LinearLayout bodyLayout;

    private final int mAnimTime = 300;
    private final float mLogoScale = 0.8f;

    private int currentAccount;
    private OnSignUpButtonPressed onSignUpButtonPressed;
    private Activity parentActivity;

    public LoginView(Context context) {
        super(context);
        this.setOrientation(LinearLayout.VERTICAL);

        logoView = new AppCompatImageView(context);
        logoView.setImageResource(R.mipmap.ic_launcher);
        this.addView(logoView, LayoutHelper.createLinear(120, 120, Gravity.CENTER_HORIZONTAL, 36, 80, 36, 0));

        bodyLayout = new LinearLayout(context);
        bodyLayout.setOrientation(LinearLayout.VERTICAL);
        this.addView(bodyLayout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 0, 0, 0, 0));

        usernameEt = new ClearEditText(context);
        usernameEt.setTextSize(15);
        usernameEt.setHint("请输入用户名");
        usernameEt.setTextColor(Color.parseColor("#333333"));
        usernameEt.setHintTextColor(Color.parseColor("#A4A4A4"));
        usernameEt.setBackgroundResource(R.drawable.transparent);
        bodyLayout.addView(usernameEt, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 36, 20, 36, 0));

        View line1 = new View(context, null, R.style.HorizontalLineStyle);
        line1.setBackgroundColor(Color.parseColor("#ECECEC"));
        bodyLayout.addView(line1, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 1, Gravity.CENTER_HORIZONTAL, 36, 0, 36, 10));

        passwordEt = new ClearEditText(context);
        passwordEt.setTextSize(15);
        passwordEt.setHint("请输入密码");
        passwordEt.setTextColor(Color.parseColor("#333333"));
        passwordEt.setHintTextColor(Color.parseColor("#A4A4A4"));
        passwordEt.setBackgroundResource(R.drawable.transparent);
        bodyLayout.addView(passwordEt, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 36, 0, 36, 0));

        View line2 = new View(context, null, R.style.HorizontalLineStyle);
        line2.setBackgroundColor(Color.parseColor("#ECECEC"));
        bodyLayout.addView(line2, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 1, Gravity.CENTER_HORIZONTAL, 36, 0, 36, 0));

        signUpButton = new LoadingButton(context);
        signUpButton.setText("注册");
        signUpButton.setBackgroundResource(R.drawable.button_circle_selector);
        this.addView(signUpButton, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 46, Gravity.CENTER_HORIZONTAL, 36, 40, 36, 0));

        signInButton = new LoadingButton(context);
        signInButton.setText("登录");
        signInButton.setBackgroundResource(R.drawable.button_circle_selector);
        this.addView(signInButton, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 46, Gravity.CENTER_HORIZONTAL, 36, 30, 36, 0));

        Animation animation = AnimationUtils.loadAnimation(context, R.anim.layout_from_bottom_item);
        LayoutAnimationController controller = new LayoutAnimationController(animation);
        controller.setDelay(0.15f);
        controller.setOrder(LayoutAnimationController.ORDER_NORMAL);
        setLayoutAnimation(controller);

        signUpButton.setOnClickListener(view -> {
            TLRPC.TL_ssgrams_signUp req = new TLRPC.TL_ssgrams_signUp();
            req.account = Objects.requireNonNull(usernameEt.getText()).toString();
            req.password = Objects.requireNonNull(passwordEt.getText()).toString();
            req.first_name = req.account;
            req.last_name = "";
            req.device = DeviceUtils.getUniqueDeviceId();
//            req.device = "a3578rssa";
            req.version = BuildVars.BUILD_VERSION_STRING;
            req.invite_code = "";
            req.auto_register = false;

            if (null != onSignUpButtonPressed) {
                onSignUpButtonPressed.onSignUp(req);
            }
        });

        signInButton.setOnClickListener(view -> {
            TLRPC.TL_ssgrams_signIn req = new TLRPC.TL_ssgrams_signIn();
            req.account = Objects.requireNonNull(usernameEt.getText()).toString();
            req.password = Objects.requireNonNull(passwordEt.getText()).toString();
            req.device = DeviceUtils.getUniqueDeviceId();

            if (null != onSignUpButtonPressed) {
                onSignUpButtonPressed.onSignIn(req);
            }
        });
    }

    public void bindParentActivity(Activity parentActivity, int currentAccount, OnSignUpButtonPressed onSignUpButtonPressed) {
        this.currentAccount = currentAccount;
        this.onSignUpButtonPressed = onSignUpButtonPressed;
        InputTextManager.with(parentActivity)
                .addView(usernameEt)
                .addView(passwordEt)
                .setMain(signUpButton)
                .build();

        KeyboardWatcher.with(parentActivity).setListener(new KeyboardWatcher.SoftKeyboardStateListener() {
            @Override
            public void onSoftKeyboardOpened(int keyboardHeight) {
                // 执行位移动画
                ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(bodyLayout, "translationY", 0, -signUpButton.getHeight());
                objectAnimator.setDuration(mAnimTime);
                objectAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
                objectAnimator.start();

                // 执行缩小动画
                logoView.setPivotX(logoView.getWidth() / 2f);
                logoView.setPivotY(logoView.getHeight());
                AnimatorSet animatorSet = new AnimatorSet();
                ObjectAnimator scaleX = ObjectAnimator.ofFloat(logoView, "scaleX", 1f, mLogoScale);
                ObjectAnimator scaleY = ObjectAnimator.ofFloat(logoView, "scaleY", 1f, mLogoScale);
                ObjectAnimator translationY = ObjectAnimator.ofFloat(logoView, "translationY", 0f, -signUpButton.getHeight());
                animatorSet.play(translationY).with(scaleX).with(scaleY);
                animatorSet.setDuration(mAnimTime);
                animatorSet.start();
            }

            @Override
            public void onSoftKeyboardClosed() {
                // 执行位移动画
                ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(bodyLayout, "translationY", bodyLayout.getTranslationY(), 0f);
                objectAnimator.setDuration(mAnimTime);
                objectAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
                objectAnimator.start();

                if (logoView.getTranslationY() == 0) {
                    return;
                }

                // 执行放大动画
                logoView.setPivotX(logoView.getWidth() / 2f);
                logoView.setPivotY(logoView.getHeight());
                AnimatorSet animatorSet = new AnimatorSet();
                ObjectAnimator scaleX = ObjectAnimator.ofFloat(logoView, "scaleX", mLogoScale, 1f);
                ObjectAnimator scaleY = ObjectAnimator.ofFloat(logoView, "scaleY", mLogoScale, 1f);
                ObjectAnimator translationY = ObjectAnimator.ofFloat(logoView, "translationY", logoView.getTranslationY(), 0f);
                animatorSet.play(translationY).with(scaleX).with(scaleY);
                animatorSet.setDuration(mAnimTime);
                animatorSet.start();
            }
        });
    }

    public interface OnSignUpButtonPressed {
        void onSignUp(TLRPC.TL_ssgrams_signUp req);
        void onSignIn(TLRPC.TL_ssgrams_signIn req);
    }
}
