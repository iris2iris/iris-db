package iris.db

/**
 * @created 09.01.2021
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */

interface SqlFactory<T> {

	fun alias(alias: String?): SqlFactory<T>
	fun creator(id: Any? = null): Creator<T>

	fun <A: Appendable>selectFieldsPart(a: A, fieldNames: List<String>? = null) : A

	fun <A: Appendable>insertPart(a: A, item: T, fieldNames: List<String>? = null): A

	fun <A: Appendable>updatePart(a: A, item: T, fieldNames: List<String>? = null): A

	fun <A: Appendable>primaryPart(a: A, item: T) : A

	fun <A: Appendable>valuesShort(a: A, item: T, fieldNames: List<String>? = null, onlyExisting: Boolean = true): A

	fun <A: Appendable>insertValuesShort(a: A, item: T, fieldNames: List<String>? = null) : A

	fun <A: Appendable>updateValuesShort(a: A, item: T, fieldNames: List<String>? = null) : A

	fun <A: Appendable>fieldNamesPart(a: A, fieldNames: List<String>? = null): A

	fun <A: Appendable>duplicatePart(a: A, updateFieldNames: List<String>? = null): A
}