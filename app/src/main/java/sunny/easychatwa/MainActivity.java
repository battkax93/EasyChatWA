package sunny.easychatwa;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import mehdi.sakout.fancybuttons.FancyButton;
import sunny.easychatwa.NotificationServices.KillNotificationsService;
import sunny.easychatwa.NotificationServices.NotificationHelper;
import sunny.easychatwa.util.util;

public class MainActivity extends MainController {

    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
    private static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";
    private AlertDialog enableNotificationListenerAlertDialog;


    SharedPreferences pref;
    SharedPreferences.Editor editor;

    Boolean toogle;
    EditText txtCall, txtMsg, txtCode;
    TextView tvOnOff;
    FancyButton btnCall;
    SwitchCompat switchCompat;
    String test;
    String code;
    String keyMessage = "txtMsg";
    String keyToogle = "isChecked";
    String api1 = "https://api.whatsapp.com/send?phone=";
    String api2 = "&text=";

    private int maxCount = 6;
    private String KEY_COUNT = "aCount";
    private InterstitialAd mInterstitialAd;                              //adMob
    AdView mAdview;                                                      //adMob

    public NotificationHelper notificationHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= 26) {
            notificationHelper = new NotificationHelper(this);
        }

        reqPermission();


      /*  if (!isNotificationServiceEnabled()) {
            util.printMe("==reqNotifAcces==");
//            reqPermission();
            enableNotificationListenerAlertDialog = buildNotificationServiceAlertDialog();
            enableNotificationListenerAlertDialog.show();
        }*/

        pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        editor = pref.edit();

        init();
        initAdmob();
        cekToogle();
        initServiceKillNotif();

        cekKode();
        clipboardListener();

    }

    /**
     * INIT
     **/

    private void init() {

        mAdview = findViewById(R.id.adView_main);                               //adMob
        tvOnOff = findViewById(R.id.on_off);
        switchCompat = findViewById(R.id.switch_auto_save);
        txtMsg = findViewById(R.id.txt_message);
        txtCode = findViewById(R.id.txt_code);
        txtCall = findViewById(R.id.txt_number_telp);
        btnCall = findViewById(R.id.btn_call);
        btnCall.setTextSize(17);
        btnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                analyticFBLog(Globals.BUTTON_ID_OPENWA, Globals.BUTTON_NAME_OPENWA, Globals.BUTTON_TYPE);
                String call = txtCall.getText().toString();
                if (TextUtils.isEmpty(call)) {
                    txtCall.setError("kolom ini harus diisi");
                } else if (call.length() < 10) {
                    txtCall.setError("nomor telepon tidak lengkap");
                } else {
                    hitWhatsappAPI2();
                    saveLastMsg();
                }
            }
        });

        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    analyticFBLog(Globals.TOGGLE_ID_SWITCHOFF, Globals.TOGGLE_NAME_SWITCHOFF, Globals.TOGGLE_TYPE);
                    toogle = false;
                    editor.putBoolean(keyToogle, false);
                    editor.apply();
                    util.printMe("===toogle = " + toogle);
                    util.printMe("cek = " + pref.getBoolean("isChecked", false));
                    tvOnOff.setText(getString(R.string.text_off));
                    if (Build.VERSION.SDK_INT >= 26) {
                        NotificationCheck(toogle);
                    } else {
                        NotificationBuilder(toogle);
                    }
                } else {
                    analyticFBLog(Globals.TOGGLE_ID_SWITCHON, Globals.TOGGLE_NAME_SWITCHON, Globals.TOGGLE_TYPE);
                    toogle = true;
                    editor.putBoolean(keyToogle, true);
                    editor.apply();
                    util.printMe("===toogle = " + toogle);
                    util.printMe("cek = " + pref.getBoolean("isChecked", false));
                    tvOnOff.setText(getString(R.string.text_on));

                    if (Build.VERSION.SDK_INT >= 26) {
                        NotificationCheck(toogle);
                    } else {
                        NotificationBuilder(toogle);
                    }
                }
            }
        });

    }

    private void initAdmob() {
        MobileAds.initialize(this, Globals.ADD_MOB_APP_ID);             //adMob
        mInterstitialAd = new InterstitialAd(this);                     //adMob
        mInterstitialAd.setAdUnitId(getString(R.string.interstitial_video));    //adMob
        adMobs();
    }

    private void initServiceKillNotif() {
        ServiceConnection mConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName className, IBinder binder) {
                ((KillNotificationsService.KillBinder) binder).service.startService(new Intent(
                        MainActivity.this, MainActivity.class));
                util.printMe("==onServON==");
                if (Build.VERSION.SDK_INT >= 26) {
                    NotificationCheck(toogle);
                } else {
                    NotificationBuilder(toogle);
                }
            }

            public void onServiceDisconnected(ComponentName className) {
                util.printMe("==onServDC==");
            }
        };
        bindService(new Intent(MainActivity.this,
                        KillNotificationsService.class), mConnection,
                Context.BIND_AUTO_CREATE);
    }

    public void NotificationCheck(Boolean tgl) {
        util.printMe("==SDK>26==");
        if (Build.VERSION.SDK_INT >= 26 && tgl) {
            util.printMe("==SDK>26>ON==");
            String autosave2 = "ON";
            Notification.Builder nb = notificationHelper.getAndroidChannelNotificationON("Easy Whatsapp", autosave2);
            notificationHelper.getManager().notify(0, nb.build());
        } else if (Build.VERSION.SDK_INT >= 26 && !tgl) {
            util.printMe("==SDK>26>OFF==");
            String autosave2 = "OFF";
            Notification.Builder nb = notificationHelper.getAndroidChannelNotificationOFF("Easy Whatsapp", autosave2);
            notificationHelper.getManager().notify(0, nb.build());
        }
    }


    public void adMobs() {
        if (Globals.admob) {
            util.printMe("== dev adMobs ==");
            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice("6B7C8118873F959A250EF2732E708691")
                    .build();

            mInterstitialAd.loadAd(adRequest);
            mAdview.loadAd(adRequest);

        } else {
            util.printMe("== prod adMobs ==");
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdview.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                }

                @Override
                public void onAdClosed() {
                }

                @Override
                public void onAdFailedToLoad(int errorCode) {
                }

                @Override
                public void onAdLeftApplication() {
                }

                @Override
                public void onAdOpened() {
                    super.onAdOpened();
                }
            });
            mAdview.loadAd(adRequest);
            mInterstitialAd.loadAd(adRequest);

        }
    }

    private void showInterstitial() {
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }
    }

    private void actCount() {
        util.printMe("===actCount===");
        int aCount;
        aCount = getFromSP(KEY_COUNT);
        util.printMe("== count1 " + getFromSP(KEY_COUNT));
        aCount++;
        int bCount = aCount;
        insertToSP(KEY_COUNT, bCount);
        util.printMe("== count2 " + getFromSP(KEY_COUNT));

        if (bCount >= maxCount) {
            util.printMe("==showingAds==");
            showInterstitial();
            clearFromSP(KEY_COUNT);
        }
    }


    public void insertToSP(String key, int count) {
        editor.putInt(key, count);
        editor.apply();
    }

    public int getFromSP(String key) {
        return pref.getInt(key, 0);
    }

    public void clearFromSP(String key) {
        util.printMe("===clearFromSP===");
        editor.remove(key);
        editor.apply();
        util.printMe("==cek count " + String.valueOf(getFromSP(KEY_COUNT)));
    }


    public void cekToogle() {
        util.printMe("===cekToogle===");
        toogle = pref.getBoolean(keyToogle, false);
        util.printMe("== togel " + toogle);
        if (toogle) {
            switchCompat.setChecked(true);
            tvOnOff.setText(getString(R.string.text_on));
        } else {
            switchCompat.setChecked(false);
            tvOnOff.setText(getString(R.string.text_off));
        }
    }

    public void cekKode() {
        Boolean cek = pref.getBoolean(cekKode, false);
        if (!cek) {
            showDialogCodeNumber(this);
        } else {
            code = pref.getString(keyCode, null);
        }
    }

    public void hitWhatsappAPI(String txt) {
//        mInterstitialAd.setAdListener(new AdListener() {
//            public void onAdLoaded() {
//                showInterstitial();
//            }
//        });

        analyticFBLog(Globals.FUNCTION_ID_HITAPI, Globals.FUNCTION_NAME_HITAPI, Globals.FUNCTION_TYPE);
        util.printMe("===hitWhatsapAPI===");
        actCount();

        String msg = txtMsg.getText().toString();
        msg.replace(" ", "%20");
        Boolean tgl = pref.getBoolean(keyToogle, false);
        Log.d("cek", String.valueOf(tgl));
        if (tgl) {
            String numb2 = txt.substring(0, 1);
            String numb3 = txt.substring(0, 4);
            util.printMe("cek numb = " + numb2 + " | " + numb3);

            if (numb2.contains("0")) {

                String numb4 = code + txt;
                util.printMe("case 2 = " + numb4);
                String API = api1 + numb4 + api2 + msg;
                startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(API)));

            } else if (txt.contains("+")) {

                String gNumb = txt.replace("+", "");
                String API = api1 + gNumb + api2 + msg;
                util.printMe("case 3 = " + txt);
                util.printMe("case 3 = " + gNumb);
                startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(API)));

            } else if (numb3.contains(code)) {

                util.printMe("case 4 = " + txt);
                String API = api1 + txt + api2 + msg;
                startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(API)));


            } else {

                util.printMe("case 5 = " + txt);
                String API = api1 + code + txt + api2 + msg;
                startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(API)));
            }
        }
    }
    
    public void hitWhatsappAPI2() {
//        mInterstitialAd.setAdListener(new AdListener() {
//            public void onAdLoaded() {
//                showInterstitial();
//            }
//        });

        analyticFBLog(Globals.FUNCTION_ID_HITAPI, Globals.FUNCTION_NAME_HITAPI, Globals.FUNCTION_TYPE);
        util.printMe("===hitWhatsapAPI2===");

        actCount();
        String numb = txtCode.getText().toString() + txtCall.getText().toString();
        String msg = txtMsg.getText().toString();
        String msg2 = msg.replace(" ", "%20");

        util.printMe("cek msg = " + msg2);

        util.printMe("case 3 = " + numb);
        String API = api1 + numb + api2 + msg2;
        startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(API)));

    }


    public void saveLastMsg() {
        String msg = txtMsg.getText().toString();
        editor.putString(keyMessage, msg);
        editor.apply();
//        Log.d("cek sp", pref.getString(keyMessage,null));
        util.printMeLog("cek saved text ", pref.getString(keyMessage, null));
    }

    protected void clipboardListener() {
//        final SharedPreferences pref = this.getSharedPreferences("Mypref", 0);
//        final SharedPreferences.Editor editor2 = pref.edit();
        util.printMe("===cbLisener===");
        final ClipboardManager clipboard = (ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.addPrimaryClipChangedListener(new ClipboardManager.OnPrimaryClipChangedListener() {

            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onPrimaryClipChanged() {

                if (clipboard.hasPrimaryClip()) {
                    android.content.ClipDescription description = clipboard.getPrimaryClipDescription();
                    android.content.ClipData data = clipboard.getPrimaryClip();
                    String cp = data.toString();
                    util.printMe("cp ..." + cp);
                    if (data != null && description != null && description.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                        util.printMe("===case 1===");
                        if (toogle) {
                            test = readFromTextPlain();
                            Boolean ct = false;
                            for (Character c : readFromTextPlain().substring(0).toCharArray()) {
                                if (Character.isAlphabetic(c)) {
                                    ct = true;
                                }
                            }
                            if (!ct) {
                                hitWhatsappAPI(test);
                            }
                        }

                    } else if (data != null && description != null && description.hasMimeType(ClipDescription.MIMETYPE_TEXT_HTML)) {

                        if (toogle) {

                            test = pasteHtmltoText();
                            hitWhatsappAPI(test);
                        }
                    }
                }
            }

        });
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        util.printMe("==onWindowsFocusChanges");
        code = pref.getString(keyCode, null);
        if (code != null) txtCode.setText(code);
    }

    @Override
    public void onPostResume() {
        super.onPostResume();
        util.printMe("===onResume===");
        String msg = pref.getString(keyMessage, null);
        Boolean toogle2 = pref.getBoolean(keyToogle, false);
        toogle = toogle2;
        txtMsg.setText(msg);
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationCheck(toogle);
        } else {
            NotificationBuilder(toogle);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        util.printMe("===onPause===");
        saveLastMsg();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        util.printMe("==onDestroy==");
        saveLastMsg();
        notifClose();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            moveTaskToBack(true);
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onStop() {
        super.onStop();
        util.printMe("==onStop==");
    }

    private AlertDialog buildNotificationServiceAlertDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(R.string.notif_listener_service);
        alertDialogBuilder.setMessage("Requires notification permissions for the application can work");
        alertDialogBuilder.setNegativeButton("back",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // If you choose to not enable the notification listener
                        // the app. will not work as expected
                    }
                });
        alertDialogBuilder.setPositiveButton("Open Notification Settings",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startActivity(new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS));
                    }
                });
        return (alertDialogBuilder.create());
    }

    private boolean isNotificationServiceEnabled() {
        String pkgName = getPackageName();
        final String flat = Settings.Secure.getString(getContentResolver(),
                ENABLED_NOTIFICATION_LISTENERS);
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (int i = 0; i < names.length; i++) {
                final ComponentName cn = ComponentName.unflattenFromString(names[i]);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    private void reqPermission() {
        util.printMe("==reqPermission");
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_NOTIFICATION_POLICY)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        util.printMe("==PermissionNotifGranted==");
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                }).check();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 2) {
            util.printMe("==PermissionNotifGranted==");
        }
    }
}