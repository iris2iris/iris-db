package iris.db.dao

import iris.db.*
import iris.util.plus
import kotlin.reflect.KClass

/**
 * @created 27.01.2021
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
open class ClassIdTable<K, T : Any> private constructor(table: String, factory: ClassIdFactory<K, T>, driver: SqlDriver = DefaultSqlDriver) : ClassTable<T>(table, factory, driver), TableId<K, T>, TableIdPart<K, T> by TableIdAbstract<K, T>(table, factory, driver) {
	constructor(clazz: KClass<T>, data: Data, driver: SqlDriver = DefaultSqlDriver) : this(data.table, ClassIdFactory(clazz, data.properties), driver)
	constructor(clazz: KClass<T>, driver: SqlDriver = DefaultSqlDriver) : this(clazz, buildData(clazz), driver)

	internal class ClassIdFactory<K, T: Any>(clazz: KClass<T>, properties: Array<ClassField>, fields: List<SqlColumn> = properties.map { it.column }, alias: String? = null) : ClassFactory<T>(clazz, properties, fields, alias),
		SqlIdFactory<K, T> {
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
}