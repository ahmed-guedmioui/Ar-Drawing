-keep class com.wang.avi.** { *; }
-keep class com.wang.avi.indicators.** { *; }


-flattenpackagehierarchy
-ignorewarnings

-keepattributes *Annotation*
-optimizationpasses 3
-overloadaggressively
-repackageclasses ''
-allowaccessmodification
-optimizations !method/removal/parameter
####

-useuniqueclassmembernames
-allowaccessmodification
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose
-dontoptimize
-dontpreverify
-keep public class com.google.vending.licensing.ILicensingService
-keep public class com.android.vending.licensing.ILicensingService

# For native methods, see http://proguard.sourceforge.net/manual/examples.html#native
-keepclasseswithmembernames class * {
    native <methods>;
}

# keep setters in Views so that animations can still work.
# see http://proguard.sourceforge.net/manual/examples.html#beans
-keepclassmembers public class * extends android.view.View {
   void set*(***);
   *** get*();
}

# We want to keep methods in Activity that could be used in the XML attribute onClick
-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

# For enumeration classes, see http://proguard.sourceforge.net/manual/examples.html#enumerations
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

#-keep class * implements android.os.Parcelable {
#  public static final android.os.Parcelable$Creator *;
#}

-keepclassmembers class **.R$* {
    public static <fields>;
}

# The support library contains references to newer platform versions.
# Don't warn about those in case this app is linking against an older
# platform version.  We know about them, and they are safe.
-dontwarn android.support.**



-keep class ** implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

-dontwarn rx.internal.util.unsafe.**
#-keepnames class rx.*

# Keep GSON stuff
-keep class sun.misc.Unsafe { *; }
-keep class sun.misc.Unsafe.** { *; }
-keep class com.google.gson.** { *; }

# Keep these for GSON and Jackson
-keepattributes Signature
-keepattributes EnclosingMethod


-keep class android.support.multidex.MultiDexApplication {
    <init>();
    void attachBaseContext(android.content.Context);
}

-keep public class * extends android.app.backup.BackupAgent {
    <init>();
}


-dontwarn androidx.annotation.**

#lottie
-dontwarn com.airbnb.lottie.**
-keep class com.airbnb.lottie.** {*;}



# Gson specific classes
-dontwarn sun.misc.**
#-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
-keep class com.google.gson.examples.android.model.** { <fields>; }

# Prevent proguard from stripping interface information from TypeAdapter, TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Prevent R8 from leaving Data object members always null
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

# Retain generic signatures of TypeToken and its subclasses with R8 version 3.0 and higher.
-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken