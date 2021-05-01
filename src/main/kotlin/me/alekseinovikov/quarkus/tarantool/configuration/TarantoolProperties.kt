package me.alekseinovikov.quarkus.tarantool.configuration

import org.eclipse.microprofile.config.inject.ConfigProperty
import javax.inject.Singleton

@Singleton
class TarantoolProperties {

    @ConfigProperty(name = "tarantool.username", defaultValue = "admin")
    lateinit var userName: String

    @ConfigProperty(name = "tarantool.password", defaultValue = "admin")
    lateinit var password: String

    @ConfigProperty(name = "tarantool.host", defaultValue = "localhost")
    lateinit var host: String

    @ConfigProperty(name = "tarantool.port", defaultValue = "3301")
    lateinit var port: Integer

}