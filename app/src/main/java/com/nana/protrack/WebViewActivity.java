package com.nana.protrack;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Environment;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class WebViewActivity extends AppCompatActivity {
    //WebView webView;
    private VideoEnabledWebView webView;
    private VideoEnabledWebChromeClient webChromeClient;
    private static final String TAG = MainActivity.class.getSimpleName();
    private String mCM, webViewUrl, urlAddress, userName, userPassword, url, remember, wsResponse;
    private ValueCallback<Uri> mUM;
    private ValueCallback<Uri[]> mUMA;
    private final static int FCR = 1;
    protected Cursor cursor;
    DataHelper dbHelper;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (Build.VERSION.SDK_INT >= 21) {
            Uri[] results = null;
            //Check if response is positive
            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == FCR) {
                    if (null == mUMA) {
                        return;
                    }
                    if (intent == null) {
                        //Capture Photo if no image available
                        if (mCM != null) {
                            results = new Uri[]{Uri.parse(mCM)};
                        }
                    } else {
                        String dataString = intent.getDataString();
                        if (dataString != null) {
                            results = new Uri[]{Uri.parse(dataString)};
                        }
                    }
                }
            }
            mUMA.onReceiveValue(results);
            mUMA = null;
        } else {
            if (requestCode == FCR) {
                if (null == mUM) return;
                Uri result = intent == null || resultCode != RESULT_OK ? null : intent.getData();
                mUM.onReceiveValue(result);
                mUM = null;
            }
        }
    }

    @SuppressLint({"SetJavaScriptEnabled", "WrongViewCast"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        if (Build.VERSION.SDK_INT >= 23 && (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(WebViewActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 1);
        }

        webView = (VideoEnabledWebView) findViewById(R.id.ifView);
        assert webView != null;
        WebSettings webSettings = webView.getSettings();
        webSettings.setAllowFileAccess(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setGeolocationEnabled(true);


        if (Build.VERSION.SDK_INT >= 21) {
            webSettings.setMixedContentMode(0);
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else if (Build.VERSION.SDK_INT >= 19) {
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else if (Build.VERSION.SDK_INT < 19) {
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        webView.setWebViewClient(new webViewClient());
        View nonVideoLayout = findViewById(R.id.nonVideoLayout); // Your own view, read class comments
        ViewGroup videoLayout = (ViewGroup) findViewById(R.id.videoLayout); // Your own view, read class comments
        dbHelper = new DataHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        cursor = db.rawQuery("select URL_ADDRESS, USERNAME, PASSWORD from LOGINDATA WHERE FL_REMEMBER='Y'", null);
        cursor.moveToFirst();
        if (!cursor.isAfterLast()) {
            urlAddress = cursor.getString(0).toString();
            userName = cursor.getString(1).toString();
            userPassword = cursor.getString(2).toString();
        }

        Bundle bundle = getIntent().getExtras();
        remember = bundle.getString("remember");
        wsResponse = bundle.getString("wsresponse");


//        if(remember.equalsIgnoreCase("false")){
//            url = str1+"/login.zul";
//        }else{
//            url = bundle.getString("url")+"/login.zul";
//        }

        if (remember.equalsIgnoreCase("true")) {
            if (wsResponse != null && wsResponse.equalsIgnoreCase("1")) {
                url = urlAddress + "/page/protrack/index.zul?user=" + userName + "&password=" + userPassword;
            } else if (wsResponse != null && wsResponse.equalsIgnoreCase("2")) {
                url = urlAddress + "/page/protrack/index.zul?user=" + userName + "&password=" + userPassword;
            }
        } else {
            if (wsResponse != null && wsResponse.equalsIgnoreCase("1")) {
                url = bundle.getString("url");
            } else if (wsResponse != null && wsResponse.equalsIgnoreCase("2")) {
                url = bundle.getString("url");
            } else if (wsResponse != null && wsResponse.equalsIgnoreCase("Success")) {
                url = bundle.getString("url");
            } else {
                url = bundle.getString("url") + "/page/protrack/index.zul";
            }

        }

//        webViewUrl = "https://indisch.co.id:8085/estim";
        webViewUrl = url;
//        Toast.makeText(getApplicationContext(),remember+webViewUrl, Toast.LENGTH_LONG).show();
//        webView.loadUrl("https://estim.co.id:8085/estim-demo/login.zul");
        webView.loadUrl(webViewUrl);
        //webView.setWebChromeClient(new webChromeClient());
        webView.addJavascriptInterface(new WebAppInterface(this), "Android");

        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                request.setMimeType(mimetype);
                //------------------------COOKIE!!------------------------
                String cookies = CookieManager.getInstance().getCookie(url);
                request.addRequestHeader("cookie", cookies);
                //------------------------COOKIE!!------------------------
                request.addRequestHeader("User-Agent", userAgent);
                request.setDescription("Downloading file...");
                request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimetype));
                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(url, contentDisposition, mimetype));
                DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                dm.enqueue(request);
                Toast.makeText(getApplicationContext(), "Downloading File", Toast.LENGTH_LONG).show();
            }
        });

        webChromeClient = new VideoEnabledWebChromeClient(nonVideoLayout, videoLayout, null, webView) { // See all available constructors...
            private static final String TAG = "MyActivity";

            @Override
            public void onGeolocationPermissionsShowPrompt(final String origin, final GeolocationPermissions.Callback callback) {
                Log.i(TAG, "onGeolocationPermissionsShowPrompt()");

                callback.invoke(origin, true, true);
//                final boolean remember = false;
//                AlertDialog.Builder builder = new AlertDialog.Builder(WebViewActivity.this);
//                builder.setTitle("Locations Permission");
//                builder.setMessage("Would like to use your Current Location ")
//                        .setCancelable(true).setPositiveButton("Allow", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        // origin, allow, remember
////                        callback.invoke(origin, true, remember);
//                    }
//                }).setNegativeButton("Don't Allow", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        // origin, allow, remember
//                        callback.invoke(origin, false, remember);
//                    }
//                });
//                AlertDialog alert = builder.create();
//                alert.show();
            }

            //For Android 3.0+
            public void openFileChooser(ValueCallback<Uri> uploadMsg) {
                mUM = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("*/*");
                WebViewActivity.this.startActivityForResult(Intent.createChooser(i, "File Chooser"), FCR);
            }

            // For Android 3.0+, above method not supported in some android 3+ versions, in such case we use this
            public void openFileChooser(ValueCallback uploadMsg, String acceptType) {
                mUM = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("*/*");
                WebViewActivity.this.startActivityForResult(
                        Intent.createChooser(i, "File Browser"),
                        FCR);
            }

            //For Android 4.1+
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                mUM = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("*/*");
                WebViewActivity.this.startActivityForResult(Intent.createChooser(i, "File Chooser"), WebViewActivity.FCR);
            }

            //For Android 5.0+
            public boolean onShowFileChooser(
                    WebView webView, ValueCallback<Uri[]> filePathCallback,
                    WebChromeClient.FileChooserParams fileChooserParams) {
                if (mUMA != null) {
                    mUMA.onReceiveValue(null);
                }
                mUMA = filePathCallback;
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(WebViewActivity.this.getPackageManager()) != null) {
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                        takePictureIntent.putExtra("PhotoPath", mCM);
                    } catch (IOException ex) {
                        Log.e(TAG, "Image file creation failed", ex);
                    }
                    if (photoFile != null) {
                        mCM = "file:" + photoFile.getAbsolutePath();
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                    } else {
                        takePictureIntent = null;
                    }
                }
                Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
                contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
                contentSelectionIntent.setType("*/*");
                Intent[] intentArray;
                if (takePictureIntent != null) {
                    intentArray = new Intent[]{takePictureIntent};
                } else {
                    intentArray = new Intent[0];
                }

                Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
                chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
                chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
                startActivityForResult(chooserIntent, FCR);
                return true;
            }
        };

        webChromeClient.setOnToggledFullscreen(new VideoEnabledWebChromeClient.ToggledFullscreenCallback() {
            @Override
            public void toggledFullscreen(boolean fullscreen) {
                // Your code to handle the full-screen change, for example showing and hiding the title bar. Example:
                if (fullscreen) {
                    //Toast.makeText(getApplicationContext(), "Full2",
                    //       Toast.LENGTH_LONG).show();
                    WindowManager.LayoutParams attrs = getWindow().getAttributes();
                    attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
                    attrs.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                    getWindow().setAttributes(attrs);
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

                    if (android.os.Build.VERSION.SDK_INT >= 14) {
                        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
                    }
                } else {
                    //Toast.makeText(getApplicationContext(), "Min2",
                    //       Toast.LENGTH_LONG).show();
                    WindowManager.LayoutParams attrs = getWindow().getAttributes();
                    attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
                    attrs.flags &= ~WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                    getWindow().setAttributes(attrs);
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    if (android.os.Build.VERSION.SDK_INT >= 14) {
                        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                    }
                }

            }
        });
        webView.setWebChromeClient(webChromeClient);

    }


