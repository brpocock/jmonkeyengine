/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_jme3_bullet_collision_shapes_CompoundCollisionShape */

#ifndef _Included_com_jme3_bullet_collision_shapes_CompoundCollisionShape
#define _Included_com_jme3_bullet_collision_shapes_CompoundCollisionShape
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_jme3_bullet_collision_shapes_CompoundCollisionShape
 * Method:    createShape
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_com_jme3_bullet_collision_shapes_CompoundCollisionShape_createShape
  (JNIEnv *, jobject);

/*
 * Class:     com_jme3_bullet_collision_shapes_CompoundCollisionShape
 * Method:    addChildShape
 * Signature: (JJLcom/jme3/math/Vector3f;Lcom/jme3/math/Matrix3f;)J
 */
JNIEXPORT jlong JNICALL Java_com_jme3_bullet_collision_shapes_CompoundCollisionShape_addChildShape
  (JNIEnv *, jobject, jlong, jlong, jobject, jobject);

/*
 * Class:     com_jme3_bullet_collision_shapes_CompoundCollisionShape
 * Method:    removeChildShape
 * Signature: (JJ)J
 */
JNIEXPORT jlong JNICALL Java_com_jme3_bullet_collision_shapes_CompoundCollisionShape_removeChildShape
  (JNIEnv *, jobject, jlong, jlong);

#ifdef __cplusplus
}
#endif
#endif
