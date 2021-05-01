package me.alekseinovikov.quarkus.tarantool.configuration

import io.quarkus.runtime.QuarkusApplication
import io.quarkus.runtime.annotations.QuarkusMain
import org.intellij.lang.annotations.Language
import org.tarantool.TarantoolClient
import org.tarantool.TarantoolClientConfig
import org.tarantool.TarantoolClientImpl
import org.tarantool.TarantoolSQLOps
import java.util.concurrent.Future

@QuarkusMain
class TarantoolConfiguration(private val properties: TarantoolProperties) : QuarkusApplication {

    private lateinit var client: TarantoolClient

    override fun run(vararg args: String?): Int {
        val config = TarantoolClientConfig().also {
            it.username = properties.userName
            it.password = properties.password
        }

        val address = "${properties.host}:${properties.port}"
        client = TarantoolClientImpl(address, config)
        runShit()

        return 0
    }

    private val syncOps: TarantoolSQLOps<Any, Long, List<Map<String, Any>>> by lazy {
        return@lazy client.sqlSyncOps()
    }

    private val asyncOps: TarantoolSQLOps<Any, Future<Long>, Future<List<Map<String, Any>>>> by lazy {
        return@lazy client.sqlAsyncOps()
    }

    private fun runShit() {
        //language=SQL
        syncOps.update(
            """CREATE TABLE IF NOT EXISTS users(id INTEGER PRIMARY KEY AUTOINCREMENT,  
                     first_name VARCHAR(512) NOT NULL, 
                     last_name VARCHAR(512) NOT NULL) """.trimMargin(),
        )

        //language=SQL
        syncOps.update(
            """REPLACE INTO users(id, first_name, last_name)
            VALUES(1, 'Aleksei', 'Novikov'),
            (2, 'Valeriia', 'Novikova'),
            (3, 'Jesus', 'Christ');""".trimMargin()
        )

        syncOps.update("""DELETE FROM users WHERE id > 3;""")

        val asyncResult = asyncOps.query("SELECT * FROM users;")
        @Language("SQL") val result: List<Map<String, Any>> =
            syncOps.query("SELECT * FROM users;")

        val users = User.from(result)
        val asyncUsers = User.from(asyncResult.get())

        assert(users == asyncUsers)
        println(users)
        println(asyncUsers)
    }

}

data class User(val id: Int, val firstName: String, val lastName: String) {
    companion object {
        fun from(map: Map<String, Any>): User = User(
            id = map["ID"] as Int,
            firstName = map["FIRST_NAME"] as String,
            lastName = map["LAST_NAME"] as String
        )

        fun from(maps: List<Map<String, Any>>): List<User> = maps.map { from(it) }
    }
}