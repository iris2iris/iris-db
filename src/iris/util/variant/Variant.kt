package iris.util.variant

import java.util.*
import kotlin.collections.HashMap

/**
 * @created 08.01.2021
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
class Variant(var value: Any?) {

	operator fun set(s: String, value: Any?) {
		when (val v = this.value) {
			is MutableMap<*, *> -> (v as MutableMap<String, Any?>)[s] = value
			null -> throw NullPointerException()
			else -> throw IllegalArgumentException("Can not set value by string key to type of ${v::class}")
		}
	}

	operator fun set(s: Int, value: Any?) {
		when (val v = this.value) {
			null -> throw NullPointerException()
			is MutableList<*> -> (v as MutableList<Any?>)[s] = value
			is MutableMap<*, *> -> (v as MutableMap<Int, Any?>)[s] = value
			else -> throw IllegalArgumentException("Can not set Int value by int key to type of ${v::class}")
		}
	}

	operator fun get(s: String): Variant {
		return Variant(when(val v = this.value) {
			null -> null
			is Map<*, *> -> (v as Map<String, Any?>)[s]
			is Variant -> return v
			else -> throw IllegalArgumentException("Can not get value by string key from type of ${v::class}")
		})
	}

	operator fun get(s: Int): Variant {
		return Variant(
			when(val v = this.value) {
				null -> null
				is Array<*> -> v.elementAtOrNull(s)
				is Iterable<*> -> (v as Iterable<Any?>).elementAtOrNull(s)
				is Map<*, *> -> (v as Map<Int, Any?>).get(s)
				is Variant -> return v
				else -> throw IllegalArgumentException(v.toString())
			}
		)
	}
	
	fun toInt() = int(value!!)
	fun toIntOrNull() = intOrNull(value)

	fun toLong() = long(value!!)
	fun toLongOrNull() = longOrNull(value)

	fun toDate() = dateOrNull(value!!)!!
	fun toDateOrNull() = dateOrNull(value)

	fun toDouble() = double(value!!)
	fun toDoubleOrNull() = doubleOrNull(value)

	fun toFloat() = float(value!!)
	fun toFloatOrNull() = floatOrNull(value)
}

fun dateOrNull(value: Any?): Date? {
	return when (value) {
		is Date -> value
		null -> null
		is Long -> Date(value)
		is Number -> time2Date(value)
		is java.sql.Date -> Date(value.time)
		else -> null
	}
}

fun time2Date(time: Any): Date {
	return Date(long(time) * 1000L)
}

fun int(o: Any): Int {
	return when (o) {
		is Int -> o
		is String -> o.toInt()
		else -> (o as Number).toInt()
	}
}

fun intOrNull(o: Any?) : Int? {
	return when (o) {
		is Int -> o
		null -> null
		is Number -> o.toInt()
		is String -> o.toIntOrNull()
		else -> null
	}
}

fun long(o: Any): Long {
	return when (o) {
		is Long -> o
		is String -> o.toLong()
		else -> (o as Number).toLong()
	}
}

fun longOrNull(o: Any?) : Long? {
	return when (o) {
		is Long -> o
		null -> null
		is Number -> o.toLong()
		is String -> o.toLongOrNull()
		else -> null
	}
}

fun double(o: Any) : Double {
	return when (o) {
		is Double -> o
		is String -> o.toDouble()
		else -> (o as Number).toDouble()
	}
}

fun doubleOrNull(o: Any?) : Double? {
	return when (o) {
		is Double -> o
		null -> null
		is Number -> o.toDouble()
		is String -> o.toDoubleOrNull()
		else -> null
	}
}

fun float(o: Any) : Float {
	return when (o) {
		is Float -> o
		is String -> o.toFloat()
		else -> (o as Number).toFloat()
	}
}

fun floatOrNull(o: Any?) : Float? {
	return when (o) {
		is Float -> o
		null -> null
		is Number -> o.toFloat()
		is String -> o.toFloatOrNull()
		else -> null
	}
}

fun boolean(o: Any?): Boolean {
	return when (o) {
		is Boolean -> o
		is Number -> o.toInt() != 0
		"0" -> false
		else -> isNotEmpty(o)
	}
}

fun booleanOrNull(o: Any?): Boolean? {
	return when (o) {
		is Boolean -> o
		null -> null
		is Number -> o.toInt() != 0
		"0" -> false
		else -> null
	}
}

fun isEmpty(o: Any?): Boolean {
	return when (o) {
		null -> true
		is String -> o.isEmpty()
		is Collection<*> -> o.isEmpty()
		is Long -> o == 0L
		is Double -> o == .0
		is Float -> o == 0f
		is Int -> o == 0
		is Boolean -> !o
		else -> false
	}
}

fun isNotEmpty(o: Any?): Boolean {
	return !isEmpty(o)
}