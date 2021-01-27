package iris.db

class JoinTableDelegate<T>(override val tableName: String, private val factory: SqlFactory<T>, private val fields: List<SqlColumn>, private val primary: List<SqlColumn> = fields.filter { it.isPrimary }) :
	JoinTable<T> {

	override fun primaryKeys() = primary

	override fun findColumn(name: String): SqlColumn {
		return fields.find { it.name == name } ?: throw IllegalStateException("Field with \"$name\" does not exist")
	}

	override infix fun leftJoin(otherTable: JoinTable<*>): JoinTableBuilder {
		return JoinTableBuilder(this) leftJoin otherTable
	}

	override fun innerJoin(otherTable: JoinTable<*>): JoinTableBuilder {
		return JoinTableBuilder(this) innerJoin otherTable
	}

	override fun rightJoin(otherTable: JoinTable<*>): JoinTableBuilder {
		return JoinTableBuilder(this) rightJoin otherTable
	}

	val foreignKeys = mutableMapOf<Pair<JoinTable<*>, String>, Pair<List<SqlColumn>, List<SqlColumn>>>()

	override fun foreignKeyFor(table: JoinTable<*>, field: String): Pair<List<SqlColumn>, List<SqlColumn>> {
		return foreignKeys.getOrPut(table to field) { pairs(primary, table.primaryKeys()) }
	}

	override infix fun String.referTo(pair: Pair<JoinTable<*>, List<String>>) {
		val keyName = this
		val (table, colNames) = pair
		val myCols = colNames.map(::findColumn)
		val otherCols = table.primaryKeys()
		if (myCols.size != otherCols.size) throw IllegalArgumentException("Primary key size does not match referrers columns amount (${myCols.size})")
		foreignKeys[table to keyName] = pairs(myCols, otherCols)
	}

	private fun pairs(myCols: List<SqlColumn>, otherCols: List<SqlColumn>): Pair<List<SqlColumn>, List<SqlColumn>> {
		if (myCols.size != otherCols.size)
			throw IllegalArgumentException("Columns size does not match. Found [${myCols.joinToString { it.name }}] to [${otherCols.joinToString { it.name }}]")
		for ((i, item) in myCols.withIndex()) {
			val oth = otherCols[i]
			if (oth.name != item.name)
				return myCols to myCols.map { my ->
					otherCols.find { o -> o.name == my.name } ?: throw IllegalArgumentException("Cannot create row data")
				}
		}
		return myCols to otherCols
	}

	override infix fun String.referTo(table: JoinTable<*>) {
		val myCols = listOf(findColumn(this))
		val otherCols = table.primaryKeys().also { if (it.size != 1) throw IllegalArgumentException("Primary size must be 1") }
		val key = table to ""
		val value = myCols to otherCols
		if (foreignKeys.contains(key))
			foreignKeys[table to this] = value
		else
			foreignKeys[key] = value
	}

	override infix fun referTo(table: JoinTable<*>) {
		foreignKeys[table to ""] = pairs(this.primary, table.primaryKeys())
	}

	override fun List<String>.referTo(data: Pair<JoinTable<*>, List<String>>) {
		val myCols = this.map(::findColumn)
		val (table, cols) = data
		val otherCols = cols.map{table.findColumn(it)}
		if (myCols.size != otherCols.size)
			throw IllegalArgumentException("Columns size does not match. Found [${myCols.joinToString { it.name }}] to [${otherCols.joinToString { it.name }}]")

		val value = myCols to otherCols
		foreignKeys[table to ""] = value
	}

	override fun String.referTo(table: JoinTable<*>, keyName: String) {
		val myCols = listOf(findColumn(this))
		val otherCols = table.primaryKeys().also { if (it.size != 1) throw IllegalArgumentException("Primary size must be 1") }
		foreignKeys[table to keyName] = myCols to otherCols
	}

	override fun factory(alias: String?): SqlFactory<*> {
		return if (alias == null) factory else factory.alias(alias)
	}

	override fun joinBuilder(alias: String, fields: List<String>?): JoinTableBuilder {
		return JoinTableBuilder(this, alias).also {
			if (fields!= null)
				it.fields(fields)
		}
	}
}