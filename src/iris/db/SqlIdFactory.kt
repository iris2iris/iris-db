package iris.db

/**
 * @created 09.01.2021
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */

interface SqlIdFactory<K, T> : SqlFactory<T> {
	fun <A: Appendable>key2Primary(a: A, key: K): A
	fun <A: Appendable>keys2Primary(a: A, keys: Collection<K>): A
}