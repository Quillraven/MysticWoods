package com.github.quillraven.mysticwoods.ui.model

import kotlin.reflect.KProperty

abstract class PropertyChangeSource {
    @PublishedApi
    internal val listenersMap = mutableMapOf<KProperty<*>, MutableList<(Any) -> Unit>>()

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T> onPropertyChange(property: KProperty<T>, noinline action: (T) -> Unit) {
        val actions = listenersMap.getOrPut(property) { mutableListOf() } as MutableList<(T) -> Unit>
        actions += action
    }

    fun notify(property: KProperty<*>, value: Any) {
        listenersMap[property]?.forEach { it(value) }
    }
}

class PropertyNotifier<T : Any>(initialValue: T) {
    private var _value: T = initialValue

    operator fun getValue(thisRef: PropertyChangeSource, property: KProperty<*>): T = _value

    operator fun setValue(thisRef: PropertyChangeSource, property: KProperty<*>, value: T) {
        _value = value
        thisRef.notify(property, value)
    }
}

inline fun <reified T : Any> propertyNotify(initialValue: T): PropertyNotifier<T> = PropertyNotifier(initialValue)