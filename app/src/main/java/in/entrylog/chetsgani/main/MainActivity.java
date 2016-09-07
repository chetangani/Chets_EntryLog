package in.entrylog.chetsgani.main;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import in.entrylog.chetsgani.R;
import in.entrylog.chetsgani.dataposting.ConnectingTask;
import in.entrylog.chetsgani.dataposting.ConnectingTask.LoginData;
import in.entrylog.chetsgani.dataposting.ConnectingTask.OrganizationPermissions;
import in.entrylog.chetsgani.values.DetailsValue;
import in.entrylog.chetsgani.values.FunctionCalls;

public class MainActivity extends AppCompatActivity {
    public static final String PREFS_NAME = "MyPrefsFile";
    private static final int REQUEST_FOR_ACTIVITY_CODE = 1;
    private static final int FAILURE_DLG = 2;
    private static final int EXISTS_DLG = 3;
    private static final int BLOCKED_DLG = 4;
    Button btn_login;
    EditText orgid_etTxt, user_etTxt, pass_etTxt;
    TextView tv_version;
    String Orgid = "", User = "", Password = "", Login = "", OverNightTime="", OTPAccess, ImageAccess, Printertype,
            Scannertype, RfidStatus, DeviceModel, Cameratype;
    ConnectingTask task;
    DetailsValue details;
    Thread mythread, permissionthread;
    public static boolean loginsuccess = false;
    SharedPreferences settings;
    SharedPreferences.Editor editor;
    static ProgressDialog dialog = null;
    boolean storage = false, camera = false, phone = false, location = false;
    FunctionCalls functionCalls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        editor = settings.edit();

        details = new DetailsValue();
        task = new ConnectingTask();
        functionCalls = new FunctionCalls();

        btn_login = (Button) findViewById(R.id.login_btn);
        orgid_etTxt = (EditText) findViewById(R.id.userorgid_etTxt);
        user_etTxt = (EditText) findViewById(R.id.userid_etTxt);
        pass_etTxt = (EditText) findViewById(R.id.password_etTxt);
        tv_version = (TextView) findViewById(R.id.version_txt);

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String version = pInfo.versionName;
        tv_version.setText("VER: "+version);

