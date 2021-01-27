package iris.db

/**
 * @created 08.01.2021
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */

interface JoinTable<T> {
	fun foreignKeyFor(table: JoinTable<*>, field: String = ""): Pair<List<SqlColumn>, List<SqlColumn>>?
	fun primaryKeys(): List<SqlColumn>
	infix fun leftJoin(otherTable: JoinTable<*>): JoinTableBuilder
	infix fun innerJoin(otherTable: JoinTable<*>): JoinTableBuilder
	infix fun rightJoin(otherTable: JoinTable<*>): JoinTableBuilder
	fun findColumn(name: String): SqlColumn
	fun factory(alias: String? = null): SqlFactory<*>
	fun joinBuilder(alias: String = "t", fields: List<String>? = null): JoinTableBuilder
	val tableName: String

	fun String.referTo(table: JoinTable<*>, keyName: String)
	//infix fun String.referTo(table: JoinTable<*>)
	infix fun String.referTo(table: JoinTable<*>)
	infix fun List<String>.referTo(data: Pair<JoinTable<*>, List<String>>)
	infix fun referTo(table: JoinTable<*>)
	infix fun String.referTo(pair: Pair<JoinTable<*>, List<String>>)
	//fun List<String>.referTo(table: JoinTable<*>, keyName: String)

}