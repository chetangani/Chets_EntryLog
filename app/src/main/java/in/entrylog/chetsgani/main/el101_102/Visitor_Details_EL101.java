package in.entrylog.chetsgani.main.el101_102;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.devkit.api.Misc;
import android.devkit.api.SerialPort;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import in.entrylog.chetsgani.R;
import in.entrylog.chetsgani.dataposting.ConnectingTask;
import in.entrylog.chetsgani.dataposting.ConnectingTask.VisitorManualCheckout;
import in.entrylog.chetsgani.main.CustomVolleyRequest;
import in.entrylog.chetsgani.main.services.FieldsService;
import in.entrylog.chetsgani.main.services.PrintingService;
import in.entrylog.chetsgani.util.ArrayUtil;
import in.entrylog.chetsgani.util.Encoder;
import in.entrylog.chetsgani.util.FileUtil;
import in.entrylog.chetsgani.util.HexDump;
import in.entrylog.chetsgani.util.ImageProcessing;
import in.entrylog.chetsgani.util.PrintPic;
import in.entrylog.chetsgani.util.Printer;
import in.entrylog.chetsgani.values.DataPrinting;
import in.entrylog.chetsgani.values.DetailsValue;
import in.entrylog.chetsgani.values.EL101_102;
import in.entrylog.chetsgani.values.FunctionCalls;

public class Visitor_Details_EL101 extends AppCompatActivity {
    public static final String PREFS_NAME = "MyPrefsFile";
    private static final int END_DLG = 6;

    LinearLayout maincontent, checkoutlayout, checkoutuserlayout, emailayout, designationlayout, departmentlayout,
            purposelayout, housenolayout, flatnolayout, blocklayout, noofvisitorlayout, classlayout, sectionlayout,
            studentnamelayout, idcardlayout;
    CoordinatorLayout coordinatorLayout;
    CollapsingToolbarLayout collapsingToolbarLayout;
    String Visitor_Name, Visitor_Mobile, Visitor_Fromaddress, Visitor_ToMeet, Visitor_CheckinTime, Visitor_CheckoutTime,
            Visitor_VehicleNo, Visitor_EntryGate, Visitor_Photo, ContextView, Visitor_id, Organization_ID, CheckingUser,
            HeaderPath, DataPath, OrganizationPath, EmptyPath, OrganizationName, BarCodeValue, CheckinUser="", CheckoutUser="",
            Visitor_Designation, Department, Purpose, House_number, Flat_number, Block, No_Visitor, aClass, Section,
            Student_Name, ID_Card, Email, Vehicleno;
    NetworkImageView Visitor_image;
    ImageLoader imageLoader;
    TextView tv_name, tv_mobile, tv_address, tv_tomeet, tv_checkintime, tv_checkouttime, tv_vehicleno, tv_entry, tv_exit,
            tv_email, tv_designation, tv_department, tv_purpose, tv_houseno, tv_flatno, tv_block, tv_noofvisitor, tv_class,
            tv_section, tv_studentname, tv_idcardno;
    Button Checkout_btn, Print_btn;
    ConnectingTask task;
    DetailsValue detailsValue;
    FunctionCalls functionCalls;
    DataPrinting dataPrinting;
    static ProgressDialog dialog = null;
    Thread checkingoutthread;
    boolean imageprinting = false, barcodeprinting = false;
    SharedPreferences settings;
    PrintingService printingService;
    FieldsService fieldsService;
    EL101_102 el101_102device;
    static ArrayList<String> printingorder, printingdisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int screenSize1 = getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK;
        int rotation1 = this.getWindowManager().getDefaultDisplay().getRotation();
        switch(screenSize1) {
            case Configuration.SCREENLAYOUT_SIZE_LARGE:
                setTheme(R.style.AppTheme);
                break;
            case Configuration.SCREENLAYOUT_SIZE_NORMAL:
                switch (rotation1) {
                    case Surface.ROTATION_0:
                        setTheme(R.style.MyTheme);
                        break;
                    case Surface.ROTATION_90:
                        setTheme(R.style.AppTheme);
                        break;
                    case Surface.ROTATION_270:
                        setTheme(R.style.AppTheme);
                        break;
                }
                break;
        }
        setContentView(R.layout.activity_visitor_details);

