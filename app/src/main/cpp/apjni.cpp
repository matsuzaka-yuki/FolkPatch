/* SPDX-License-Identifier: GPL-2.0-or-later */
/* 
 * Copyright (C) 2023 bmax121. All Rights Reserved.
 * Copyright (C) 2024 GarfieldHan. All Rights Reserved.
 * Copyright (C) 2024 1f2003d5. All Rights Reserved.
 */

#include <cstring>
#include <vector>
#include <cstdlib>

#include "apjni.hpp"
#include "supercall.h"
#include "security.hpp"

// Forward declaration to ensure visibility
extern jstring nativeGetApiToken(JNIEnv *env, jobject thiz, jobject context);

static jstring SafeNewStringUTF(JNIEnv *env, const char *str) {
    if (!str || !str[0]) return env->NewStringUTF("");

    const unsigned char *p = (const unsigned char *)str;
    bool valid = true;
    while (*p) {
        if (*p < 0x80) {
            p++;
        } else if ((*p & 0xE0) == 0xC0) {
            if ((p[1] & 0xC0) != 0x80) { valid = false; break; }
            p += 2;
        } else if ((*p & 0xF0) == 0xE0) {
            if ((p[1] & 0xC0) != 0x80 || (p[2] & 0xC0) != 0x80) { valid = false; break; }
            p += 3;
        } else if ((*p & 0xF8) == 0xF0) {
            if ((p[1] & 0xC0) != 0x80 || (p[2] & 0xC0) != 0x80 || (p[3] & 0xC0) != 0x80) { valid = false; break; }
            p += 4;
        } else {
            valid = false; break;
        }
    }

    if (valid) return env->NewStringUTF(str);

    std::string sanitized;
    p = (const unsigned char *)str;
    while (*p) {
        if (*p < 0x80) {
            sanitized += (char)*p;
            p++;
        } else if ((*p & 0xE0) == 0xC0) {
            if ((p[1] & 0xC0) == 0x80) {
                sanitized += (char)*p;
                sanitized += (char)p[1];
                p += 2;
            } else {
                sanitized += '?';
                p++;
            }
        } else if ((*p & 0xF0) == 0xE0) {
            if ((p[1] & 0xC0) == 0x80 && (p[2] & 0xC0) == 0x80) {
                sanitized += (char)*p;
                sanitized += (char)p[1];
                sanitized += (char)p[2];
                p += 3;
            } else {
                sanitized += '?';
                p++;
            }
        } else if ((*p & 0xF8) == 0xF0) {
            if ((p[1] & 0xC0) == 0x80 && (p[2] & 0xC0) == 0x80 && (p[3] & 0xC0) == 0x80) {
                sanitized += (char)*p;
                sanitized += (char)p[1];
                sanitized += (char)p[2];
                sanitized += (char)p[3];
                p += 4;
            } else {
                sanitized += '?';
                p++;
            }
        } else {
            sanitized += '?';
            p++;
        }
    }

    return env->NewStringUTF(sanitized.c_str());
}

jboolean nativeReady(JNIEnv *env, jobject /* this */, jstring super_key_jstr) {
    ensureSuperKeyNonNull(super_key_jstr);

    const auto super_key = JUTFString(env, super_key_jstr);
    return sc_ready(super_key.get());
}

jlong nativeKernelPatchVersion(JNIEnv *env, jobject /* this */, jstring super_key_jstr) {
    ensureSuperKeyNonNull(super_key_jstr);

    const auto super_key = JUTFString(env, super_key_jstr);

    return sc_kp_ver(super_key.get());
}

jstring nativeKernelPatchBuildTime(JNIEnv *env, jobject /* this */, jstring super_key_jstr) {
    ensureSuperKeyNonNull(super_key_jstr);

    const auto super_key = JUTFString(env, super_key_jstr);
    char buf[4096] = { '\0' };

    sc_get_build_time(super_key.get(), buf, sizeof(buf));
    return SafeNewStringUTF(env, buf);
}

