package org.w1001.schedule

// Example implementation concept
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.prefs.Preferences

class CredentialManager {
    private val prefs = Preferences.userNodeForPackage(this::class.java)
    private val connectionStringKey = "mongodb.connection.string"

    // Add a StateFlow to track credential status
    private val _hasCredentialsFlow = MutableStateFlow(prefs.get(connectionStringKey, null) != null)
    val hasCredentialsFlow: StateFlow<Boolean> = _hasCredentialsFlow.asStateFlow()

    fun hasStoredCredentials(): Boolean {
        return prefs.get(connectionStringKey, null) != null
    }

    fun storeConnectionString(connectionString: String) {
        prefs.put(connectionStringKey, connectionString)
        _hasCredentialsFlow.value = true
    }

    fun getConnectionString(): String? {
        return prefs.get(connectionStringKey, null)
    }

    fun deleteConnectionString() {
        prefs.remove(connectionStringKey)
        _hasCredentialsFlow.value = false
    }

    companion object {
        private val instance = CredentialManager()

        fun hasStoredCredentials(): Boolean = instance.hasStoredCredentials()

        val hasCredentialsFlow: StateFlow<Boolean> get() = instance.hasCredentialsFlow

        fun storeConnectionString(connectionString: String) =
            instance.storeConnectionString(connectionString)

        fun getConnectionString(): String? = instance.getConnectionString()

        fun deleteConnectionString() = instance.deleteConnectionString()
    }
}
