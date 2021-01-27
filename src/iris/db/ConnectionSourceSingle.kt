package iris.db

import java.sql.Connection
import java.sql.DriverManager

/**
 * @created 26.12.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */

class ConnectionSourceSingle(
	private var host: String,
	private var user: String? = null,
	private var password: String? = null,
	private var databaseName: String? = null

) : ConnectionSource {

	private var connection: Connection? = null

	init {
		reconnect()
	}
	fun connect(host: String, user: String? = null, password: String? = null, databaseName: String? = null) {
		this.host = host
		this.user = user
		this.password = password
		this.databaseName = databaseName
		reconnect()
	}

	private fun reconnect() {
		connection?.close()
		connection = DriverManager.getConnection(host, user, password)
		databaseName?.let { connection!!.createStatement().execute("USE $it") }
	}

	override fun getConnection(): Connection {
		val connection = this.connection ?: throw IllegalStateException("Connection is not yet established")
		if (connection.isValid(1))
			return connection
		reconnect()
		return this.connection!!
	}

	override fun returnConnection(connection: Connection) {

	}
}