        task = new ConnectingTask();
        detailsValue = new DetailsValue();
        functionCalls = new FunctionCalls();
        dataPrinting = new DataPrinting();

        fieldsService = new FieldsService();
        printingService = new PrintingService();
        el101_102device = new EL101_102();

        functionCalls.OrientationView(Visitor_Details_EL101.this);

        settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        Intent intent = getIntent();
        Bundle bnd = intent.getExtras();
        //region Intent Values
        ContextView = bnd.getString("View");
        Visitor_Photo = bnd.getString("Image");
        Organization_ID = bnd.getString("OrganizationID");
        Visitor_id = bnd.getString("VisitorID");
        Visitor_Name = bnd.getString("Name");
        Visitor_Mobile = bnd.getString("Mobile");
        Visitor_Fromaddress = bnd.getString("From");
        Visitor_ToMeet = bnd.getString("ToMeet");
        Visitor_CheckinTime = bnd.getString("CheckinTime");
        Visitor_CheckoutTime = bnd.getString("CheckoutTime");
        BarCodeValue = bnd.getString("BarCode");
        Visitor_VehicleNo = bnd.getString("VehicleNo");
        Visitor_EntryGate = bnd.getString("Entry");
        CheckingUser = bnd.getString("CheckingUser");
        CheckinUser = bnd.getString("CheckinUser");
        CheckoutUser = bnd.getString("CheckoutUser");
        Email = bnd.getString("Email");
        Vehicleno = bnd.getString("VehicleNo");
        if (CheckoutUser.equals("null")) {
            CheckoutUser = "";
        }
        Visitor_Designation = bnd.getString("visitor_designation");
        Department = bnd.getString("department");
        Purpose = bnd.getString("purpose");
        House_number = bnd.getString("house_number");
        Flat_number = bnd.getString("flat_number");
        Block = bnd.getString("block");
        No_Visitor = bnd.getString("no_visitor");
        aClass = bnd.getString("class");
        Section = bnd.getString("section");
        Student_Name = bnd.getString("student_name");
        ID_Card = bnd.getString("id_card_number");
        //endregion

        //region Linear Layout Initialization
        checkoutlayout = (LinearLayout) findViewById(R.id.detailscheckoutdatelayout);
        checkoutuserlayout = (LinearLayout) findViewById(R.id.exitgate_layout);
        emailayout = (LinearLayout) findViewById(R.id.detailsemaillayout);
        designationlayout = (LinearLayout) findViewById(R.id.detailsvisitordesignation_layout);
        departmentlayout = (LinearLayout) findViewById(R.id.detailsdepartmentlayout);
        purposelayout = (LinearLayout) findViewById(R.id.detailspurposelayout);
        housenolayout = (LinearLayout) findViewById(R.id.detailshousenolayout);
        flatnolayout = (LinearLayout) findViewById(R.id.detailsflatnolayout);
        blocklayout = (LinearLayout) findViewById(R.id.detailsblocklayout);
        noofvisitorlayout = (LinearLayout) findViewById(R.id.detailsnoofvisitorlayout);
        classlayout = (LinearLayout) findViewById(R.id.detailsclasslayout);
        sectionlayout = (LinearLayout) findViewById(R.id.detailssectionlayout);
        studentnamelayout = (LinearLayout) findViewById(R.id.detailsstudentnamelayout);
        idcardlayout = (LinearLayout) findViewById(R.id.detailsidcardnolayout);
        //endregion

