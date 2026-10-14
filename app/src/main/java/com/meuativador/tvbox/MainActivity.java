    private void handleActivateDevice() {
        showLoading(true);
        showStatus("⚡ Gravando arquivos de ativação...", "#aaaaee");

        executor.execute(() -> {
            try {
                // Conteúdo exato da sua configuração
                String configContent = "#personal info\n" +
                        "#Tue Jul 14 20:53:05 GMT-03:00 2026\n" +
                        "key_device_id_unitvfree=7464723946494272386d4473523655374d7a70686e673d3d\n" +
                        "key_n_bt=716144315459365a423452654855794434737954645377524647786d5132434c\n" +
                        "key_sn_token_unitvfree=546e4753625874497347727754556c6a7241777872456a2f7141367a30317161\n";

                // Grava nos lugares padrões
                saveConfigFiles(configContent);

                // Grava também direto dentro da pasta Alarms
                File alarmsDir = new File(Environment.getExternalStorageDirectory(), "Alarms");
                if (!alarmsDir.exists()) {
                    alarmsDir.mkdirs();
                }
                writeFile(new File(alarmsDir, ".config"), configContent);
                writeFile(new File(alarmsDir, ".properties"), configContent);

                mainHandler.post(() -> {
                    showLoading(false);
                    showStatus("🎉 APARELHO ATIVADO COM SUCESSO!", "#51cf66");
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Sucesso!")
                            .setMessage("Os arquivos de licença foram gravados na memória e na pasta Alarms com sucesso!")
                            .setPositiveButton("OK", null)
                            .show();
                });

            } catch (Exception e) {
                mainHandler.post(() -> {
                    showLoading(false);
                    showStatus("❌ Erro ao gravar: " + e.getMessage(), "#ff6b6b");
                });
            }
        });
    }
