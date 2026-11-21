package com.tecnopedidos.service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Bucket;
import com.google.firebase.FirebaseApp;
import com.google.firebase.cloud.StorageClient;
import com.tecnopedidos.domain.Producto;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/** Acceso a las imagenes de productos almacenadas en Firebase Storage. */
@Service
public class FirebaseStorageService {

    private static final Logger log = LoggerFactory.getLogger(FirebaseStorageService.class);
    private static final long MAX_IMAGE_BYTES = 5L * 1024L * 1024L;
    private static final List<String> CATALOG_CODES = List.of(
            "TEC-001", "MOU-001", "MON-001", "SSD-001", "RAM-001", "FUE-001",
            "CAB-001", "ADP-001", "PAS-001", "AUD-001", "BAT-001");
    private static final Map<Long, String> CATEGORY_FALLBACKS = Map.of(
            1L, "TEC-001", 2L, "MON-001", 3L, "SSD-001",
            4L, "RAM-001", 5L, "CAB-001", 6L, "BAT-001");

    private final FirebaseApp firebaseApp;
    private final Environment environment;
    private final Map<String, String> urlCache = new ConcurrentHashMap<>();
    private volatile Bucket cachedBucket;

    public FirebaseStorageService(FirebaseApp firebaseApp, Environment environment) {
        this.firebaseApp = firebaseApp;
        this.environment = environment;
    }

    /**
     * Devuelve la URL de Firebase Storage para el producto. Los productos de demostracion
     * reciben una imagen de catalogo basada en su codigo; para productos nuevos se usa una
     * ilustracion de su categoria hasta que se cargue una imagen propia.
     */
    public String obtenerUrlImagen(Producto producto) {
        if (producto == null) {
            return null;
        }
        if (producto.getImagenPath() != null && !producto.getImagenPath().isBlank()) {
            return obtenerUrl(producto.getImagenPath());
        }

        String codigo = normalizarCodigo(producto.getCodigo());
        if (!CATALOG_CODES.contains(codigo)) {
            Long idCategoria = producto.getCategoria() == null
                    ? null : producto.getCategoria().getIdCategoria();
            codigo = idCategoria == null
                    ? "TEC-001" : CATEGORY_FALLBACKS.getOrDefault(idCategoria, "TEC-001");
        }
        return asegurarImagenCatalogo(codigo);
    }

    /** Sube o reemplaza la imagen propia de un producto y devuelve su ruta en el bucket. */
    public String subirImagenProducto(Long idProducto, MultipartFile archivo) throws IOException {
        if (idProducto == null || archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("Seleccione una imagen de producto");
        }
        if (archivo.getSize() > MAX_IMAGE_BYTES) {
            throw new IllegalArgumentException("La imagen no puede superar los 5 MB");
        }

        String contentType = archivo.getContentType();
        if (contentType == null || !List.of("image/jpeg", "image/png", "image/webp")
                .contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Use una imagen JPG, PNG o WebP");
        }

        String objectPath = "productos/" + idProducto + "/imagen";
        String token = UUID.randomUUID().toString();
        BlobInfo blobInfo = BlobInfo.newBuilder(getBucket().getName(), objectPath)
                .setContentType(contentType.toLowerCase())
                .setMetadata(Map.of("firebaseStorageDownloadTokens", token))
                .build();
        getBucket().getStorage().create(blobInfo, archivo.getBytes());

        urlCache.put(objectPath, construirUrl(getBucket().getName(), objectPath, token));
        return objectPath;
    }

    /** Elimina una imagen propia reemplazada o asociada a un producto desactivado. */
    public void eliminarImagen(String objectPath) {
        if (objectPath == null || objectPath.isBlank() || objectPath.startsWith("productos/catalogo/")) {
            return;
        }
        Blob blob = getBucket().get(objectPath);
        if (blob != null) {
            blob.delete();
        }
        urlCache.remove(objectPath);
    }