        try {
            Login = settings.getString("Login", "");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (Login.equals("Yes")) {
            loginsuccess = true;
            loginpageview();
        } else {
            loginsuccess = false;
            loginpageview();
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                checkforPermissionsMandAbove();
            }
        }, 2500);

        if (loginsuccess) {
            Intent login = new Intent(MainActivity.this, BlocksActivity.class);
            /*startActivityForResult(login, REQUEST_FOR_ACTIVITY_CODE);*/
            startActivity(login);
            finish();
        }

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Logindetails();
            }
        });

        pass_etTxt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE) {
                    Logindetails();
                }
                return false;
            }
        });
    }

    private void Logindetails() {
        if (functionCalls.isInternetOn(MainActivity.this)) {
            Orgid = orgid_etTxt.getText().toString();
            if (!Orgid.equals("")) {
                User = user_etTxt.getText().toString();
                if (!User.equals("")) {
                    Password = pass_etTxt.getText().toString();
                    if (!Password.equals("")) {
                        LoginData login = task.new LoginData(User, Password, Orgid, details);
                        login.execute();
                        dialog = ProgressDialog.show(MainActivity.this, "", "Logging In please wait..", true);
                        mythread = null;
                        Runnable runnable = new LoginTimer();
                        mythread = new Thread(runnable);
                        mythread.start();
                    } else {
                        pass_etTxt.setError("Enter Password");
                        if (Orgid.equals("")) {
                            orgid_etTxt.setError("Enter Organization ID");
                        }
                        if (User.equals("")) {
                            user_etTxt.setError("Enter Username");
                        }
                    }
                } else {
                    user_etTxt.setError("Enter Username");
                    if (Orgid.equals("")) {
                        orgid_etTxt.setError("Enter Organization ID");
                    }
                    if (Password.equals("")) {
                        pass_etTxt.setError("Enter Password");
                    }
                }
            } else {
                orgid_etTxt.setError("Enter Organization ID");
                if (User.equals("")) {
                    user_etTxt.setError("Enter Username");
                }
                if (Password.equals("")) {
                    pass_etTxt.setError("Enter Password");
                }
            }
        } else {
            Toast.makeText(MainActivity.this, "Please Turn On Internet", Toast.LENGTH_SHORT).show();
        }
    }

    class LoginTimer implements Runnable {

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    doWork();
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch(Exception e){
                }
            }
        }
    }

    public void doWork() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (details.isLoginSuccess()) {
                        details.setLoginSuccess(false);
                        String ID = details.getOrganizationID();
                        showtoast("ID: "+ID);
                        String SecurityID = details.getGuardID();
                        String OrganizationName = details.getOrganizationName();
                        String Nighttime = details.getOverNightStay_Time();
                        if (!Nighttime.equals("")) {
                            if (!Nighttime.substring(0, 1).equals(":")) {
                                if (!Nighttime.substring(1, 2).equals(":")) {
                                    if (Nighttime.substring(2, 3).equals(":")) {
                                        OverNightTime = Nighttime.substring(0, 2);
                                    }
                                } else {
                                    OverNightTime = Nighttime.substring(0, 1);
                                }
                            }
                        }
                        String BarCode = details.getVisitors_BarCode();
                        editor.putString("BarCode", functionCalls.setBarCodeStatus(BarCode, ID));
                        editor.putString("Orgid", Orgid);
                        editor.putString("OrganizationID", ID);
                        editor.putString("OrganizationName", OrganizationName);
                        editor.putString("User", User);
                        editor.putString("GuardID", SecurityID);
                        editor.putString("CurrentDate", Currentdate());
                        editor.putString("OverNightTime", OverNightTime);
                        editor.commit();
                        functionCalls.deleteDataBasefile();
                        OrganizationPermissions organizationPermissions = task.new OrganizationPermissions(ID, details);
                        organizationPermissions.execute();
                    }
                    if (details.isLoginFailure()) {
                        btn_login.setVisibility(View.VISIBLE);
                        mythread.interrupt();
                        dialog.dismiss();
                        details.setLoginFailure(false);
                        showdialog(FAILURE_DLG);
                    }
                    if (details.isLoginExist()) {
                        details.setLoginExist(false);
                        mythread.interrupt();
                        dialog.dismiss();
                        btn_login.setVisibility(View.VISIBLE);
                        showdialog(EXISTS_DLG);
                    }
                    if (details.isAccountblocked()) {
                        details.setAccountblocked(false);
                        mythread.interrupt();
                        dialog.dismiss();
                        btn_login.setVisibility(View.VISIBLE);
                        showdialog(BLOCKED_DLG);
                    }
                    if (details.isPermissionSuccess()) {
                        details.setPermissionSuccess(false);
                        mythread.interrupt();
                        editor.putString("Login", "Yes");
                        editor.commit();
                        OTPAccess = details.getOTPAccess();
                        if (OTPAccess.equals("1")) {
                            editor.putString("OTPAccess", "Yes");
                        } else {
                            editor.putString("OTPAccess", "No");
                        }
                        editor.commit();
                        ImageAccess = details.getImageAccess();
                        if (ImageAccess.equals("1")) {
                            editor.putString("ImageAccess", "Yes");
                        } else {
                            editor.putString("ImageAccess", "No");
                        }
                        editor.commit();
                        Printertype = details.getPrintertype();
                        editor.putString("Printertype", Printertype);
                        editor.commit();
                        Scannertype = details.getScannertype();
                        functionCalls.LogStatus("Scanner type: "+Scannertype);
                        editor.putString("Scannertype", Scannertype);
                        editor.commit();
                        DeviceModel = details.getDeviceModel();
                        if (DeviceModel.equals("El-101")) {
                            editor.putString("Device", "EL101");
                        }
                        if (DeviceModel.equals("El-102")) {
                            editor.putString("Device", "EL101");
                        }
                        if (DeviceModel.equals("El-201")) {
                            editor.putString("Device", "EL201");
                        }
                        editor.commit();
                        RfidStatus = details.getRfidStatus();
                        if (DeviceModel.equals("El-201")) {
                            if (RfidStatus.equals("Enabled")) {
                                editor.putString("RFID", "true");
                            } else {
                                editor.putString("RFID", "false");
                            }
                        }
                        editor.commit();
                        Cameratype = details.getCameratype();
                        if (Cameratype.equals("Internal Camera")) {
                            editor.putString("Cameratype", "Internal");
                        } else if (Cameratype.equals("External Camera")) {
                            editor.putString("Cameratype", "External");
                        }
                        editor.commit();
                        editor.putString("UpdateData", "");
                        editor.commit();
                        loginsuccess = true;
                        loginpageview();
                        dialog.dismiss();
                        Intent login = new Intent(MainActivity.this, BlocksActivity.class);
                        /*startActivityForResult(login, REQUEST_FOR_ACTIVITY_CODE);*/
                        startActivity(login);
                        finish();
                    }
                    if (details.isPermissionFailure()) {
                        details.setPermissionFailure(false);
                        mythread.interrupt();
                        loginsuccess = true;
                        loginpageview();
                        dialog.dismiss();
                        Intent login = new Intent(MainActivity.this, BlocksActivity.class);
                        startActivityForResult(login, REQUEST_FOR_ACTIVITY_CODE);
                    }
                } catch(Exception e){
                }
            }
        });
    }

    public String Currentdate() throws ParseException {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        String present_date1 = day +"/"+ (month+1) +"/" + ""+year;
        Date date = new SimpleDateFormat("dd/MM/yyyy", Locale.US).parse(present_date1);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        c.setTime(date);
        String present_date2 = sdf.format(c.getTime());
        return present_date2;
    }

    private void showtoast(String message) {
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    public void loginpageview() {
        if (loginsuccess) {
            user_etTxt.setText(settings.getString("User", ""));
            user_etTxt.setEnabled(false);
            orgid_etTxt.setText(settings.getString("Orgid", ""));
            orgid_etTxt.setEnabled(false);
            pass_etTxt.setText("");
            pass_etTxt.setEnabled(false);
            orgid_etTxt.requestFocus();
        } else {
            user_etTxt.setText("");
            user_etTxt.setEnabled(true);
            orgid_etTxt.setText("");
            orgid_etTxt.setEnabled(true);
            pass_etTxt.setText("");
            pass_etTxt.setEnabled(true);
            orgid_etTxt.requestFocus();
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                loginsuccess = false;
                loginpageview();
            }
        }
    }*/

    @TargetApi(23)
    public void checkforPermissionsMandAbove() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkForWriteExternalStoragePermissionsMAndAboveBlocking(MainActivity.this);
            permissionthread = null;
            Runnable runnable = new CheckPermissions();
            permissionthread = new Thread(runnable);
            permissionthread.start();
        }
    }

    class CheckPermissions implements Runnable {

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    CheckPermissions();
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch(Exception e){
                }
            }
        }
    }

    public void CheckPermissions() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (storage) {
                        storage = false;
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                checkForCameraPermissionsMAndAboveBlocking(MainActivity.this);
                            }
                        }, 1000);
                    }
                    if (camera) {
                        camera = false;
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                checkForPhoneStatePermissionsMAndAboveBlocking(MainActivity.this);
                            }
                        }, 1000);
                    }
                    if (phone) {
                        phone = false;
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                checkForLocationPermissionsMAndAboveBlocking(MainActivity.this);
                            }
                        }, 1000);//358187072616272
                    }
                    if (location) {
                        location = false;
                        permissionthread.interrupt();
                        functionCalls.LogStatus("Permission Thread Interrupted");
                    }
                } catch(Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    @TargetApi(23)
    public void checkForWriteExternalStoragePermissionsMAndAboveBlocking(Activity act) {
        // Here, thisActivity is the current activity
        if (act.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // No explanation needed, we can request the permission.
            act.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},0);
            while (true) {
                if (act.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED) {
                    storage = true;
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } else {
            storage = true;
        }
    }

    @TargetApi(23)
    public void checkForCameraPermissionsMAndAboveBlocking(Activity act) {
        // Here, thisActivity is the current activity
        if (act.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            act.requestPermissions(new String[]{Manifest.permission.CAMERA},0);
            while (true) {
                if (act.checkSelfPermission(Manifest.permission.CAMERA)== PackageManager.PERMISSION_GRANTED) {
                    camera = true;
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } else {
            camera = true;
        }

    }

    @TargetApi(23)
    public void checkForPhoneStatePermissionsMAndAboveBlocking(Activity act) {
        // Here, thisActivity is the current activity
        if (act.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            act.requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE},0);
            while (true) {
                if (act.checkSelfPermission(Manifest.permission.READ_PHONE_STATE)== PackageManager.PERMISSION_GRANTED) {
                    phone = true;
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } else {
            phone = true;
        }
    }

    @TargetApi(23)
    public void checkForLocationPermissionsMAndAboveBlocking(Activity act) {
        // Here, thisActivity is the current activity
        int permissionCheck = act.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
        permissionCheck += act.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            act.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION},0);
            while (true) {
                if ((act.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                        +act.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)) == PackageManager.PERMISSION_GRANTED) {
                    location = true;
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } else {
            location = true;
        }
    }

    private void showdialog(int id) {
        switch (id) {
            case FAILURE_DLG:
                AlertDialog.Builder failurebuilder = new AlertDialog.Builder(this);
                failurebuilder.setTitle("Login Details");
                failurebuilder.setCancelable(false);
                failurebuilder.setMessage("Please Check Organization ID and Username and Password are not matching");
                failurebuilder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        orgid_etTxt.requestFocus();
                    }
                });
                AlertDialog failurealert = failurebuilder.create();
                failurealert.show();
                break;

            case EXISTS_DLG:
                AlertDialog.Builder existbuilder = new AlertDialog.Builder(this);
                existbuilder.setTitle("Login Details");
                existbuilder.setCancelable(false);
                existbuilder.setMessage("User already logged in some other device.. " +
                        "So please logout in that device and proceed");
                existbuilder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        user_etTxt.setText("");
                        orgid_etTxt.setText("");
                        pass_etTxt.setText("");
                        orgid_etTxt.requestFocus();
                    }
                });
                AlertDialog existalert = existbuilder.create();
                existalert.show();
                break;

            case BLOCKED_DLG:
                AlertDialog.Builder blockbuilder = new AlertDialog.Builder(this);
                blockbuilder.setTitle("Login Details");
                blockbuilder.setCancelable(false);
                blockbuilder.setMessage("Your account has been Blocked. Please contact " +
                        "support@ecotreesolutions.com or Call 8095312121");
                blockbuilder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        user_etTxt.setText("");
                        orgid_etTxt.setText("");
                        pass_etTxt.setText("");
                        orgid_etTxt.requestFocus();
                    }
                });
                AlertDialog blockalert = blockbuilder.create();
                blockalert.show();
                break;
        }
    }
}