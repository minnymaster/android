package org.hse.finance;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import java.util.HashMap;
import java.util.Map;

public class QrScannerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startQrScan();
    }

    private void startQrScan() {
        ScanOptions options = new ScanOptions();
        options.setCameraId(0);
        options.setTimeout(12000);
        options.setBarcodeImageEnabled(false); // Не сохранять изображение
        options.setPrompt("Наведите камеру на QR-код чека");
        options.setBeepEnabled(false);
        options.setOrientationLocked(true);

        barcodeLauncher.launch(options);
    }

    private final androidx.activity.result.ActivityResultLauncher<ScanOptions> barcodeLauncher =
            registerForActivityResult(new ScanContract(), result -> {
                if (result.getContents() != null) {
                    String qrText = result.getContents();
                    Map<String, String> qrData = parseQrData(qrText);

                    // Передаем данные в MainActivity
                    Intent intent = new Intent();
                    intent.putExtra("qr_amount", qrData.get("s"));
                    intent.putExtra("qr_date", qrData.get("t"));
                    intent.putExtra("qr_fn", qrData.get("fn"));
                    setResult(RESULT_OK, intent);
                }
                finish();
            });

    // Парсим строку QR-кода в Map (ключ-значение)
    private Map<String, String> parseQrData(String qrText) {
        Map<String, String> data = new HashMap<>();
        String[] parts = qrText.split("&");
        for (String part : parts) {
            String[] keyValue = part.split("=");
            if (keyValue.length == 2) {
                data.put(keyValue[0], keyValue[1]);
            }
        }
        return data;
    }
}