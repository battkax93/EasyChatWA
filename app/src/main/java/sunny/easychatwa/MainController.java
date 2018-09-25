package sunny.easychatwa;

import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;

import mehdi.sakout.fancybuttons.FancyButton;
import sunny.easychatwa.util.util;


public class MainController extends AppCompatActivity {

    private NotificationCompat.Builder notificationBuilder;
    private NotificationManager notificationManager;

    SharedPreferences pref;
    SharedPreferences.Editor editor;

    String keyCode = "keyCode";
    String cekKode = "cekKode";

    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        editor = pref.edit();
    }

    public void ToastHelper(String msg) {
        Toast toast = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public String pasteHtmltoText() {

        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard.hasPrimaryClip()) {
            android.content.ClipDescription description = clipboard.getPrimaryClipDescription();
            android.content.ClipData data = clipboard.getPrimaryClip();
            ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
            util.printMe("cek data = " + item.getText().toString());
            if (data != null && description != null && description.hasMimeType(ClipDescription.MIMETYPE_TEXT_HTML))
                return String.valueOf(item.getText());
        }
        return null;
    }

    public String readFromTextPlain() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard.hasPrimaryClip()) {
            android.content.ClipDescription description = clipboard.getPrimaryClipDescription();
            android.content.ClipData data = clipboard.getPrimaryClip();
            util.printMe("cek data = " + data.toString());
            if (data != null && description != null && description.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN))
                return String.valueOf(data.getItemAt(0).getText());
        }
        return null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuItem edit_item2 = menu.add(0, 1, 0, "Edit Country Code");
        edit_item2.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

        MenuItem edit_item = menu.add(0, 2, 0, "Exit");
        edit_item.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_NEVER);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case 1:
                showDialogCodeNumber(this);
                break;
            case 2:
                showDialogExit(this);
                break;
        }
        return false;
    }

    public void analyticFBLog(String id, String name, String type) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, id);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, name);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, type);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        util.printMe("send analytic " + name);
    }


    public void NotificationBuilder(Boolean tgl) {
        util.printMe("==NotifBuilder==");
        String autosave;

        if (tgl) {
            autosave = "ON";

            Intent notificationIntent = new Intent(this, MainActivity.class);
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                    notificationIntent, 0);

            Bitmap logo = BitmapFactory.decodeResource(getResources(), R.drawable.ew_icon2);
            notificationBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_enable)
                    .setLargeIcon(logo)
                    .setContentTitle("Easy Whatsapp")
                    .setContentText("auto chat : " + autosave)
                    .setContentIntent(contentIntent);
        } else {
            autosave = "OFF";

            Intent notificationIntent = new Intent(this, MainActivity.class);
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                    notificationIntent, 0);

            Bitmap logo = BitmapFactory.decodeResource(getResources(), R.drawable.ew_icon2);
            notificationBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_disable)
                    .setLargeIcon(logo)
                    .setContentTitle("Easy Whatsapp")
                    .setContentText("auto chat : " + autosave)
                    .setContentIntent(contentIntent);

        }

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notificationBuilder.build());

    }

    public void notifClose() {
        util.printMe("====NotifClose===");
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    public void showDialogExit(final Context context) {
        final Dialog dialog = new Dialog(context, R.style.PauseDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_exit);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        FancyButton dialogBtn_cancel = dialog.findViewById(R.id.btn_no);
        FancyButton dialogBtn_okay = dialog.findViewById(R.id.btn_yes);

        dialogBtn_okay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                finishAndRemoveTask();
                finish();
            }
        });

        dialogBtn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public void showDialogCodeNumber(final Context context) {
        final Dialog dialog = new Dialog(context, R.style.PauseDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_code);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        FancyButton dialogBtn_save = dialog.findViewById(R.id.btn_save);
        final EditText etCountryCode = dialog.findViewById(R.id.country_code);

        dialogBtn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String countrycode = etCountryCode.getText().toString().trim();
                if (TextUtils.isEmpty(countrycode)) {
                    etCountryCode.setError("cannot be empty");
                } else {

                    editor.putBoolean(cekKode, true);
                    editor.putString(keyCode, countrycode);

                    editor.apply(   );

                    dialog.dismiss();

                    util.printMe("cek " + pref.getString(keyCode, null) +
                            " | " + pref.getBoolean(cekKode, false));
                }

            }
        });

        dialog.show();
    }

}
