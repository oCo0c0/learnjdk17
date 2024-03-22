/*
 * Copyright (c) 2018, 2019, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */
package jdk.vm.ci.hotspot;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import jdk.vm.ci.meta.JavaConstant;
import jdk.vm.ci.meta.ResolvedJavaMethod;
import jdk.vm.ci.meta.ResolvedJavaType;

/**
 * API for reflecting on the internals of HotSpot JVMCI types and objects.
 */
abstract class HotSpotJVMCIReflection {

    abstract boolean isInstance(HotSpotResolvedObjectTypeImpl holder, HotSpotObjectConstantImpl obj);

    abstract boolean isAssignableFrom(HotSpotResolvedObjectTypeImpl holder, HotSpotResolvedObjectTypeImpl otherType);

    abstract Annotation[] getAnnotations(HotSpotResolvedObjectTypeImpl holder);

    abstract Annotation[] getDeclaredAnnotations(HotSpotResolvedObjectTypeImpl holder);

    abstract <T extends Annotation> T getAnnotation(HotSpotResolvedObjectTypeImpl holder, Class<T> annotationClass);

    abstract boolean isLocalClass(HotSpotResolvedObjectTypeImpl holder);

    abstract boolean isMemberClass(HotSpotResolvedObjectTypeImpl holder);

    abstract HotSpotResolvedObjectType getEnclosingClass(HotSpotResolvedObjectTypeImpl holder);

    abstract boolean equals(HotSpotObjectConstantImpl hotSpotResolvedJavaType, HotSpotObjectConstantImpl that);

    abstract ResolvedJavaMethod.Parameter[] getParameters(HotSpotResolvedJavaMethodImpl javaMethod);

    abstract Annotation[][] getParameterAnnotations(HotSpotResolvedJavaMethodImpl javaMethod);

    abstract Type[] getGenericParameterTypes(HotSpotResolvedJavaMethodImpl javaMethod);

    abstract Annotation[] getFieldAnnotations(HotSpotResolvedJavaFieldImpl javaMethod);

    abstract Annotation[] getMethodAnnotations(HotSpotResolvedJavaMethodImpl javaField);

    abstract Annotation[] getMethodDeclaredAnnotations(HotSpotResolvedJavaMethodImpl javaMethod);

    abstract Annotation[] getFieldDeclaredAnnotations(HotSpotResolvedJavaFieldImpl javaMethod);

    abstract <T extends Annotation> T getMethodAnnotation(HotSpotResolvedJavaMethodImpl javaMethod, Class<T> annotationClass);

    abstract HotSpotResolvedObjectTypeImpl getType(HotSpotObjectConstantImpl object);

    abstract String asString(HotSpotObjectConstantImpl object);

    /**
     * Given a {@link java.lang.Class} instance, return the corresponding ResolvedJavaType.
     */
    abstract ResolvedJavaType asJavaType(HotSpotObjectConstantImpl object);

    abstract <T> T asObject(HotSpotObjectConstantImpl object, Class<T> type);

    abstract Object asObject(HotSpotObjectConstantImpl object, HotSpotResolvedJavaType type);

    abstract String formatString(HotSpotObjectConstantImpl object);

    abstract Integer getLength(HotSpotObjectConstantImpl arrayObject);

    abstract JavaConstant readArrayElement(HotSpotObjectConstantImpl arrayObject, int index);

    abstract JavaConstant unboxPrimitive(HotSpotObjectConstantImpl source);

    abstract JavaConstant forObject(Object value);

    abstract JavaConstant boxPrimitive(JavaConstant source);

    abstract <T extends Annotation> T getFieldAnnotation(HotSpotResolvedJavaFieldImpl javaField, Class<T> annotationClass);

    /**
     * Resolves {@code objectHandle} to a raw object if possible.
     *
     * @throws HotSpotJVMCIUnsupportedOperationError if {@code objectHandle} refers to an object in
     *             another heap
     */
    abstract Object resolveObject(HotSpotObjectConstantImpl objectHandle);
}
