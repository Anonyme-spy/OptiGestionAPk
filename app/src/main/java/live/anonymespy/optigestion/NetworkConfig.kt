package live.anonymespy.optigestion

/**
 * Global network configuration for OptiGestion.
 */
object NetworkConfig {
    /**
     * The base URL for the Node.js backend.
     * 
     * - For local development with an Android Emulator: Use "http://10.0.2.2:4000"
     * - For a physical device on the same WiFi: Use your computer's local IP (e.g., "http://192.168.1.50:4000")
     * - For production: Use your public domain (e.g., "https://api.optigestion.com")
     */
    // for easier use in LAN when launching the DB copy the LAN IP show in console log
    const val BASE_URL = "http://192.168.178.25:4000"

    /** API Versioning prefix */
    const val API_PATH = "/api/v1"

    /** Full endpoint helper */
    fun endpoint(path: String) = "$BASE_URL$API_PATH$path"
}
