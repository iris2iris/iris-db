package iris.db.assoc

import iris.db.*
import iris.util.plus

/**
 * @created 06.01.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
open class AssociativeIdTable<K>(table: String, private val fields: List<SqlColumn>, factory: SqlIdFactory<K, Map<String, Any?>>, driver: SqlDriver = GlobalSqlDriver) : TableIdAbstract<K, Map<String, Any?>>(table, factory, driver), JoinTable<Map<String, Any?>> by JoinTableDelegate(
	table,
	factory,
	fields
) {

	constructor(table: String, alias: String? = null, fields: List<SqlColumn>, driver: SqlDriver = GlobalSqlDriver) : this(table, fields, AssociativeIdFactory<K>(alias, fields), driver)
	constructor(table: String, alias: String? = null, driver: SqlDriver = GlobalSqlDriver) : this(table, alias,
		AssociativeTable.buildFields(table, driver), driver)

	private val primary = fields.filter { it.isPrimary }

	internal class AssociativeIdFactory<K>(alias: String?, fields: List<SqlColumn>) : AssociativeTable.AssociativeFactory(alias, fields),
		SqlIdFactory<K, Map<String, Any?>> {
		private val idColumn: SqlColumn
		private val keyTextPart: String
		private val keyInTextPart: String
		init {
			if (primary.size != 1)
				throw IllegalStateException("Primary key must be single column")
			idColumn = primary[0]
			keyTextPart = SqlData.fieldEscape(idColumn.name) + " = "
			keyInTextPart = SqlData.fieldEscape(idColumn.name) + " IN ("
		}

		override fun <A : Appendable> key2Primary(a: A, key: K): A {
			a + keyTextPart + idColumn.toSql(key)
			return a
		}

		override fun <A : Appendable> keys2Primary(a: A, keys: Collection<K>): A {
			a + keyInTextPart
			keys.joinTo(a) { idColumn.toSql(it) } + ')'
			return a
		}
	}

	override val tableName = super.tableName


}