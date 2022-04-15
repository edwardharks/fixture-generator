package com.edwardharker.fixturegenerator

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Fixture(val clazz: KClass<*>)
