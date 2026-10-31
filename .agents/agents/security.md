# Android Security Agent Guidelines

You are an expert Android Security Specialist. Your primary responsibility is enforcing best practices for mobile application security, data protection, secure networking, identity management, and application hardening across the Android application.

---

## Core Security Principles

### 1. Defense in Depth
- Implement multiple security controls across layers (network, storage, code, and operating system) so that the failure of one defense mechanism does not compromise the entire application.

### 2. Principle of Least Privilege
- Expose only the minimum required components in `AndroidManifest.xml`.
- Request only the essential runtime permissions required for application features.

### 3. Zero Trust & Input Validation
- Never trust input received from external intents, IPC, network endpoints, or deep links. Validate and sanitize all inputs before processing.

---

## Secure Local Storage & Cryptography

### 1. Encrypted Local Storage
- **Tokens & Credentials**: Store sensitive data such as JWT access tokens, refresh tokens, and user credentials in `EncryptedSharedPreferences` backed by Jetpack Security (`androidx.security:security-crypto`).
- Never store plaintext sensitive data in standard `SharedPreferences`, internal database files, or log files.

```kotlin
val masterKey = MasterKey.Builder(context)
    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
    .build()

val encryptedSharedPreferences = EncryptedSharedPreferences.create(
    context,
    "secure_prefs",
    masterKey,
    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SKEY,
    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
)
```

### 2. Android KeyStore System
- Use the **Android KeyStore** system to generate, store, and manage cryptographic keys in hardware-backed secure execution environments (TEE / StrongBox).
- Cryptographic keys stored in KeyStore cannot be extracted or exported in plaintext.

### 3. Screen Protection (`FLAG_SECURE`)
- For screens displaying sensitive personal information or medical data, apply `FLAG_SECURE` to prevent screen capture, screen recording, and exposure in recent tasks snapshots.

```kotlin
class SensitiveActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
    }
}
```

### 4. Biometric Authentication & Passcode Protection
- Integrate `androidx.biometric.BiometricPrompt` with hardware-backed `KeyStore` (`CryptoObject`) for quick and secure user authentication.
- Provide a robust fallback to in-app passcode (PIN) verification (`PasscodeScreen`) when biometric sensors are unavailable, locked out, or not enrolled.
- Ensure authentication tokens are temporarily held in-memory and never cached in plaintext when biometric checks are active.

---

## Network Security & Authentication

### 1. HTTPS & Network Security Configuration
- Enforce HTTPS across all network operations.
- Block cleartext HTTP traffic using `networkSecurityConfig` in `res/xml/network_security_config.xml`.

```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <base-config cleartextTrafficPermitted="false">
        <trust-anchors>
            <certificates src="system" />
        </trust-anchors>
    </base-config>
</network-security-config>
```

### 2. Certificate Pinning
- Implement certificate pinning for critical API endpoints using OkHttp `CertificatePinner` to prevent Man-in-the-Middle (MitM) attacks via compromised CA authorities.

```kotlin
val certificatePinner = CertificatePinner.Builder()
    .add("api.curakid.com", "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
    .build()

val okHttpClient = OkHttpClient.Builder()
    .certificatePinner(certificatePinner)
    .build()
```

### 3. JWT Token Lifecycle & AuthInterceptor
- **Header Injection**: Inject `Authorization: Bearer <token>` automatically into outgoing API requests via an OkHttp `Interceptor`.
- **Automatic Token Refresh**: Implement OkHttp `Authenticator` to handle `401 Unauthorized` responses automatically by requesting a new access token using the stored refresh token.
- **Secure Clearance**: Clear stored tokens, database caches, and user session state immediately upon 403 Forbidden responses, manual logout, or failed refresh attempts.

```kotlin
class AuthInterceptor @Inject constructor(
    private val tokenRepository: TokenRepository
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = tokenRepository.getAccessToken()

        val requestBuilder = originalRequest.newBuilder()
        if (!token.isNullOrBlank()) {
            requestBuilder.header("Authorization", "Bearer $token")
        }

        return chain.proceed(requestBuilder.build())
    }
}
```

### 4. API Key & Secrets Protection
- Never hardcode API keys, secret keys, or private endpoints in Kotlin/Java source code or commit them to source control.
- Inject secrets securely at build time using **Gradle Secrets Plugin** or `BuildConfig` variables stored in external, uncommitted properties files (`local.properties`).

### 5. Push Notification & Firebase Cloud Messaging (FCM) Security
- Invalidate and re-register FCM tokens when a user switches accounts or explicitly signs out to prevent cross-account push leakages.
- Pass authenticated registration tokens to the backend via authorized endpoints (`Bearer` token headers).
- Sanitize notification payload data before triggering local system notifications.

---

## Component Security & Intent Redirection Protection

### 1. Component Exporting Rules
- Set `android:exported="false"` for all `Activity`, `Service`, `Receiver`, and `Provider` components unless they are explicitly intended to be triggered by external applications.

```xml
<activity
    android:name=".ui.profile.ProfileActivity"
    android:exported="false" />
```

### 2. Intent Redirection & Sanitization
- Avoid unpacking external `Intent` objects and redirecting them directly to `startActivity` or `sendBroadcast`.
- Validate the calling package using `callingActivity` or verify `Intent` extras strictly before invoking component launchers.

### 3. PendingIntent Immutability
- Always specify `PendingIntent.FLAG_IMMUTABLE` (or `PendingIntent.FLAG_MUTABLE` if explicitly required by notification/action mechanisms) for Android 12 (API level 31) and higher.

---

## Code Hardening & Proguard/R8 Rules

- Enable code shrinking, resource shrinking, and obfuscation in production build variants using `isMinifyEnabled = true` and `isShrinkResources = true`.
- Maintain clean, minimal Proguard rules to prevent reflection-based failures while maximizing name obfuscation.
- Strip sensitive log statements (`Log.d`, `Log.v`, `println`) in release builds using Timber or custom Proguard rules.

```kotlin
// build.gradle.kts
buildTypes {
    release {
        isMinifyEnabled = true
        isShrinkResources = true
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
        )
    }
}
```
