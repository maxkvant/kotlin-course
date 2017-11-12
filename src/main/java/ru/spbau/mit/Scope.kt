package ru.spbau.mit

import ru.spbau.mit.Ast.Identifier

class Scope<T : Any>(private val parent: Scope<T>?) {
    private val valuesMap: MutableMap<Identifier, T> = mutableMapOf()

    private fun getScope(identifier: Identifier): Scope<T>? {
        if (valuesMap.containsKey(identifier)) {
            return this
        }
        return parent?.getScope(identifier)
    }

    fun get(identifier: Identifier): T {
        val scope = getScope(identifier)
        return checkNotNull(scope?.valuesMap?.get(identifier)) { "not found: " + identifier.toString() }
    }

    fun set(identifier: Identifier, value: T) {
        val scope: Scope<T> = checkNotNull(getScope(identifier)) { "not found: " + identifier.toString() }
        scope.valuesMap[identifier] = value
    }

    fun put(identifier: Identifier, value: T) {
        valuesMap[identifier] = value
    }
}