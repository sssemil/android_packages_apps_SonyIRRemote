/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_sssemil_ir_IRCommon */

#ifndef _Included_com_sssemil_ir_IRCommon
#define _Included_com_sssemil_ir_IRCommon
#ifdef __cplusplus
extern "C" {
#endif
#undef com_sssemil_ir_IRCommon_MODE_PRIVATE
#define com_sssemil_ir_IRCommon_MODE_PRIVATE 0L
#undef com_sssemil_ir_IRCommon_MODE_WORLD_READABLE
#define com_sssemil_ir_IRCommon_MODE_WORLD_READABLE 1L
#undef com_sssemil_ir_IRCommon_MODE_WORLD_WRITEABLE
#define com_sssemil_ir_IRCommon_MODE_WORLD_WRITEABLE 2L
#undef com_sssemil_ir_IRCommon_MODE_APPEND
#define com_sssemil_ir_IRCommon_MODE_APPEND 32768L
#undef com_sssemil_ir_IRCommon_MODE_MULTI_PROCESS
#define com_sssemil_ir_IRCommon_MODE_MULTI_PROCESS 4L
#undef com_sssemil_ir_IRCommon_MODE_ENABLE_WRITE_AHEAD_LOGGING
#define com_sssemil_ir_IRCommon_MODE_ENABLE_WRITE_AHEAD_LOGGING 8L
#undef com_sssemil_ir_IRCommon_BIND_AUTO_CREATE
#define com_sssemil_ir_IRCommon_BIND_AUTO_CREATE 1L
#undef com_sssemil_ir_IRCommon_BIND_DEBUG_UNBIND
#define com_sssemil_ir_IRCommon_BIND_DEBUG_UNBIND 2L
#undef com_sssemil_ir_IRCommon_BIND_NOT_FOREGROUND
#define com_sssemil_ir_IRCommon_BIND_NOT_FOREGROUND 4L
#undef com_sssemil_ir_IRCommon_BIND_ABOVE_CLIENT
#define com_sssemil_ir_IRCommon_BIND_ABOVE_CLIENT 8L
#undef com_sssemil_ir_IRCommon_BIND_ALLOW_OOM_MANAGEMENT
#define com_sssemil_ir_IRCommon_BIND_ALLOW_OOM_MANAGEMENT 16L
#undef com_sssemil_ir_IRCommon_BIND_WAIVE_PRIORITY
#define com_sssemil_ir_IRCommon_BIND_WAIVE_PRIORITY 32L
#undef com_sssemil_ir_IRCommon_BIND_IMPORTANT
#define com_sssemil_ir_IRCommon_BIND_IMPORTANT 64L
#undef com_sssemil_ir_IRCommon_BIND_ADJUST_WITH_ACTIVITY
#define com_sssemil_ir_IRCommon_BIND_ADJUST_WITH_ACTIVITY 128L
#undef com_sssemil_ir_IRCommon_CONTEXT_INCLUDE_CODE
#define com_sssemil_ir_IRCommon_CONTEXT_INCLUDE_CODE 1L
#undef com_sssemil_ir_IRCommon_CONTEXT_IGNORE_SECURITY
#define com_sssemil_ir_IRCommon_CONTEXT_IGNORE_SECURITY 2L
#undef com_sssemil_ir_IRCommon_CONTEXT_RESTRICTED
#define com_sssemil_ir_IRCommon_CONTEXT_RESTRICTED 4L
#undef com_sssemil_ir_IRCommon_RESULT_CANCELED
#define com_sssemil_ir_IRCommon_RESULT_CANCELED 0L
#undef com_sssemil_ir_IRCommon_RESULT_OK
#define com_sssemil_ir_IRCommon_RESULT_OK -1L
#undef com_sssemil_ir_IRCommon_RESULT_FIRST_USER
#define com_sssemil_ir_IRCommon_RESULT_FIRST_USER 1L
#undef com_sssemil_ir_IRCommon_DEFAULT_KEYS_DISABLE
#define com_sssemil_ir_IRCommon_DEFAULT_KEYS_DISABLE 0L
#undef com_sssemil_ir_IRCommon_DEFAULT_KEYS_DIALER
#define com_sssemil_ir_IRCommon_DEFAULT_KEYS_DIALER 1L
#undef com_sssemil_ir_IRCommon_DEFAULT_KEYS_SHORTCUT
#define com_sssemil_ir_IRCommon_DEFAULT_KEYS_SHORTCUT 2L
#undef com_sssemil_ir_IRCommon_DEFAULT_KEYS_SEARCH_LOCAL
#define com_sssemil_ir_IRCommon_DEFAULT_KEYS_SEARCH_LOCAL 3L
#undef com_sssemil_ir_IRCommon_DEFAULT_KEYS_SEARCH_GLOBAL
#define com_sssemil_ir_IRCommon_DEFAULT_KEYS_SEARCH_GLOBAL 4L
/*
 * Class:     com_sssemil_ir_IRCommon
 * Method:    startIR
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_com_sssemil_ir_IRCommon_startIR
  (JNIEnv *, jobject, jstring);

/*
 * Class:     com_sssemil_ir_IRCommon
 * Method:    stopIR
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_com_sssemil_ir_IRCommon_stopIR
  (JNIEnv *, jobject, jstring);

/*
 * Class:     com_sssemil_ir_IRCommon
 * Method:    learnKey
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_com_sssemil_ir_IRCommon_learnKey
  (JNIEnv *, jobject, jstring);

/*
 * Class:     com_sssemil_ir_IRCommon
 * Method:    sendKey
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_com_sssemil_ir_IRCommon_sendKey
  (JNIEnv *, jobject, jstring);

/*
 * Class:     com_sssemil_ir_IRCommon
 * Method:    sendRawKey
 * Signature: (Ljava/lang/String, Ljava/lang/int;)I
 */
JNIEXPORT jint JNICALL Java_com_sssemil_ir_IRCommon_sendRawKey
  (JNIEnv *, jobject, jstring, jint);

#ifdef __cplusplus
}
#endif
#endif
