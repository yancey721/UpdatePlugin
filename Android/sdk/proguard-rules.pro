# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# ==========  AppUpdaterSDK ProGuard Rules ==========

# 保留SDK对外API不被混淆
-keep public class com.yancey.sdk.AppUpdaterSDK {
    public *;
}

# 保留配置类和建造者模式
-keep public class com.yancey.sdk.config.UpdateConfig {
    public *;
}
-keep public class com.yancey.sdk.config.UpdateConfig$Builder {
    public *;
}

# 保留数据模型类
-keep public class com.yancey.sdk.data.** {
    public *;
}

# 保留回调接口
-keep public interface com.yancey.sdk.callback.** {
    public *;
}

# 保留枚举
-keep public enum com.yancey.sdk.config.LogLevel {
    **[] $VALUES;
    public *;
}

# 保留日志类的公开方法
-keep public class com.yancey.sdk.util.Logger {
    public static *;
}

# Gson相关规则
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# 保留Gson序列化相关的类
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

# 保留内部类不被混淆（如果有匿名内部类）
-keepattributes InnerClasses
-keep class com.yancey.sdk.**$* { *; }