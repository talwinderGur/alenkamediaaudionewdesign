package com.alenkasmartaudioplayer.utils;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.alenkasmartaudioplayer.activities.Splash_Activity;
import com.alenkasmartaudioplayer.api_manager.OkHttpUtil;
import com.alenkasmartaudioplayer.application.AlenkaMedia;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import org.json.JSONObject;

/**
 * Created by love on 19/5/18.
 */

public class LoggingExceptionHandler implements Thread.UncaughtExceptionHandler {

    private final static String TAG = LoggingExceptionHandler.class.getSimpleName();
    private final static String ERROR_FILE = Exception.class.getSimpleName() + ".error";

    private final Context context;
    private final Thread.UncaughtExceptionHandler rootHandler;

    public LoggingExceptionHandler(Context context) {
        this.context = context;
        // we should store the current exception handler -- to invoke it for all not handled exceptions ...
        rootHandler = Thread.getDefaultUncaughtExceptionHandler();
        // we replace the exception handler now with us -- we will properly dispatch the exceptions ...
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(final Thread thread, final Throwable ex) {

        try {
            Log.d(TAG, "called for " + ex.getClass());
            // assume we would write each error in one file ...

            String stackTrace = Log.getStackTraceString(ex);
            final JSONObject jsonObject = new JSONObject();

            try {

                Calendar calendar;
                calendar =Calendar.getInstance();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MMM/yyyy hh:mm:ss aa", Locale.US);
                String crash_date_time = simpleDateFormat.format(calendar.getTime());

                jsonObject.put("CrashDateTime",crash_date_time);
                jsonObject.put("TokenId", SharedPreferenceUtil.getStringPreference(this.context,Constants.TOKEN_ID));
                jsonObject.put("CrashLog",stackTrace);


            }catch (Exception e){
                e.printStackTrace();
                Log.e(TAG, "uncaughtException: "+e.getMessage());
            }

            saveCrashLogForNextTime(jsonObject.toString(),thread, ex);


        } catch (Exception e) {
            Log.e(TAG, "Exception Logger failed!", e);
        }
    }

    private void saveCrashLogForNextTime(String jsonString,Thread thread,final Throwable ex){

        SharedPreferenceUtil.setStringPreference(context,Constants.CRASH_MESSAGE,jsonString);
        displayToastAndCrashApplication(thread, ex);
    }

    private void displayToastAndCrashApplication(Thread thread,final Throwable ex){

        Intent intent = new Intent(context, Splash_Activity.class);
        intent.putExtra("crash", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_CLEAR_TASK
                | Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(AlenkaMedia.getInstance().getBaseContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);

        AlarmManager mgr = (AlarmManager) AlenkaMedia.getInstance().getBaseContext().getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, pendingIntent);

        System.exit(2);

        /*new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                // we cant start a dialog here, as the context is maybe just a background activity ...
                Toast.makeText(context, ex.getMessage() + " Application will close!", Toast.LENGTH_LONG).show();
                Looper.loop();
            }
        }.start();

        try {
            Thread.sleep(4000); // Let the Toast display before app will get shutdown
        } catch (InterruptedException e) {
            // Ignored.
        }

        rootHandler.uncaughtException(thread, ex);*/
    }

    private static String getExceptionDetails(Exception e) {

        StackTraceElement[] stackTraceElement = e.getStackTrace();

        String fileName = "";
        String methodName = "";
        int lineNumber = 0;

        try {

            for (int i = 0; i < stackTraceElement.length; i++) {

                    fileName = stackTraceElement[i].getFileName();
                    methodName = stackTraceElement[i].getMethodName();
                    lineNumber = stackTraceElement[i].getLineNumber();


            }
        } catch (Exception e2) {
        }


        return fileName + ":" + methodName + "():line "
                + String.valueOf(lineNumber);
    }
}