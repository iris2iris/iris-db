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

	val user = Users.get(661079614)
	println(user)


	val spamJoin = (
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
}

/*
inline fun <T>lazy(item: T?, buildItem: () -> T): T {
	return item ?: buildItem()
}

inline fun <T>lazy(item: T?, lock: Any, buildItem: () -> T): T {
	if (item != null)
		return item
	synchronized(lock) {
		if (item != null) return item
		return buildItem()
	}
}*/
