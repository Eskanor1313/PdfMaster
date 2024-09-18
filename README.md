

---

# PdfMaster

**PdfMaster** es una biblioteca de Android para la generación y gestión de archivos PDF. Permite crear PDFs con texto, imágenes, gráficos y tablas, además de ofrecer una arquitectura extensible a través de plugins.

## Descripción

PdfMaster facilita la creación de documentos PDF en aplicaciones Android, permitiendo personalizar la apariencia y el contenido de los archivos PDF generados. La biblioteca es flexible, permitiendo agregar diferentes tipos de contenido y extensiones mediante plugins.

##Instalación
Para utilizar PdfMaster en tu proyecto, primero debes añadir el archivo JAR a tu proyecto y configurar tu archivo build.gradle.kts (o build.gradle si usas Groovy) para incluirlo como una dependencia.

1. Agregar el JAR al Proyecto
Coloca el archivo JAR en el directorio adecuado:

Copia el archivo JAR (por ejemplo, PdfMaster-1.0.0.jar) en un directorio de tu proyecto, como libs.
Actualizar el archivo build.gradle.kts:

Asegúrate de que tu proyecto reconozca el JAR como una dependencia. Añade lo siguiente a tu archivo build.gradle.kts:

kotlin
Copiar código
repositories {
    flatDir {
        dirs("libs") // Reemplaza con la ruta a tu directorio de JAR
    }
}

dependencies {
    implementation("com.github.Eskanor1313:PdfMaster:1.0.0") // Reemplaza con el grupo y versión correctos
}

## Uso

Aquí tienes un ejemplo básico de cómo usar PdfMaster para generar un PDF:

```kotlin
import java.io.FileOutputStream
import com.aplication.PdfMaster.PdfCreator         --reemplazar con la ruta que de directorio tu jar si decides usar el mismo

fun main() {
    val pdfCreator = PdfCreator()
    val outputStream = FileOutputStream("output.pdf")

    pdfCreator.createPdf(
        outputStream = outputStream,
        text = "Hello, PDF World!",
        imagePath = "path/to/image.jpg",
        chartData = listOf(Pair("January", 100f), Pair("February", 150f)),
        tableHeaders = listOf("Header1", "Header2"),
        tableRows = listOf(
            listOf("Row1Col1", "Row1Col2"),
            listOf("Row2Col1", "Row2Col2")
        )
    )
}
```

### PdfCreator

La clase `PdfCreator` permite generar un documento PDF con múltiples tipos de contenido. Aquí está el código de `PdfCreator`:

```kotlin
import android.graphics.pdf.PdfDocument
import java.io.OutputStream

class PdfCreator {
    fun createPdf(
        outputStream: OutputStream,
        pageSize: PageSize = PageSize.A4,
        margins: PageMargins = defaultMargins,
        fontName: String = "Roboto-Regular", // Nombre de la fuente sin extensión
        text: String = "Hello, PDF World!",
        textPosition: Position? = null,
        imagePath: String? = null,
        chartData: List<Pair<String, Float>>? = null, // Datos para gráficos
        tableHeaders: List<String>? = null, // Encabezados para tablas
        tableRows: List<List<String>>? = null // Filas para tablas
    ) {
        try {
            outputStream.use {
                val pageWidth = pageSize.width
                val pageHeight = pageSize.height

                // Crear el documento PDF
                val pdfDocument = PdfDocument()

                // Crear una página
                val pageInfo = PdfDocument.PageInfo.Builder(pageWidth.toInt(), pageHeight.toInt(), 1).create()
                val page = pdfDocument.startPage(pageInfo)
                val canvas = page.canvas

                // Agregamos el texto con la fuente seleccionada
                if (textPosition != null) {
                    addTextAtPosition(canvas, text, textPosition, fontName, margins, pdfDocument)
                } else {
                    addTextAuto(canvas, text, margins, fontName, pageWidth, pageHeight, pdfDocument)
                }

                // Agregamos la imagen si se proporciona
                if (imagePath != null) {
                    if (textPosition != null) {
                        addImageAtPosition(canvas, imagePath, Position(textPosition.x, textPosition.y + 100))
                    } else {
                        addImageAuto(canvas, imagePath, margins)
                    }
                }

                // Agregamos gráficos si se proporcionan datos
                if (chartData != null) {
                    drawBarChart(canvas, chartData, Position(margins.left, margins.top + 200))
                }

                // Agregamos tablas si se proporcionan encabezados y filas
                if (tableHeaders != null && tableRows != null) {
                    drawTable(canvas, tableHeaders, tableRows, Position(margins.left, margins.top + 400))
                }

                // Finalizamos la página
                pdfDocument.finishPage(page)

                // Guardamos el PDF en el OutputStream
                pdfDocument.writeTo(outputStream)

                // Cerramos el documento
                pdfDocument.close()

                Log.d("PdfCreator", "PDF generado con éxito.")
            }
        } catch (e: Exception) {
            Log.e("PdfCreator", "Error al generar el PDF", e)
        }
    }
}
```

### Plugins

PdfMaster admite extensiones mediante plugins. Puedes registrar y ejecutar plugins personalizados para agregar funcionalidades adicionales.

#### Registro de Plugins

```kotlin
import com.aplication.PdfMaster.PluginRegistry
import com.aplication.PdfMaster.TextContentHandler

val textHandler = TextContentHandler("Hello, PDF World!", 100f, 200f)
PluginRegistry.registerPlugin(textHandler)
```

#### Ejecución de Plugins

```kotlin
val result = PluginRegistry.executePlugin(TextContentHandler::class.java, someData)
```

#### Ejemplo de `ContentHandler` y `Plugin`

```kotlin
// ContentHandler.kt
import android.graphics.pdf.PdfDocument
import java.io.OutputStream

interface ContentHandler {
    fun handleContent(pdfDocument: PdfDocument, outputStream: OutputStream, content: ByteArray)
}

// TextContentHandler.kt
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import java.io.OutputStream

class TextContentHandler(private val text: String, private val x: Float, private val y: Float) : ContentHandler {

    override fun handleContent(pdfDocument: PdfDocument, outputStream: OutputStream, convertedData: ByteArray) {
        // Configurar la página y el canvas
        val pageWidth = 595
        val pageHeight = 842

        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        // Configurar el Paint para el texto
        val paint = Paint().apply {
            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            color = android.graphics.Color.BLACK
        }

        // Agregar el texto al canvas
        canvas.drawText(text, x, y, paint)

        // Finalizar la página
        pdfDocument.finishPage(page)

        // Escribir el documento PDF al OutputStream
        pdfDocument.writeTo(outputStream)
    }
}

// Plugin.kt
interface Plugin {
    fun initialize()
    fun execute(data: Any): Any
}

// PluginRegistry.kt
import android.util.Log

object PluginRegistry {
    private val plugins = mutableListOf<Plugin>()

    fun registerPlugin(plugin: Plugin) {
        plugin.initialize()
        plugins.add(plugin)
        Log.d("PluginRegistry", "Plugin registrado: ${plugin::class.simpleName}")
    }

    fun getPluginByType(type: Class<out Plugin>): Plugin? {
        return plugins.find { type.isInstance(it) }
    }

    fun executePlugin(type: Class<out Plugin>, data: Any): Any? {
        val plugin = getPluginByType(type)
        return plugin?.execute(data)
    }
}
```

--
