package iris.db

import java.sql.Connection

/**
 * @created 26.12.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */

object Database : ConnectionSource {

	private lateinit var cs: ConnectionSource

	fun setConnectionSource(cs: ConnectionSource) {
		this.cs = cs
	}

	override fun getConnection(): Connection {
		return cs.getConnection()
	}

	override fun returnConnection(connection: Connection) {
		cs.returnConnection(connection)
	}
}