jlong nativeSu(JNIEnv *env, jobject /* this */, jstring super_key_jstr, jint to_uid, jstring selinux_context_jstr) {
    ensureSuperKeyNonNull(super_key_jstr);

    const auto super_key = JUTFString(env, super_key_jstr);
    const char *selinux_context = nullptr;
    if (selinux_context_jstr) selinux_context = JUTFString(env, selinux_context_jstr);
    struct su_profile profile{};
    profile.uid = getuid();
    profile.to_uid = (uid_t)to_uid;
    if (selinux_context) strncpy(profile.scontext, selinux_context, sizeof(profile.scontext) - 1);
    long rc = sc_su(super_key.get(), &profile);
    if (rc < 0) [[unlikely]] {
        LOGE("nativeSu error: %ld", rc);
    }

    return rc;
}

jint nativeSetUidExclude(JNIEnv *env, jobject /* this */, jstring super_key_jstr, jint uid, jint exclude) {
    ensureSuperKeyNonNull(super_key_jstr);

    const auto super_key = JUTFString(env, super_key_jstr);
    return static_cast<int>(sc_set_ap_mod_exclude(super_key.get(), (uid_t) uid, exclude));
}

jint nativeGetUidExclude(JNIEnv *env, jobject /* this */, jstring super_key_jstr, uid_t uid) {
    ensureSuperKeyNonNull(super_key_jstr);

    const auto super_key = JUTFString(env, super_key_jstr);
    return static_cast<int>(sc_get_ap_mod_exclude(super_key.get(), uid));
}

jintArray nativeSuUids(JNIEnv *env, jobject /* this */, jstring super_key_jstr) {
    ensureSuperKeyNonNull(super_key_jstr);

    const auto super_key = JUTFString(env, super_key_jstr);
    int num = static_cast<int>(sc_su_uid_nums(super_key.get()));

    if (num <= 0) [[unlikely]] {
        LOGW("SuperUser Count less than 1, skip allocating vector...");
        return env->NewIntArray(0);
    }

    std::vector<int> uids(num);

    long n = sc_su_allow_uids(super_key.get(), (uid_t *) uids.data(), num);
    if (n > 0) [[unlikely]] {
        auto array = env->NewIntArray(n);
        env->SetIntArrayRegion(array, 0, n, uids.data());
        return array;
    }

    return env->NewIntArray(0);
}

jobject nativeSuProfile(JNIEnv *env, jobject /* this */, jstring super_key_jstr, jint uid) {
    ensureSuperKeyNonNull(super_key_jstr);

    const auto super_key = JUTFString(env, super_key_jstr);
    struct su_profile profile{};
    long rc = sc_su_uid_profile(super_key.get(), (uid_t) uid, &profile);
    if (rc < 0) [[unlikely]] {
        LOGE("nativeSuProfile error: %ld\n", rc);
        return nullptr;
    }
    jclass cls = env->FindClass("me/bmax/apatch/Natives$Profile");
    jmethodID constructor = env->GetMethodID(cls, "<init>", "()V");
    jfieldID uidField = env->GetFieldID(cls, "uid", "I");
    jfieldID toUidField = env->GetFieldID(cls, "toUid", "I");
    jfieldID scontextFild = env->GetFieldID(cls, "scontext", "Ljava/lang/String;");

    jobject obj = env->NewObject(cls, constructor);
    env->SetIntField(obj, uidField, (int) profile.uid);
    env->SetIntField(obj, toUidField, (int) profile.to_uid);
    env->SetObjectField(obj, scontextFild, SafeNewStringUTF(env, profile.scontext));

    return obj;
}

jlong nativeLoadKernelPatchModule(JNIEnv *env, jobject /* this */, jstring super_key_jstr, jstring module_path_jstr, jstring args_jstr) {
    ensureSuperKeyNonNull(super_key_jstr);

    const auto super_key = JUTFString(env, super_key_jstr);
    const auto module_path = JUTFString(env, module_path_jstr);
    const auto args = JUTFString(env, args_jstr);
    long rc = sc_kpm_load(super_key.get(), module_path.get(), args.get(), nullptr);
    if (rc < 0) [[unlikely]] {
        LOGE("nativeLoadKernelPatchModule error: %ld", rc);
    }

    return rc;
}

