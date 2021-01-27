package iris.db

import iris.util.cast
import iris.util.variant.*
import java.sql.*



class SqlDriver(
	private val connectionSource: ConnectionSource = Database
) {

	fun <T>multiSelectWalk(query: Query, creator: Creator<T>, processor: (item: T?) -> Boolean) {
		return multiSelectWalk(query.generate(), creator, processor)
	}

	fun <T>multiSelectWalk(query: String, creator: Creator<T>, processor: (item: T?) -> Boolean) {
		return statement {
			val res = it.executeQuery(query)
			if (!res.next())
				return@statement
			do {
				val d = creator.create(res)
				if (!processor(d))
					break
			} while (res.next())
		}
	}

	fun <T>multiSelect(query: Query, creator: Creator<T>): List<T> {
		return multiSelect(query.generate(), creator)
	}

	fun <T>multiSelect(query: String, creator: Creator<T>): List<T> {
		return statement {
			val res = it.executeQuery(query)
			if (!res.next())
				return@statement emptyList()
			val arr = ArrayList<T>()
			do {
				creator.create(res)?.apply { arr += this }
			} while (res.next())
			arr
		}
	}

	fun <T>singleSelect(query: Query, creator: Creator<T>): T? {
		return singleSelect(query.generate(), creator)
	}

	fun <T>singleSelect(query: String, creator: Creator<T>): T? {
		return statement {
			try {
				val res = it.executeQuery(query)
				if (!res.next())
					return@statement null
				creator.create(res)
			} catch (e: Exception) {
				println(query)
				throw e
			}
		}
	}

	fun saveQuery(query: String) : Number {
		return statement {
			try {
				it.execute(query, Statement.RETURN_GENERATED_KEYS)
			} catch (e: Exception) {
				println(query)
				throw e
			}
			val rs = it.resultSet
			if (rs == null || !rs.next())
				return@statement 0
			(rs.getObject(1) as Number)
		}
	}

	data class SaveOrAffect(val id: Number, val affected: Int)

	fun saveOrAffectQuery(sql: String) : SaveOrAffect {
		return statement {
			val affected = it.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS)
			val rs = it.resultSet
			if (rs == null || !rs.next())
				SaveOrAffect(0, affected)
			else
				SaveOrAffect(rs.getObject(1).cast(), affected)
		}
	}

	fun simpleQuery(query: String) : Int {
		println(query)
		return statement {
			it.executeUpdate(query)
		}
	}

	fun <T>statement(block: (statement: Statement) -> T): T {
		return transaction {
			statement(it, block)
		}
	}

	fun <T>transaction(block: (connection: Connection) -> T): T {
		val connection = connectionSource.getConnection()
		try {
			return block(connection)
		} finally {
			connectionSource.returnConnection(connection)
		}
	}

	fun <T>statement(connection: Connection, block: (statement: Statement) -> T): T {
		val statement = connection.createStatement()
		val t = block(statement)
		statement.close()
		return t
	}
}

