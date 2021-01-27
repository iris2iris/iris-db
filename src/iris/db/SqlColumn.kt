package iris.db

/**
 * @created 26.12.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */

data class SqlColumn(val name: String, val type: SqlType, val isPrimary: Boolean, val defaultValue: Any?, val isNullable: Boolean) {
	fun toSql(value: Any?): String {
		return type.toSql(value ?: if (isNullable) null else defaultValue, isNullable)
	}

	fun toObject(value: Any?): Any? {
		return type.toObject(value) ?: if (isNullable) null else defaultValue
	}
}