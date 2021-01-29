package iris.db

import iris.db.QuerySelect.QueryPart
import iris.util.plus

/**
 * @created 09.01.2021
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */

abstract class TableAbstract<T>(val table: String, val factory: SqlFactory<T>, val driver: SqlDriver = DefaultSqlDriver) : Table<T> {

	override fun creator(id: Any?): Creator<T> {
		return factory.creator(id)
	}

	override fun selectOne(): T? {
		return selectOne(where = null as String?)
	}

	override fun selectOne(fields: QueryPart, where: QueryPart?, order: QueryPart?, start: Int): T? {
		return driver.singleSelect(QuerySelect(table, fields, order, start, 1, where), creator(fields) )
	}

	override fun selectOne(fields: String, where: String?, order: String?, start: Int): T? {
		return driver.singleSelect(QuerySelect(table, fields, order, start, 1, where), creator(fields) )
	}

	override fun selectList(fields: QueryPart, where: QueryPart?, order: QueryPart?, start: Int, amount: Int): List<T> {
		return driver.multiSelect(QuerySelect(table, fields, order, start, amount, where), creator(fields) )
	}

	override fun selectList(fields: String, where: String?, order: String?, start: Int, amount: Int): List<T> {
		return driver.multiSelect(QuerySelect(table, fields, order, start, amount, where), creator(fields) )
	}

	override fun walkList(fields: QueryPart, where: QueryPart?, order: QueryPart?, start: Int, amount: Int, processor: (T?) -> Boolean) {
		return driver.multiSelectWalk(QuerySelect(table, fields, order, start, amount, where), creator(fields), processor)
	}

	override fun walkList(fields: String, where: String?, order: String?, start: Int, amount: Int, processor: (T?) -> Boolean) {
		return driver.multiSelectWalk(QuerySelect(table, fields, order, start, amount, where), creator(fields), processor)
	}

	override fun getByItem(item: T) : T? {
		val sb = StringBuilder("SELECT * FROM ") + table + " WHERE "; factory.primaryPart(sb, item)
		return driver.singleSelect(sb.toString(), factory.creator("*"))
	}

	/////////////////////////

	override fun save(item: T, ignore: Boolean): Number {
		val sql = StringBuilder("INSERT")
		if (ignore)
			sql + " IGNORE"
		sql + " INTO " + table + " SET "; factory.insertPart(sql, item)
		return driver.saveQuery(sql.toString())
	}

	override fun saveOrAffect(item: T, ignore: Boolean): SqlDriver.SaveOrAffect {
		val sql = StringBuilder("INSERT")
		if (ignore)
			sql + " IGNORE"
		sql + " INTO " + table + " SET "; factory.insertPart(sql, item)
		return driver.saveOrAffectQuery(sql.toString())
	}

	override fun saveItems(items: Collection<T>, ignore: Boolean): Int {
		if (items.isEmpty()) return 0
		return driver.simpleQuery(saveItemsPart(items, ignore).toString())
	}

	private fun saveItemsPart(items: Collection<T>, ignore: Boolean): StringBuilder {
		val sb = StringBuilder("INSERT ")
		if (ignore)
			sb.append(" IGNORE")
		sb + " INTO " + table
		sb + "("; factory.fieldNamesPart(sb) + ") VALUES"
		val ind = sb.length
		for (item in items) {
			sb + ",("; factory.insertValuesShort(sb, item) + ')'
		}
		sb.setCharAt(ind, ' ')
		return sb
	}

	override fun saveOrUpdateItem(item: T, ignore: Boolean, fieldsToUpdate: List<String>?): SqlDriver.SaveOrAffect {
		val sb = StringBuilder("INSERT ") + (if (ignore) "IGNORE " else "") + "INTO " + table + " SET "; factory.insertPart(sb, item)
		sb + " ON DUPLICATE KEY UPDATE "
		factory.duplicatePart(sb, fieldsToUpdate)

		return driver.saveOrAffectQuery(sb.toString())
	}

	override fun saveOrUpdateItems(items: Collection<T>, ignore: Boolean, fieldsToUpdate:List<String>?): Int {
		if (items.isEmpty()) return 0
		val sb = saveItemsPart(items, ignore)
		sb + " ON DUPLICATE KEY UPDATE "
		factory.duplicatePart(sb, fieldsToUpdate)
		return driver.simpleQuery(sb.toString())
	}

	////////////////////

	override fun update(item: T, fields: List<String>?): Int {
		val sql = StringBuilder("UPDATE ") + table + " SET "; factory.updatePart(sql, item) + " WHERE "; factory.primaryPart(sql, item)
		return driver.simpleQuery(sql.toString())
	}

	////////////////////

	override fun updateField(item: T, fieldName: String): Int {
		val sb = StringBuilder("UPDATE ") + SqlData.fieldEscape(table) + " SET "; factory.updatePart(sb, item, listOf(fieldName)) + " WHERE "; factory.primaryPart(sb, item)
		return driver.simpleQuery(sb.toString())
	}

	override fun setFieldsByCondition(cond: QueryPart, item: T): Int {
		val sql = StringBuilder("UPDATE ") + SqlData.fieldEscape(table) + " SET "; factory.updatePart(sql, item) +
				" WHERE "; cond.joinTo(sql)
		return driver.simpleQuery(sql.toString())
	}

	override fun changeAmountByItem(item: T, field: String, amount: Int) : Int {
		if (amount == 0)
			return 0
		val sb = StringBuilder("UPDATE ") + table + " SET " + SqlData.fieldEscape(field) + " = " + SqlData.fieldEscape(field) + " "; sign(sb, amount) + " WHERE "; factory.primaryPart(sb, item)
		return driver.simpleQuery(sb.toString())
	}

	override fun changeAmountByItem(item: T, field: String, amount: Double) : Int {
		if (amount == 0.0)
			return 0
		val sb = StringBuilder("UPDATE ") + table + " SET " + SqlData.fieldEscape(field) + " = " + SqlData.fieldEscape(field) + " "; sign(sb, amount) + " WHERE "; factory.primaryPart(sb, item)
		return driver.simpleQuery(sb.toString())
	}

	protected fun sign(sb: StringBuilder, value: Double): StringBuilder {
		if (value >= 0)
			sb.append('+')
		sb.append(value)
		return sb
	}

	protected fun sign(sb: StringBuilder, value: Int): StringBuilder {
		if (value >= 0)
			sb.append('+')
		sb.append(value)
		return sb
	}
	/////////////////////

	override fun remove(item: T): Int {
		val sb = StringBuilder("DELETE FROM ") + table + " WHERE "; factory.primaryPart(sb, item)
		return driver.simpleQuery(sb.toString())
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false
		other as TableAbstract<*>
		if (table != other.table) return false
		return true
	}

	override fun hashCode(): Int {
		return table.hashCode()
	}

	override val tableName = table
}