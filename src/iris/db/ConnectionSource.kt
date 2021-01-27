package iris.db

import java.sql.Connection

/**
 * @created 26.12.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */

interface ConnectionSource {
	fun getConnection(): Connection
	fun returnConnection(connection: Connection)
}