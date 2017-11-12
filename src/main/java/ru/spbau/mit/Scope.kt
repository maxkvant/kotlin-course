package ru.spbau.mit

import ru.spbau.mit.Ast.Identifier

open class Scope<T>(private val parent: Scope<T>?) {
    private val valuesMap: MutableMap<Identifier, T> = mutableMapOf<Identifier, T>()

    protected fun getScope(identifier: Identifier): Scope<T>? {
        if (valuesMap.containsKey(identifier)) {
            return this
        }
        return parent?.getScope(identifier)
    }

    fun get(identifier: Identifier): T {
        val scope = getScope(identifier)
        return scope?.valuesMap?.get(identifier) ?: throw RuntimeException("not found: " + identifier.toString())
    }

    fun set(identifier: Identifier, value: T) {
        val scope: Scope<T> = getScope(identifier) ?: throw RuntimeException("not found: " + identifier.toString())
        scope.valuesMap[identifier] = value
    }

    fun put(identifier: Identifier, value: T) {
        valuesMap[identifier] = value
    }
}