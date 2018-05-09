package com.excel.welcomeguestapp.secondgen;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;

import com.excel.configuration.ConfigurationReader;
import com.excel.excelclasslibrary.UtilNetwork;
import com.excel.excelclasslibrary.UtilShell;
import com.excel.excelclasslibrary.UtilURL;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    RelativeLayout rl_webview, rl_video_screen;
    WebView wv_welcome_screen;
    ConfigurationReader configurationReader;
    Context context;

    public static final String TAG = "WelcomeGuestApp";

    String[] permissions = {
            // Manifest.permission.RECEIVE_BOOT_COMPLETED, // Normal Permission
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.INTERNET,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            // Manifest.permission.WRITE_SETTINGS   // Special Permission -> https://developer.android.com/reference/android/Manifest.permission.html#WRITE_SETTINGS
    };

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        //setContentView( R.layout.activity_main );

        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {
            if ( checkPermissions() ) {
                // permissions  granted.
                init();
            }
        }
        else{
            init();
        }
    }

    private void init(){
        configurationReader = ConfigurationReader.reInstantiate();
        context = this;

        initViews();

        checkConfiguration();

    }

    private void initViews(){
        rl_webview = (RelativeLayout) findViewById( R.id.rl_webview );
        rl_video_screen = (RelativeLayout) findViewById( R.id.rl_video_screen );
        wv_welcome_screen = (WebView) findViewById( R.id.wv_welcome_screen );


        context.deleteDatabase("webview.db");
        context.deleteDatabase("webviewCache.db");

    }

    private void checkConfiguration(){
        if( configurationReader.getWelcomeScreenType().equals( "webview" ) )
            configureWebView();
        else
            configureVideoView();

    }

    private void configureWebView(){
        Log.d( TAG, "configureWebView()" );

        wv_welcome_screen = new WebView( context );// (WebView) findViewById( R.id.wv_welcome_screen );
        wv_welcome_screen.clearCache(true);
        wv_welcome_screen.setFocusable( false );
        wv_welcome_screen.getSettings().setJavaScriptEnabled( true );
        wv_welcome_screen.getSettings().setAppCacheEnabled( false );

        wv_welcome_screen.setWebViewClient(new WebViewClient() {

            public void onPageFinished(WebView webview, String url){
                Log.d( TAG, "page finished" );
                super.onPageFinished(webview, url);
            }

            public void onPageStarted(WebView webview, String s, Bitmap bitmap){
                Log.d( TAG, "page started " );
                super.onPageStarted(webview, s, bitmap);
            }

            public void onReceivedError( WebView view, int errorCode, String description, String failingUrl ) {
                Log.e( TAG, "error "+description );
                wv_welcome_screen.loadUrl( "file:///android_asset/local_welcome/index.html" );
            }

        });
        wv_welcome_screen.loadUrl( UtilURL.getWebserviceURL() + "?what_do_you_want=url_forward&url_type=welcome_screen&mac_address="+ UtilNetwork.getMacAddress( context ) );
        //Log.d( null, UtilURL.getWebserviceURL() + "?what_do_you_want=url_forward&url_type=welcome_screen&mac_address="+ UtilNetwork.getMacAddress( context ) );
        setContentView( wv_welcome_screen );

        //setIsWelcomeScreenShown( true );
    }

    private void configureVideoView(){
        Log.d( TAG, "configureVideoView()" );
    }

    public boolean onKeyDown( int i, KeyEvent keyevent ){
        KeyEvent.keyCodeToString( i );

        // onPause();

        String key_name = KeyEvent.keyCodeToString( i );
        Log.d( null, "KeyPressed : "+i+","+key_name );

        if( i == 23 ){
            finish();
            //Log.d( "LoadingActivity", "Monkey executing now !" );
            //UtilShell.executeShellCommandWithOp( "monkey -p com.excel.appstvlauncher.secondgen -c android.intent.category.LAUNCHER 1" );
            this.overridePendingTransition( 0, 0 );

            String pid = UtilShell.executeShellCommandWithOp( "pidof com.excel.welcomeguestapp.secondgen" ).trim();
            UtilShell.executeShellCommandWithOp( "kill " +pid );
            return false;
        }

        //return false;
        //finish();
        this.overridePendingTransition( 0, 0 );
        return true;
    }

    @Override
    protected void onPause(){
        super.onPause();

        Log.i( TAG, "onPause()" );

        //if( isWelcomeScreenShown() )
        //    finish();

        setIsWelcomeScreenShown( true );
    }


    public static void setIsWelcomeScreenShown( boolean is_it ){
        String s = ( is_it )?"1":"0";
        UtilShell.executeShellCommandWithOp( "setprop welcome_screen_shown " + s );
    }

    public static boolean isWelcomeScreenShown(){
        String is_it = UtilShell.executeShellCommandWithOp( "getprop welcome_screen_shown" ).trim();
        return ( is_it.equals( "1" ) )?true:false;
    }

    @Override
    public void onRequestPermissionsResult( int requestCode, String permissions[], int[] grantResults ) {
        switch ( requestCode ) {
            case 10:
            {
                if( grantResults.length > 0 && grantResults[ 0 ] == PackageManager.PERMISSION_GRANTED ){
                    // permissions granted.
                    Log.d( TAG, grantResults.length + " Permissions granted : " );
                } else {
                    String permission = "";
                    for ( String per : permissions ) {
                        permission += "\n" + per;
                    }
                    // permissions list of don't granted permission
                    Log.d( TAG, "Permissions not granted : "+permission );
                }
                return;
            }
        }
    }

    private boolean checkPermissions() {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for ( String p:permissions ) {
            result = ContextCompat.checkSelfPermission( this, p );
            if ( result != PackageManager.PERMISSION_GRANTED ) {
                listPermissionsNeeded.add( p );
            }
        }
        if ( !listPermissionsNeeded.isEmpty() ) {
            ActivityCompat.requestPermissions( this, listPermissionsNeeded.toArray( new String[ listPermissionsNeeded.size() ] ), 10 );
            return false;
        }
        return true;
    }


}
