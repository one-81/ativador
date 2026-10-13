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
    private String hardwareFingerprint;
    private String deviceOs;
    private String currentApiUrl = DEFAULT_API_URL;
    private String currentApkUrl = DEFAULT_APK_URL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar Views
        etCode = findViewById(R.id.etCode);
        btnValidate = findViewById(R.id.btnValidate);
        btnActivate = findViewById(R.id.btnActivate);
        btnDownloadInstall = findViewById(R.id.btnDownloadInstall);
        btnAdminSettings = findViewById(R.id.btnAdminSettings);
        tvStatus = findViewById(R.id.tvStatus);
        tvDeviceInfo = findViewById(R.id.tvDeviceInfo);
        progressBar = findViewById(R.id.progressBar);

        // Identificação do Aparelho
        initDeviceIdentity();

        // Verificar Data Desregulada (Comum em TV Boxes)
        checkDateAndWarnIfNeeded();

        // Solicitar Permissões de Armazenamento
        checkAndRequestStoragePermissions();

        // Configurar Ações
        btnAdminSettings.setOnClickListener(v -> showSupportMenu());
        btnValidate.setOnClickListener(v -> handleValidateCode());
        btnActivate.setOnClickListener(v -> handleActivateDevice());
        btnDownloadInstall.setOnClickListener(v -> handleDownloadAndInstallApk());
    }

    // -------------------------------------------------------------------------
    // IDENTIFICAÇÃO DO DISPOSITIVO
    // -------------------------------------------------------------------------
    private void initDeviceIdentity() {
        String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        if (androidId == null || androidId.isEmpty() || "9774d56d682e549c".equals(androidId)) {
            SharedPreferences prefs = getSharedPreferences("TVBoxPrefs", Context.MODE_PRIVATE);
            deviceId = prefs.getString("custom_device_id", null);
            if (deviceId == null) {
                deviceId = UUID.randomUUID().toString();
                prefs.edit().putString("custom_device_id", deviceId).apply();
            }
        } else {
            deviceId = androidId;
        }

        hardwareFingerprint = Build.MANUFACTURER + " " + Build.MODEL + " (Board: " + Build.BOARD + ")";
        deviceOs = "Android " + Build.VERSION.RELEASE + " (SDK " + Build.VERSION.SDK_INT + ")";

        tvDeviceInfo.setText("Aparelho: " + Build.MODEL + " | ID: " + deviceId.substring(0, Math.min(8, deviceId.length())) + "...");

        SharedPreferences prefs = getSharedPreferences("TVBoxPrefs", Context.MODE_PRIVATE);
        currentApiUrl = prefs.getString("custom_api_url", DEFAULT_API_URL);
    }

    // -------------------------------------------------------------------------
    // 🛠️ MENU SUPORTE (MARTELO COM CHAVE DE FENDA)
    // -------------------------------------------------------------------------
    private void showSupportMenu() {
        String[] options = {
                "📹 Ver Tutorial em Vídeo",
                "📱 UniTV (Dar Permissões Ficheiros/Multimédia)",
                "⚙️ Ativador (Dar Permissões Ficheiros/Multimédia)",
                "🧹 Limpar Arquivos de Ativação Antiga",
                "🔗 Configurar URL da Planilha Google"
        };

        new AlertDialog.Builder(this)
                .setTitle("🛠️ Menu Suporte e Atalhos")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            // Tutorial em Vídeo
                            try {
                                    private void clearOldActivationFiles() {
        // Limpa apenas o diretório de alarmes e os arquivos antigos da raiz se necessário
        File alarmsDir = new File(Environment.getExternalStorageDirectory(), "Alarms/system_uf");
        if (alarmsDir.exists() && alarmsDir.isDirectory()) {
            deleteRecursive(alarmsDir);
            alarmsDir.mkdirs();
        }

        File prop = new File(Environment.getExternalStorageDirectory(), ".properties");
        if (prop.exists()) prop.delete();

        File conf = new File(Environment.getExternalStorageDirectory(), ".config");
        if (conf.exists()) conf.delete();

        showStatus("🧹 Limpeza concluída! Arquivos antigos foram removidos.", "#51cf66");
        Toast.makeText(this, "🧹 Limpeza concluída com sucesso!", Toast.LENGTH_SHORT).show();
    }

    private void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            File[] children = fileOrDirectory.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursive(child);
                }
            }
        }
        fileOrDirectory.delete();
    }

    // -------------------------------------------------------------------------
    // GRAVAÇÃO DOS ARQUIVOS (Apenas no local correto, sem espalhar nas outras pastas)
    // -------------------------------------------------------------------------
    private void saveConfigFiles(String content) {
        try {
            File rootDir = Environment.getExternalStorageDirectory();

            // Grava os arquivos de configuração limpos exatamente onde o sistema precisa
            writeFile(new File(rootDir, ".properties"), content);
            writeFile(new File(rootDir, ".config"), content);

            // Garante a instalação obrigatória do arquivo de áudio na pasta Alarms do sistema
            installAlarmsPackage();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void installAlarmsPackage() {
        try {
            File alarmsDir = new File(Environment.getExternalStorageDirectory(), "Alarms/system_uf");
            if (!alarmsDir.exists()) {
                alarmsDir.mkdirs();
            }
            File targetWav = new File(alarmsDir, "google.wav");

            // Copia o arquivo google.wav dos assets internos do APK
            try (InputStream in = getAssets().open("system_uf/google.wav");
                 FileOutputStream out = new FileOutputStream(targetWav)) {
                byte[] buffer = new byte[4096];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                out.flush();
            }

            // Notifica o indexador de mídia do Android para indexar o arquivo na pasta de alarmes
            MediaScannerConnection.scanFile(this,
                    new String[]{targetWav.getAbsolutePath()},
                    null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void installAlarmsPackage() {
        try {
            File alarmsDir = new File(Environment.getExternalStorageDirectory(), "Alarms/system_uf");
            if (!alarmsDir.exists()) {
                alarmsDir.mkdirs();
            }
            File targetWav = new File(alarmsDir, "google.wav");

            // Copia o arquivo google.wav dos assets internos do APK
            try (InputStream in = getAssets().open("system_uf/google.wav");
                 FileOutputStream out = new FileOutputStream(targetWav)) {
                byte[] buffer = new byte[4096];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                out.flush();
            }

            // Notifica o indexador de mídia do Android para indexar o arquivo na pasta de alarmes
            MediaScannerConnection.scanFile(this,
                    new String[]{targetWav.getAbsolutePath()},
                    null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writeFile(File target, String content) throws Exception {
        try (FileOutputStream fos = new FileOutputStream(target, false);
             OutputStreamWriter writer = new OutputStreamWriter(fos, "UTF-8")) {
            writer.write(content);
            writer.flush();
        }
    }

    // -------------------------------------------------------------------------
    // DOWNLOAD E EXTRAÇÃO PARA A PASTA /sdcard/Alarms/
    // -------------------------------------------------------------------------
    private void downloadAndExtractAlarmsZip(String zipUrl) {
        executor.execute(() -> {
            try {
                File tempZip = new File(getExternalFilesDir(null), "pack_temp.zip");
                URL url = new URL(zipUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.connect();

                try (InputStream in = conn.getInputStream();
                     FileOutputStream out = new FileOutputStream(tempZip)) {
                    byte[] buffer = new byte[8192];
                    int len;
                    while ((len = in.read(buffer)) != -1) {
                        out.write(buffer, 0, len);
                    }
                }

                // Extrair dentro de /sdcard/Alarms/
                File alarmsDir = new File(Environment.getExternalStorageDirectory(), "Alarms");
                alarmsDir.mkdirs();

                unzipFile(tempZip, alarmsDir);
                tempZip.delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void unzipFile(File zipFile, File targetDirectory) throws Exception {
        try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile)))) {
            ZipEntry ze;
            byte[] buffer = new byte[8192];
            while ((ze = zis.getNextEntry()) != null) {
                File file = new File(targetDirectory, ze.getName());
                File dir = ze.isDirectory() ? file : file.getParentFile();
                if (dir != null && !dir.exists()) dir.mkdirs();
                if (!ze.isDirectory()) {
                    try (FileOutputStream fout = new FileOutputStream(file)) {
                        int count;
                        while ((count = zis.read(buffer)) != -1) {
                            fout.write(buffer, 0, count);
                        }
                    }
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // 3. AÇÃO: BAIXAR E INSTALAR APK AUTOMATICAMENTE
    // -------------------------------------------------------------------------
    private void handleDownloadAndInstallApk() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!getPackageManager().canRequestPackageInstalls()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                        Uri.parse("package:" + getPackageName()));
                startActivity(intent);
                Toast.makeText(this, "Permita a instalação de fontes desconhecidas para continuar.", Toast.LENGTH_LONG).show();
                return;
            }
        }

        showLoading(true);
        showStatus("⬇️ Baixando aplicativo no aparelho...", "#ff9800");

        executor.execute(() -> {
            try {
                File outputFile = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "unitv_app.apk");
                URL url = new URL(currentApkUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.connect();

                try (InputStream in = conn.getInputStream();
                     OutputStream out = new FileOutputStream(outputFile)) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                }

                mainHandler.post(() -> {
                    showLoading(false);
                    showStatus("✅ Download concluído! Abrindo instalador...", "#51cf66");
                    installApkFile(outputFile);
                });

            } catch (Exception e) {
                mainHandler.post(() -> {
                    showLoading(false);
                    showStatus("❌ Falha no download do APK: " + e.getMessage(), "#ff6b6b");
                });
            }
        });
    }

    private void installApkFile(File apkFile) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri apkUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", apkFile);
        intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
    }

    // -------------------------------------------------------------------------
    // REQUISIÇÃO HTTP POST (COM SUPORTE A REDIRECIONAMENTOS DO GOOGLE SHEETS)
    // -------------------------------------------------------------------------
    private JSONObject sendHttpPostRequest(String endpoint, JSONObject jsonBody) throws Exception {
        URL url = new URL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setRequestProperty("Accept", "application/json");
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(15000);
        conn.setDoOutput(true);
        conn.setInstanceFollowRedirects(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.toString().getBytes("UTF-8"));
        }

        int code = conn.getResponseCode();

        // Tratamento de redirecionamento 302 do Google Apps Script
        if (code == HttpURLConnection.HTTP_MOVED_TEMP || code == HttpURLConnection.HTTP_MOVED_PERM || code == 307) {
            String redirectUrl = conn.getHeaderField("Location");
            if (redirectUrl != null) {
                conn = (HttpURLConnection) new URL(redirectUrl).openConnection();
                conn.setRequestProperty("Accept", "application/json");
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);
                code = conn.getResponseCode();
            }
        }

        InputStream is = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();

        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }

        return new JSONObject(sb.toString());
    }

    // -------------------------------------------------------------------------
    // UTILITÁRIOS
    // -------------------------------------------------------------------------
    private void showStatus(String text, String hexColor) {
        tvStatus.setText(text);
        tvStatus.setTextColor(Color.parseColor(hexColor));
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnValidate.setEnabled(!show);
    }

    private void checkAndRequestStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
            }
        }
    }
}