jobject nativeControlKernelPatchModule(JNIEnv *env, jobject /* this */, jstring super_key_jstr, jstring module_name_jstr, jstring control_args_jstr) {
    ensureSuperKeyNonNull(super_key_jstr);

    const auto super_key = JUTFString(env, super_key_jstr);
    const auto module_name = JUTFString(env, module_name_jstr);
    const auto control_args = JUTFString(env, control_args_jstr);

    char buf[4096] = { '\0' };
    long rc = sc_kpm_control(super_key.get(), module_name.get(), control_args.get(), buf, sizeof(buf));
    if (rc < 0) [[unlikely]] {
        LOGE("nativeControlKernelPatchModule error: %ld", rc);
    }

    jclass cls = env->FindClass("me/bmax/apatch/Natives$KPMCtlRes");
    jmethodID constructor = env->GetMethodID(cls, "<init>", "()V");
    jfieldID rcField = env->GetFieldID(cls, "rc", "J");
    jfieldID outMsg = env->GetFieldID(cls, "outMsg", "Ljava/lang/String;");

    jobject obj = env->NewObject(cls, constructor);
    env->SetLongField(obj, rcField, rc);
    env->SetObjectField(obj, outMsg, SafeNewStringUTF(env, buf));

    return obj;
}

jlong nativeUnloadKernelPatchModule(JNIEnv *env, jobject /* this */, jstring super_key_jstr, jstring module_name_jstr) {
    ensureSuperKeyNonNull(super_key_jstr);

    const auto super_key = JUTFString(env, super_key_jstr);
    const auto module_name = JUTFString(env, module_name_jstr);
    long rc = sc_kpm_unload(super_key.get(), module_name.get(), nullptr);
    if (rc < 0) [[unlikely]] {
        LOGE("nativeUnloadKernelPatchModule error: %ld", rc);
    }

    return rc;
}

jlong nativeKernelPatchModuleNum(JNIEnv *env, jobject /* this */, jstring super_key_jstr) {
    ensureSuperKeyNonNull(super_key_jstr);

    const auto super_key = JUTFString(env, super_key_jstr);
    long rc = sc_kpm_nums(super_key.get());
    if (rc < 0) [[unlikely]] {
        LOGE("nativeKernelPatchModuleNum error: %ld", rc);
    }

    return rc;
}

jstring nativeKernelPatchModuleList(JNIEnv *env, jobject /* this */, jstring super_key_jstr) {
    ensureSuperKeyNonNull(super_key_jstr);

    const auto super_key = JUTFString(env, super_key_jstr);

    char buf[4096] = { '\0' };
    long rc = sc_kpm_list(super_key.get(), buf, sizeof(buf));
    if (rc < 0) [[unlikely]] {
        LOGE("nativeKernelPatchModuleList error: %ld", rc);
    }

    return SafeNewStringUTF(env, buf);
}

jstring nativeKernelPatchModuleInfo(JNIEnv *env, jobject /* this */, jstring super_key_jstr, jstring module_name_jstr) {
    ensureSuperKeyNonNull(super_key_jstr);

    const auto super_key = JUTFString(env, super_key_jstr);
    const auto module_name = JUTFString(env, module_name_jstr);
    char buf[1024] = { '\0' };
    long rc = sc_kpm_info(super_key.get(), module_name.get(), buf, sizeof(buf));
    if (rc < 0) [[unlikely]] {
        LOGE("nativeKernelPatchModuleInfo error: %ld", rc);
    }

    return SafeNewStringUTF(env, buf);
}

jlong nativeGrantSu(JNIEnv *env, jobject /* this */, jstring super_key_jstr, jint uid, jint to_uid, jstring selinux_context_jstr) {
    ensureSuperKeyNonNull(super_key_jstr);

    const auto super_key = JUTFString(env, super_key_jstr);
    const auto selinux_context = JUTFString(env, selinux_context_jstr);
    struct su_profile profile{};
    profile.uid = uid;
    profile.to_uid = to_uid;
    if (selinux_context) strncpy(profile.scontext, selinux_context, sizeof(profile.scontext) - 1);
    return sc_su_grant_uid(super_key.get(), &profile);
}

jlong nativeRevokeSu(JNIEnv *env, jobject /* this */, jstring super_key_jstr, jint uid) {
    ensureSuperKeyNonNull(super_key_jstr);

    const auto super_key = JUTFString(env, super_key_jstr);
    return sc_su_revoke_uid(super_key.get(), (uid_t) uid);
}

jstring nativeSuPath(JNIEnv *env, jobject /* this */, jstring super_key_jstr) {
    ensureSuperKeyNonNull(super_key_jstr);

    const auto super_key = JUTFString(env, super_key_jstr);
    char buf[SU_PATH_MAX_LEN] = { '\0' };
    long rc = sc_su_get_path(super_key.get(), buf, sizeof(buf));
    if (rc < 0) [[unlikely]] {
        LOGE("nativeSuPath error: %ld", rc);
    }

    return SafeNewStringUTF(env, buf);
}

