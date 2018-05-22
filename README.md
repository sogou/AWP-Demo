# Quick Start Guide to Using AWP(Accelerated WebView Project)
AWP is the extension of Android WebView put into a library for use on
mobile. It offers an easy-to-use, high performance, standards-compliant,
and secure way to use WebView. AWP has support for ALL Android version after
Ice Cream Sandwich. This demo gives a brief introduction to using AWP.

### Basics
First you will need to put `awp_sdk.aar` into your project's libs directory,
and add below codes to your build.gradle.

        android {

            aaptOptions {

                additionalParameters '--extra-packages', 'com.sogou.android.chromium:com.sogou.org.chromium.ui:com.sogou.com.android.webview.chromium:com.sogou.org.chromium.content:com.sogou.org.chromium.components.autofill:com.sogou.org.chromium.components.web_contents_delegate_android'
            }

            repositories {
                flatDir {
                    dirs 'libs'
                }
            }
        }

        dependencies {
            compile(name: 'awp_sdk', ext: 'aar')
        }

Second you will need to call `AwpEnvirnoment.init()` to initialize AWP's
envirnoment in `Application.onCreate()`.

        class MyApplication extends Application {
            @Override
            public void onCreate() {
                super.onCreate();
                AwpEnvironment.init(this, true);
            }
        }

Now, you can use many fantasy features of AWP like these:

        // Enables debugging of web contents (HTML / CSS / JavaScript)
        AwpEnvironment.getInstance().setAwpDebuggingEnabled(true);
        AwpExtension extension = AwpEnvironment.getInstance().getAwpExtension(mWebView);
        if (extension != null) {
            // Enables SmartImages loading
            extension.getAwpSettings().setSmartImagesEnabled(true);
            // Enables NightMode
            extension.getAwpSettings().setNightModeEnabled(true);
        }
        // Enables AdBlock
        AwpSharedStatics statics = AwpEnvironment.getInstance().getSharedStatics();
        if (statics != null) {
            statics.setAdBlockEnabled(true);
        }

        and so on ...

### Fantasy features

* Supports all Android version
* Nothing need to change, only add single line codes
* Friendly video playback
* Ad Block
* Night Mode

[<img src="https://dlmse.sogoucdn.com/awp/nightmode_slim.gif" width="270" height="480">](https://dlmse.sogoucdn.com/awp/nightmode_slim.gif)

* Smart Images loading

[<img src="https://dlmse.sogoucdn.com/awp/smartimage.gif" width="270" height="480">](https://dlmse.sogoucdn.com/awp/smartimage.gif)

* Fast ScrollThumb

[<img src="https://dlmse.sogoucdn.com/awp/fastscrollthumb_slim.gif" width="270" height="480">](https://dlmse.sogoucdn.com/awp/fastscrollthumb_slim.gif)

* Error Page

<img src="https://dlmse.sogoucdn.com/awp/errorpage.gif" width="270" height="480">

* Selection Menu

<img src="https://dlmse.sogoucdn.com/awp/selectmenu_page.gif" width="270" height="480" align="left">
<img src="https://dlmse.sogoucdn.com/awp/selectmenu_edited.gif" width="270" height="480">

### AWP Home
Very welcome to try [AWP Demo].
If you have any problem or suggestion, please let me know [AWP Home].

There are also useful examples for Developing apps with the [Android WebView].

[AWP Demo]: https://dlmse.sogoucdn.com/awp/AWPDemo.apk
[AWP Home]: http://awp.mse.sogou.com/
[Android WebView]: https://github.com/googlearchive/chromium-webview-samples
