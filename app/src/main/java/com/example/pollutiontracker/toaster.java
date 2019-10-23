package com.example.pollutiontracker;

import android.content.Context;
import android.widget.Toast;

public class toaster {

    //Toast function

    public static void shortToast(String msg, Context context){
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static void longToast(String msg, Context context){
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

}
