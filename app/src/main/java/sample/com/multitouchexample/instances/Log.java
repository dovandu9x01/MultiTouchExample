package sample.com.multitouchexample.instances;


import sample.com.multitouchexample.BuildConfig;

public class Log {
    public final static boolean DEBUG = BuildConfig.DEBUG;
    final static String TAG = "---------------------";

    public static void d(Object str) {
        if (DEBUG)
            android.util.Log.d(TAG, str != null ? str.toString() : null + "");
    }

    public static void e(Object str) {
        if (DEBUG) android.util.Log.e(TAG, str != null ? str.toString() : null + "");
    }

    public static void i(Object str) {
        if (DEBUG) android.util.Log.i(TAG, str != null ? str.toString() : null + "");
    }

    public static void w(Object str) {
        if (DEBUG) android.util.Log.w(TAG, str != null ? str.toString() : null + "");
    }
}
