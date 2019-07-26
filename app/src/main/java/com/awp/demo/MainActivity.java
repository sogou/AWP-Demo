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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
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
import com.awp.webkit.AwpVersion;

import java.util.concurrent.Executors;

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

    private View mCustomView;
    private FrameLayout mFullscreenContainer;
    private WebChromeClient.CustomViewCallback mCustomViewCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initWebView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mWebView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mWebView.onPause();
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
        if (meetApiRequirement(21)) {
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            CookieManager.getInstance().setAcceptThirdPartyCookies(mWebView, true);
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
                CookieSyncManager.getInstance().sync();
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

            @Override
            public void onShowCustomView(View view, WebChromeClient.CustomViewCallback callback) {
                onShowDefaultCustomView(view, callback);
            }

            @Override
            public void onHideCustomView() {
                onHideDefaulCustomView();
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
                    Log.i(TAG, "onFirstVisuallyNonEmptyPaint, url=" + url);
                }

                @Override
                public void onDocumentConstructed(WebView view, boolean isMainFrame) {
                    super.onDocumentConstructed(view, isMainFrame);
                    Log.i(TAG, "onDocumentConstructed, url=" + view.getUrl());
                }

                @Override
                public boolean shouldIgnoreNavigation(WebView view, String url, String referrer, boolean isReload, boolean isRedirect, boolean shouldReplaceCurrentHistory) {
                    return !isValidUrl(url);
                }

                @Override
                public boolean navigationBackForward(WebView view, int offset) {
                    Log.i(TAG, "navigationBackForward, offset=" + offset);
                    return true;
                }

                @Override
                public void onTitleBarChanged(float titleBarOffsetY, float topContentOffsetY) {
                    Log.i(TAG, "onTitleBarChanged, titleBarOffsetY=" + titleBarOffsetY + "; topContentOffsetY=" + topContentOffsetY);
                    mLlTitleBar.setTranslationY(titleBarOffsetY);
                    setProgressBarPosition(titleBarOffsetY);
                }
            });
        }
        CookieManager.getInstance().setAcceptCookie(true);
        CookieSyncManager.createInstance(this);
        goHome();
    }

    private void setWebViewPosition() {
        FrameLayout webviewParent = (FrameLayout) findViewById(R.id.fl_webview);
        AwpExtension extension = AwpEnvironment.getInstance().getAwpExtension(mWebView);
        if (extension == null) { // 当前使用的为系统WebView
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

    boolean isAdBlockEnabled() {
        AwpSharedStatics statics = AwpEnvironment.getInstance().getSharedStatics();
        return statics != null && statics.getAdBlockEnabled();
    }

    void switchAdBlockStatus() {
        AwpSharedStatics statics = AwpEnvironment.getInstance().getSharedStatics();
        if (statics != null) {
            statics.setAdBlockEnabled(!statics.getAdBlockEnabled());
        }
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

    String getAwpCoreVersion() {
        String version = AwpVersion.getAwpCoreVersion();
        if (TextUtils.isEmpty(version)) {
            return "AWP Core: null";
        }
        return "AWP Core: " + version;
    }

    void setProxyOverride(final String proxyRule) {
        if (TextUtils.isEmpty(proxyRule)) return;
        SharedPrefsUtils.getInstance().put(SharedPrefsUtils.PROXY, proxyRule);
        AwpSharedStatics statics = AwpEnvironment.getInstance().getSharedStatics();
        if (statics != null) {
            String[][] rules = new String[1][];
            rules[0] = new String[2];
            rules[0][1] = proxyRule;
            statics.setProxyOverride(rules, null,
                new Runnable() {
                    @Override
                    public void run() {
                        Log.i(TAG, "Set proxy override (" + proxyRule + ") successfully!");
                    }
                },
                Executors.newSingleThreadExecutor());
        }
    }

    void clearProxyOverride() {
        SharedPrefsUtils.getInstance().remove(SharedPrefsUtils.PROXY);
        AwpSharedStatics statics = AwpEnvironment.getInstance().getSharedStatics();
        if (statics != null) {
            statics.clearProxyOverride(
                new Runnable() {
                    @Override
                    public void run() {
                        Log.i(TAG, "Clear proxy override successfully!");
                    }
                },
                Executors.newSingleThreadExecutor());
        }
    }

    private boolean isValidUrl(String url) {
        return !TextUtils.isEmpty(url) && (url.startsWith("http://")
                    || url.startsWith("https://")
                    || url.startsWith("about:"));
    }

    private void loadUrl(String url) {
        if (!isValidUrl(url)) {
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
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && mWebView != null
                && mWebView.canGoBack()
                && mCustomView == null) {
            goBack();
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    static class FullscreenHolder extends FrameLayout {
        public FullscreenHolder(Context ctx) {
            super(ctx);
            setBackgroundColor(0xFF000000);
        }

        @Override
        public boolean onTouchEvent(MotionEvent evt) {
            return true;
        }
    }

    private void onShowDefaultCustomView(
            View view, WebChromeClient.CustomViewCallback callback) {
        if (mCustomView != null) {
            return;
        }
        FrameLayout decor = (FrameLayout) getWindow().getDecorView();
        mFullscreenContainer = new FullscreenHolder(this);
        FrameLayout.LayoutParams params =
            new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
        mFullscreenContainer.addView(view, params);
        decor.addView(mFullscreenContainer, params);
        mCustomView = view;
        setFullscreen(true);
        mCustomViewCallback = callback;
    }

    private void onHideDefaulCustomView() {
        if (mFullscreenContainer == null || mCustomViewCallback == null) {
            return;
        }
        setFullscreen(false);
        FrameLayout decor = (FrameLayout) getWindow().getDecorView();
        decor.removeView(mFullscreenContainer);
        mFullscreenContainer.removeView(mCustomView);
        mFullscreenContainer.setVisibility(View.GONE);
        mFullscreenContainer = null;
        mCustomView = null;
        mCustomViewCallback.onCustomViewHidden();
        mCustomViewCallback = null;
    }

    private void setFullscreen(boolean enabled) {
        View decor = getWindow().getDecorView();
        if (enabled) {
            getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return;
        }

        int systemUiVisibility = decor.getSystemUiVisibility();
        int flags = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        if (enabled) {
            systemUiVisibility |= flags;
        } else {
            systemUiVisibility &= ~flags;
        }
        decor.setSystemUiVisibility(systemUiVisibility);
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