//    public class webChromeClient extends WebChromeClient {
//        private static final String TAG = "MyActivity";
//
//        @Override
//        public void onGeolocationPermissionsShowPrompt(final String origin, final GeolocationPermissions.Callback callback) {
//            Log.i(TAG, "onGeolocationPermissionsShowPrompt()");
//
//            final boolean remember = false;
//            AlertDialog.Builder builder = new AlertDialog.Builder(WebViewActivity.this);
//            builder.setTitle("Locations Permission");
//            builder.setMessage("Would like to use your Current Location ")
//                    .setCancelable(true).setPositiveButton("Allow", new DialogInterface.OnClickListener() {
//                public void onClick(DialogInterface dialog, int id) {
//                    // origin, allow, remember
//                    callback.invoke(origin, true, remember);
//                }
//            }).setNegativeButton("Don't Allow", new DialogInterface.OnClickListener() {
//                public void onClick(DialogInterface dialog, int id) {
//                    // origin, allow, remember
//                    callback.invoke(origin, false, remember);
//                }
//            });
//            AlertDialog alert = builder.create();
//            alert.show();
//        }
//
//        //For Android 3.0+
//        public void openFileChooser(ValueCallback<Uri> uploadMsg) {
//            mUM = uploadMsg;
//            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
//            i.addCategory(Intent.CATEGORY_OPENABLE);
//            i.setType("*/*");
//            WebViewActivity.this.startActivityForResult(Intent.createChooser(i, "File Chooser"), FCR);
//        }
//
//        // For Android 3.0+, above method not supported in some android 3+ versions, in such case we use this
//        public void openFileChooser(ValueCallback uploadMsg, String acceptType) {
//            mUM = uploadMsg;
//            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
//            i.addCategory(Intent.CATEGORY_OPENABLE);
//            i.setType("*/*");
//            WebViewActivity.this.startActivityForResult(
//                    Intent.createChooser(i, "File Browser"),
//                    FCR);
//        }
//
//        //For Android 4.1+
//        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
//            mUM = uploadMsg;
//            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
//            i.addCategory(Intent.CATEGORY_OPENABLE);
//            i.setType("*/*");
//            WebViewActivity.this.startActivityForResult(Intent.createChooser(i, "File Chooser"), WebViewActivity.FCR);
//        }
//
//        //For Android 5.0+
//        public boolean onShowFileChooser(
//                WebView webView, ValueCallback<Uri[]> filePathCallback,
//                WebChromeClient.FileChooserParams fileChooserParams) {
//            if (mUMA != null) {
//                mUMA.onReceiveValue(null);
//            }
//            mUMA = filePathCallback;
//            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//            if (takePictureIntent.resolveActivity(WebViewActivity.this.getPackageManager()) != null) {
//                File photoFile = null;
//                try {
//                    photoFile = createImageFile();
//                    takePictureIntent.putExtra("PhotoPath", mCM);
//                } catch (IOException ex) {
//                    Log.e(TAG, "Image file creation failed", ex);
//                }
//                if (photoFile != null) {
//                    mCM = "file:" + photoFile.getAbsolutePath();
//                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
//                } else {
//                    takePictureIntent = null;
//                }
//            }
//            Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
//            contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
//            contentSelectionIntent.setType("*/*");
//            Intent[] intentArray;
//            if (takePictureIntent != null) {
//                intentArray = new Intent[]{takePictureIntent};
//            } else {
//                intentArray = new Intent[0];
//            }
//
//            Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
//            chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
//            chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser");
//            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
//            startActivityForResult(chooserIntent, FCR);
//            return true;
//        }
//
//    }

    public class webViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(WebViewActivity.this);
            String message = "SSL Certificate error.";
            switch (error.getPrimaryError()) {
                case SslError.SSL_UNTRUSTED:
                    message = "The certificate authority is not trusted.";
                    break;
                case SslError.SSL_EXPIRED:
                    message = "The certificate has expired.";
                    break;
                case SslError.SSL_IDMISMATCH:
                    message = "The certificate Hostname mismatch.";
                    break;
                case SslError.SSL_NOTYETVALID:
                    message = "The certificate is not yet valid.";
                    break;
            }
            message += " Do you want to continue anyway?";

            builder.setTitle("SSL Certificate Error");
            builder.setMessage(message);
            builder.setPositiveButton("continue", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    handler.proceed();
                }
            });
            builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    handler.cancel();
                }
            });
            final AlertDialog dialog = builder.create();
            dialog.show();
        }

        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
