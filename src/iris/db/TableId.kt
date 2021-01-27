package iris.db

/**
 * @created 08.01.2021
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */

interface TableId<K, T>: Table<T>, TableIdPart<K, T>

interface TableIdPart<K, T> {
	fun get(id: K): T?
	fun setField(id: K, column: SqlColumn, value: Any?): Int
	fun setFields(id: K, values: Collection<Pair<SqlColumn, Any?>>): Int
	fun removeById(id: K): Int
	fun getByIdList(ids: Collection<K>): List<T>
	fun removeByIdList(ids: Collection<K>): Int
	fun changeAmountById(id: K, column: SqlColumn, amount: Int): Int
	fun changeAmountById(id: K, column: SqlColumn, amount: Double): Int
}