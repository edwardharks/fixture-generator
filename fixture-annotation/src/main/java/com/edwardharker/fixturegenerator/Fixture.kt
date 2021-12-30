package com.edwardharker.fixturegenerator

import kotlin.annotation.AnnotationRetention.SOURCE
import kotlin.annotation.AnnotationTarget.CLASS

@Target(CLASS)
@Retention(SOURCE)
annotation class Fixture
