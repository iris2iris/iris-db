package iris.db.assoc

import iris.db.ConnectionSourceSingle
import iris.db.Database
import iris.db.DefaultSqlDriver
import iris.db.TestUtil

/**
 * @created 27.12.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */

fun main() {
	TestUtil.init()
	Database.setConnectionSource(ConnectionSourceSingle(TestUtil.properties.getProperty("connection-url")))
	DefaultSqlDriver.debug = true
	test()
}

object UsersIn: AssociativeTable("cm_user_in") {
	init {
		"id_user" referTo Users
		this referTo Bans
	}
}

object Users : AssociativeIdTable<Int>("cm_users")

object Bans: AssociativeTable("cm_chat_bans") {
	init {
		"id_user" referTo Users
		"id_moderator" referTo Users
		this referTo UsersIn
	}
}

fun test() {

	run {
		println("Test Users.get()")
		val user = Users.get(1)
		println(user)
		println("---------------\n")
	}

	run {
		println("Test Users.selectOne()")
		val user = Users.selectOne()
		println(user)
		println("---------------\n")
	}

	run {
		println("Test Users.selectList()")
		val items = Users.selectList(fields = "id_user, first_name, last_name", where = "id_user < 100000 AND id_user > 0", amount = 10)
		items.forEach(::println)
		println("---------------\n")
	}

	run {
		println("Test Users.getByIdList()")
		val items = Users.getByIdList(listOf(1, 2))
		Users.factory.alias("a")
		items.forEach(::println)
		println("---------------\n")
	}

	run {
		println("Test Users.removeByIdList()")
		val res = Users.removeByIdList(listOf(1, 2))
		println(res)
		println("---------------\n")
	}

	run {
		println("Test Users.saveItems()")
		val res = Users.saveItems(listOf(
			mapOf("first_name" to "Name", "last_name" to "Naname", "id_user" to 1),
			mapOf("first_name" to "Name2", "last_name" to "Naname2", "id_user" to 2)
		))
		println(res)
		println("---------------\n")
	}

	run {
		println("Test join tables")
		val users = (
			Bans.joinBuilder().fields("id_user", "id_moderator", "dateAdd")
			leftJoin UsersIn
			leftJoin Users fields listOf("id_user", "domain")
			leftJoin Users on "id_moderator" fields listOf("domain")
		).selectList(where = "t.id_user > 1", order = "t.id_user ASC", start = 0, amount = 10)

		users.forEach{
			val (ban, isIn, user, moderator) = it
			println('[')
			println("\tBan: $ban")
			println("\tUser: $user")
			println("\tIs in: $isIn")
			println("\tModerator: $moderator")
			println(']')
		}
		println("---------------\n")
	}

}