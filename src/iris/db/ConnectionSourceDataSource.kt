package iris.db

import java.sql.Connection
import javax.sql.DataSource

/**
 * @created 26.12.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
class ConnectionSourceDataSource : ConnectionSource {

	private var dataSource: DataSource? = null

	fun setDataSource(dataSource: DataSource) {
		this.dataSource = dataSource
	}

	override fun getConnection(): Connection {
		return dataSource?.connection ?: throw IllegalStateException("No dataSource established")

	}

	override fun returnConnection(connection: Connection) {
		connection.close()
	}
}