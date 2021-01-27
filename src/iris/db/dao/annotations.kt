package iris.db.dao

/**
 * @created 27.01.2021
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
@Target(AnnotationTarget.CLASS)
annotation class TableData(val name: String)

@Target(AnnotationTarget.PROPERTY)
annotation class Field(val name: String = "", val type: String = "", val isPrimary: Boolean = false, val defaultValue: String = "")