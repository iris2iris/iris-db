package iris.db

import iris.util.plus

/**
 * @created 16.01.2021
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
abstract class SqlFactoryAbstract<T>(val alias: String?, val fields: List<SqlColumn>, val primary: List<SqlColumn> = fields.filter { it.isPrimary }) : SqlFactory<T> {

	override fun <A : Appendable> selectFieldsPart(a: A, fieldNames: List<String>?): A {
		if (alias != null) {
			val alias = alias
			val prefix = alias + '_'
			var first = true
			for (col in findColums(fieldNames)) {
				if (first) first = false else a + ", "
				a + alias + '.' + col.name + " AS " + SqlData.fieldEscape(prefix + col.name)
			}
		} else
			fieldNamesPart(a, fieldNames)
		return a
	}

	private fun <A: Appendable>setFieldsPart(a: A, item: T, fields: Collection<String>? = null, onlyExisting: Boolean = true): A {
		var first = true
		for (field in findColums(fields)) {
			val name = field.name
			val value = getValue(item, name) ?: if (onlyExisting) {if (!keyExists(item, name)) continue else null} else null
			if (first) first = false else (a + ", ")
			a + '`' + SqlData.fieldEscape(name) + "` = " + field.type.toSql(value, field.isNullable)
		}
		return a
	}

	abstract fun getValue(item: T, field: String): Any?

	abstract fun keyExists(item: T, field: String): Boolean

	override fun <A : Appendable> insertPart(a: A, item: T, fieldNames: List<String>?): A {
		return setFieldsPart(a, item, fieldNames, false)
	}

	override fun <A : Appendable> updatePart(a: A, item: T, fieldNames: List<String>?): A {
		return setFieldsPart(a, item, fieldNames, true)
	}

	override fun <A : Appendable> primaryPart(a: A, item: T): A {
		var first = true
		for (field in primary) {
			val name = if (alias == null) field.name else alias + "_" + field.name
			val value = getValue(item, field.name) ?: continue
			if (first) first = false else (a + " AND ")
			a + SqlData.fieldEscape(name) + " = " + field.type.toSql(value, field.isNullable)
		}
		return a
	}

	fun findColums(fieldNames: Collection<String>?) : Collection<SqlColumn> {
		val tableFields = fields
		return fieldNames?.let { it.mapNotNull { tableFields.find { field -> field.name == it } } } ?: tableFields
	}

	override fun <A : Appendable> valuesShort(a: A, item: T, fieldNames: List<String>?, onlyExisting: Boolean): A {
		val fields = findColums(fieldNames)
		var first = true
		for (field in fields) {
			val name = field.name
			val value = getValue(item, name) ?: if (onlyExisting) {if (!keyExists(item, name)) continue else null} else null
			if (first)
				first = false
			else
				a + ", "
			a + field.type.toSql(value, field.isNullable)
		}
		return a
	}

	override fun <A : Appendable> insertValuesShort(a: A, item: T, fieldNames: List<String>?): A {
		return valuesShort(a, item, fieldNames, false)
	}

	override fun <A : Appendable> updateValuesShort(a: A, item: T, fieldNames: List<String>?): A {
		return valuesShort(a, item, fieldNames, true)
	}

	override fun <A : Appendable> fieldNamesPart(a: A, fieldNames: List<String>?): A {
		fieldNames?.joinTo(a, "`, `", "`", "`") { SqlData.fieldEscape(it) }
			?: fields.joinTo(a, "`, `", "`", "`") { SqlData.fieldEscape(it.name) }
		return a
	}

	override fun <A : Appendable> duplicatePart(a: A, updateFieldNames: List<String>?): A {
		if (updateFieldNames == null) {
			var first = true
			for (name in fields) {
				if (name.isPrimary) continue
				if (first) first = false else a + ", "
				val f = SqlData.fieldEscape(name.name)
				a + f + " = VALUES(" + f + ')'
			}
		} else {
			var first = true
			for (name in updateFieldNames) {
				if (first) first = false else a + ", "
				val f = SqlData.fieldEscape(name)
				a + f + " = VALUES(" + f + ')'
			}
		}

		return a
	}
}