package iris.db

import iris.db.dao.Field
import iris.util.TimeFormats
import iris.util.variant.*
import java.util.*
import kotlin.reflect.KType
import kotlin.reflect.jvm.javaType

/**
 * @created 26.12.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
abstract class SqlType(/*val kotlinType: String, */) {

	abstract fun toSql(value: Any?, isNullable: Boolean): String
	abstract fun toObject(value: Any?): Any?
	abstract fun stringToValue(stringValue: String?, isNullable: Boolean): Any?

	class IntegerType : SqlType(/*"Int"*/) {
		override fun toSql(value: Any?, isNullable: Boolean): String {
			return value?.let { SqlData.intVal(int(it)) } ?: if (isNullable) "null" else "0"
		}
		override fun toObject(value: Any?): Int? {
			return intOrNull(value)
		}

		override fun stringToValue(stringValue: String?, isNullable: Boolean): Int? {
			return stringValue?.toIntOrNull() ?: (if (isNullable) null else 0)
		}
	}

	class LongType : SqlType(/*"Long"*/) {
		override fun toSql(value: Any?, isNullable: Boolean): String {
			return value?.let { SqlData.longVal(long(it)) } ?: if (isNullable) "null" else "0"
		}
		override fun toObject(value: Any?): Long? {
			return longOrNull(value)
		}

		override fun stringToValue(stringValue: String?, isNullable: Boolean): Long? {
			return stringValue?.toLongOrNull() ?: (if (isNullable) null else 0L)
		}
	}

	class DoubleType : SqlType(/*"Double"*/) {
		override fun toSql(value: Any?, isNullable: Boolean): String {
			return value?.let { SqlData.doubleVal(double(it)) } ?: if (isNullable) "null" else "0.0"
		}
		override fun toObject(value: Any?): Double? {
			return doubleOrNull(value)
		}

		override fun stringToValue(stringValue: String?, isNullable: Boolean): Double? {
			return stringValue?.toDoubleOrNull() ?: (if (isNullable) null else 0.0)
		}
	}

	class FloatType : SqlType(/*"Float"*/) {
		override fun toSql(value: Any?, isNullable: Boolean): String {
			return value?.let { SqlData.floatVal(float(it)) } ?: if (isNullable) "null" else "0.0"
		}
		override fun toObject(value: Any?): Float? {
			return floatOrNull(value)
		}

		override fun stringToValue(stringValue: String?, isNullable: Boolean): Any? {
			return stringValue?.toFloatOrNull() ?: (if (isNullable) null else 0f)
		}
	}

	class StringType : SqlType(/*"String"*/) {
		override fun toSql(value: Any?, isNullable: Boolean): String {
			return value?.let { SqlData.stringVal(value.toString()) } ?: if (isNullable) "null" else "\"\""
		}
		override fun toObject(value: Any?): String? {
			return value?.toString()
		}

		override fun stringToValue(stringValue: String?, isNullable: Boolean): Any? {
			return if (isNullable) stringValue else stringValue ?: ""
		}
	}

	class BooleanType : SqlType(/*"Boolean"*/) {
		override fun toSql(value: Any?, isNullable: Boolean): String {
			return value?.let { SqlData.booleanVal(boolean(value)) } ?:  if (isNullable) "null" else "false"
		}
		override fun toObject(value: Any?): Boolean? {
			return booleanOrNull(value)
		}

		override fun stringToValue(stringValue: String?, isNullable: Boolean): Boolean? {
			return stringValue?.toBoolean() ?: (if (isNullable) null else false)
		}
	}

	class DateTimeType : SqlType(/*"java.util.Date"*/) {
		override fun toSql(value: Any?, isNullable: Boolean): String {
			return value?.let { SqlData.dateTimeVal(it) } ?:  if (isNullable) "null" else "NOW()"
		}
		override fun toObject(value: Any?): Date? {
			return dateOrNull(value)
		}

		override fun stringToValue(stringValue: String?, isNullable: Boolean): Date? {
			return stringValue?.run { TimeFormats.ISO_DATETIME_FORMAT.parse(this) } ?: (if (isNullable) null else Date())
		}
	}

	class DateType : SqlType(/*"java.util.Date"*/) {
		override fun toSql(value: Any?, isNullable: Boolean): String {
			return dateOrNull(value)?.let { SqlData.dateVal(it) } ?:  if (isNullable) "null" else "CURDATE()"
		}
		override fun toObject(value: Any?): Date? {
			return dateOrNull(value)
		}

