/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class sun_nio_fs_GnomeFileTypeDetector */

#ifndef _Included_sun_nio_fs_GnomeFileTypeDetector
#define _Included_sun_nio_fs_GnomeFileTypeDetector
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     sun_nio_fs_GnomeFileTypeDetector
 * Method:    initializeGio
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_sun_nio_fs_GnomeFileTypeDetector_initializeGio
  (JNIEnv *, jclass);

/*
 * Class:     sun_nio_fs_GnomeFileTypeDetector
 * Method:    probeUsingGio
 * Signature: (J)[B
 */
JNIEXPORT jbyteArray JNICALL Java_sun_nio_fs_GnomeFileTypeDetector_probeUsingGio
  (JNIEnv *, jclass, jlong);

/*
 * Class:     sun_nio_fs_GnomeFileTypeDetector
 * Method:    initializeGnomeVfs
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_sun_nio_fs_GnomeFileTypeDetector_initializeGnomeVfs
  (JNIEnv *, jclass);

/*
 * Class:     sun_nio_fs_GnomeFileTypeDetector
 * Method:    probeUsingGnomeVfs
 * Signature: (J)[B
 */
JNIEXPORT jbyteArray JNICALL Java_sun_nio_fs_GnomeFileTypeDetector_probeUsingGnomeVfs
  (JNIEnv *, jclass, jlong);

#ifdef __cplusplus
}
#endif
#endif
