package iris.util

import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField
import java.time.temporal.TemporalAccessor
import java.time.temporal.TemporalField

/** @created 19.04.2020 */
object LocalDateFormats {

	val ISO_DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")//("yyyy-MM-dd HH:mm:ss")
	val ISO_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd")//SimpleDateFormat("yyyy-MM-dd")
	val GERMAN_DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy")
	val GERMAN_DATETIME_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")
	val GERMAN_TIME_SHORT_FORMAT = DateTimeFormatter.ofPattern("HH:mm")
	val GERMAN_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss")

	fun germanDate(date: Any? = null): String {
		return format(date, GERMAN_DATE_FORMAT)
	}

	fun gd(date: Any?): String {
		return format(date, GERMAN_DATE_FORMAT)
	}

	fun germanDateTime(date: Any? = null): String {
		return format(date, GERMAN_DATETIME_FORMAT)
	}

	fun gdt(date: Any? = null): String {
		return format(date, GERMAN_DATETIME_FORMAT)
	}

	fun isoDate(date: Any?): String {
		return format(date, ISO_DATE_FORMAT)
	}

	fun id(date: Any?): String {
		return format(date, ISO_DATE_FORMAT)
	}

	fun isoDateTime(date: Any?): String {
		return format(date, ISO_DATETIME_FORMAT)
	}

	fun idt(date: Any?): String {
		return format(date, ISO_DATETIME_FORMAT)
	}

	fun germanTimeShort(date: Any?): String {
		return format(date, GERMAN_TIME_SHORT_FORMAT)
	}

	fun format(date: Any?, format: String): String {
		return format(date, DateTimeFormatter.ofPattern(format))
	}

	fun format(date: Any?, format: DateTimeFormatter): String {
		if (date == null) return format.format(Instant.now())
		if (date !is TemporalAccessor)
			throw IllegalArgumentException(date.javaClass.toString() + " is not allowed here")
		return format.format(date)?: ""
	}

	fun day(date: TemporalAccessor): Int {
		return datePart(date, ChronoField.DAY_OF_MONTH)
	}

	fun month(date: TemporalAccessor): Int {
		return datePart(date, ChronoField.MONTH_OF_YEAR)
	}

	fun year(date: TemporalAccessor): Int {
		return datePart(date, ChronoField.YEAR)
	}

	fun hour(date: TemporalAccessor): Int {
		return datePart(date, ChronoField.HOUR_OF_DAY)
	}

	fun minute(date: TemporalAccessor): Int {
		return datePart(date, ChronoField.MINUTE_OF_HOUR)
	}

	fun second(date: TemporalAccessor): Int {
		return datePart(date, ChronoField.SECOND_OF_MINUTE)
	}

	fun datePart(date: TemporalAccessor, part: TemporalField): Int {
		return date.get(part)
	}

	fun toLocalDateTime(temporal: TemporalAccessor): LocalDateTime {
		return when (temporal) {
			is LocalDateTime -> temporal
			is ZonedDateTime -> temporal.toLocalDateTime()
			is OffsetDateTime -> temporal.toLocalDateTime()
			is LocalDate -> {
				val date = LocalDate.from(temporal)
				LocalDateTime.of(date, LocalTime.MIN)
			}
			else -> try {
				val date = LocalDate.from(temporal)
				val time = if (temporal.isSupported(ChronoField.HOUR_OF_DAY))
					LocalTime.from(temporal)
				else
					LocalTime.MIN
				LocalDateTime.of(date, time)
			} catch (ex: DateTimeException) {
				throw DateTimeException("Unable to obtain LocalDateTime from TemporalAccessor: " +
						temporal + " of type " + temporal.javaClass.getName(), ex)
			}
		}
	}

	fun toLocalDate(temporal: TemporalAccessor): LocalDate {
		return LocalDate.from(temporal)
	}
}

inline fun TemporalAccessor.toLocalDate(): LocalDate {
	return LocalDateFormats.toLocalDate(this)
}

inline fun TemporalAccessor.toLocalDateTime(): LocalDateTime {
	return LocalDateFormats.toLocalDateTime(this)
}