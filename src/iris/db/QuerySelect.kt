package iris.db

/**
 * @created 26.12.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */

class QuerySelect(
	private val table: String,
	private val fields: QueryPart?,
	private val order: QueryPart?,
	private val start: Int,
	private val amount: Int,
	private val condition: QueryPart?
) : Query {
	constructor(
		table: String,
		fields: String?,
		order: String?,
		start: Int,
		amount: Int,
		condition: String?
	) : this(table, fields?.let { QueryPart.StringPart(it) },
		order?.let { QueryPart.StringPart(it) },
		start, amount,
		condition?.let { QueryPart.StringPart(it) }
	)

	companion object {
		val allFields = QueryPart.StringPart("*")
	}

	interface QueryPart {
		fun <A: Appendable>joinTo(a: A): A

		class StringPart(private val part: String) : QueryPart {
			override fun <A : Appendable> joinTo(a: A): A {
				a.append(part); return a
			}

			override fun equals(other: Any?): Boolean {
				if (this === other) return true
				if (javaClass != other?.javaClass) return false
				other as StringPart
				if (part != other.part) return false
				return true
			}

			override fun hashCode(): Int {
				return part.hashCode()
			}
		}
	}

	override fun generate(): String {
		val fields = fields ?: allFields
		val sb = StringBuilder()
		sb.append("SELECT "); fields.joinTo(sb).append(" FROM `").append(table).append("`")
		if (condition != null) {
			sb.append(" WHERE ")
			condition.joinTo(sb)
		}
		if (order != null) {
			sb.append(" ORDER BY ")
			order.joinTo(sb)
		}
		if (amount != 0)
			sb.append(" LIMIT ").append(start).append(',').append(amount)
		return sb.toString()
	}
}