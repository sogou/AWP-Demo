// Copyright 2018 The Sogou Inc. All rights reserved.
// Author: AWP TEAM.

package com.awp.demo;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;

class MenuPopupWindow extends PopupWindow implements View.OnClickListener {

    private static final int ENABLE_COLOR = Color.BLUE;
    private static final int DISABLE_COLOR = Color.BLACK;

    private MainActivity mActivity;
    private Button mBtnAdBlock;
    private Button mBtnWebContentsDebug;
    private Button mBtnSmartImages;
    private Button mBtnNightMode;
    private Button mBtnCustonErrorPage;
    private Button mBtnFastScrollThumb;

    MenuPopupWindow(MainActivity activity) {
        super(activity);
        mActivity = activity;
        init(activity);
    }

    private void init(Context context) {
        View contentView = LayoutInflater.from(context).inflate(R.layout.menu_popup, null);
        setContentView(contentView);
        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        setFocusable(true);
        setAnimationStyle(R.style.MenuPopupAnim);
        setBackgroundDrawable(new ColorDrawable(0xFFFFFFFF));
        mBtnAdBlock = (Button) contentView.findViewById(R.id.btn_ad_block);
        mBtnWebContentsDebug = (Button) contentView.findViewById(R.id.btn_webcontent_debug);
        mBtnSmartImages = (Button) contentView.findViewById(R.id.btn_smart_images);
        mBtnNightMode = (Button) contentView.findViewById(R.id.btn_night_mode);
        mBtnCustonErrorPage = (Button) contentView.findViewById(R.id.btn_error_page);
        mBtnFastScrollThumb = (Button) contentView.findViewById(R.id.btn_fast_scroll);
        mBtnAdBlock.setOnClickListener(this);
        mBtnWebContentsDebug.setOnClickListener(this);
        mBtnSmartImages.setOnClickListener(this);
        mBtnNightMode.setOnClickListener(this);
        mBtnCustonErrorPage.setOnClickListener(this);
        mBtnFastScrollThumb.setOnClickListener(this);
        initBtnsColor();
    }

    void show(View parent) {
        showAtLocation(parent, Gravity.BOTTOM, 0, 0);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_ad_block:
                if (mActivity.isAdBlockEnabled()) {
                    mBtnAdBlock.setTextColor(DISABLE_COLOR);
                } else {
                    mBtnAdBlock.setTextColor(ENABLE_COLOR);
                }
                mActivity.switchAdBlockStatus();
                dismiss();
                break;
            case R.id.btn_webcontent_debug:
                if (mActivity.isWebContentsDebuggingEnabled()) {
                    mBtnWebContentsDebug.setTextColor(DISABLE_COLOR);
                } else {
                    mBtnWebContentsDebug.setTextColor(ENABLE_COLOR);
                }
                mActivity.switchWebContentsDebuggingEnableStatus();
                dismiss();
                break;
            case R.id.btn_smart_images:
                if (mActivity.isSmartImagesEnabled()) {
                    mBtnSmartImages.setTextColor(DISABLE_COLOR);
                } else {
                    mBtnSmartImages.setTextColor(ENABLE_COLOR);
                }
                mActivity.switchSmartImagesStatus();
                dismiss();
                break;
            case R.id.btn_night_mode:
                if (mActivity.isNightModeEnabled()) {
                    mBtnNightMode.setTextColor(DISABLE_COLOR);
                } else {
                    mBtnNightMode.setTextColor(ENABLE_COLOR);
                }
                mActivity.switchNightModeStatus();
                dismiss();
                break;
            case R.id.btn_error_page:
                if (mActivity.isCustonErrorPageSet()) {
                    mBtnCustonErrorPage.setTextColor(DISABLE_COLOR);
                } else {
                    mBtnCustonErrorPage.setTextColor(ENABLE_COLOR);
                }
                mActivity.setCustomErrorPage();
                dismiss();
                break;
            case R.id.btn_fast_scroll:
                if (mActivity.isFastScrollThumbEnabled()) {
                    mBtnFastScrollThumb.setTextColor(DISABLE_COLOR);
                } else {
                    mBtnFastScrollThumb.setTextColor(ENABLE_COLOR);
                }
                mActivity.setFastScrollThumb();
                dismiss();
                break;
            default:
                break;
        }
    }

    private void initBtnsColor() {
        if (mActivity.isAdBlockEnabled()) {
            mBtnAdBlock.setTextColor(ENABLE_COLOR);
        } else {
            mBtnAdBlock.setTextColor(DISABLE_COLOR);
        }
        if (mActivity.isWebContentsDebuggingEnabled()) {
            mBtnWebContentsDebug.setTextColor(ENABLE_COLOR);
        } else {
            mBtnWebContentsDebug.setTextColor(DISABLE_COLOR);
        }
        if (mActivity.isSmartImagesEnabled()) {
            mBtnSmartImages.setTextColor(ENABLE_COLOR);
        } else {
            mBtnSmartImages.setTextColor(DISABLE_COLOR);
        }
        if (mActivity.isNightModeEnabled()) {
            mBtnNightMode.setTextColor(ENABLE_COLOR);
        } else {
            mBtnNightMode.setTextColor(DISABLE_COLOR);
        }
        if (mActivity.isCustonErrorPageSet()) {
            mBtnCustonErrorPage.setTextColor(ENABLE_COLOR);
        } else {
            mBtnCustonErrorPage.setTextColor(DISABLE_COLOR);
        }
        if (mActivity.isFastScrollThumbEnabled()) {
            mBtnFastScrollThumb.setTextColor(ENABLE_COLOR);
        } else {
            mBtnFastScrollThumb.setTextColor(DISABLE_COLOR);
        }
    }

}
