package iris.util

import iris.util.variant.dateOrNull
import org.apache.commons.lang3.time.DatePrinter
import org.apache.commons.lang3.time.FastDateFormat
import java.util.*

/** Создано 14.09.2019 */
object TimeFormats {
	val ISO_DATETIME_FORMAT = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss")
	val ISO_DATE_FORMAT = FastDateFormat.getInstance("yyyy-MM-dd")
	val GERMAN_DATE_FORMAT = FastDateFormat.getInstance("dd.MM.yyyy")
	val GERMAN_DATETIME_FORMAT = FastDateFormat.getInstance("dd.MM.yyyy HH:mm:ss")
	val GERMAN_TIME_SHORT_FORMAT = FastDateFormat.getInstance("HH:mm")
	val GERMAN_TIME_FORMAT = FastDateFormat.getInstance("HH:mm:ss")

	fun germanDate(date: Any? = null): String? {
		return format(date, GERMAN_DATE_FORMAT)
	}

	fun gd(date: Any?): String? {
		return format(date, GERMAN_DATE_FORMAT)
	}

	fun germanDateTime(date: Any? = null): String? {
		return format(date, GERMAN_DATETIME_FORMAT)
	}

	fun gdt(date: Any? = null): String? {
		return format(date, GERMAN_DATETIME_FORMAT)
	}

	fun isoDate(date: Any?): String? {
		return format(date, ISO_DATE_FORMAT)
	}

	fun id(date: Any?): String? {
		return format(date, ISO_DATE_FORMAT)
	}

	fun isoDateTime(date: Any?): String? {
		return format(date, ISO_DATETIME_FORMAT)
	}

	fun idt(date: Any?): String? {
		return format(date, ISO_DATETIME_FORMAT)
	}

	fun germanTimeShort(date: Any?): String? {
		return format(date, GERMAN_TIME_SHORT_FORMAT)
	}

	fun format(date: Any?, format: String): String? {
		return format(date, FastDateFormat.getInstance(format))
	}

	fun format(date: Any?, format: DatePrinter): String? {
		if (date == null) return format.format(Date())
		val el = dateOrNull(date) ?: return null
		return format.format(el)?: ""
	}

	fun day(date: Date): Int {
		return datePart(date, Calendar.DAY_OF_MONTH)
	}

	fun month(date: Date): Int {
		return datePart(date, Calendar.MONTH)
	}

	fun year(date: Date): Int {
		return datePart(date, Calendar.YEAR)
	}

	fun hour(date: Date): Int {
		return datePart(date, Calendar.HOUR_OF_DAY)
	}

	fun minute(date: Date): Int {
		return datePart(date, Calendar.MINUTE)
	}

	fun second(date: Date): Int {
		return datePart(date, Calendar.SECOND)
	}

	fun datePart(date: Date, part: Int): Int {
		val c = Calendar.getInstance()
		c.time = date
		return c.get(part)
	}
}