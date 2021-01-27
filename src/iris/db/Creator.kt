package iris.db

import java.sql.ResultSet

/**
 * @created 26.12.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */

interface Creator<T> {
	fun create(resultSet: ResultSet): T?
}