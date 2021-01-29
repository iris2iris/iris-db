package iris.db.dao

import iris.db.*
import java.sql.ResultSet
import kotlin.reflect.*
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

/**
 * @created 23.01.2021
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
open class ClassTable<T: Any>(table: String, factory: ClassFactory<T>, driver: SqlDriver = DefaultSqlDriver) : TableAbstract<T>(table, factory, driver), JoinTable<T> by JoinTableDelegate<T>(table, factory, factory.fields, factory.primary) {

	constructor(clazz: KClass<T>, data: Data, driver: SqlDriver = DefaultSqlDriver) : this(data.table, ClassFactory(clazz, data.properties), driver)
	constructor(clazz: KClass<T>, driver: SqlDriver = DefaultSqlDriver) : this(clazz, buildData(clazz), driver)

	class Data(val table: String, val properties: Array<ClassField>)

	class ClassField(property: KProperty<*>, val column: SqlColumn) {
		val getter = property.getter
		val setter = (property as? KMutableProperty)?.setter

		override fun toString(): String {
			return column.toString()
		}
	}

	override val tableName = super.tableName

	companion object {

		fun <T: Any>buildData(d: KClass<T>): Data {
			val tableData = d.findAnnotation<TableData>()?: throw IllegalArgumentException("Class does not have Annotation of type TableData")
			val fieldsOrder = getFieldsOrder(d.constructors)
			val fieldsMap = d.memberProperties.associate {it.name to extractFormField(it) }
			val fields = Array(fieldsOrder.size) {
				val field = fieldsOrder[it]
				fieldsMap[field]!!
			}

			val table = if (tableData.name.isBlank()) throw IllegalArgumentException("TableData annotation must have table name") else tableData.name
			return Data(table, fields)
		}

		private fun <T>getFieldsOrder(constructors: Collection<KFunction<T>>): List<String> {
			var best: KFunction<T>? = null
			for (c in constructors)
				if (best == null || c.parameters.size > best.parameters.size)
					best = c
			if (best == null)
				throw IllegalArgumentException("No any constructor")
			return best.parameters.map { it.name!! }
		}

		private fun extractFormField(field: KProperty<*>): ClassField {
			val data = field.findAnnotation<Field>()
			val fieldName = if (data?.name.isNullOrBlank()) field.name else data!!.name

			val dataType = SqlType.convertKotlinType(field.returnType, data)
			val isPrimary = data?.isPrimary == true

			val defaultValue = if (data != null) {
				val value = data.defaultValue
				if (value.isEmpty() || value == "-")
					null
				else
					value
			} else
				null
			val isNullable = field.returnType.isMarkedNullable

			return ClassField(field, SqlColumn(fieldName, dataType, isPrimary, SqlType.kotlinDefault(dataType, defaultValue, isPrimary), isNullable))
		}

	}


	private class ClassCreator<T>(private val builder: KFunction<T>, private val fields: List<SqlColumn>, private val alias: String? = null): Creator<T> {

		private class FieldInfo(val column: SqlColumn, val parameterPosition: Int, var columnPosition: Int)

		private val generatedAlias = alias?.let { alias + '_' }
		private var metadata: Array<FieldInfo>? = null
		private var notNullTest: FieldInfo? = null

		private fun getMetadata(rs: ResultSet): Array<FieldInfo> {
			if (metadata != null) return metadata!!
			synchronized(this) {
				if (metadata != null) return metadata!!
				val metaData = rs.metaData
				val res = Array(fields.size) { FieldInfo(fields[it], it, -1) }
				for (i in 1..metaData.columnCount) {
					val name = metaData.getColumnLabel(i).let { if (generatedAlias == null) it else if (it.startsWith(generatedAlias)) it.substring(generatedAlias.length) else null } ?: continue
					val ind = fields.indexOfFirst { it.name == name }
					if (ind == -1)
						continue
					res[ind].columnPosition = i
				}
				notNullTest = res.find { !it.column.isNullable }
				return res.also { metadata = it }
			}
		}

		override fun create(rs: ResultSet): T? {
			val metadata = getMetadata(rs)
			val notNullObj = notNullTest?. run { rs.getObject(columnPosition).let { it ?: return null }}
			val values = Array(fields.size) { fields[it].defaultValue }
			for (field in metadata) {
				if (field.columnPosition == -1) continue
				values[field.parameterPosition] = field.column.toObject(if (field === notNullTest) notNullObj else rs.getObject(field.columnPosition))
			}
			try {
				return builder.call(*values)
			} catch (e: Exception) {
				println(values.joinToString { it.toString() })
				throw e
			}
		}
	}

	open class ClassFactory<T: Any>(private val classInfo: KClass<T>, private val properties: Array<ClassField>, fields: List<SqlColumn> = properties.map { it.column }, alias: String? = null) : SqlFactoryAbstract<T>(alias, fields) {

		private val builder = (classInfo.primaryConstructor ?: throw IllegalArgumentException("There is no primary constructor"))

		override fun keyExists(item: T, field: String): Boolean {
			return fields.any { it.name == field }
		}

		override fun alias(alias: String?): SqlFactory<T> {
			return ClassFactory(classInfo, properties, fields, alias)
		}

		override fun creator(id: Any?): Creator<T> {
			return ClassCreator(builder, fields, alias)
		}

		override fun getValue(item: T, field: String): Any? {
			return properties.find { field == it.column.name }?.getter?.call(item)
		}
	}
}

