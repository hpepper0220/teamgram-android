package org.telegram.ext;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.appcompat.widget.AppCompatImageView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.just.agentweb.AgentWeb;
import com.just.agentweb.DefaultWebClient;
import com.just.agentweb.WebChromeClient;
import com.just.agentweb.WebViewClient;

import net.csdn.roundview.RoundLinearLayout;

import org.telegram.ext.components.DialogCreator;
import org.telegram.ext.components.dialog.SkBottomDialog;
import org.telegram.ext.widgets.FloatingView;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.LaunchActivity;

public class WebFragment extends BaseFragment {

    private FrameLayout contentView;

    private AgentWeb mAgentWeb;
    private String web_url;
    private long web_id;
    private String web_title;
    private String web_img_url;

    public WebFragment() {
    }

    public WebFragment(Bundle args) {
        super(args);
        web_id = args.getLong("web_id");
        web_url = args.getString("web_url");
        web_title = args.getString("web_title");
        web_img_url = args.getString("web_img_url");
    }

    private LaunchActivity mParentActivity;

    public void setParentActivity(LaunchActivity parentActivity) {
        this.mParentActivity = parentActivity;
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackgroundColor(Color.WHITE);

        contentView = new FrameLayout(context);
        contentView.setLayoutParams(LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        FrameLayout titleLayout = new FrameLayout(context);
        actionBar.addView(titleLayout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 50, 12, 30, 12, 0));

        RoundLinearLayout menuLayout = new RoundLinearLayout(context);
        menuLayout.setRadius(20f);
        menuLayout.setStrokeWidth(1f);
        menuLayout.setStrokeColor(Color.parseColor("#999999"));
        menuLayout.setOrientation(LinearLayout.HORIZONTAL);
        menuLayout.setBackgroundColor(Color.parseColor("#999999"));
        titleLayout.addView(menuLayout, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, 28, Gravity.CENTER_VERTICAL | Gravity.END));

        AppCompatImageView aiv_more = new AppCompatImageView(context);
        aiv_more.setImageResource(R.mipmap.game_play_icon_drag);
        menuLayout.addView(aiv_more, LayoutHelper.createLinear(16, 16, Gravity.CENTER, 12, 0, 12, 0));
        aiv_more.setOnClickListener(view -> {
            DialogCreator.createBottomDialog(context, "", view1 -> {
                if (view1.getId() == R.id.dialog_hide) {
                    FloatingView.show(ApplicationLoader.applicationContext, currentAccount, web_id, web_title, web_img_url, web_url);
                    finishFragment();
                } else if (view1.getId() == R.id.dialog_refresh) {
                    reload();
                }
            });
        });

        AppCompatImageView aiv_menu = new AppCompatImageView(context);
        aiv_menu.setImageResource(R.mipmap.game_play_icon_show_menu);
        menuLayout.addView(aiv_menu, LayoutHelper.createLinear(16, 16, Gravity.CENTER, 6, 0, 12, 0));
        aiv_menu.setOnClickListener(view -> finishFragment());

        mAgentWeb = AgentWeb.with(mParentActivity)
                .setAgentWebParent(contentView, new LinearLayout.LayoutParams(-1, -1))
                .useDefaultIndicator()
                .setWebChromeClient(mWebChromeClient)
                .setWebViewClient(mWebViewClient)
                .setMainFrameErrorView(R.layout.agentweb_error_page, -1)
                .setSecurityType(AgentWeb.SecurityType.STRICT_CHECK)
                .setOpenOtherPageWays(DefaultWebClient.OpenOtherPageWays.ASK)
                .interceptUnkownUrl()
                .createAgentWeb().ready().go(web_url);

        return contentView;
    }

    public void reload() {
        mAgentWeb.getUrlLoader().reload();
    }

    private com.just.agentweb.WebViewClient mWebViewClient = new WebViewClient() {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            web_url = view.getUrl();
            return false;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {

        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
        }
    };

    private com.just.agentweb.WebChromeClient mWebChromeClient = new WebChromeClient() {
        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
        }
    };
}
