package iris.db

import iris.db.QuerySelect.QueryPart

/**
 * @created 08.01.2021
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
interface Table<T> {

	fun selectOne(): T?

	fun selectOne(fields: QueryPart = QuerySelect.allFields, where: QueryPart? = null, order: QueryPart? = null, start: Int = 0): T?

	fun selectOne(fields: String = "*", where: String? = null, order: String? = null, start: Int = 0): T?

	fun selectList(fields: QueryPart = QuerySelect.allFields, where: QueryPart? = null, order: QueryPart? = null, start: Int = 0, amount: Int = 0): List<T>

	fun selectList(fields: String = "*", where: String? = null, order: String? = null, start: Int = 0, amount: Int = 0): List<T>

	fun walkList(fields: QueryPart = QuerySelect.allFields, where: QueryPart? = null, order: QueryPart? = null, start: Int = 0, amount: Int = 0, processor: (T?) -> Boolean)

	fun walkList(fields: String = "*", where: String? = null, order: String? = null, start: Int = 0, amount: Int = 0, processor: (T?) -> Boolean)

	fun getByItem(item: T) : T?

	fun creator(id: Any? = null): Creator<T>
	fun save(item: T, ignore: Boolean = false): Number
	fun saveOrAffect(item: T, ignore: Boolean = false): SqlDriver.SaveOrAffect
	fun saveItems(items: Collection<T>, ignore: Boolean = false): Int

	fun saveOrUpdateItem(item: T, ignore: Boolean = false, fieldsToUpdate: List<String>? = null): SqlDriver.SaveOrAffect
	fun saveOrUpdateItems(items: Collection<T>, ignore: Boolean = false, fieldsToUpdate:List<String>? = null): Int
	fun update(item: T, fields: List<String>? = null): Int
	fun updateField(item: T, fieldName: String): Int
	fun setFieldsByCondition(cond: QueryPart, item: T): Int
	fun changeAmountByItem(item: T, field: String, amount: Int): Int
	fun changeAmountByItem(item: T, field: String, amount: Double): Int
	fun remove(item: T): Int
	override fun equals(other: Any?): Boolean
	override fun hashCode(): Int
	val tableName: String
}

