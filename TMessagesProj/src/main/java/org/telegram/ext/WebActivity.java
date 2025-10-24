package org.telegram.ext;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.just.agentweb.AgentWeb;
import com.just.agentweb.DefaultWebClient;
import com.just.agentweb.WebChromeClient;
import com.just.agentweb.WebViewClient;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.ui.BasePermissionsActivity;

public class WebActivity extends BasePermissionsActivity {

    private static final String mIdPrefix = "web_url";
    private static final String mScrollYPrefix = "web_scroll";

    protected AgentWeb mAgentWeb;
    private FrameLayout mFrameLayout;
    private TextView mTitleTextView;

    private Long mId;
    private String mTitle;
    private String mWebUrl;
    private String mImageUrl;

    public static void launch(Context context, Long id, String title, String webUrl) {
        Intent intent = new Intent(context, WebActivity.class);
        intent.putExtra("id", id);
        intent.putExtra("title", title);
        intent.putExtra("web_url", webUrl);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sk_activity_web);
        AndroidUtilities.setLightStatusBar(getWindow(), true);

        mId = getIntent().getLongExtra("id", -1);
        mTitle = getIntent().getStringExtra("title");
        mWebUrl = getIntent().getStringExtra("web_url");
        mImageUrl = getIntent().getStringExtra("image_url");

        this.findViewById(R.id.rl_container).setBackgroundColor(Color.WHITE);

        mFrameLayout = this.findViewById(R.id.fl_container);
        mTitleTextView = this.findViewById(R.id.title_view);
        mTitleTextView.setText(mTitle);

        this.findViewById(R.id.btn_action_back).setOnClickListener(v -> {
            if (mAgentWeb.getWebCreator().getWebView().canGoBack()) {
                mAgentWeb.back();
            } else {
                finish();
            }
        });

        this.findViewById(R.id.btn_action_refresh).setOnClickListener(v -> mAgentWeb.getUrlLoader().reload());

        mAgentWeb = AgentWeb.with(this)
                .setAgentWebParent(mFrameLayout, new LinearLayout.LayoutParams(-1, -1))
                .useDefaultIndicator()
                .setWebChromeClient(mWebChromeClient)
                .setWebViewClient(mWebViewClient)
                .setMainFrameErrorView(R.layout.agentweb_error_page, -1)
                .setSecurityType(AgentWeb.SecurityType.STRICT_CHECK)
                // .setWebLayout(new WebLayout(this))
                .setOpenOtherPageWays(DefaultWebClient.OpenOtherPageWays.ASK)
                // 打开其他应用时，弹窗咨询用户是否前往其他应用
                .interceptUnkownUrl()
                // 拦截找不到相关页面的Scheme
                .createAgentWeb().ready().go(mWebUrl);

    }

    private com.just.agentweb.WebViewClient mWebViewClient = new WebViewClient() {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            Log.i("WebActivity", "shouldOverrideUrlLoading ===> " + view.getUrl());
            mWebUrl = view.getUrl();
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mAgentWeb.handleKeyEvent(keyCode, event)) {
            return true;
        }
        finish();
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onPause() {
        mAgentWeb.getWebLifeCycle().onPause();
        super.onPause();

    }

    @Override
    protected void onResume() {
        mAgentWeb.getWebLifeCycle().onResume();
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i("Info", "onResult:" + requestCode + " onResult:" + resultCode);
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //mAgentWeb.destroy();
        mAgentWeb.getWebLifeCycle().onDestroy();
    }
}
