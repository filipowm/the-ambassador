package com.roche.ambassador.advisor.dsl

import com.roche.ambassador.advisor.AdvisorContext
import com.roche.ambassador.advisor.model.BuildableAdvice

class AdviceBuilder<A : BuildableAdvice> internal constructor(buildableAdvice: A, context: AdvisorContext) : ConditionsBuilder<A>(buildableAdvice, context) {

    infix fun conditionally(conditionally: Conditionally<A>.() -> Unit) {
        val c = Conditionally(buildableAdvice, context)
        conditionally(c)
        apply(c)
    }

    override operator fun invoke(): Boolean {
        conditions.forEach { it() }
        return true
    }
}