//            view.loadUrl("file:///android_asset/error.html");
//            Toast.makeText(getApplicationContext(), "Failed loading app!", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(getApplicationContext(), ErrorActivity.class);
//            AutoCompleteTextView textView = (AutoCompleteTextView) findViewById(R.id.autocomplete);
//            String getrec=textView.getText().toString();

            //Create the bundle
            Bundle bundle = new Bundle();

            //Add your data to bundle
            bundle.putString("remember", remember);
            bundle.putString("url", url);

            //Add the bundle to the intent
            i.putExtras(bundle);

            //Fire that second activity
            startActivity(i);
        }


    }

    // Create an image file
    private File createImageFile() throws IOException {
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "img_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

//    @Override
//    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
//        if (event.getAction() == KeyEvent.ACTION_DOWN) {
//            switch (keyCode) {
//                case KeyEvent.KEYCODE_BACK:
//                    if (webView.canGoBack()) {
//                        webView.goBack();
//                    } else {
//                        finish();
//                    }
//                    return true;
//            }
//        }
//        return super.onKeyDown(keyCode, event);
//    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Really Exit?")
                .setMessage("Are you sure you want to exit?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) {
                        WebViewActivity.super.onBackPressed();
                    }
                }).create().show();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public class WebAppInterface {
        Context mContext;

        /**
         * Instantiate the interface and set the context
         */
        WebAppInterface(Context c) {
            mContext = c;
        }

        /**
         * Javascript method
         */
        @JavascriptInterface

        public void shareLink(final String link) {
//            Toast.makeText(mContext, link, Toast.LENGTH_SHORT).show();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i("Share Link in", "Share Link in");
                    if (link != null && link.equalsIgnoreCase("login") && !link.equalsIgnoreCase("kosong")) {
                        Intent loginIntent = new Intent(WebViewActivity.this, LoginActivity.class);
                        startActivity(loginIntent);
                        finish();
                    } else if (link != null && link.equalsIgnoreCase("mapservice")) {
                        Intent myservice = new Intent(WebViewActivity.this, MyService.class);
                        Log.i("Share Link in", "Share Link in --------- ");
                        startService(myservice);
                        finish();
                    } else if (link != null && link.equalsIgnoreCase("stopservice")) {
                        Intent myservice = new Intent(WebViewActivity.this, MyService.class);
                        Log.i("Stop Service", "Stop Service ----------- ");
                        stopService(myservice);
                        finish();
                    } else if (link != null && !link.equalsIgnoreCase("") && !link.equalsIgnoreCase("kosong")) {
                        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                        sharingIntent.setType("text/plain");
                        String shareBody = webView.getUrl();
//                Toast.makeText(getApplicationContext(), link, Toast.LENGTH_LONG).show();
                        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject Here");
                        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, link);
                        startActivity(Intent.createChooser(sharingIntent, "Share via"));
                    }
                }
            });
        }
    }

    public void deleteData() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("delete from LOGINDATA where FL_REMEMBER = 'Y'");
        finish();
    }
}
