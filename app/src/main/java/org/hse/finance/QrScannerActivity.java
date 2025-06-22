package org.hse.finance;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

// Просто пустой класс для портретной ориентации

public class QrScannerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startQrScan();
    }

    private void startQrScan() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Наведите камеру на QR-код");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        barcodeLauncher.launch(options);
    }

    private final androidx.activity.result.ActivityResultLauncher<ScanOptions> barcodeLauncher =
            registerForActivityResult(new ScanContract(), result -> {
                if (result.getContents() != null) {
                    // Тут ты получаешь текст из QR-кода
                    String qrText = result.getContents();
                    // Например, передать результат в MainActivity
                    Intent intent = new Intent();
                    intent.putExtra("qr_result", qrText);
                    setResult(RESULT_OK, intent);
                }
                finish(); // Закрываем сканер
            });
}