    private String asegurarImagenCatalogo(String codigo) {
        String objectPath = "productos/catalogo/" + codigo + ".svg";
        String cachedUrl = urlCache.get(objectPath);
        if (cachedUrl != null) {
            return cachedUrl;
        }

        try {
            Bucket bucket = getBucket();
            Blob existing = bucket.get(objectPath);
            String token = tokenDe(existing);
            if (existing == null || token == null) {
                ClassPathResource image = new ClassPathResource(
                        "Firebase/product-images/" + codigo + ".svg");
                if (!image.exists()) {
                    throw new IllegalStateException("No existe la imagen de catalogo " + codigo);
                }
                token = UUID.randomUUID().toString();
                BlobInfo blobInfo = BlobInfo.newBuilder(bucket.getName(), objectPath)
                        .setContentType("image/svg+xml")
                        .setMetadata(Map.of("firebaseStorageDownloadTokens", token))
                        .build();
                try (var input = image.getInputStream()) {
                    bucket.getStorage().create(blobInfo, input.readAllBytes());
                }
            }

            String url = construirUrl(bucket.getName(), objectPath, token);
            urlCache.put(objectPath, url);
            return url;
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo preparar la imagen " + codigo, e);
        }
    }

    private String obtenerUrl(String objectPath) {
        String cachedUrl = urlCache.get(objectPath);
        if (cachedUrl != null) {
            return cachedUrl;
        }

        Blob blob = getBucket().get(objectPath);
        String token = tokenDe(blob);
        if (blob == null || token == null) {
            throw new IllegalStateException("No se encontro una URL valida para la imagen del producto");
        }
        String url = construirUrl(getBucket().getName(), objectPath, token);
        urlCache.put(objectPath, url);
        return url;
    }

    private Bucket getBucket() {
        Bucket bucket = cachedBucket;
        if (bucket != null) {
            return bucket;
        }

        synchronized (this) {
            if (cachedBucket != null) {
                return cachedBucket;
            }

            StorageClient storage = StorageClient.getInstance(firebaseApp);
            String configuredBucket = environment.getProperty("firebase.storage.bucket", "").trim();
            if (!configuredBucket.isEmpty()) {
                cachedBucket = storage.bucket(configuredBucket);
                return cachedBucket;
            }

            String projectId = firebaseApp.getOptions().getProjectId();
            if (projectId == null || projectId.isBlank()) {
                projectId = environment.getProperty("GOOGLE_CLOUD_PROJECT");
            }
            if (projectId == null || projectId.isBlank()) {
                throw new IllegalStateException(
                        "Configure firebase.storage.bucket o GOOGLE_CLOUD_PROJECT para Firebase Storage");
            }

            for (String suffix : List.of(".firebasestorage.app", ".appspot.com")) {
                try {
                    Bucket candidate = storage.bucket(projectId + suffix);
                    if (candidate.exists()) {
                        cachedBucket = candidate;
                        return candidate;
                    }
                } catch (RuntimeException e) {
                    log.debug("No se pudo comprobar el bucket Firebase {}", projectId + suffix, e);
                }
            }
        }

        throw new IllegalStateException(
                "No se encontro el bucket de Firebase Storage; configure FIREBASE_STORAGE_BUCKET");
    }

    private static String tokenDe(Blob blob) {
        if (blob == null || blob.getMetadata() == null) {
            return null;
        }
        String tokens = blob.getMetadata().get("firebaseStorageDownloadTokens");
        return (tokens == null || tokens.isBlank()) ? null : tokens.split(",", 2)[0];
    }

    private static String construirUrl(String bucket, String objectPath, String token) {
        String encodedPath = URLEncoder.encode(objectPath, StandardCharsets.UTF_8)
                .replace("+", "%20");
        return "https://firebasestorage.googleapis.com/v0/b/" + bucket
                + "/o/" + encodedPath + "?alt=media&token=" + token;
    }

    private static String normalizarCodigo(String codigo) {
        if (codigo == null) {
            return "";
        }
        return codigo.trim().toUpperCase().replaceAll("[^A-Z0-9_-]", "");
    }
}
