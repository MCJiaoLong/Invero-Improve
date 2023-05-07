package cc.trixey.invero.core.action

import cc.trixey.invero.core.action.Comparator.Type.*
import cc.trixey.invero.core.serialize.ComparatorSerializer
import kotlinx.serialization.Serializable
import taboolib.common5.cdouble

/**
 * Invero
 * cc.trixey.invero.core.action.Comparator
 *
 * @author Arasple
 * @since 2023/1/14 12:45
 */
@Serializable(with = ComparatorSerializer::class)
class Comparator(val type: Type, val value: String) {

    private val numbericValue by lazy {
        value.cdouble
    }

    fun compare(input: String): Boolean {
        return when (type) {
            EQUALS -> input == value
            EQUALS_IGNORECASE -> input.equals(value, true)
            CONTAINS -> value.contains(input)
            CONTAINS_IGNORECASE -> value.contains(input, true)
            GREATER -> input.cdouble > numbericValue
            GREATER_OR_EQUALS -> input.cdouble >= numbericValue
            SMALLER -> input.cdouble < numbericValue
            SMALLER_OR_EQUALS -> input.cdouble <= numbericValue
        }
    }

    companion object {

        fun parse(format: String): Comparator {
            var value = format
            val type = Type.values().find { it.operators.any { sym -> format.startsWith(sym) } }
            type?.let {
                it.operators.forEach { sym -> value = value.removePrefix(sym) }
                value = value.removePrefix(" ")
            }
            return Comparator(type ?: EQUALS, value)
        }

    }

    override fun toString(): String {
        return "${type.operators.first()} $value".also {

        }
    }

    enum class Type(val operators: List<String>) {

        GREATER_OR_EQUALS(">=", "≥"),

        GREATER(">"),

        SMALLER_OR_EQUALS("<=", "≤"),

        SMALLER("<"),

        EQUALS("=", "=="),

        EQUALS_IGNORECASE("~=", "~=="),

        CONTAINS_IGNORECASE("~~"),

        CONTAINS("has", "contains", "~");

        constructor(vararg operators: String) : this(operators.map { it })

    }

}