package iris.db

import iris.util.plus

/**
 * @created 13.01.2021
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
open class TableIdAbstract<K, T>(table: String, factory: SqlIdFactory<K, T>, driver: SqlDriver = GlobalSqlDriver) : TableAbstract<T>(table, factory, driver),
	TableId<K, T> {
	private val idFactory = factory

	private inner class PrimaryPart(private val key: K) : QuerySelect.QueryPart {
		override fun <A : Appendable> joinTo(a: A): A {
			return idFactory.key2Primary(a, key)
		}

		override fun equals(other: Any?): Boolean {
			TODO("Not yet implemented")
		}
	}

	override fun get(id: K): T? {
		return selectOne(where = PrimaryPart(id))
	}

	override fun setField(id: K, column: SqlColumn, value: Any?): Int {
		val sb = StringBuilder("UPDATE ") + SqlData.fieldEscape(table) + " SET "
		sb + SqlData.fieldEscape(column.name) + " = " + column.toSql(value)

		sb + " WHERE "; idFactory.key2Primary(sb, id)
		return driver.simpleQuery(sb.toString())
	}

	override fun setFields(id: K, values: Collection<Pair<SqlColumn, Any?>>): Int {
		val sb = StringBuilder("UPDATE ") + SqlData.fieldEscape(table) + " SET"
		val len = sb.length
		for ((column, value) in values)
			sb + "," + SqlData.fieldEscape(column.name) + " = " + column.toSql(value)
		sb[len] = ' '
		sb + " WHERE "; idFactory.key2Primary(sb, id)
		return driver.simpleQuery(sb.toString())
	}

	override fun removeById(id: K) : Int {
		val sb = StringBuilder("DELETE FROM ") + table + " WHERE "; idFactory.key2Primary(sb, id)
		return driver.simpleQuery(sb.toString())
	}

	override fun getByIdList(ids: Collection<K>) : List<T> {
		val sb = StringBuilder("SELECT * FROM ") + table + " WHERE "; idFactory.keys2Primary(sb, ids)
		return driver.multiSelect(sb.toString(), idFactory.creator(QuerySelect.allFields))
	}

	override fun removeByIdList(ids: Collection<K>) : Int {
		val sb = StringBuilder("DELETE FROM ") + table + " WHERE "; idFactory.keys2Primary(sb, ids)
		return driver.simpleQuery(sb.toString())
	}

	override fun changeAmountById(id: K, column: SqlColumn, amount: Int): Int {
		if (amount == 0)
			return 0
		val escapedField = SqlData.fieldEscape(column.name)
		val sb = StringBuilder("UPDATE ") + table + " SET " + escapedField + " = " + escapedField + " "; sign(sb, amount) + " WHERE "; idFactory.key2Primary(sb, id)
		return driver.simpleQuery(sb.toString())
	}

	override fun changeAmountById(id: K, column: SqlColumn, amount: Double): Int {
		if (amount == 0.0)
			return 0
		val escapedField = SqlData.fieldEscape(column.name)
		val sb = StringBuilder("UPDATE ") + table + " SET " + escapedField + " = " + escapedField + " "; sign(sb, amount) + " WHERE "; idFactory.key2Primary(sb, id)
		return driver.simpleQuery(sb.toString())
	}
}