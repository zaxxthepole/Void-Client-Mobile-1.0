package com.voidclient.vrelay.listener

import com.voidclient.vrelay.WRelaySession
import com.voidclient.vrelay.address.WAddress
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket
import org.cloudburstmc.protocol.bedrock.packet.TransferPacket

@Suppress("MemberVisibilityCanBePrivate")
class TransferPacketListener(
    val wRelaySession: WRelaySession
) : WRelayPacketListener {

    override fun beforeServerBound(packet: BedrockPacket): Boolean {
        if (packet is TransferPacket) {
            val remoteAddress = WAddress(packet.address, packet.port)
            val localAddress = wRelaySession.wRelay.localAddress
            wRelaySession.wRelay.remoteAddress = remoteAddress
            wRelaySession.clientBoundImmediately(TransferPacket().apply {
                address = localAddress.hostName
                port = localAddress.port
            })

            wRelaySession.wRelay.wRelaySession = null
            return true
        }
        return false
    }

}