package iris.db.assoc

import iris.db.*
import iris.util.cast
import java.sql.ResultSet

/**
 * @created 27.12.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
open class AssociativeTable(
	table: String,
	val fields: List<SqlColumn>,
	private val primary: List<SqlColumn> = fields.filter { it.isPrimary },
	factory: SqlFactory<Map<String, Any?>>,
	driver: SqlDriver = DefaultSqlDriver,
) : TableAbstract<Map<String, Any?>>(table, factory, driver), JoinTable<Map<String, Any?>> by JoinTableDelegate(table, factory, fields, primary) {

	constructor(table: String, fields: List<SqlColumn>, primary: List<SqlColumn> = fields.filter { it.isPrimary }, driver: SqlDriver = DefaultSqlDriver) : this(table, fields, primary, AssociativeFactory(null, fields, primary), driver)
	constructor(table: String, driver: SqlDriver = DefaultSqlDriver) : this(table, buildFields(table, driver), driver = driver)

	companion object {

		private val initCreator = SimpleCreator()

		fun buildFields(table: String, driver: SqlDriver): List<SqlColumn> {
			return driver.multiSelect("DESCRIBE ${SqlData.fieldEscape(table)}", initCreator)
				.map {
					SqlColumn(it["COLUMN_NAME"].cast(),
						SqlType.getType(it["COLUMN_TYPE"].cast()), it["COLUMN_KEY"] == "PRI", null, it["IS_NULLABLE"] == "YES")
				}
		}
	}

	class SimpleCreator() : Creator<Map<String, Any?>> {

		private var metadata: Array<String>? = null

		override fun create(resultSet: ResultSet): Map<String, Any?>? {
			val metadata = getMetaData(resultSet)
			var i = 0
			val res = HashMap<String, Any?>(metadata.size)
			for (name in metadata) {
				res[name] = resultSet.getObject(++i)
			}
			return res;
		}

		private fun getMetaData(resultSet: ResultSet) : Array<String> {
			if (metadata != null) return metadata!!
			synchronized(this) {
				val meta = resultSet.metaData
				return Array(meta.columnCount) { meta.getColumnName(it + 1) }.also { metadata = it }
			}
		}
	}

	class DynamicFieldsCreator(private val fields: List<SqlColumn>, private val alias: String? = null) :
		Creator<Map<String, Any?>> {

		private var metadata: Array<FieldData>? = null
		private val generatedAlias = alias?.let { alias + '_' }
		private var notNullTest: FieldData? = null

		override fun create(resultSet: ResultSet): Map<String, Any?>? {
			val metadata = getMetadata(resultSet)
			val notNullObj = notNullTest?.run { resultSet.getObject(index).let { it ?: return null }}
			val res = HashMap<String, Any?>(metadata.size)
			for (meta in metadata) {
				val column = meta.column
				res[column.name] = column.type.toObject(if (meta === notNullTest) notNullObj else resultSet.getObject(meta.index))
			}
			return res
		}

		private class FieldData(val index: Int, val column: SqlColumn)

		private fun getMetadata(rs: ResultSet): Array<FieldData> {
			if (metadata != null) return metadata!!
			synchronized(this) {
				if (metadata != null) return metadata!!
				val metaData = rs.metaData
				val res = mutableListOf<FieldData>()
				for (i in 1..metaData.columnCount) {
					val name = metaData.getColumnLabel(i).let { if (generatedAlias == null) it else if (it.startsWith(generatedAlias)) it.substring(generatedAlias.length) else null } ?: continue
					fields.find { it.name == name }?.also { res += FieldData(i, it) }
				}
				notNullTest = res.find { !it.column.isNullable }
				return res.toTypedArray().also { metadata = it }
			}
		}
	}

	internal open class AssociativeFactory(alias: String?, fields: List<SqlColumn>, primary: List<SqlColumn> = fields.filter { it.isPrimary }
		) : SqlFactoryAbstract<Map<String, Any?>>(alias, fields, primary) {

		private val cache = mutableMapOf<Any, DynamicFieldsCreator>()

		override fun alias(alias: String?): SqlFactory<Map<String, Any?>> {
			return AssociativeFactory(alias, fields, primary)
		}

		private val defaultCreator = DynamicFieldsCreator(fields, alias)

		override fun creator(id: Any?): Creator<Map<String, Any?>> {
			return id?.let { cache.getOrPut(id) { DynamicFieldsCreator(fields, alias) } } ?: defaultCreator
		}

		override fun getValue(item: Map<String, Any?>, field: String): Any? {
			return item[field]
		}

		override fun keyExists(item: Map<String, Any?>, field: String): Boolean {
			return item.contains(field)
		}
	}

	override val tableName = super.tableName
}

