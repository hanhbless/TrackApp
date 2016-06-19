# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in E:\AndroidStudio Release\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}


-verbose

-dontoptimize

-dontobfuscate

# Serializable
-keepclassmembers class * implements java.io.Serializable
{
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

-keepclassmembers class * {
  public <init>(android.content.Context);
}


#App
# keep unused fields in response classes so we can see them in debugger
-keepclassmembers class com.sunnet.service.task.** {
        public *;
}

#EventBus
-keepclassmembers class ** {
    public void onEvent*(***);
}

#Ormlite
-keep class com.sunnet.service.db.** { *; }

#Bouncy
-keep class org.bouncycastle.** { *; }

-keepattributes Signature,*Annotation*,EnclosingMethod

#Retrofit
-keep class com.squareup.okhttp.** { *; }
-keep class retrofit2.** { *; }
-keep interface com.squareup.okhttp.** { *; }

-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

#Donwarm
-dontwarn java.lang.**
-dontwarn com.amazonaws.**
-dontwarn com.fasterxml.jackson.databind.**
-dontwarn javax.xml.stream.events.**
-dontwarn org.codehaus.jackson.**
-dontwarn org.codehaus.mojo.**
-dontwarn org.apache.commons.logging.impl.**
-dontwarn org.apache.http.conn.scheme.**
-dontwarn org.apache.http.annotation.**

-dontwarn com.android.**
-dontwarn com.google.**
-dontwarn com.squareup.okhttp.**
-dontwarn okio.**
-dontwarn retrofit2.**
-dontwarn rx.**
-dontwarn javax.naming.**

-dontwarn org.apache.commons.**
-keep class org.apache.http.** { *; }
-dontwarn org.apache.http.**
