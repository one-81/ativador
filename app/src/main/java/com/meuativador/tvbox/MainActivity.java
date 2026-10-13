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
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(TUTORIAL_VIDEO_URL));
                                startActivity(browserIntent);
                            } catch (Exception e) {
                                Toast.makeText(this, "Não foi possível abrir o navegador.", Toast.LENGTH_SHORT).show();
                            }
                            break;

                        case 1:
                            // Abrir Configurações do UniTV
                            openAppPermissionSettings("com.integration.unitvsiptv", "UniTV");
                            break;

                        case 2:
                            // Abrir Configurações deste Ativador
                            openAppPermissionSettings(getPackageName(), "Ativador");
                            break;

                        case 3:
                            // Limpar pasta /sdcard/Alarms/ e residuais
                            clearOldActivationFiles();
                            break;

                        case 4:
                            showChangeApiUrlDialog();
                            break;
                    }
                })
                .setNegativeButton("FECHAR", null)
                .show();
    }

    private void showChangeApiUrlDialog() {
        final EditText input = new EditText(this);
        input.setText(currentApiUrl);
        input.setPadding(30, 20, 30, 20);
        new AlertDialog.Builder(this)
                .setTitle("🔗 URL da Planilha Google")
                .setMessage("Cole o link gerado pelo Google Apps Script:")
                .setView(input)
                .setPositiveButton("SALVAR", (d, w) -> {
                    String newUrl = input.getText().toString().trim();
                    if (!newUrl.isEmpty()) {
                        currentApiUrl = newUrl;
                        getSharedPreferences("TVBoxPrefs", Context.MODE_PRIVATE)
                                .edit().putString("custom_api_url", newUrl).apply();
                        Toast.makeText(this, "URL da API atualizada!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("CANCELAR", null)
                .show();
    }

    private void openAppPermissionSettings(String packageName, String appLabel) {
        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.fromParts("package", packageName, null));
            startActivity(intent);
            Toast.makeText(this, "📢 Clique em 'Permissões' e autorize 'Ficheiros e Conteúdo Multimédia'.", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "O aplicativo " + appLabel + " não está instalado.", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearOldActivationFiles() {
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
    // VERIFICADOR DE DATA DESREGULADA
    // -------------------------------------------------------------------------
    private void checkDateAndWarnIfNeeded() {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        if (currentYear < 2024) {
            new AlertDialog.Builder(this)
                    .setTitle("🕒 Data e Hora Incorretas")
                    .setMessage("⚠️ A data da sua TV Box está desregulada (" + currentYear + ").\n\nIsso impede a conexão segura com a internet.\nPor favor, vá em Configurações da TV Box e ative 'Data e Hora Automáticas'.")
                    .setPositiveButton("Abrir Configurações", (d, w) -> {
                        try {
                            startActivity(new Intent(Settings.ACTION_DATE_SETTINGS));
                        } catch (Exception ignored) {}
                    })
                    .setNegativeButton("Continuar Mesmo Assim", null)
                    .show();
        }
    }

    // -------------------------------------------------------------------------
    // 1. AÇÃO: VALIDAR CÓDIGO
    // -------------------------------------------------------------------------
    private void handleValidateCode() {
        String code = etCode.getText().toString().trim().toUpperCase();
        if (code.isEmpty()) {
            showStatus("⚠️ Por favor, digite o código de ativação.", "#ffb300");
            return;
        }

        showLoading(true);
        showStatus("🔍 Consultando servidor...", "#aaaaee");

        executor.execute(() -> {
            try {
                JSONObject payload = new JSONObject();
                payload.put("action", "validate");
                payload.put("activation_code", code);
                payload.put("device_id", deviceId);
                payload.put("hardware_fingerprint", hardwareFingerprint);
                payload.put("device_os", deviceOs);

                JSONObject response = sendHttpPostRequest(currentApiUrl, payload);
                boolean success = response.optBoolean("success", false);
                String msg = response.optString("message", "Sem resposta");

                mainHandler.post(() -> {
                    showLoading(false);
                    if (success) {
                        int maxUses = response.optInt("max_uses", 1);
                        int used = response.optInt("used_count", 0);
                        showStatus("✅ " + msg + " (" + used + "/" + maxUses + " telas usadas)\nClique em '2. ATIVAR APARELHO'.", "#51cf66");
                        btnActivate.setEnabled(true);
                        btnActivate.requestFocus();
                    } else {
                        showStatus("❌ " + msg, "#ff6b6b");
                        btnActivate.setEnabled(false);
                    }
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    showLoading(false);
                    showStatus("❌ Erro de conexão com a API: " + e.getMessage(), "#ff
