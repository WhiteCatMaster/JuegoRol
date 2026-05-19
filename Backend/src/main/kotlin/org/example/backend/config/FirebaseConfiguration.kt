package org.example.backend.config

import com.google.auth.oauth2.GoogleCredentials
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import java.io.File
import java.io.InputStream


@Configuration
class FirebaseConfiguration(
    @Value("\${firebase.credentials.path:/app/firebase-adminsdk.json}")
    private val credentialsPath: String
) {
    @PostConstruct
    fun initFirebase(){
        try {
            val external = File(credentialsPath)
            val stream: InputStream = if (external.isFile) {
                println("✅ Firebase credentials loaded from $credentialsPath")
                external.inputStream()
            } else {
                val classpath = ClassPathResource("firebase-adminsdk.json")
                if (!classpath.exists()) {
                    println("⚠️ Firebase credentials not found (checked $credentialsPath and classpath). Firebase will not be initialized.")
                    return
                }
                println("✅ Firebase credentials loaded from classpath")
                classpath.inputStream
            }

            val options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(stream))
                .build()

            // Evitamos inicializarlo dos veces si el servidor se reinicia
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options)
            }
            println("✅ Firebase initialized successfully")

        } catch (e: Exception) {
            e.printStackTrace()
            println("❌ Error initializing Firebase: ${e.message}")
        }
    }

}
