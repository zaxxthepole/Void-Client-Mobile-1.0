package com.voidclient.vrelay.examples

import com.voidclient.vrelay.WRelay
import com.voidclient.vrelay.address.WAddress
import com.voidclient.vrelay.codec.CodecRegistry
import com.voidclient.vrelay.config.EnhancedServerConfig
import com.voidclient.vrelay.listener.WRelayPacketListener
import com.voidclient.vrelay.listener.VersionTrackingListener
import com.voidclient.vrelay.util.ServerCompatUtils
import kotlinx.coroutines.runBlocking
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket

object EnhancedServerExample {
    
    @JvmStatic
    fun main(args: Array<String>) {
        println("W Relay - Enhanced Server Support Example")
        println("============================================")

        val protectedServer = WAddress("play.lbsg.net", 19132)

        if (ServerCompatUtils.isProtectedServer(protectedServer)) {
            println("✓ Protected server detected: ${protectedServer.hostName}")

            val tips = ServerCompatUtils.getConnectionTips(protectedServer)
            tips.forEach { tip ->
                println("  💡 $tip")
            }

            val serverInfo = ServerCompatUtils.extractServerInfo(protectedServer.hostName)
            if (serverInfo != null) {
                println("  📋 Server ID: ${serverInfo.serverId}")
                println("  🌐 Domain: ${serverInfo.domain}")
                println("  🔢 Numeric ID: ${serverInfo.isNumericId}")
            }
        }
        
        println()

        val relay = WRelay(
            localAddress = WAddress("0.0.0.0", 19132),
            serverConfig = EnhancedServerConfig.DEFAULT
        )
        
        println("🚀 Starting W Relay...")

        relay.capture(protectedServer) {
            println("📡 W Relay session created")

            listeners.add(VersionTrackingListener { protocol, version ->
                println("🎮 Client connecting with Minecraft $version (Protocol $protocol)")
            })

            listeners.add(object : WRelayPacketListener {
                override fun onDisconnect(reason: String) {
                    println("❌ Disconnected: $reason")
                }
                
                override fun beforeClientBound(packet: BedrockPacket): Boolean {
                    return false
                }
                
                override fun beforeServerBound(packet: BedrockPacket): Boolean {
                    return false
                }
            })
            
            println("🔗 Attempting to connect to protected server...")

            runBlocking {
                try {
                    val result = wRelay.connectToServerAsync {
                        println("✅ Successfully connected to protected server!")
                        println("🎮 You can now connect your Minecraft client to localhost:19132")
                        println("📊 All traffic will be proxied through W Relay")
                    }

                    if (result.isFailure) {
                        println("❌ Failed to connect: ${result.exceptionOrNull()?.message}")
                        println("💡 Try the following:")
                        println("   - Make sure the server is online")
                        println("   - Check the server address and port")
                        println("   - Wait a few minutes and try again (DDoS protection)")
                    }
                } catch (e: Exception) {
                    println("❌ Connection error: ${e.message}")
                }
            }
        }
        
        println("🎯 W Relay is running on localhost:19132")
        println("📱 Connect your Minecraft Bedrock client to localhost:19132")
        println("🔄 Traffic will be relayed to ${protectedServer.hostName}:${protectedServer.port}")
        println()
        println("Press Ctrl+C to stop the relay")

        try {
            Thread.currentThread().join()
        } catch (e: InterruptedException) {
            println("🛑 W Relay stopped")
        }
    }

    fun demonstrateConfigurations() {
        println("Configuration Examples")
        println("=============================")

        val fastRelay = WRelay(serverConfig = EnhancedServerConfig.FAST)
        println("⚡ Fast config - for stable servers")
        println("   Max retries: ${EnhancedServerConfig.FAST.maxRetryAttempts}")
        println("   Initial delay: ${EnhancedServerConfig.FAST.initialRetryDelay}ms")

        val defaultRelay = WRelay(serverConfig = EnhancedServerConfig.DEFAULT)
        println("🔧 Default config - for most servers")
        println("   Max retries: ${EnhancedServerConfig.DEFAULT.maxRetryAttempts}")
        println("   Initial delay: ${EnhancedServerConfig.DEFAULT.initialRetryDelay}ms")

        val aggressiveRelay = WRelay(serverConfig = EnhancedServerConfig.AGGRESSIVE)
        println("🔥 Aggressive config - for problematic servers")
        println("   Max retries: ${EnhancedServerConfig.AGGRESSIVE.maxRetryAttempts}")
        println("   Initial delay: ${EnhancedServerConfig.AGGRESSIVE.initialRetryDelay}ms")
    }

    fun testServerConnectivity(hostname: String, port: Int) = runBlocking {
        val server = WAddress(hostname, port)
        
        println("Testing connectivity to $hostname:$port")
        
        if (ServerCompatUtils.isProtectedServer(server)) {
            println("✓ Protected server detected")
            val config = ServerCompatUtils.getRecommendedConfig(server)
            println("📋 Recommended config: ${config.maxRetryAttempts} retries, ${config.initialRetryDelay}ms delay")
        } else {
            println("ℹ️ Regular Minecraft server")
        }
        
        val relay = WRelay()
        relay.capture(server) {
            runBlocking {
                try {
                    val result = relay.connectToServerAsync {
                        println("✅ Connection successful!")
                    }

                    if (result.isSuccess) {
                        println("🎉 Server is reachable")
                    } else {
                        println("❌ Connection failed: ${result.exceptionOrNull()?.message}")
                    }
                } catch (e: Exception) {
                    println("❌ Test failed: ${e.message}")
                }
            }
        }
    }
}