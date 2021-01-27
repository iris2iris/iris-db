package iris.db

/**
 * @created 26.12.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */

interface Query {
	fun generate(): String
}