import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import java.io.InputStream
import java.io.InputStreamReader


class DriveQuickstart {
    companion object {

        private val JSON_FACTORY: JsonFactory = GsonFactory.getDefaultInstance()
        private val TOKENS_DIRECTORY_PATH = "tokens"

        val SCOPES = listOf(DriveScopes.DRIVE_METADATA_READONLY)
        val CREDENTIALS_FILE_PATH = "/credentials.json"
        fun getCredentials(httpTransport: NetHttpTransport): HttpRequestInitializer? {
            val `in`: InputStream = DriveQuickstart::class.java.getResourceAsStream(CREDENTIALS_FILE_PATH)
                ?: throw java.io.FileNotFoundException("Resource not found: $CREDENTIALS_FILE_PATH")

            val clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, InputStreamReader(`in`))

            val flow = GoogleAuthorizationCodeFlow.Builder(
                httpTransport, JSON_FACTORY, clientSecrets, SCOPES
            )
                .setDataStoreFactory(FileDataStoreFactory(java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build()
            val receiver = LocalServerReceiver.Builder().setPort(8888).build()
            //returns an authorized Credential object.
            //returns an authorized Credential object.
            return AuthorizationCodeInstalledApp(flow, receiver).authorize("user")
        }
    }
}

fun main() {

    val APPLICATION_NAME = "Google Drive API Kotlin Quickstart"
    val JSON_FACTORY: JsonFactory = GsonFactory.getDefaultInstance()

    val HTTP_TRANSPORT: NetHttpTransport = GoogleNetHttpTransport.newTrustedTransport()

    val service: Drive = Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, DriveQuickstart.getCredentials(HTTP_TRANSPORT))
        .setApplicationName(APPLICATION_NAME)
        .build()

    val result = service.files().list()
        .setPageSize(20)
        .setFields("nextPageToken, files(id, name)")
        .execute()

    val files: List<File>? = result.files
    if (files == null || files.isEmpty()) {
        println("No files found.")
    } else {
        println("Files:")
        for (file in files) {
            System.out.printf("%s (%s)\n", file.getName(), file.getId())
        }
    }
}
