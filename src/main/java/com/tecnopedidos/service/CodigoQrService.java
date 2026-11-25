package com.tecnopedidos.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import org.springframework.stereotype.Service;

// Genera el codigo QR que abre la consulta publica del pedido (HU-13)
// Usa la libreria ZXing de Google (tecnologia investigada)
@Service
public class CodigoQrService {

    // Devuelve la imagen PNG del QR en Base64 para mostrarla directo en el HTML
    public String generarQrBase64(String texto, int tamano) {
        try {
            BitMatrix matriz = new QRCodeWriter().encode(texto, BarcodeFormat.QR_CODE, tamano, tamano);

            ByteArrayOutputStream salida = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matriz, "PNG", salida);

            return Base64.getEncoder().encodeToString(salida.toByteArray());

        } catch (WriterException | IOException e) {
            throw new IllegalStateException("No se pudo generar el codigo QR", e);
        }
    }
}