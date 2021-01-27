package iris.db

import iris.util.*
import java.time.temporal.TemporalAccessor
import java.util.*

/** Создано 09.09.2019  */
object SqlData {

	fun intVal(value: Int): String {
		return value.toString()
	}

	fun intVal(value: Any?): String {
		return intVal(value?.cast() ?: 0)
	}

	fun fieldEscape(str: String): String {
		return escapeString(str)
	}

	fun escapeLikeString(str: String) : String {
		return escapeString(str).replace(ESC_LIKE, "\\$1")
	}

	private val ESC = Regex("[\u0000\u0008\u0009\u001a\n\r\"'#\\\\]")
	private val ESC_LIKE = Regex("[%_]")

	fun escapeString(str: String) : String {
		return str.replace(ESC) {
			when (it.value) {
				"\u0000" -> "\\0"
				"\u0008" -> "\\b"
				"\u0009" -> "\\t"
				"\u001a" -> "\\z"
				"\n" -> "\\n"
				"\r" -> "\\r"
				else -> "\\" + it.value
			}
		}
	}

	fun stringVal(str: String): String {
		return "\"" + escapeString(str) + "\""
	}

	fun stringVal(str: Any?): String {
		return stringVal(str?.cast() ?: "")
	}

	fun stringValStripped(str: String): String {
		return escapeString(str)
	}

	private val ISO_DATETIME_FORMAT = TimeFormats.ISO_DATETIME_FORMAT
	private val ISO_DATE_FORMAT = TimeFormats.ISO_DATE_FORMAT

	fun dateVal(date: TemporalAccessor?): String {
		if (date == null) return "NULL"
		return '"' + LocalDateFormats.ISO_DATE_FORMAT.format(date) + '"'
	}

	fun dateVal(date: Date?): String {
		if (date == null) return "NULL"
		return '"' + ISO_DATE_FORMAT.format(date) + '"'
	}

	fun dateTimeVal(date: Date?): String {
		if (date == null) return "NULL"
		return '"' + ISO_DATETIME_FORMAT.format(date) + '"'
	}

	fun dateTimeVal(date: Any?): String {
		return when (date) {
			null -> "NULL"
			is Date -> dateTimeVal(date)
			is TemporalAccessor -> dateTimeVal(date)
			is Int -> dateVal(Date(date*1000L))
			else -> throw IllegalArgumentException("Not supported datetime type ${date::class} with value \"$date\"")
		}
	}

	fun dateTimeVal(date: TemporalAccessor?): String {
		if (date == null) return "NULL"
		return '"' + LocalDateFormats.ISO_DATETIME_FORMAT.format(LocalDateFormats.toLocalDateTime(date)) + '"'
	}

	fun booleanVal(str: Boolean): String {
		return if (str) "1" else "0"
	}

	fun longVal(value: Long?): String {
		return value?.toString() ?: "NULL"
	}

	fun doubleVal(value: Double?): String {
		return value?.toString() ?: "NULL"
	}

	fun floatVal(value: Float?): String {
		return value?.toString() ?: "NULL"
	}

	fun generateFields(items: List<String>, prefix: String? = null): String {
		if (prefix != null) {
			items.forEach() {
				val d = escapeString(it)
				prefix + '.' + d + " AS " + prefix + "_" + d
			}
		}
		return items.joinToString(", ")
	}

	fun signIntVal(amount: Int): String {
		return (if (amount > 0) " + " else "") + amount.toString()
	}
}
