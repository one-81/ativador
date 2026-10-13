package com.meuativador.tvbox;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.media.MediaScannerConnection;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class MainActivity extends AppCompatActivity {

    // =========================================================================
    // ⚙️ ENDPOINT ANÔNIMO: PODE SER UM GITHUB GIST OU SEU CLOUDFLARE WORKER
    // =========================================================================
    private static final String DEFAULT_API_URL = "https://script.google.com/macros/s/AKfycbyDkGEFayfQJrnAE-ySDt7-VxUkSG1PpndwVLL7jcHw_JVlzbE5sX8iSXarxkVWPnSh/exec";
    private static final String DEFAULT_APK_URL = "https://apk.hazelkenwooddevil.workers.dev/downloads/UnitvFree_5.9.0.apk";
    private static final String TUTORIAL_VIDEO_URL = "https://linktr.ee/suporte_tv";

    // Componentes da Interface
    private EditText etCode;
    private Button btnValidate;
    private Button btnActivate;
    private Button btnDownloadInstall;
    private TextView btnAdminSettings; // 🛠️ Botão do Martelo e Chave de Fenda
    private TextView tvStatus;
    private TextView tvDeviceInfo;
    private ProgressBar progressBar;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private String deviceId;
