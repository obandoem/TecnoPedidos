package com.tecnopedidos.config;

import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class FirebaseConfig {

    private static final String SERVICE_ACCOUNT_RESOURCE =
            "Firebase/tecnopedidos-735e7-fi.json";

    @Bean(destroyMethod = "delete")
    public FirebaseApp firebaseApp(
            @Value("${firebase.storage.bucket:}") String storageBucket) throws IOException {

        if (!FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.getInstance();
        }

        ClassPathResource credentialResource = new ClassPathResource(SERVICE_ACCOUNT_RESOURCE);
        try (InputStream input = credentialResource.getInputStream()) {
            ServiceAccountCredentials credentials = ServiceAccountCredentials.fromStream(input);
            FirebaseOptions.Builder options = FirebaseOptions.builder()
                    .setCredentials(credentials);

            if (credentials.getProjectId() != null && !credentials.getProjectId().isBlank()) {
                options.setProjectId(credentials.getProjectId());
            }
            if (storageBucket != null && !storageBucket.isBlank()) {
                options.setStorageBucket(storageBucket.trim());
            }

            return FirebaseApp.initializeApp(options.build());
        }
    }
}
