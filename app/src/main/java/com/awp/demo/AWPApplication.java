// Copyright 2018 The Sogou Inc. All rights reserved.
// Author: AWP TEAM.

package com.awp.demo;

import android.app.Application;

import com.awp.webkit.AwpEnvironment;

public class AWPApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AwpEnvironment.init(this, false);
    }

}
