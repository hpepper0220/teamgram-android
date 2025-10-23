package org.telegram.ext;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.just.agentweb.AgentWeb;
import com.just.agentweb.DefaultWebClient;
import com.just.agentweb.WebChromeClient;
import com.just.agentweb.WebViewClient;

import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.LaunchActivity;

public class ExploreFragment extends BaseFragment {
    private FrameLayout contentView;
    protected AgentWeb mAgentWeb;
    private LaunchActivity mParentActivity;
    private String web_url;

    public ExploreFragment(Bundle args) {
        super(args);
        web_url = args.getString("web_url");
    }

    public void setParentActivity(LaunchActivity parentActivity) {
        this.mParentActivity = parentActivity;
    }

    @Override
    public View createView(Context context) {
        contentView = new FrameLayout(context);
        contentView.setLayoutParams(LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        mAgentWeb = AgentWeb.with(mParentActivity)
                .setAgentWebParent(contentView, new LinearLayout.LayoutParams(-1, -1))
                .useDefaultIndicator().setWebChromeClient(mWebChromeClient)
                .setWebViewClient(mWebViewClient)
                .setMainFrameErrorView(R.layout.agentweb_error_page, -1)
                .setSecurityType(AgentWeb.SecurityType.STRICT_CHECK)
                // .setWebLayout(new WebLayout(this))
                .setOpenOtherPageWays(DefaultWebClient.OpenOtherPageWays.ASK)
                // 打开其他应用时，弹窗咨询用户是否前往其他应用
                .interceptUnkownUrl()
                // 拦截找不到相关页面的Scheme
                .createAgentWeb().ready().go(web_url);

        return contentView;
    }

    public void reload() {
        mAgentWeb.getUrlLoader().reload();
    }

    private com.just.agentweb.WebViewClient mWebViewClient = new WebViewClient() {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            Log.i("WebActivity", "shouldOverrideUrlLoading ===> " + view.getUrl());
            web_url = view.getUrl();
            return false;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            //do you  work
            Log.i("Info", "BaseWebActivity onPageStarted");
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
