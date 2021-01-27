package iris.db

import java.io.File
import java.util.*
import java.util.logging.LogManager

/**
 * @created 27.01.2021
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
object TestUtil {

	private const val confPath = "excl/cfg.properties"

	val properties by lazy {
		val props = Properties()
		File(confPath).reader().use { props.load(it) }
		props
	}

	fun init() {
		//initLogger()
	}

	/*private fun initLogger() {
		val ist = this.javaClass.getResourceAsStream("logger.properties")
		LogManager.getLogManager().readConfiguration(ist)
		ist.close()
	}*/
}