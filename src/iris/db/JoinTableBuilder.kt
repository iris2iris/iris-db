package iris.db

import java.sql.ResultSet
import iris.util.plus

/**
 * @created 08.01.2021
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */

class JoinTableBuilder(firstTable: JoinTable<*>, alias: String = "t") {

	private class JoinData(val table: JoinTable<*>, val type: String, var alias: String, var fields: List<String>? = null, var on: OnJoin? = null, var onJoin: String? = null)
	class OnJoin(val leftAlias: String, val left: List<SqlColumn>, val rightAlias: String, val right: List<SqlColumn>)

	/*companion object {
		private val firstTableOn = OnJoin("", emptyList(), "", emptyList())
	}*/
	private val parts = mutableListOf(JoinData(firstTable, "", alias))

	infix fun leftJoin(otherTable: JoinTable<*>): JoinTableBuilder {
		return leftJoin(otherTable, "")
	}

	infix fun alias(alias: String): JoinTableBuilder {
		parts.last().alias = alias
		return this
	}

	infix fun on(keyName: String): JoinTableBuilder {
		if (parts.size == 1) throw IllegalStateException("There is only one table yet")
		val last = parts.last()
		if (last.on != null) throw IllegalStateException("ON JOIN part was already initialized")
		last.on = chooseJoinOn(last, keyName) ?: throw IllegalStateException("Cannot find key")
		return this
	}

	private fun chooseJoinOn(part: JoinData, keyName: String): OnJoin? {
		return when (part.type) {
			"LEFT JOIN" -> leftJoinOn(part, keyName)
			"INNER JOIN" -> innerJoinOn(part, keyName)
			"RIGHT JOIN" -> rightJoinOn(part, keyName)
			else -> throw IllegalArgumentException(part.type + " is unknown")
		}
	}

	infix fun onJoinString(onJoinPart: String) {
		parts.last().onJoin = onJoinPart
	}

	private var customFields = false

	fun fields(vararg fields: String): JoinTableBuilder {
		customFields = true
		parts.last().fields = fields.toList()
		return this
	}

	infix fun fields(fields: List<String>): JoinTableBuilder {
		customFields = true
		parts.last().fields = fields
		return this
	}

	fun leftJoin(otherTable: JoinTable<*>, keyName: String): JoinTableBuilder {
		val newAlias = "t" + (parts.size)
		parts += JoinData(otherTable, "LEFT JOIN", newAlias, on = if (keyName.isEmpty()) null else leftJoinOn(parts.last(), keyName))
		return this
	}

	private fun leftJoinOn(until: JoinData, keyName: String): OnJoin? {
		val otherTable = until.table
		for (it in parts) {
			if (it === until) break
			it.table.foreignKeyFor(otherTable, keyName)?.apply { return OnJoin(it.alias, this.first, until.alias, this.second) }
		}
		return null
	}

	private fun rightJoinOn(until: JoinData, keyName: String): OnJoin? {
		val otherTable = until.table
		for (it in parts) {
			if (it === until) break
			otherTable.foreignKeyFor(it.table, keyName)?.apply { return OnJoin(it.alias, this.first, until.alias, this.second) }
		}
		return null
	}

	private fun innerJoinOn(until: JoinData, keyName: String): OnJoin? {
		val otherTable = until.table
		run {
			for (it in parts) {
				if (it === until) break
				it.table.foreignKeyFor(otherTable, keyName)?.apply { return OnJoin(it.alias, this.first, until.alias, this.second) }
			}
			null
		} ?: run {
			for (it in parts) {
				if (it === until) break
				otherTable.foreignKeyFor(it.table, keyName)?.apply { return OnJoin(it.alias, this.first, until.alias, this.second) }
			}
			null
		}
		return null
	}

	infix fun innerJoin(otherTable: JoinTable<*>): JoinTableBuilder {
		return innerJoin(otherTable, "")
	}

	infix fun innerJoin(tableWithKey: Pair<JoinTable<*>, String>): JoinTableBuilder {
		return innerJoin(tableWithKey.first, tableWithKey.second)
	}

	fun innerJoin(otherTable: JoinTable<*>, keyName: String): JoinTableBuilder {
		val newAlias = "t" + (parts.size)
		parts += JoinData(otherTable, "INNER JOIN", newAlias, on = if (keyName.isEmpty()) null else innerJoinOn(parts.last(), keyName))
		return this
	}

	infix fun rightJoin(tableWithKey: Pair<JoinTable<*>, String>): JoinTableBuilder {
		return rightJoin(tableWithKey.first, tableWithKey.second)
	}

	infix fun rightJoin(otherTable: JoinTable<*>): JoinTableBuilder {
		return rightJoin(otherTable, "")
	}

	fun rightJoin(otherTable: JoinTable<*>, keyName: String): JoinTableBuilder {
		val newAlias = "t" + (parts.size)
		parts += JoinData(otherTable, "RIGHT JOIN", newAlias, on = if (keyName.isEmpty()) null else rightJoinOn(parts.last(), keyName))
		return this
	}

	private val cacheQuery by lazy { JoinQueryPart().joinTo(StringBuilder()).toString() }
	private val cacheCreator by lazy {
		val first = parts[0].let { if (it.fields == null) it else null }
		val res = parts.map {
			(if (it == first) it.table.factory() else it.table.factory(it.alias)).creator()
		}.toTypedArray() as Array<Creator<Any?>>
		MultiCreator(res as Array<Creator<Any?>>)
	}

