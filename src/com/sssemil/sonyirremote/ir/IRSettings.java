/*
 * Copyright (c) 2014 Emil Suleymanov
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
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
package com.sssemil.sonyirremote.ir;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.sssemil.sonyirremote.ir.Utils.Compress;
import com.sssemil.sonyirremote.ir.Utils.Decompress;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

public class IRSettings extends PreferenceActivity implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "IRSettings";
    private SharedPreferences settings;
    private String http_path_root2;
    private String http_path_last_download1;
    private String http_path_last_download2;
    private ArrayList<String> ar = new ArrayList<String>();
    private String ar2;
    private String irpath = Environment.getExternalStorageDirectory() + "/irremote_keys/";//place to store commands
    private String last_ver = "zirt";
    private String cur_ver;
    private ProgressDialog mProgressDialog;
    private AlertDialog.Builder adb;
    private String resp = "ko";
    private String lastWord;
    private boolean cont = false;
    private String item = "null";
    private EditText brandN, itemN;
    private Spinner spinner;
    private int saved_theme;

    public static String normalisedVersion(String version) {
        return normalisedVersion(version, ".", 4);
    }

    public static String normalisedVersion(String version, String sep, int maxWidth) {
        String[] split = Pattern.compile(sep, Pattern.LITERAL).split(version);
        StringBuilder sb = new StringBuilder();
        for (String s : split) {
            sb.append(String.format("%" + maxWidth + 's', s));
        }
        return sb.toString();
    }

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
        getActionBar().setDisplayHomeAsUpEnabled(true);
        http_path_root2 = getString(R.string.http_path_root2);
        http_path_last_download1 = getString(R.string.http_path_last_download1);
        http_path_last_download2 = getString(R.string.http_path_last_download2);
        settings = getSharedPreferences(IRCommon.getInstance().PREFS_NAME(this), 0);
        adb = new AlertDialog.Builder(this);
        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            cur_ver = pInfo.versionName;
            findPreference("buildPref").setSummary(cur_ver);
        } catch (PackageManager.NameNotFoundException e) {
            Log.d(TAG, "catch " + e.toString() + " hit in run", e);
        }
        cur_ver = pInfo.versionName;
    }

    @Override
    protected void onApplyThemeResource(Resources.Theme theme, int resid, boolean first) {
        saved_theme = IRCommon.getInstance().getCurrentThemeId(this, resid);
        theme.applyStyle(saved_theme, true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                Intent intent = new Intent(IRSettings.this,
                        IRMain.class);
                intent.putExtra("restart", "1");
                startActivity(intent);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public String compare(String v1, String v2) {
        String s1 = normalisedVersion(v1);
        String s2 = normalisedVersion(v2);
        int cmp = s1.compareTo(s2);
        return cmp < 0 ? "<" : cmp > 0 ? ">" : "==";
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
                    adb.setIcon(android.R.drawable.ic_dialog_alert);
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
                        File df = new File(irpath + lastWord.substring(lastWord.lastIndexOf("/") + 1).substring(0, lastWord.substring(lastWord.lastIndexOf("/") + 1).length() - 4));
                        IRCommon.delete(df);
                    } catch (IOException e) {
                        Log.d(TAG, "catch " + e.toString() + " hit in run", e);
                    }
                    final DownloadTask downloadTask = new DownloadTask();
                    try {
                        downloadTask.execute(lastWord).get();
                    } catch (InterruptedException e) {
                        Log.d(TAG, "catch " + e.toString() + " hit in run", e);
                    } catch (ExecutionException e) {
                        Log.d(TAG, "catch " + e.toString() + " hit in run", e);
                    }
                    mProgressDialog.cancel();

                    if (!resp.equals("ok")) {
                        adb.setTitle(getString(R.string.download));
                        adb.setMessage(getString(R.string.ser3));
                        adb.setIcon(android.R.drawable.ic_dialog_alert);
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
                        LayoutInflater li = LayoutInflater.from(IRSettings.this);
                        final View promptsView = li.inflate(R.layout.done_menu, null);
                        TextView tw = (TextView) promptsView.findViewById(R.id.textView2);
                        tw.setText(ar2);
                        adb = new AlertDialog.Builder(IRSettings.this);
                        adb.setTitle(getString(R.string.downloadT));
                        adb.setIcon(android.R.drawable.ic_dialog_info);
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
                    resp = "ko";
                }
            }
        }).start();
    }


    public void onAddDeviceClick(View paramView) {
        AlertDialog.Builder adb;
        try {
            itemN = (EditText) paramView
                    .findViewById(R.id.editText);
            brandN = (EditText) paramView
                    .findViewById(R.id.editText2);
            if (itemN.getText() != null && brandN.getText() != null) {
                String all = brandN.getText().toString() + "-" + itemN.getText().toString();
                if (!all.equals("-")) {
                    File localFile2 = new File(irpath + brandN.getText().toString() + "-" + itemN.getText().toString());
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
            adb.setIcon(android.R.drawable.ic_dialog_alert);
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
            if (key.equals("aboutPref")) {
                Intent intent = new Intent(this,
                        IRAbout.class);
                startActivity(intent);
            } else if (key.equals("open_sourcePref")) {
                Intent intent = new Intent(this,
                        IRLicense.class);
                startActivity(intent);
            } else if (key.equals("addBtn")) {

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
            } else if (key.equals("downBtn")) {
                mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setMessage(getString(R.string.gtlst));
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                mProgressDialog.show();

                new Thread(new Runnable() {
                    public void run() {
                        final GetAwItems getAwItems1 = new GetAwItems();
                        try {
                            getAwItems1.execute().get();
                        } catch (InterruptedException e) {
                            Log.d(TAG, "catch " + e.toString() + " hit in run", e);
                        } catch (ExecutionException e) {
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
            } else if (key.equals("rmBtn")) {
                try {
                    adb.setTitle(getString(R.string.remove));
                    ar.clear();
                    for (File localFile1 : new File(irpath).listFiles()) {
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
                            adb.setIcon(android.R.drawable.ic_dialog_alert);
                            adb.setPositiveButton(getString(R.string.pos_ans), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    item = ar.get(selected);
                                    File dir = new File(irpath + item);
                                    try {
                                        IRCommon.delete(dir);
                                    } catch (IOException e) {
                                        Log.d(TAG, "catch " + e.toString() + " hit in run", e);
                                        adb.setTitle(getString(R.string.error));
                                        adb.setMessage(getString(R.string.failed_del_fl_io));
                                        adb.setIcon(android.R.drawable.ic_dialog_alert);
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
                    adb.setIcon(android.R.drawable.ic_dialog_alert);
                    adb.setPositiveButton(getString(R.string.pos_ans), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    adb.show();
                }
            } else if (key.equals("checkUpd")) {
                update();
            } else if (key.equals("sbmtBug")) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/sssemil/android_packages_apps_SonyIRRemote/issues"));
                startActivity(browserIntent);
            } else if (key.equals("sbmtDev")) {
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
                                            spinner = (Spinner) promptsView.findViewById(R.id.spinner);
                                            item = spinner.getSelectedItem().toString();
                                            Compress c = new Compress(irpath + item, Environment.getExternalStorageDirectory() + "/" + item + ".zip");
                                            c.zip();
                                            Intent emailIntent = new Intent(Intent.ACTION_SEND);
                                            emailIntent.setType("application/zip");
                                            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"suleymanovemil8@gmail.com"});
                                            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "New IR device");
                                            emailIntent.putExtra(Intent.EXTRA_TEXT, item);
                                            emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:///sdcard/" + item + ".zip"));
                                            startActivity(Intent.createChooser(emailIntent, "Send by mail..."));
                                        } catch (NullPointerException e) {
                                            Log.d(TAG, "catch " + e.toString() + " hit in run", e);
                                            adb = new AlertDialog.Builder(IRSettings.this);
                                            adb.setTitle(getString(R.string.error));
                                            adb.setMessage(getString(R.string.you_need_to_select));
                                            adb.setIcon(android.R.drawable.ic_dialog_alert);
                                            adb.setPositiveButton(getString(R.string.pos_ans), new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {

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
                ArrayList<String> localArrayList1 = new ArrayList<String>();
                boolean edited = false;

                for (File localFile1 : new File(irpath).listFiles()) {
                    if (localFile1.isDirectory()) {
                        if (!localArrayList1.contains(localFile1.getName())) {
                            localArrayList1.add(localFile1.getName());
                            edited = true;
                        }
                    }

                    if (edited) {
                        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                                android.R.layout.simple_spinner_item, localArrayList1);
                        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner.setAdapter(dataAdapter);
                    }
                }
            } else if (key.equals(("theme"))) {
                SharedPreferences settings = getSharedPreferences(IRCommon.getInstance().PREFS_NAME(this), 0);
                if (settings.contains("theme")) {
                    if (settings.getString("theme", null).equals("1")) {
                        super.setTheme(R.style.Holo);
                    } else if (settings.getString("theme", null).equals("2")) {
                        super.setTheme(R.style.Holo_Light_DarkActionBar);
                    } else if (settings.getString("theme", null).equals("3")) {
                        super.setTheme(R.style.Theme_Holo_Light);
                    }
                }
            }
        }
        return true;
    }

    public void update() {
        final GetLastVer getLastVer1 = new GetLastVer();
        final AlertDialog.Builder adb = new AlertDialog.Builder(this);
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
                try {
                    Log.i("Update", "last_ver : " + getLastVer1.execute().get() + " cur_ver : " + cur_ver);
                } catch (InterruptedException e) {
                    Log.d(TAG, "catch " + e.toString() + " hit in run", e);
                } catch (ExecutionException e) {
                    Log.d(TAG, "catch " + e.toString() + " hit in run", e);
                }
                if (last_ver.equals("zirt")) {
                    adb.setTitle(getString(R.string.update));
                    adb.setMessage(getString(R.string.ser3));
                    adb.setIcon(android.R.drawable.ic_dialog_alert);
                    adb.setPositiveButton(getString(R.string.pos_ans), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            update();
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
                    String result = compare(cur_ver, last_ver);
                    boolean doUpdate = false;
                    if (result.equals(">")) {
                        doUpdate = false;
                    } else if (result.equals("<")) {
                        doUpdate = true;
                    } else if (result.equals("==")) {
                        doUpdate = false;
                    }


                    if (doUpdate) {
                        adb.setTitle(getString(R.string.update));
                        adb.setMessage(getString(R.string.new_version_available));
                        adb.setIcon(android.R.drawable.ic_dialog_alert);
                        adb.setPositiveButton(getString(R.string.pos_ans), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                mProgressDialog = new ProgressDialog(IRSettings.this);
                                new Thread(new Runnable() {
                                    public void run() {
                                        mProgressDialog.setMessage(getString(R.string.downloading_new));
                                        mProgressDialog.setIndeterminate(true);
                                        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                mProgressDialog.show();
                                            }
                                        });

                                        final DownloadApp downloadApp1 = new DownloadApp();
                                        try {
                                            downloadApp1.execute(http_path_last_download1 + last_ver + http_path_last_download2).get();
                                        } catch (InterruptedException e) {
                                            Log.d(TAG, "catch " + e.toString() + " hit in run", e);
                                        } catch (ExecutionException e) {
                                            Log.d(TAG, "catch " + e.toString() + " hit in run", e);
                                        }
                                        mProgressDialog.cancel();
                                    }
                                }).start();
                            }
                        });

                        adb.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
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
                    } else if (!doUpdate) {
                        adb.setTitle(getString(R.string.update));
                        adb.setMessage(getString(R.string.already_new));
                        adb.setPositiveButton(getString(R.string.pos_ans), null);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mProgressDialog.cancel();
                                adb.show();
                            }
                        });
                    }
                }
            }
        }).start();
    }

    @Override
    public void onStop() {
        super.onStop();
        EasyTracker.getInstance(this).activityStart(this);
        settings.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        EasyTracker easyTracker = EasyTracker.getInstance(this);
        easyTracker.set(Fields.TRACKING_ID, "UA-52301928-1");
        easyTracker.activityStart(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        settings.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        settings.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        settings = getSharedPreferences(IRCommon.getInstance().PREFS_NAME(this), 0);
        if (settings.contains("theme")) {
            if (!String.valueOf(saved_theme).equals(settings.getString("theme", null))) {
                Intent i = getBaseContext().getPackageManager()
                        .getLaunchIntentForPackage(getBaseContext().getPackageName());
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();
            }
        }
    }

    class DownloadTask extends AsyncTask<String, Integer, String> {

        public DownloadTask() {
        }

        protected String doInBackground(String... sUrl) {
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
                Log.v("DownloadTask", "Starting... ");
                URL url = new URL(sUrl[0]);
                String filePath = url.getFile();
                String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);

                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage();
                }

                // this will be useful to display download percentage
                // might be -1: server did not report the length
                int fileLength = connection.getContentLength();

                // download the file
                input = connection.getInputStream();
                output = new FileOutputStream(Environment.getExternalStorageDirectory() + "/" + fileName);
                Log.v("DownloadTask", "output " + Environment.getExternalStorageDirectory() + "/" + fileName);

                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    // allow canceling with back button
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;
                    // publishing the progress....
                    if (fileLength > 0) // only if total length is known
                        publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }
                Log.v("DownloadTask", "Done!");
                //---------Unzip--------
                String zipFile = Environment.getExternalStorageDirectory() + "/" + fileName;
                String unzipLocation = irpath;

                Decompress d = new Decompress(zipFile, unzipLocation);
                ar2 = d.unzip();
                //----------------------
                resp = "ok";
                return "ok";
            } catch (MalformedURLException e) {
                Log.d(TAG, "catch " + e.toString() + " hit in run", e);
                return e.toString();
            } catch (FileNotFoundException e) {
                Log.d(TAG, "catch " + e.toString() + " hit in run", e);
                return e.toString();
            } catch (IOException e) {
                Log.d(TAG, "catch " + e.toString() + " hit in run", e);
                return e.toString();
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException e) {
                    Log.d(TAG, "catch IOException hit in run", e);
                }

                if (connection != null)
                    connection.disconnect();
            }
        }
    }

    class GetAwItems extends AsyncTask<String, Integer, String> {

        DefaultHttpClient httpclient = new DefaultHttpClient();

        public GetAwItems() {
        }

        protected String doInBackground(String... sUrl) {
            try {
                HttpGet httppost = new HttpGet(http_path_root2 + "downloads");
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity ht = response.getEntity();

                BufferedHttpEntity buf = new BufferedHttpEntity(ht);

                InputStream is = buf.getContent();

                BufferedReader r = new BufferedReader(new InputStreamReader(is));

                String line;
                ar.clear();
                while ((line = r.readLine()) != null) {
                    ar.add(line);
                }
                Log.i("line", String.valueOf(ar.size()));

                return ar.get(0);
            } catch (IOException e) {
                Log.d(TAG, "catch " + e.toString() + " hit in run", e);
                return null;
            }
        }
    }

    class DownloadApp extends AsyncTask<String, Integer, String> {

        public DownloadApp() {
        }

        protected String doInBackground(String... sUrl) {
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
                Log.v("DownloadApp", "Starting... ");
                URL url = new URL(sUrl[0]);

                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage();
                }

                // this will be useful to display download percentage
                // might be -1: server did not report the length
                int fileLength = connection.getContentLength();

                // download the file
                input = connection.getInputStream();
                output = new FileOutputStream(Environment.getExternalStorageDirectory() + "/upd.apk");
                Log.v("DownloadApp", "output " + Environment.getExternalStorageDirectory() + "/upd.apk");

                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    // allow canceling with back button
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;
                    // publishing the progress....
                    if (fileLength > 0) // only if total length is known
                        publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }
                Log.v("DownloadApp", "Done!");

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/upd.apk")), "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } catch (MalformedURLException e) {
                Log.d(TAG, "catch " + e.toString() + " hit in run", e);
            } catch (FileNotFoundException e) {
                Log.d(TAG, "catch " + e.toString() + " hit in run", e);
            } catch (IOException e) {
                Log.d(TAG, "catch " + e.toString() + " hit in run", e);
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException e) {
                    Log.d(TAG, "catch " + e.toString() + " hit in run", e);
                }

                if (connection != null)
                    connection.disconnect();
            }
            return null;
        }
    }

    class GetLastVer extends AsyncTask<String, Integer, String> {

        DefaultHttpClient httpclient = new DefaultHttpClient();

        public GetLastVer() {
        }

        protected String doInBackground(String... sUrl) {
            try {
                HttpGet httppost = new HttpGet(http_path_root2 + "last.php");
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity ht = response.getEntity();

                BufferedHttpEntity buf = new BufferedHttpEntity(ht);

                InputStream is = buf.getContent();

                BufferedReader r = new BufferedReader(new InputStreamReader(is));

                String line;
                last_ver = "";
                while ((line = r.readLine()) != null) {
                    last_ver += line;
                }
                Log.i("GetLastVer", last_ver);
                return last_ver;
            } catch (IOException e) {
                Log.d(TAG, "catch " + e.toString() + " hit in run", e);
                return null;
            }
        }
    }
}