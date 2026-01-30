#pragma once

#include <jni.h>

#define XSTR(s) STR(s)
#define STR(s) #s

jstring nativeGetApiToken(JNIEnv *env, jobject thiz, jobject context);
