/*
 * Copyright (c) 2014-2015 Emil Suleymanov <suleymanovemil8@gmail.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package com.sssemil.ir;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.sssemil.ir.Utils.net.Download;
import com.sssemil.ir.Utils.net.GetText;
import com.sssemil.ir.Utils.zip.Compress;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class IRSettings extends PreferenceActivity {

    private static final String TAG = "IRSettings";
    private String http_path_root2;
    private ProgressDialog mProgressDialog;
    private AlertDialog.Builder adb;
    private String lastWord;
    private boolean cont = false;
    private String item = "null";
    private Spinner spinner;
    private ArrayList<String> ar = new ArrayList<>();

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(IRSettings.this,
                IRMain.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);

        http_path_root2 = getString(R.string.http_path_root2);
        adb = new AlertDialog.Builder(this);
        try {
            findPreference("buildPref").setSummary(
                    getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            Log.d(TAG, "catch " + e.toString() + " hit in run", e);
        }
    }

    public void doOnDown(final String content) {
        adb = new AlertDialog.Builder(this);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(R.string.checking));
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgressDialog.show();
            }
        });
        new Thread(new Runnable() {
            public void run() {
                cont = false;
                try {
                    lastWord = content.substring(content.lastIndexOf(" ") + 1);
                    cont = true;
                } catch (NullPointerException e) {
                    Log.d(TAG, "catch " + e.toString() + " hit in run", e);
                    cont = false;
                    adb.setTitle(getString(R.string.error));
                    adb.setMessage(getString(R.string.you_need_to_select));
                    adb.setPositiveButton(getString(R.string.pos_ans), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mProgressDialog.cancel();
                            adb.show();
                        }
                    });
                }
                if (cont) {
                    mProgressDialog.setMessage(getString(R.string.downloading));
                    mProgressDialog.setIndeterminate(true);
                    mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mProgressDialog.cancel();
                            mProgressDialog.show();
                        }
                    });
                    try {
                        File df = new File(IRCommon.getIrPath()
                                + lastWord.substring(lastWord.lastIndexOf("/") + 1)
                                .substring(0, lastWord.substring(
                                        lastWord.lastIndexOf("/") + 1).length() - 4));
                        IRCommon.delete(df);
                    } catch (IOException e) {
                        Log.d(TAG, "catch " + e.toString() + " hit in run", e);
                    }
                    Download downloadZip1 = new Download(lastWord);

                    try {
                        Log.d(TAG, lastWord);
                        String list = downloadZip1.execute().get();
                        Log.d(TAG, list);
                        if (list.equals("ko")) {
                            mProgressDialog.cancel();
                            adb.setTitle(getString(R.string.download));
                            adb.setMessage(getString(R.string.ser3));
                            adb.setPositiveButton(getString(R.string.pos_ans), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    doOnDown(content);
                                }
                            });

                            adb.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    //finish();
                                }
                            });
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mProgressDialog.cancel();
                                    adb.show();
                                }
                            });
                        } else {
                            mProgressDialog.cancel();
                            LayoutInflater li = LayoutInflater.from(IRSettings.this);
                            final View promptsView = li.inflate(R.layout.done_menu, null);
                            TextView tw = (TextView) promptsView.findViewById(R.id.textView2);
                            tw.setText(list);
                            adb = new AlertDialog.Builder(IRSettings.this);
                            adb.setTitle(getString(R.string.downloadT));
                            adb
                                    .setCancelable(false)
                                    .setPositiveButton(getString(R.string.pos_ans),
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    //onAddDeviceClick(promptsView);
                                                }
                                            }
                                    );
                            adb.setView(promptsView);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mProgressDialog.cancel();
                                    adb.show();
                                }
                            });
                        }
                    } catch (InterruptedException | ExecutionException e) {
                        Log.d(TAG, "catch " + e.toString() + " hit in run", e);
                    }
                }
            }
        }).start();
    }


    public void onAddDeviceClick(View paramView) {
        AlertDialog.Builder adb;
        try {
            EditText itemN = (EditText) paramView
                    .findViewById(R.id.editText);
            EditText brandN = (EditText) paramView
                    .findViewById(R.id.editText2);
            if (itemN.getText() != null && brandN.getText() != null) {
                String all = brandN.getText().toString() + "-" + itemN.getText().toString();
                if (!all.equals("-")) {
                    File localFile2 = new File(IRCommon.getIrPath() + brandN.getText().toString() + "-" + itemN.getText().toString());
                    if (!localFile2.isDirectory()) {
                        localFile2.mkdirs();
                    }
                }
                adb = new AlertDialog.Builder(this);
                adb.setTitle(getString(R.string.done));
                adb.setMessage(getString(R.string.new_item) + " " + brandN.getText().toString() + "-" + itemN.getText().toString() + " " + getString(R.string.crt_slf));
                adb.setPositiveButton(getString(R.string.pos_ans), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                adb.show();
            } else {
                throw new NullPointerException();
            }
        } catch (NullPointerException e) {
            Log.d(TAG, "catch " + e.toString() + " hit in run", e);
            adb = new AlertDialog.Builder(this);
            adb.setTitle(getString(R.string.error));
            adb.setMessage(getString(R.string.you_need_to_select));
            adb.setPositiveButton(getString(R.string.pos_ans), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            adb.show();
        }
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
                                         final Preference preference) {
        String key = preference.getKey();
        adb = new AlertDialog.Builder(this);
        if (key != null) {
            switch (key) {
                case "aboutPref": {
                    Intent intent = new Intent(this,
                            IRAbout.class);
                    startActivity(intent);
                    break;
                }
                case "open_sourcePref": {
                    Intent intent = new Intent(this,
                            IRLicense.class);
                    startActivity(intent);
                    break;
                }
                case "addBtn": {

                    LayoutInflater li = LayoutInflater.from(IRSettings.this);
                    final View promptsView = li.inflate(R.layout.add_device_menu, null);
                    adb = new AlertDialog.Builder(IRSettings.this);
                    adb.setTitle(getString(R.string.add_new_device));
                    adb
                            .setCancelable(false)
                            .setPositiveButton(getString(R.string.pos_ans),
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            onAddDeviceClick(promptsView);
                                        }
                                    }
                            )
                            .setNegativeButton(getString(R.string.cancel),
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    }
                            );
                    adb.setView(promptsView);
                    adb.show();
                    break;
                }
                case "downBtn":
                    mProgressDialog = new ProgressDialog(this);
                    mProgressDialog.setMessage(getString(R.string.gtlst));
                    mProgressDialog.setIndeterminate(true);
                    mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    mProgressDialog.show();

                    new Thread(new Runnable() {
                        public void run() {
                            final GetText getAwItems1 = new GetText();
                            try {
                                ar = getAwItems1.execute(http_path_root2 + "downloads").get();
                            } catch (InterruptedException | ExecutionException e) {
                                Log.d(TAG, "catch " + e.toString() + " hit in run", e);
                            }
                            adb.setTitle(getString(R.string.downloadT));
                            String[] types = new String[ar.size()];
                            types = ar.toArray(types);
                            adb.setItems(types, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    mProgressDialog.cancel();
                                    Log.i("pr", ar.get(which));
                                    doOnDown(ar.get(which));
                                }

                            });
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mProgressDialog.cancel();
                                    adb.show();
                                }
                            });
                        }
                    }).start();
                    break;
                case "rmBtn":
                    try {
                        adb.setTitle(getString(R.string.remove));
                        ar.clear();
                        for (File localFile1 : new File(IRCommon.getIrPath()).listFiles()) {
                            if (localFile1.isDirectory()) {
                                if (!ar.contains(localFile1.getName())) {
                                    ar.add(localFile1.getName());
                                }
                            }
                        }
                        String[] types = new String[ar.size()];
                        types = ar.toArray(types);
                        adb.setItems(types, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final int selected = which;
                                dialog.dismiss();
                                adb = new AlertDialog.Builder(IRSettings.this);
                                adb.setTitle(getString(R.string.warning));
                                adb.setMessage(getString(R.string.are_u_s_del));
                                adb.setPositiveButton(getString(R.string.pos_ans), new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        item = ar.get(selected);
                                        File dir = new File(IRCommon.getIrPath() + item);
                                        try {
                                            IRCommon.delete(dir);
                                        } catch (IOException e) {
                                            Log.d(TAG, "catch " + e.toString() + " hit in run", e);
                                            adb.setTitle(getString(R.string.error));
                                            adb.setMessage(getString(R.string.failed_del_fl_io));
                                            adb.setPositiveButton(getString(R.string.pos_ans), new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                }
                                            });
                                            adb.show();
                                        }
                                        adb = new AlertDialog.Builder(IRSettings.this);
                                        adb.setTitle(getString(R.string.done));
                                        adb.setMessage(getString(R.string.done_removing) + " " + item + " " + getString(R.string.files));
                                        adb.setPositiveButton(getString(R.string.pos_ans), null);
                                        adb.show();
                                    }
                                });

                                adb.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                });
                                adb.show();
                            }
                        });
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adb.show();
                            }
                        });
                    } catch (NullPointerException e) {
                        Log.d(TAG, "catch " + e.toString() + " hit in run", e);
                        adb.setTitle(getString(R.string.error));
                        adb.setMessage(getString(R.string.you_need_to_select));
                        adb.setPositiveButton(getString(R.string.pos_ans), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                        adb.show();
                    }
                    break;
                case "sbmtBug":
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://github.com/sssemil/android_packages_apps_SonyIRRemote/issues"));
                    startActivity(browserIntent);
                    break;
                case "sbmtDev": {
                    LayoutInflater li = LayoutInflater.from(IRSettings.this);
                    final View promptsView = li.inflate(R.layout.sbmt_device_menu, null);
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                            IRSettings.this);
                    alertDialogBuilder.setTitle(getString(R.string.select_dev));
                    alertDialogBuilder
                            .setCancelable(false)
                            .setPositiveButton("OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            try {
                                                spinner = (Spinner) promptsView
                                                        .findViewById(R.id.spinner);
                                                item = spinner.getSelectedItem().toString();
                                                Compress c = new Compress(IRCommon.getIrPath() + item,
                                                        Environment.getExternalStorageDirectory()
                                                                + "/" + item + ".zip");
                                                c.zip();
                                                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                                                emailIntent.setType("application/zip");
                                                emailIntent.putExtra(Intent.EXTRA_EMAIL,
                                                        new String[]{"suleymanovemil8@gmail.com"});
                                                emailIntent.putExtra(Intent.EXTRA_SUBJECT,
                                                        "New IR device");
                                                emailIntent.putExtra(Intent.EXTRA_TEXT, item);
                                                emailIntent.putExtra(Intent.EXTRA_STREAM,
                                                        Uri.parse("file:///"
                                                                + Environment
                                                                .getExternalStorageDirectory() + "/"
                                                                + item + ".zip"));
                                                startActivity(Intent.createChooser(emailIntent,
                                                        "Send by mail..."));
                                            } catch (NullPointerException e) {
                                                Log.d(TAG, "catch " + e.toString() + " hit in run", e);
                                                adb = new AlertDialog.Builder(IRSettings.this);
                                                adb.setTitle(getString(R.string.error));
                                                adb.setMessage(getString(R.string.you_need_to_select));
                                                adb.setPositiveButton(getString(R.string.pos_ans),
                                                        new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog,
                                                                                int which) {

                                                            }
                                                        });
                                                adb.show();
                                            }

                                        }
                                    }
                            )
                            .setNegativeButton(getString(R.string.cancel),
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    }
                            );
                    alertDialogBuilder.setView(promptsView);
                    alertDialogBuilder.show();

                    spinner = ((Spinner) promptsView.findViewById(R.id.spinner));
                    ArrayList<String> localArrayList1 = new ArrayList<>();
                    boolean edited = false;

                    for (File localFile1 : new File(IRCommon.getIrPath()).listFiles()) {
                        if (localFile1.isDirectory()) {
                            if (!localArrayList1.contains(localFile1.getName())) {
                                localArrayList1.add(localFile1.getName());
                                edited = true;
                            }
                        }

                        if (edited) {
                            ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this,
                                    android.R.layout.simple_spinner_item, localArrayList1);
                            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinner.setAdapter(dataAdapter);
                        }
                    }
                    break;
                }
            }
        }
        return true;
    }

    @Override
    public void onStop() {
        super.onStop();
        EasyTracker.getInstance(this).activityStart(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        EasyTracker easyTracker = EasyTracker.getInstance(this);
        easyTracker.set(Fields.TRACKING_ID, IRCommon.getID());
        easyTracker.activityStart(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}