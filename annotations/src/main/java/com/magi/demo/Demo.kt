package com.magi.demo

import com.magi.annotations.TestAnnotation


/**
 * @author zhangzhaowen @ Zhihu Inc.
 * @since 09-06-2017
 */
class Demo {

    @TestAnnotation("Hello Annotation!")
    private val testAnnotation: String? = null


    fun main() {
        try {
            val cls = Class.forName("com.magi.demo.Demo")
            val fields = cls.declaredFields

            fields
                    .map { it.getAnnotation(TestAnnotation::class.java) }
                    .forEach { print(it?.value) }

        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        }
    }
}