		override fun stringToValue(stringValue: String?, isNullable: Boolean): Date? {
			return stringValue?.run { TimeFormats.ISO_DATE_FORMAT.parse(this) } ?: (if (isNullable) null else Date())
		}
	}

	class DateTimeExType : SqlType(/*"???"*/) {
		override fun toSql(value: Any?, isNullable: Boolean): String {
			return dateOrNull(value)?.let { SqlData.dateTimeVal(it) } ?:  if (isNullable) "null" else "NOW()"
		}
		override fun toObject(value: Any?): Date? {
			return dateOrNull(value)
		}

		override fun stringToValue(stringValue: String?, isNullable: Boolean): Date? {
			return stringValue?.run { TimeFormats.ISO_DATETIME_FORMAT.parse(this) } ?: (if (isNullable) null else Date())
		}
	}

	class DateExType : SqlType(/*"???"*/) {
		override fun toSql(value: Any?, isNullable: Boolean): String {
			return dateOrNull(value)?.let { SqlData.dateVal(it) } ?:  if (isNullable) "null" else "CURDATE()"
			//return SqlData.dateVal(dateOrNull(value))
		}
		override fun toObject(value: Any?): Date? {
			return dateOrNull(value)
		}

		override fun stringToValue(stringValue: String?, isNullable: Boolean): Date? {
			return stringValue?.run { TimeFormats.ISO_DATE_FORMAT.parse(this) } ?: (if (isNullable) null else Date())
		}
	}

	class JsonType : SqlType(/*"???"*/) {
		override fun toSql(value: Any?, isNullable: Boolean): String {
			return SqlData.stringVal(value.toString())
		}
		override fun toObject(value: Any?): String? {
			return value?.toString()
		}

		override fun stringToValue(stringValue: String?, isNullable: Boolean): Date? {
			TODO()
		}
	}

	companion object {
		val INTEGER = IntegerType()
		val LONG = LongType()
		val STRING = StringType()
		val DOUBLE = DoubleType()
		val FLOAT = FloatType()
		val BOOLEAN = BooleanType()

		val DATETIME = DateTimeType()
		val DATE = DateType()
		val DATETIME_EX = DateTimeExType()
		val DATE_EX = DateExType()
		val JSON = JsonType()

		fun getType(sqlType: String): SqlType {
			if (sqlType.startsWith("int(")) {
				val len = sqlType.substring("int(".length, sqlType.length - 1).toInt()
				if (len <= 11)
					return INTEGER
				else
					throw IllegalArgumentException("Unknown int type \"$sqlType\"")
			} else if (sqlType.startsWith("bigint(")) {
				return LONG
			} else if (sqlType.startsWith("smallint(")) {
				return INTEGER
			} else if (sqlType.startsWith("tinyint(")) {
				val len = sqlType.substring("tinyint(".length, sqlType.length - 1).toInt()
				if (len == 1)
					return BOOLEAN
				return INTEGER
			} else if (sqlType.startsWith("decimal(")) {
				return DOUBLE
			} else if (sqlType.startsWith("varchar(")) {
				return STRING
			}

			return when (sqlType) {
				"datetime" -> DATETIME_EX
				"date" -> DATE_EX
				"timestamp" -> DATETIME
				"text", "mediumtext" -> STRING
				else -> {
					throw IllegalArgumentException("No such sql type \"$sqlType\"")
				}
			}
		}

		fun kotlinDefault(sqlType: SqlType, defaultValue: String?, isNullable: Boolean = false): Any? {
			val defaultValue = if (defaultValue.isNullOrEmpty()) null else defaultValue
			if (isNullable && defaultValue == null) {
				return null
			}
			return sqlType.stringToValue(defaultValue, isNullable)
		}

		fun convertKotlinType(s: KType, fieldData: Field?): SqlType {

			return if (fieldData?.type.isNullOrBlank()) {
				when (s.javaType.typeName) {
					"java.util.Date" -> DATETIME
					"int" -> INTEGER
					"long" -> LONG
					"double" -> DOUBLE
					"float" -> FLOAT
					"boolean" -> BOOLEAN
					"java.lang.String" -> STRING
					else -> throw IllegalArgumentException("Unsupported type \"${s.javaType.typeName}\"")
				}
			} else {
				when (fieldData!!.type) {
					"datetime" -> DATETIME
					"date" -> DATE
					"json" -> JSON
					"integer", "int" -> INTEGER
					"long" -> LONG
					"double" -> DOUBLE
					"float" -> FLOAT
					"bool", "boolean" -> BOOLEAN
					"string", "text" -> STRING
					else -> throw IllegalArgumentException("Unsupported type \"$s\"")
				}
			}
		}
	}
}