jboolean nativeResetSuPath(JNIEnv *env, jobject /* this */, jstring super_key_jstr, jstring su_path_jstr) {
    ensureSuperKeyNonNull(super_key_jstr);

    const auto super_key = JUTFString(env, super_key_jstr);
    const auto su_path = JUTFString(env, su_path_jstr);

    return sc_su_reset_path(super_key.get(), su_path.get()) == 0;
}

JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void * /*reserved*/) {
    LOGI("Enter OnLoad");

    JNIEnv* env{};
    if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK) [[unlikely]] {
        LOGE("Get JNIEnv error!");
        return JNI_FALSE;
    }

    auto clazz = JNI_FindClass(env, "me/bmax/apatch/Natives");
    if (clazz.get() == nullptr) [[unlikely]] {
        LOGE("Failed to find Natives class");
        return JNI_FALSE;
    }

    const static JNINativeMethod gMethods[] = {
        {"nativeReady", "(Ljava/lang/String;)Z", reinterpret_cast<void *>(&nativeReady)},
        {"nativeKernelPatchVersion", "(Ljava/lang/String;)J", reinterpret_cast<void *>(&nativeKernelPatchVersion)},
        {"nativeKernelPatchBuildTime", "(Ljava/lang/String;)Ljava/lang/String;", reinterpret_cast<void *>(&nativeKernelPatchBuildTime)},
        {"nativeSu", "(Ljava/lang/String;ILjava/lang/String;)J", reinterpret_cast<void *>(&nativeSu)},
        {"nativeSetUidExclude", "(Ljava/lang/String;II)I", reinterpret_cast<void *>(&nativeSetUidExclude)},
        {"nativeGetUidExclude", "(Ljava/lang/String;I)I", reinterpret_cast<void *>(&nativeGetUidExclude)},
        {"nativeSuUids", "(Ljava/lang/String;)[I", reinterpret_cast<void *>(&nativeSuUids)},
        {"nativeSuProfile", "(Ljava/lang/String;I)Lme/bmax/apatch/Natives$Profile;", reinterpret_cast<void *>(&nativeSuProfile)},
        {"nativeLoadKernelPatchModule", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)J", reinterpret_cast<void *>(&nativeLoadKernelPatchModule)},
        {"nativeControlKernelPatchModule", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Lme/bmax/apatch/Natives$KPMCtlRes;", reinterpret_cast<void *>(&nativeControlKernelPatchModule)},
        {"nativeUnloadKernelPatchModule", "(Ljava/lang/String;Ljava/lang/String;)J", reinterpret_cast<void *>(&nativeUnloadKernelPatchModule)},
        {"nativeKernelPatchModuleNum", "(Ljava/lang/String;)J", reinterpret_cast<void *>(&nativeKernelPatchModuleNum)},
        {"nativeKernelPatchModuleList", "(Ljava/lang/String;)Ljava/lang/String;", reinterpret_cast<void *>(&nativeKernelPatchModuleList)},
        {"nativeKernelPatchModuleInfo", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;", reinterpret_cast<void *>(&nativeKernelPatchModuleInfo)},
        {"nativeGrantSu", "(Ljava/lang/String;IILjava/lang/String;)J", reinterpret_cast<void *>(&nativeGrantSu)},
        {"nativeRevokeSu", "(Ljava/lang/String;I)J", reinterpret_cast<void *>(&nativeRevokeSu)},
        {"nativeSuPath", "(Ljava/lang/String;)Ljava/lang/String;", reinterpret_cast<void *>(&nativeSuPath)},
        {"nativeResetSuPath", "(Ljava/lang/String;Ljava/lang/String;)Z", reinterpret_cast<void *>(&nativeResetSuPath)},
        {"nativeGetApiToken", "(Landroid/content/Context;)Ljava/lang/String;", reinterpret_cast<void *>(&nativeGetApiToken)},
    };

    if (JNI_RegisterNatives(env, clazz, gMethods, sizeof(gMethods) / sizeof(gMethods[0])) < 0) [[unlikely]] {
        LOGE("Failed to register native methods");
        return JNI_FALSE;
    }
    
    LOGI("JNI_OnLoad Done!");
    return JNI_VERSION_1_6;
}
