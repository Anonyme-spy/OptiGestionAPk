package live.anonymespy.optigestion

/**
 * Global network configuration for OptiGestion.
 * Configuration réseau globale pour OptiGestion.
 */
object NetworkConfig {
    /**
     * The base URL for the Node.js backend.
     * 
     * - For local development with an Android Emulator: Use "http://10.0.2.2:4000"
     * - For a physical device on the same WiFi: Use your computer's local IP (e.g., "http://192.168.1.50:4000")
     * - For production: Use your public domain (e.g., "https://api.optigestion.com")
     *
     * L'URL de base pour le backend Node.js.
     * 
     * - Pour le développement local avec un émulateur Android : Utilisez "http://10.0.2.2:4000"
     * - Pour un appareil physique sur le même WiFi : Utilisez l'IP locale de votre ordinateur (ex: "http://192.168.1.50:4000")
     * - Pour la production : Utilisez votre domaine public (ex: "https://api.optigestion.com")
     */
    // for easier use in LAN when launching the DB copy the LAN IP show in console log
    // pour une utilisation plus facile en LAN lors du lancement de la DB, copiez l'IP LAN affichée dans le log de la console
    const val BASE_URL = "http://192.168.178.25:4000"

    /** API Versioning prefix
     * Préfixe de versionnage de l'API */
    const val API_PATH = "/api/v1"

    /** Full endpoint helper
     * Aide pour l'URL complète du point de terminaison */
    fun endpoint(path: String) = "$BASE_URL$API_PATH$path"
}
