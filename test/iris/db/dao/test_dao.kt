package iris.db.dao

import iris.db.*
import iris.db.assoc.AssociativeTable
import java.util.*

/**
 * @created 26.01.2021
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
fun main() {
	TestUtil.init()
	Database.setConnectionSource(ConnectionSourceSingle(TestUtil.properties.getProperty("connection-url")))
	DefaultSqlDriver.debug = true
	testDao()
}

object UsersIn: AssociativeTable("cm_user_in") {
	init {
		"id_user" referTo Users
	}
}

object Bans: AssociativeTable("cm_chat_bans") {
	init {
		"id_user" referTo Users
		"id_moderator" referTo Users
	}
}

@TableData("cm_users")
data class User (
	@Field(name = "id_user", isPrimary = true)
	val userId: Int = 0,

	@Field("first_name")
	val firstName: String? = "",

	@Field("last_name")
	val lastName: String? = "",
)

object Users : ClassIdTable<Int, User>(User::class)

@TableData("cm_spam_iris")
data class SpamIris(
	@Field("id_user", isPrimary = true)
	val userId: Int,

	@Field("id_from")
	val fromId: Int,

	val dateAdd: Date?
)

object SpamIrisTable : ClassTable<SpamIris>(SpamIris::class) {
	init {
		this referTo Users
		"id_from" referTo Users
	}
}

fun testDao() {

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
		println("Test joins")
		(
			SpamIrisTable
			leftJoin Users
			leftJoin Users on "id_from"
		).walkList(null as String?, amount = 15) {
			println("{")
			val (spam, user, moderator) = it!!
			println("\tSpam: $spam")
			println("\tUser: $user")
			println("\tModerator: $moderator")
			println("}")
			true
		}
	}

	run {
		println("Test joins")
		val joinTable = (
			Bans
			leftJoin Users
			leftJoin Users on "id_moderator"
			leftJoin UsersIn
		)


		joinTable.walkList(where = "t.id_chat = 1 AND t.id_user > 1", amount = 50) {
			println("{")
			val (ban, user, moderator, isIn) = it!!
			println("\tBan: $ban")
			println("\tUser: $user")
			println("\tВ чате?: $isIn")
			println("\tModerator: $moderator")
			println("}")
			true
		}

		println("---------------\n")

		joinTable.walkList(where = "t.id_chat = 4 AND t.id_user > 1", amount = 50) {
			println("{")
			val (ban, user, moderator, isIn) = it!!
			println("\tBan: $ban")
			println("\tUser: $user")
			println("\tВ чате?: $isIn")
			println("\tModerator: $moderator")
			println("}")
			true
		}
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
			User(1, "Name", "Naname"),
			User(2, "Name2", "Naname2"),
		))
		println(res)
		println("---------------\n")
	}
}