        //region TextView Initialization
        tv_name = (TextView) findViewById(R.id.visitor_name);
        tv_mobile = (TextView) findViewById(R.id.visitor_mobile);
        tv_address = (TextView) findViewById(R.id.visitor_fromaddress);
        tv_tomeet = (TextView) findViewById(R.id.visitor_tomeet);
        tv_checkintime = (TextView) findViewById(R.id.visitorcheckin_date);
        tv_checkouttime = (TextView) findViewById(R.id.visitorcheckout_date);
        tv_vehicleno = (TextView) findViewById(R.id.visitor_vehicleno);
        tv_entry = (TextView) findViewById(R.id.entry_gate);
        tv_exit = (TextView) findViewById(R.id.exit_gate);
        tv_email = (TextView) findViewById(R.id.visitor_email);
        tv_designation = (TextView) findViewById(R.id.visitor_designation);
        tv_department = (TextView) findViewById(R.id.visitor_department);
        tv_purpose = (TextView) findViewById(R.id.visitor_purpose);
        tv_houseno = (TextView) findViewById(R.id.visitor_houseno);
        tv_flatno = (TextView) findViewById(R.id.visitor_flatno);
        tv_block = (TextView) findViewById(R.id.visitor_block);
        tv_noofvisitor = (TextView) findViewById(R.id.noofvisitor);
        tv_class = (TextView) findViewById(R.id.visitor_class);
        tv_section = (TextView) findViewById(R.id.visitor_section);
        tv_studentname = (TextView) findViewById(R.id.visitor_section);
        tv_idcardno = (TextView) findViewById(R.id.visitor_idcardno);
        //endregion

        Checkout_btn = (Button) findViewById(R.id.checkout_btn);
        Print_btn = (Button) findViewById(R.id.detailsprint_btn);

        Visitor_image = (NetworkImageView) findViewById(R.id.visitor_image);
        imageLoader = CustomVolleyRequest.getInstance(this.getApplicationContext()).getImageLoader();

