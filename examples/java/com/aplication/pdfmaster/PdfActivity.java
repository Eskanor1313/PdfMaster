package com.aplication.pdfmaster;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.application.pdfmaster.PdfCreator;
import com.application.pdfmaster.DocumentIntegration;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

public class PdfActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 1;
    private StringBuilder documentContent;
    private DocumentIntegration.FontStyle fontStyle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        // Solicitar permisos en tiempo de ejecución
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE);
        }

        Button loadExcelButton = findViewById(R.id.load_doc_button);
        Button generatePdfButton = findViewById(R.id.generate_pdf_button);

        loadExcelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadExcel();
            }
        });

        generatePdfButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (documentContent != null && documentContent.length() > 0) {
                    generatePdf();
                } else {
                    Toast.makeText(PdfActivity.this, "No document loaded", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadExcel() {
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File[] files = downloadsDir.listFiles((dir, name) -> name.endsWith(".xlsx"));

        if (files != null && files.length > 0) {
            File excelFile = files[0]; // Tomamos el primer archivo .xlsx encontrado

            documentContent = new StringBuilder();
            try (InputStream inputStream = new FileInputStream(excelFile)) {
                Workbook workbook = WorkbookFactory.create(inputStream);
                Sheet sheet = workbook.getSheetAt(0);

                for (Row row : sheet) {
                    for (Cell cell : row) {
                        switch (cell.getCellType()) {
                            case STRING:
                                documentContent.append(cell.getStringCellValue()).append(" ");
                                break;
                            case NUMERIC:
                                documentContent.append(cell.getNumericCellValue()).append(" ");
                                break;
                            case BOOLEAN:
                                documentContent.append(cell.getBooleanCellValue()).append(" ");
                                break;
                            default:
                                documentContent.append(" ");
                                break;
                        }
                    }
                    documentContent.append("\n");
                }

                workbook.close();
                Log.d("PdfActivity", "Excel cargado exitosamente. Contenido: " + documentContent.toString());
            } catch (IOException e) {
                Log.e("PdfActivity", "Error al leer el archivo Excel", e);
            }
        } else {
            Log.e("PdfActivity", "No se encontraron archivos .xlsx en la carpeta de Descargas.");
        }
    }

    private void generatePdf() {
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File pdfFile = new File(downloadsDir, "converted_document.pdf");

        // Crear el PdfCreator con el contexto de la actividad
        PdfCreator pdfCreator = new PdfCreator(this);

        // Verifica si fontStyle es null y establece un valor predeterminado si es necesario
        if (fontStyle == null) {
            fontStyle = DocumentIntegration.FontStyle.REGULAR; // Valor predeterminado
        }

        try (OutputStream outputStream = new FileOutputStream(pdfFile)) {
            // Crear PDF usando PdfCreator
            pdfCreator.createPdf(
                    outputStream,
                    PdfCreator.PageSize.A4,
                    new PdfCreator.PageMargins(50f, 50f, 50f, 50f),
                    getFontNameForStyle(fontStyle),  // Fuente
                    documentContent.toString(),  // Texto del documento
                    new PdfCreator.Position(50f, 150f),  // Posición del texto
                    null,  // Ruta de la imagen
                    null,  // Datos del gráfico
                    null,  // Encabezados de la tabla
                    null  // Filas de la tabla
            );
            Toast.makeText(this, "PDF generado en Downloads", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("PdfActivity", "Error al generar el PDF", e);
        }
    }

    private String getFontNameForStyle(DocumentIntegration.FontStyle fontStyle) {
        switch (fontStyle) {
            case BOLD:
                return "Roboto-Bold";
            case ITALIC:
                return "Roboto-Italic";
            default:
                return "Roboto-Regular";
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido, puedes acceder al archivo
            } else {
                // Permiso denegado
                Log.e("PdfActivity", "Permiso de lectura del almacenamiento denegado");
            }
        }
    }
}
