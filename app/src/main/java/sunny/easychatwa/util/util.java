package sunny.easychatwa.util;

import android.util.Log;

import sunny.easychatwa.Globals;


public class util {

    public static void printMe(String str) {
        if (Globals.showlog) {
            System.out.println(str);
        }
    }
    public static void printMeLog (String key, String value){
        Log.d("cek", String.valueOf(Globals.showlog));
        if(Globals.showlog){
            Log.d(key,value);
        } else {
            Log.d(key,value);
        }
    }

}
