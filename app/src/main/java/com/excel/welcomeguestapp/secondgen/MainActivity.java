package com.excel.welcomeguestapp.secondgen;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.ImageViewCompat;
import android.support.v7.widget.AppCompatImageView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.excel.configuration.ConfigurationReader;
import com.excel.excelclasslibrary.UtilNetwork;
import com.excel.excelclasslibrary.UtilShell;
import com.excel.excelclasslibrary.UtilURL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.excel.configuration.ConfigurationReader;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class MainActivity extends Activity {

    RelativeLayout rl_webview, rl_video_screen, rl_native_welcome, activity_main;
    TextView tv_native_welcome_text;
    WebView wv_welcome_screen;
    ConfigurationReader configurationReader;
    Context context;
    AppCompatImageView iv_native_bg, iv_hotel_logo;
    Button bt_native_language_en, bt_native_language_zh;

    public static final String TAG = "WelcomeGuestApp";
    AnimatedGifImageView loading;

    boolean timeout;
    long timeout_interval = 10000;

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
        setContentView( R.layout.activity_main );

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
        rl_native_welcome = (RelativeLayout) findViewById( R.id.rl_native_welcome );
        activity_main = (RelativeLayout) findViewById( R.id.activity_main );
        iv_native_bg = (AppCompatImageView) findViewById( R.id.iv_native_bg );
        iv_hotel_logo = (AppCompatImageView) findViewById( R.id.iv_hotel_logo );
        tv_native_welcome_text = (TextView) findViewById( R.id.tv_native_welcome_text );
        bt_native_language_en = (Button) findViewById( R.id.bt_native_language_en );
        bt_native_language_zh = (Button) findViewById( R.id.bt_native_language_zh );;
        wv_welcome_screen = (WebView) findViewById( R.id.wv_welcome_screen );

        loading = (AnimatedGifImageView) findViewById( R.id.loading );
        loading.setAnimatedGif( context.getResources().getIdentifier( "drawable/small_loading1" , null, context.getPackageName() ), AnimatedGifImageView.TYPE.AS_IS );

        context.deleteDatabase("webview.db");
        context.deleteDatabase("webviewCache.db");

    }

    private void checkConfiguration(){
        if( configurationReader.getWelcomeScreenType().equals( "webview" ) )
            configureWebView();
        else if( configurationReader.getWelcomeScreenType().equals( "videoview" ) )
            configureVideoView();
        else
            configureNativeView();

    }

    private void configureWebView(){
        Log.d( TAG, "configureWebView()" );
        showWebViewScreen();
        //wv_welcome_screen = new WebView( context );//
        //wv_welcome_screen = (WebView) findViewById( R.id.wv_welcome_screen );
        wv_welcome_screen.clearCache(true);
        wv_welcome_screen.setFocusable( false );
        wv_welcome_screen.getSettings().setJavaScriptEnabled( true );
        wv_welcome_screen.getSettings().setAppCacheEnabled( false );

        wv_welcome_screen.setWebViewClient(new WebViewClient() {

            public void onPageFinished( WebView webview, String url ){
                super.onPageFinished( webview, url );

                Log.d( TAG, "onPageFinished()" );

                loading.setVisibility( View.GONE );

                timeout = false;
            }

            public void onPageStarted( WebView webview, String s, Bitmap bitmap ){
                super.onPageStarted( webview, s, bitmap );

                Log.d( TAG, "onPageStarted()" );
                loading.setVisibility( View.VISIBLE );

                Runnable run = new Runnable() {
                    public void run() {

                        Log.e( TAG, "Timeout" );
                        if( timeout ) {

                            wv_welcome_screen.loadUrl( "file:///android_asset/local_welcome/index.html" );

                        }
                    }
                };
                Handler myHandler = new Handler( Looper.myLooper() );
                myHandler.postDelayed( run, timeout_interval );

            }

            public void onReceivedError( WebView view, int errorCode, String description, String failingUrl ) {
                Log.e( TAG, "error "+description );
                wv_welcome_screen.loadUrl( "file:///android_asset/local_welcome/index.html" );
            }

        });
        wv_welcome_screen.loadUrl( UtilURL.getWebserviceURL() + "?what_do_you_want=url_forward&url_type=welcome_screen&mac_address="+ UtilNetwork.getMacAddress( context ) );
        //Log.d( null, UtilURL.getWebserviceURL() + "?what_do_you_want=url_forward&url_type=welcome_screen&mac_address="+ UtilNetwork.getMacAddress( context ) );
        //setContentView( wv_welcome_screen );

        //setIsWelcomeScreenShown( true );
    }

    private void configureVideoView(){
        Log.d( TAG, "configureVideoView()" );
        showVideoViewScreen();
    }

    private void configureNativeView(){
        Log.d( TAG, "configureNativeView()" );


        // Pull the Welcome Screen data from the CMS
        AsyncFetchWelcomeText fetchWelcomeText = new AsyncFetchWelcomeText();
        fetchWelcomeText.execute();


    }

    public boolean onKeyDown( int i, KeyEvent keyevent ){
        KeyEvent.keyCodeToString( i );

        // onPause();

        String key_name = KeyEvent.keyCodeToString( i );
        Log.d( null, "KeyPressed : "+i+","+key_name );

        //if( ( i == 23 ) || ( i == 66 ) ){
        if( i == KeyEvent.KEYCODE_DPAD_CENTER ){
            finish();
            //Log.d( "LoadingActivity", "Monkey executing now !" );
            //UtilShell.executeShellCommandWithOp( "monkey -p com.excel.appstvlauncher.secondgen -c android.intent.category.LAUNCHER 1" );
            this.overridePendingTransition( 0, 0 );

            String pid = UtilShell.executeShellCommandWithOp( "pidof com.excel.welcomeguestapp.secondgen" ).trim();
            UtilShell.executeShellCommandWithOp( "kill " +pid );
            return false;
        }
        else if( ( i == keyevent.KEYCODE_DPAD_LEFT ) || ( i == keyevent.KEYCODE_DPAD_RIGHT ) ){
            return super.onKeyDown( i, keyevent );
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



    private void showWebViewScreen(){
        rl_native_welcome.setVisibility( View.GONE );
        rl_video_screen.setVisibility( View.GONE );

        rl_webview.setVisibility( View.VISIBLE );
    }

    private void showNativeWelcomeScreen(){
        rl_webview.setVisibility( View.GONE );
        rl_video_screen.setVisibility( View.GONE );

        rl_native_welcome.setVisibility( View.VISIBLE );
    }

    private void showVideoViewScreen(){
        rl_webview.setVisibility( View.GONE );
        rl_native_welcome.setVisibility( View.GONE );

        rl_video_screen.setVisibility( View.VISIBLE );
    }

    private void hideEverything(){
        rl_webview.setVisibility( View.GONE );
        rl_native_welcome.setVisibility( View.GONE );

        rl_video_screen.setVisibility( View.GONE );
    }


    class AsyncFetchWelcomeText extends AsyncTask< String, Integer, String >{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loading.setVisibility( View.VISIBLE );
        }

        @Override
        protected String doInBackground( String... params ) {
            String url = UtilURL.getWebserviceURL();
            Log.d( TAG, "Webservice path : "+url );
            String response = UtilNetwork.makeRequestForData( url, "POST",
                    UtilURL.getURLParamsFromPairs( new String[][]{ { "what_do_you_want", "get_native_welcome_text" },
                            { "mac_address", UtilNetwork.getMacAddress( context ) } } ) );

            return response;
        }

        @Override
        protected void onPostExecute( String result ) {
            super.onPostExecute( result );

            Log.d( TAG,  "inside onPostExecute()" );

            if( result != null ){
                //Log.i( TAG,  result );
                try {
                    JSONArray jsonArray = new JSONArray( result );
                    JSONObject jsonObject = jsonArray.getJSONObject( 0 );
                    String type = jsonObject.getString( "type" );
                    String info = jsonObject.getString( "info" );
                    if( type.equals( "error" ) ){
                        Log.e( TAG, info );
                        return;
                    }

                    JSONObject welcomeTexts = new JSONObject( info );

                    Picasso.get()
                            .load( UtilURL.getCMSRootPath() + File.separator + "templates/silks-place/images/drum.jpg" )
                            .resize( 1920, 1080 )
                            .into( iv_native_bg );

                    Picasso.get()
                            .load( UtilURL.getCMSRootPath() + File.separator + "templates/silks-place/images/logo.png" )
                            .resize( 183, 150 )
                            .into( iv_hotel_logo );

                    final String en = welcomeTexts.getString( "en" );
                    final String zh = welcomeTexts.getString( "zh" );

                    tv_native_welcome_text.setText( en );

                    bt_native_language_en.setOnFocusChangeListener( new View.OnFocusChangeListener() {

                        @Override
                        public void onFocusChange( View v, boolean hasFocus ) {
                            if( hasFocus ){
                                tv_native_welcome_text.setText( en );
                            }
                        }

                    });
                    bt_native_language_zh.setOnFocusChangeListener( new View.OnFocusChangeListener() {

                        @Override
                        public void onFocusChange( View v, boolean hasFocus ) {
                            if( hasFocus ){
                                tv_native_welcome_text.setText( zh );
                            }
                        }

                    });

                    View.OnClickListener buttonClick = new View.OnClickListener(){

                        @Override
                        public void onClick( View v ) {
                            if( v.getId() == R.id.bt_native_language_en ){
                                UtilShell.executeShellCommandWithOp( "setprop language_code en" );
                            }
                            else{
                                UtilShell.executeShellCommandWithOp( "setprop language_code zh" );
                            }
                            configurationReader = ConfigurationReader.reInstantiate();
                            //recreate();
                            context.sendBroadcast( new Intent( "receive_outside_update_launcher_config " ) );
                            context.sendBroadcast( new Intent( "receive_update_launcher_config" ) );

                            //UtilShell.executeShellCommandWithOp( "input keyevent " + KeyEvent.KEYCODE_DPAD_CENTER );
                            hideEverything();
                            loading.setVisibility( View.VISIBLE );
                            finish();
                        }
                    };
                    bt_native_language_en.setOnClickListener( buttonClick );
                    bt_native_language_zh.setOnClickListener( buttonClick );

                    showNativeWelcomeScreen();
                    loading.setVisibility( View.GONE );

                }
                catch ( Exception e ) {
                    e.printStackTrace();
                }
            }
            else{
                Log.e( TAG, "Null was returned !" );
            }

        }
    }

}