	fun selectList(): List<Array<*>> {
		return selectList(null as String?, null, 0, 0)
	}

	fun selectList(where: String? = null, order: String? = null, start: Int = 0, amount: Int = 0): List<Array<*>> {
		val sb = StringBuilder(cacheQuery)
		where?.apply { sb.append("\nWHERE ") + this }
		order?.apply { sb.append("\nORDER BY ") + this }
		if (amount != 0) {
			sb.append("\nLIMIT ").append(start).append(", ").append(amount)
		}
		return DefaultSqlDriver.multiSelect(sb.toString(), cacheCreator)
	}

	fun selectList(where: QuerySelect.QueryPart? = null, order: QuerySelect.QueryPart? = null, start: Int = 0, amount: Int = 0): List<Array<*>> {
		val sb = StringBuilder(cacheQuery)
		where?.apply { sb.append(" WHERE "); joinTo(sb) }
		order?.apply { sb.append(" ORDER BY "); joinTo(sb) }
		if (amount != 0) {
			sb.append(" LIMIT ").append(start).append(", ").append(amount)
		}
		return DefaultSqlDriver.multiSelect(sb.toString(), cacheCreator)
	}

	fun walkList(processor: (Array<*>?) -> Boolean) {
		walkList(null as String?, null, 0, 0, processor)
	}

	fun walkList(where: QuerySelect.QueryPart? = null, order: QuerySelect.QueryPart? = null, start: Int = 0, amount: Int = 0, processor: (Array<*>?) -> Boolean) {
		val sb = StringBuilder(cacheQuery)
		where?.apply { sb.append(" WHERE "); joinTo(sb) }
		order?.apply { sb.append(" ORDER BY "); joinTo(sb) }
		if (amount != 0) {
			sb.append(" LIMIT ").append(start).append(", ").append(amount)
		}
		println(sb)
		return DefaultSqlDriver.multiSelectWalk(sb.toString(), cacheCreator, processor)
	}

	fun walkList(where: String? = null, order: String? = null, start: Int = 0, amount: Int = 0, processor: (Array<*>?) -> Boolean) {
		val sb = StringBuilder(cacheQuery)
		where?.apply { sb.append("\nWHERE ") + this }
		order?.apply { sb.append("\nORDER BY ") + this }
		if (amount != 0) {
			sb.append("\nLIMIT ").append(start).append(", ").append(amount)
		}
		println(sb)
		DefaultSqlDriver.multiSelectWalk(sb.toString(), cacheCreator, processor)
	}

	fun selectOne(): Array<*>? {
		return selectOne(null as String?, null, 0)
	}

	fun selectOne(where: QuerySelect.QueryPart? = null, order: QuerySelect.QueryPart? = null, start: Int = 0): Array<*>? {
		val sb = StringBuilder(cacheQuery)
		where?.apply { sb.append(" WHERE "); joinTo(sb) }
		order?.apply { sb.append(" ORDER BY "); joinTo(sb) }
		sb.append("\nLIMIT ").append(start).append(", 1")
		println(sb)
		return DefaultSqlDriver.singleSelect(sb.toString(), cacheCreator)
	}

	fun selectOne(where: String? = null, order: String? = null, start: Int = 0): Array<*>? {
		val sb = StringBuilder(cacheQuery)
		where?.apply { sb.append("\nWHERE ") + this }
		order?.apply { sb.append("\nORDER BY ") + this }
		sb.append("\nLIMIT ").append(start).append(", 1")
		println(sb)
		return DefaultSqlDriver.singleSelect(sb.toString(), cacheCreator)
	}

	private class MultiCreator(private val creators: Array<Creator<Any?>>) : Creator<Array<*>> {
		override fun create(resultSet: ResultSet): Array<*> {
			return Array(creators.size) { creators[it].create(resultSet) }
		}
	}

	private inner class JoinQueryPart : QuerySelect.QueryPart {

		override fun <A : Appendable> joinTo(sql: A): A {

			val firstData = parts[0]
			sql + "SELECT "
			if (firstData.fields == null)
				sql + firstData.alias + ".*"
			else {
				val d = firstData
				d.table.factory(d.alias).selectFieldsPart(sql, d.fields)
			}
			for (d in parts) {
				if (d === firstData) continue
				sql + ",\n"; d.table.factory(d.alias).selectFieldsPart(sql, d.fields)
			}
			sql + "\nFROM " + firstData.table.tableName + " AS t"
			for (d in parts) {
				if (d === firstData) continue
				val alias = d.alias
				sql + "\n" + d.type + " " + d.table.tableName + " AS " + alias + " ON "
				val on = d.on
				if (d.onJoin != null) {
					sql + d.onJoin!!
				} else {
					val on = d.on ?: initOn(d)
					var isFirst = true
					for ((i, left) in on.left.withIndex()) {
						val right = on.right[i]
						val rightAlias = on.rightAlias
						val leftAlias = on.leftAlias
						if (isFirst) isFirst = false else sql + " AND "
						sql + leftAlias + "." + left.name + " = " + rightAlias + "." + right.name
					}
				}
			}

			return sql
		}

		private fun initOn(d: JoinData): OnJoin {
			d.on?.let { return it }
			return chooseJoinOn(d, "")?.also { d.on = it } ?: throw IllegalStateException("No default foreign key for table ${d.table}")
		}

		override fun equals(other: Any?): Boolean {
			TODO("Not yet implemented")
		}
	}

}