        int screenSize = getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
        int rotation = this.getWindowManager().getDefaultDisplay().getRotation();
        switch(screenSize) {
            case Configuration.SCREENLAYOUT_SIZE_NORMAL:
                switch (rotation) {
                    case Surface.ROTATION_0:
                        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_content);
                        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing);
                        collapsingToolbarLayout.setTitle(Visitor_Name);
                        collapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));
                        break;
                    case Surface.ROTATION_90:
                        maincontent = (LinearLayout) findViewById(R.id.main_content);
                        break;
                    case Surface.ROTATION_270:
                        maincontent = (LinearLayout) findViewById(R.id.main_content);
                        break;
                }
                break;
        }

        if (ContextView.equals("Manually Checkout")) {
            Checkout_btn.setVisibility(View.VISIBLE);
            checkoutlayout.setVisibility(View.GONE);
        } else if (ContextView.equals("Visitors")) {
            Print_btn.setVisibility(View.VISIBLE);
            if (Visitor_CheckoutTime.equals("")) {
                Checkout_btn.setVisibility(View.VISIBLE);
            }
            functionCalls.LogStatus("Enabling Printer");
            el101_102device.EnableBtn(true);
        } else {
        }

        OrganizationPath = functionCalls.filepath("Textfile") + File.separator + "Organization.txt";
        HeaderPath = functionCalls.filepath("Textfile") + File.separator + "Header.txt";
        DataPath = functionCalls.filepath("Textfile") + File.separator + "Data.txt";
        EmptyPath = functionCalls.filepath("Textfile") + File.separator + "Empty.txt";

        OrganizationName = settings.getString("OrganizationName", "");

        /*Picasso.with(Visitor_Details_Bluetooth.this).load(Visitor_Photo).error(R.drawable.blankperson).into(Visitor_image);*/
        imageLoader.get(Visitor_Photo, ImageLoader.getImageListener(Visitor_image, R.drawable.blankperson,
                R.drawable.blankperson));
        Visitor_image.setImageUrl(Visitor_Photo, imageLoader);
        tv_name.setText(Visitor_Name);
        tv_mobile.setText(Visitor_Mobile);
        tv_address.setText(Visitor_Fromaddress);
        tv_tomeet.setText(Visitor_ToMeet);
        tv_checkintime.setText(Visitor_CheckinTime);
        tv_checkouttime.setText(Visitor_CheckoutTime);
        tv_vehicleno.setText(Visitor_VehicleNo);
        tv_entry.setText(CheckinUser);
        tv_exit.setText(CheckoutUser);
        DisplayFields();

        Checkout_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VisitorManualCheckout visitorManualCheckout = task.new VisitorManualCheckout(detailsValue, Organization_ID,
                        Visitor_id, CheckingUser, Visitor_Details_EL101.this);
                dialog = ProgressDialog.show(Visitor_Details_EL101.this, "", "Checking Out Please wait...", true);
                checkingoutthread = null;
                Runnable runnable = new TestCheckOut();
                checkingoutthread = new Thread(runnable);
                checkingoutthread.start();
                visitorManualCheckout.execute();
            }
        });

        Print_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PrintingData();
            }
        });
    }

    class TestCheckOut implements Runnable {

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    dochecking();
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                }
            }
        }
    }

    public void dochecking() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (detailsValue.isVisitorsCheckOutSuccess()) {
                        detailsValue.setVisitorsCheckOutSuccess(false);
                        dialog.dismiss();
                        Toast.makeText(Visitor_Details_EL101.this, "Successfully Checked Out", Toast.LENGTH_SHORT).show();
                        Intent returnIntent = new Intent();
                        setResult(Activity.RESULT_OK, returnIntent);
                        finish();
                    } else if (detailsValue.isVisitorsCheckOutFailure()) {
                        detailsValue.setVisitorsCheckOutFailure(false);
                        dialog.dismiss();
                        Toast.makeText(Visitor_Details_EL101.this, "CheckOut Failed Please try once again", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                }
            }
        });
    }

    private void PrintingData() {
        dialog = ProgressDialog.show(Visitor_Details_EL101.this, "", "Printing Data...", true);
        Log.d("debug", "Saving Text");
        /*dataPrinting.SaveOrganization(OrganizationName);
        dataPrinting.SaveHeader();
        SaveData();
        dataPrinting.SaveEmpty();*/
        Log.d("debug", "Printing Header");
        el101_102device.SendCommad(new byte[]{0x1d, 0x21, 0x01});
        el101_102device.SendCommad(new byte[]{0x1b, 0x61, 0x01});
        /*printString(OrganizationPath);*/
        el101_102device.printString(OrganizationName/*+"\n"*/);
        el101_102device.SendCommad(new byte[]{0x1d, 0x21, 0x00});
        el101_102device.SendCommad(new byte[]{0x1d, 0x21, 0x00});
        el101_102device.SendCommad(new byte[]{0x1d, 0x21, 0x00});
        /*printString(HeaderPath);*/
        el101_102device.printString("VISITOR"/*+"\n"*/);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d("debug", "Printing Image");
                imageprinting = true;
                /*PrintImage2(Visitor_image);*/
                el101_102device.PrintHumanImage(Visitor_image);
            }
        }, 1500);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d("debug", "Printing BarCode");
                if (imageprinting) {
                    imageprinting = false;
                }
                /*printString(EmptyPath);*/
                if (settings.getString("Scannertype", "").equals("Barcode")) {
                    el101_102device.printString("    "+"\n");
                    barcodeprinting = true;
                    el101_102device.printBarCode(BarCodeValue);
                } else {
                    el101_102device.printQRcode(195, BarCodeValue);
                }
            }
        }, 4000);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d("debug", "Printing Header");
                if (barcodeprinting) {
                    barcodeprinting = false;
                    el101_102device.printString("    "+"\n");
                }
                /*printString(EmptyPath);*/
                el101_102device.SendCommad(new byte[]{0x1b, 0x61, 0x00});
                el101_102device.SendCommad(new byte[]{0x1b, 0x61, 0x00});
                el101_102device.SendCommad(new byte[]{0x1b, 0x61, 0x00});
                /*printString(DataPath);*/
                SaveData();
                /*printString(EmptyPath);
                printString(EmptyPath);*/
                el101_102device.printString("    "+"\n");
                el101_102device.printString("    "+"\n");
            }
        }, 6000);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                dialog.dismiss();
                showdialog(END_DLG);
            }
        }, 7500);
    }

    public void SaveData() {

        String path = functionCalls.filepath("Textfile");
        String filename = "Data.txt";
        try {
            /*File f = new File(path + File.separator + filename);
            FileOutputStream fOut = new FileOutputStream(f);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);*/

            HashSet<String> Printdisplay = new HashSet<>();
            Printdisplay = printingService.printingset;
            printingdisplay = new ArrayList<>();
            printingdisplay.addAll(Printdisplay);
            Collections.sort(printingdisplay);
            if (printingdisplay.size() > 0) {
                functionCalls.LogStatus("Printing Display Size: "+printingdisplay.size());
                for (int i = 0; i < printingdisplay.size(); i++) {
                    String PrintOrder = printingdisplay.get(i).toString();
                    functionCalls.LogStatus("Print Order: "+PrintOrder);
                    String Display = PrintOrder.substring(2, PrintOrder.length());
                    functionCalls.LogStatus("Display: "+Display);
                    /*printString(Display+": ");*/
                    if (Display.equals("Name")) {
                        /*myOutWriter.append(Visitor_Name + "\r\n");*/
                        el101_102device.printString(Display+": "+Visitor_Name/*+"\n"*/);
                    }
                    if (Display.equals("Mobile")) {
                        /*myOutWriter.append(Visitor_Mobile + "\r\n");*/
                        el101_102device.printString(Display+": "+Visitor_Mobile/*+"\n"*/);
                    }
                    if (Display.equals("From")) {
                        /*myOutWriter.append(Visitor_Fromaddress + "\r\n");*/
                        el101_102device.printString(Display+": "+Visitor_Fromaddress/*+"\n"*/);
                    }
                    if (Display.equals("To Meet")) {
                        /*myOutWriter.append(Visitor_ToMeet + "\r\n");*/
                        el101_102device.printString(Display+": "+Visitor_ToMeet/*+"\n"*/);
                    }
                    if (Display.equals("Date")) {
                        /*myOutWriter.append(Visitor_CheckinTime);*/
                        el101_102device.printString(Display+": "+Visitor_CheckinTime/*+"\n"*/);
                    }
                    if (Display.equals("Visitor Designation")) {
                        /*myOutWriter.append(Visitor_Designation + "\r\n");*/
                        el101_102device.printString(Display+": "+Visitor_Designation/*+"\n"*/);
                    }
                    if (Display.equals("Department")) {
                        /*myOutWriter.append(Department + "\r\n");*/
                        el101_102device.printString(Display+": "+Department/*+"\n"*/);
                    }
                    if (Display.equals("Purpose")) {
                        /*myOutWriter.append(Purpose + "\r\n");*/
                        el101_102device.printString(Display+": "+Purpose/*+"\n"*/);
                    }
                    if (Display.equals("House No")) {
                        /*myOutWriter.append(House_number + "\r\n");*/
                        el101_102device.printString(Display+": "+House_number/*+"\n"*/);
                    }
                    if (Display.equals("Flat No")) {
                        /*myOutWriter.append(Flat_number + "\r\n");*/
                        el101_102device.printString(Display+": "+Flat_number/*+"\n"*/);
                    }
                    if (Display.equals("Block")) {
                        /*myOutWriter.append(Block + "\r\n");*/
                        el101_102device.printString(Display+": "+Block/*+"\n"*/);
                    }
                    if (Display.equals("No of Visitor")) {
                        /*myOutWriter.append(No_Visitor + "\r\n");*/
                        el101_102device.printString(Display+": "+No_Visitor/*+"\n"*/);
                    }
                    if (Display.equals("Class")) {
                        /*myOutWriter.append(aClass + "\r\n");*/
                        el101_102device.printString(Display+": "+aClass/*+"\n"*/);
                    }
                    if (Display.equals("Section")) {
                        /*myOutWriter.append(Section + "\r\n");*/
                        el101_102device.printString(Display+": "+Section/*+"\n"*/);
                    }
                    if (Display.equals("Student")) {
                        /*myOutWriter.append(Student_Name + "\r\n");*/
                        el101_102device.printString(Display+": "+Student_Name/*+"\n"*/);
                    }
                    if (Display.equals("Id Card")) {
                        /*myOutWriter.append(ID_Card + "\r\n");*/
                        el101_102device.printString(Display+": "+ID_Card/*+"\n"*/);
                    }
                    if (Display.equals("Entry")) {
                        /*myOutWriter.append(CheckinUser + "\r\n");*/
                        el101_102device.printString(Display+": "+CheckinUser/*+"\n"*/);
                    }
                    if (Display.equals("Email")) {
                        /*myOutWriter.append(Email + "\r\n");*/
                        el101_102device.printString(Display+": "+Email/*+"\n"*/);
                    }
                    if (Display.equals("Vehicle Number")) {
                        /*myOutWriter.append(Vehicleno + "\r\n");*/
                        el101_102device.printString(Display+": "+Vehicleno/*+"\n"*/);
                    }
                }
            }
            /*myOutWriter.append(" " + "\r\n");
            myOutWriter.close();
            fOut.close();*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showdialog(int id) {
        switch (id) {
            case END_DLG:
                AlertDialog.Builder endbuilder = new AlertDialog.Builder(this);
                endbuilder.setTitle("Printing Details");
                endbuilder.setCancelable(false);
                endbuilder.setMessage("Did a Data got a printed correctly...??");
                endbuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                endbuilder.setNegativeButton("REPRINT", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PrintingData();
                    }
                });
                AlertDialog endalert = endbuilder.create();
                endalert.show();
                break;
        }
    }

    private void DisplayFields() {
        functionCalls.LogStatus("Display field Started");
        HashSet<String> hashSet = new HashSet<>();
        hashSet = fieldsService.fieldset;
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.addAll(hashSet);
        if (arrayList.size() > 0) {
            for (int i = 0; i < arrayList.size(); i++) {
                String value = arrayList.get(i).toString();
                if (value.equals("Visitor Email")) {
                    emailayout.setVisibility(View.VISIBLE);
                    tv_email.setText(Email);
                }
                if (value.equals("Visitor Designation")) {
                    designationlayout.setVisibility(View.VISIBLE);
                    tv_designation.setText(Visitor_Designation);
                }
                if (value.equals("Department")) {
                    departmentlayout.setVisibility(View.VISIBLE);
                    tv_department.setText(Department);
                }
                if (value.equals("Purpose")) {
                    purposelayout.setVisibility(View.VISIBLE);
                    tv_purpose.setText(Purpose);
                }
                if (value.equals("House No")) {
                    housenolayout.setVisibility(View.VISIBLE);
                    tv_houseno.setText(House_number);
                }
                if (value.equals("Flat No")) {
                    flatnolayout.setVisibility(View.VISIBLE);
                    tv_flatno.setText(Flat_number);
                }
                if (value.equals("Block")) {
                    blocklayout.setVisibility(View.VISIBLE);
                    tv_block.setText(Block);
                }
                if (value.equals("No of Visitor")) {
                    noofvisitorlayout.setVisibility(View.VISIBLE);
                    tv_noofvisitor.setText(No_Visitor);
                }
                if (value.equals("Class")) {
                    classlayout.setVisibility(View.VISIBLE);
                    tv_class.setText(aClass);
                }
                if (value.equals("Section")) {
                    sectionlayout.setVisibility(View.VISIBLE);
                    tv_section.setText(Section);
                }
                if (value.equals("Student Name")) {
                    studentnamelayout.setVisibility(View.VISIBLE);
                    tv_studentname.setText(Student_Name);
                }
                if (value.equals("ID Card No")) {
                    idcardlayout.setVisibility(View.VISIBLE);
                    tv_idcardno.setText(ID_Card);
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        el101_102device.timehandler.removeCallbacks(el101_102device.timerunnable);
        if (el101_102device.mSerialPort != null)
            el101_102device.mSerialPort.close();
        el101_102device.mSerialPort = null;
        if (ContextView.equals("Visitors")) {
            el101_102device.EnableBtn(false);
            functionCalls.deleteTextfile("Organization.txt");
            functionCalls.deleteTextfile("Header.txt");
            functionCalls.deleteTextfile("Empty.txt");
            functionCalls.deleteTextfile("Data.txt");
        }
        super.onDestroy();
    }
}
