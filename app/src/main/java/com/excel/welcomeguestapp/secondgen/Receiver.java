package com.excel.welcomeguestapp.secondgen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.excel.configuration.ConfigurationReader;

/**
 * Created by Sohail on 02-02-2017.
 */

public class Receiver extends BroadcastReceiver {

    final static String TAG = "Receiver";
    ConfigurationReader configurationReader;


    @Override
    public void onReceive( Context context, Intent intent ) {

        String action = intent.getAction();
        configurationReader = ConfigurationReader.reInstantiate();

        if( action.equals( "android.net.conn.CONNECTIVITY_CHANGE" ) || action.equals( "connectivity_changed" ) ){

            // Show Welcome Screen
            context.sendBroadcast( new Intent( "show_welcome_screen" ) );

            // Force Show Welcome Screen

        }
        else if( action.equals( "show_welcome_screen" ) ){

            if( ! configurationReader.getIsWelcomeScreenEnabled() ){
                Log.e( TAG, "Welcome screen has been disabled, hence will not show up !" );
                return;
            }


            if( ! MainActivity.isWelcomeScreenShown() ) {
                String is_ots_completed = configurationReader.getIsOtsCompleted().trim();
                if( is_ots_completed.equals( "1" ) ) {
                    showWelcomeScreen( context );
                }
                else{
                    Log.e( TAG, "OTS Not completed, so no Welcome Screen !" );
                }
            }
            else{
                Log.e( TAG, "Welcome screen already shown !" );
            }
        }
    }

    private void showWelcomeScreen( Context context ){
        Intent in = new Intent( context, MainActivity.class );
        in.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
        context.startActivity( in );
    }
}
