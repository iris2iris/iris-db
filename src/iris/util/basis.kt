package iris.util

/**
 * @created 08.01.2021
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */

inline fun<T>Any?.cast(): T = this as T

inline operator fun Appendable.plus(text: String): Appendable {
	this.append(text)
	return this
}

inline operator fun StringBuilder.plus(text: String): StringBuilder {
	this.append(text)
	return this
}

inline operator fun StringBuilder.plus(text: Char): StringBuilder {
	this.append(text)
	return this
}

inline operator fun Appendable.plus(text: Char): Appendable {
	this.append(text)
	return this
}