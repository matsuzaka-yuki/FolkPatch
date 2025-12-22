-dontwarn org.bouncycastle.jsse.BCSSLParameters
-dontwarn org.bouncycastle.jsse.BCSSLSocket
-dontwarn org.bouncycastle.jsse.provider.BouncyCastleJsseProvider
-dontwarn org.conscrypt.Conscrypt$Version
-dontwarn org.conscrypt.Conscrypt
-dontwarn org.conscrypt.ConscryptHostnameVerifier
-dontwarn org.openjsse.javax.net.ssl.SSLParameters
-dontwarn org.openjsse.javax.net.ssl.SSLSocket
-dontwarn org.openjsse.net.ssl.OpenJSSE
-dontwarn java.beans.Introspector
-dontwarn java.beans.VetoableChangeListener
-dontwarn java.beans.VetoableChangeSupport

# Keep ini4j Service Provider Interface
-keep,allowobfuscation,allowoptimization public class org.ini4j.spi.*

# Keep native methods and JNI classes
-keep class me.yuki.folk.Natives {
    *;
}

-keepclasseswithmembernames class * {
    native <methods>;
}

-keep class me.yuki.folk.Natives$Profile { *; }
-keep class me.yuki.folk.Natives$KPMCtlRes { *; }

# Keep RootServices
-keep class me.yuki.folk.services.RootServices { *; }

# Keep AIDL interfaces
-keep class me.yuki.folk.IAPRootService { *; }
-keep class me.yuki.folk.IAPRootService$Stub { *; }
-keep class rikka.parcelablelist.ParcelableListSlice { *; }

# Kotlin
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    public static void check*(...);
    public static void throw*(...);
}

-repackageclasses
-allowaccessmodification
-overloadaggressively
-renamesourcefileattribute SourceFile
