package com.magi.annotations


/**
 * @author zhangzhaowen @ Zhihu Inc.
 * @since 09-06-2017
 */
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
@kotlin.annotation.Target(AnnotationTarget.FIELD)
annotation class TestAnnotation(val value: String, val value2: Array<String> = arrayOf("value2"))