// Copyright 2018 The Sogou Inc. All rights reserved.
// Author: AWP TEAM.

package com.awp.demo;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.DownloadListener;
import android.webkit.GeolocationPermissions;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.awp.webkit.AwpEnvironment;
import com.awp.webkit.AwpExtension;
import com.awp.webkit.AwpExtensionClient;
import com.awp.webkit.AwpSharedStatics;

public class MainActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "AWPDemo";
    private static final String HOME_URL = "http://dh.123.sogou.com/";
    private static final int ALPHA_ENABLED = 255;
    private static final int ALPHA_DISABLED = 127;

    private LinearLayout mLlTitleBar;
    private EditText mEtUrl;
    private ProgressBar mProgressBar;
    private WebView mWebView;
    private ImageButton mBtnBack;
    private ImageButton mBtnForward;

    private int mTitlebarHeight;
    private boolean mIsWebContentsDebuggingEnabled = true;
    private boolean mIsUsingCustomErrorPage = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initWebView();
    }

    private void initViews() {
        mTitlebarHeight = getResources().getDimensionPixelSize(R.dimen.titlebar_height);
        mLlTitleBar = (LinearLayout) findViewById(R.id.ll_titlebar);
        mEtUrl = (EditText) findViewById(R.id.et_url);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mWebView = (WebView) findViewById(R.id.webview);
        mBtnBack = (ImageButton) findViewById(R.id.btn_back);
        mBtnForward = (ImageButton) findViewById(R.id.btn_forward);
        mEtUrl.setText(HOME_URL);
        mEtUrl.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                loadUrl(mEtUrl.getText().toString());
                return true;
            }
        });
        findViewById(R.id.btn_go).setOnClickListener(this);
        mBtnBack.setOnClickListener(this);
        mBtnForward.setOnClickListener(this);
        findViewById(R.id.btn_reload).setOnClickListener(this);
        findViewById(R.id.btn_home).setOnClickListener(this);
        findViewById(R.id.btn_menu).setOnClickListener(this);
        setButtonState();
    }

    private void initWebView() {
        setWebViewPosition();
        AwpEnvironment.getInstance().setAwpDebuggingEnabled(mIsWebContentsDebuggingEnabled);
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBuiltInZoomControls(true);
        if (meetApiRequirement(Build.VERSION_CODES.HONEYCOMB)) {
            webSettings.setDisplayZoomControls(false);
            webSettings.setEnableSmoothTransition(true);
            webSettings.setAllowContentAccess(true);
        }
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setNeedInitialFocus(false);
        webSettings.setAppCacheEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setDomStorageEnabled(true);
        if (meetApiRequirement(Build.VERSION_CODES.JELLY_BEAN)) {
            webSettings.setAllowUniversalAccessFromFileURLs(false);
            webSettings.setAllowFileAccessFromFileURLs(false);
        }
        if (meetApiRequirement(Build.VERSION_CODES.L)) {
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                mProgressBar.setVisibility(View.VISIBLE);
                mEtUrl.setText(url);
                AwpExtension extension = AwpEnvironment.getInstance().getAwpExtension(mWebView);
                if (extension != null) {
                    extension.showTitleBar();
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                setButtonState();
                mProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }
        });
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onGeolocationPermissionsShowPrompt(String origin,
                                                           GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, true);
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                mProgressBar.setProgress(newProgress);
            }
        });
        mWebView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition,
                                        String mimetype, long contentLength) {
                Log.i(TAG, "onDownloadStart, url=" + url);
            }
        });
        final AwpExtension extension = AwpEnvironment.getInstance().getAwpExtension(mWebView);
        if (extension != null) {
            extension.getAwpSettings().setSmartImagesEnabled(false);
            extension.setTitleBarHeight(mTitlebarHeight);
            extension.showTitleBar();
            extension.setAwpExtensionClient(new AwpExtensionClient() {
                @Override
                public void onFirstVisuallyNonEmptyPaint(WebView view, String url) {
                    super.onFirstVisuallyNonEmptyPaint(view, url);
                }

                @Override
                public void onDocumentConstructed(WebView view, boolean isMainFrame) {
                    super.onDocumentConstructed(view, isMainFrame);
                }

                @Override
                public boolean shouldIgnoreNavigation(WebView view, String url, String referrer, boolean isReload, boolean isRedirect, boolean shouldReplaceCurrentHistory) {
                    return false;
                }

                @Override
                public boolean navigationBackForward(WebView view, int offset) {
                    return true;
                }

                @Override
                public void onTitleBarChanged(float titleBarOffsetY, float topContentOffsetY) {
                    mLlTitleBar.setTranslationY(titleBarOffsetY);
                    setProgressBarPosition(titleBarOffsetY);
                }
            });
        }
        goHome();
    }

    private void setWebViewPosition() {
        FrameLayout webviewParent = (FrameLayout) findViewById(R.id.fl_webview);
        AwpExtension extension = AwpEnvironment.getInstance().getAwpExtension(mWebView);
        if (extension == null) {
            // 当前使用的为系统WebView
            RelativeLayout.LayoutParams webviewParentLayoutParams =
                    (RelativeLayout.LayoutParams) webviewParent.getLayoutParams();
            webviewParentLayoutParams.topMargin = getResources().getDimensionPixelSize(R.dimen.titlebar_height);
            webviewParent.setLayoutParams(webviewParentLayoutParams);

            ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar);
            FrameLayout.LayoutParams progressbarLayoutParams =
                    (FrameLayout.LayoutParams) progressBar.getLayoutParams();
            progressbarLayoutParams.topMargin = 0;
            progressBar.setLayoutParams(progressbarLayoutParams);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_go:
                loadUrl(mEtUrl.getText().toString());
                break;
            case R.id.btn_back:
                goBack();
                break;
            case R.id.btn_forward:
                goForward();
                break;
            case R.id.btn_reload:
                reload();
                break;
            case R.id.btn_home:
                goHome();
                break;
            case R.id.btn_menu:
                AwpExtension extension = AwpEnvironment.getInstance().getAwpExtension(mWebView);
                if (extension == null) {
                    Toast.makeText(this, R.string.awp_uninitialized, Toast.LENGTH_SHORT).show();
                } else {
                    MenuPopupWindow window = new MenuPopupWindow(this);
                    window.show(v);
                }
                break;
            default:
                break;
        }
    }

    void switchAdBlockStatus() {
        AwpSharedStatics statics = AwpEnvironment.getInstance().getSharedStatics();
        if (statics != null) {
            statics.setAdBlockEnabled(!statics.getAdBlockEnabled());
        }
    }

    boolean isAdBlockEnabled() {
        AwpSharedStatics statics = AwpEnvironment.getInstance().getSharedStatics();
        return statics != null && statics.getAdBlockEnabled();
    }

    void switchWebContentsDebuggingEnableStatus() {
        mIsWebContentsDebuggingEnabled = !mIsWebContentsDebuggingEnabled;
        AwpEnvironment.getInstance().setAwpDebuggingEnabled(mIsWebContentsDebuggingEnabled);
    }

    boolean isWebContentsDebuggingEnabled() {
        return mIsWebContentsDebuggingEnabled;
    }

    void switchSmartImagesStatus() {
        AwpExtension extension = AwpEnvironment.getInstance().getAwpExtension(mWebView);
        if (extension != null) {
            extension.getAwpSettings().setSmartImagesEnabled(!extension.getAwpSettings().getSmartImagesEnabled());
        }
    }

    boolean isSmartImagesEnabled() {
        AwpExtension extension = AwpEnvironment.getInstance().getAwpExtension(mWebView);
        return extension != null && extension.getAwpSettings().getSmartImagesEnabled();
    }

    void switchNightModeStatus() {
        AwpExtension extension = AwpEnvironment.getInstance().getAwpExtension(mWebView);
        if (extension != null) {
            extension.getAwpSettings().setNightModeEnabled(!extension.getAwpSettings().getNightModeEnabled());
        }
    }

    boolean isNightModeEnabled() {
        AwpExtension extension = AwpEnvironment.getInstance().getAwpExtension(mWebView);
        return extension != null && extension.getAwpSettings().getNightModeEnabled();
    }

    void setCustomErrorPage() {
        AwpSharedStatics statics = AwpEnvironment.getInstance().getSharedStatics();
        if (statics != null) {
            mIsUsingCustomErrorPage = !mIsUsingCustomErrorPage;
            statics.setErrorPageAssetFilePath(mIsUsingCustomErrorPage ? "error/http404_ad.html" : null);
        }
    }

    void setFastScrollThumb() {
        AwpExtension extension = AwpEnvironment.getInstance().getAwpExtension(mWebView);
        if (extension != null) {
            extension.getAwpSettings().setFastScrollThumbEnabled(!extension.getAwpSettings().getFastScrollThumbEnabled());
        }
    }

    boolean isFastScrollThumbEnabled() {
        AwpExtension extension = AwpEnvironment.getInstance().getAwpExtension(mWebView);
        return extension != null && extension.getAwpSettings().getFastScrollThumbEnabled();
    }

    boolean isCustonErrorPageSet() {
        return mIsUsingCustomErrorPage;
    }

    private void loadUrl(String url) {
        if (TextUtils.isEmpty(url) || url.trim().length() == 0) {
            return;
        }
        String fixedUrl = UrlUtils.smartUrlFilter(url);
        mWebView.loadUrl(fixedUrl);
        mEtUrl.clearFocus();
        setSoftInputVisibility(false);
        mWebView.requestFocus();
    }

    private boolean goBack() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        return false;
    }

    private boolean goForward() {
        if (mWebView.canGoForward()) {
            mWebView.goForward();
            return true;
        }
        return false;
    }

    private void reload() {
        mWebView.reload();
    }

    private void goHome() {
        loadUrl(HOME_URL);
    }

    private void setSoftInputVisibility(boolean visible) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            if (visible) {
                imm.showSoftInput(mEtUrl, InputMethodManager.SHOW_IMPLICIT);
            } else {
                imm.hideSoftInputFromWindow(mEtUrl.getWindowToken(), 0);
            }
        }
    }

    private void setButtonState() {
        if (mWebView.canGoBack()) {
            mBtnBack.setAlpha(ALPHA_ENABLED);
        } else {
            mBtnBack.setAlpha(ALPHA_DISABLED);
        }
        if (mWebView.canGoForward()) {
            mBtnForward.setAlpha(ALPHA_ENABLED);
        } else {
            mBtnForward.setAlpha(ALPHA_DISABLED);
        }
    }

    private void setProgressBarPosition(float titleBarOffsetY) {
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mProgressBar.getLayoutParams();
        if (titleBarOffsetY == -mTitlebarHeight) {
            params.topMargin = 0;
        } else if (titleBarOffsetY == 0) {
            params.topMargin = mTitlebarHeight;
        }
        mProgressBar.setLayoutParams(params);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && goBack()) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mWebView.freeMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mWebView != null) {
            mWebView.clearCache(true);
            mWebView.destroy();
        }
    }

    private static boolean meetApiRequirement(int requires) {
        return Build.VERSION.SDK_INT >= requires;
    }

}
