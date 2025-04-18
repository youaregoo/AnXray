/******************************************************************************
 *                                                                            *
 * Copyright (C) 2021 by nekohasekai <sekai@neko.services>                    *
 * Copyright (C) 2021 by Max Lv <max.c.lv@gmail.com>                          *
 * Copyright (C) 2021 by Mygod Studio <contact-shadowsocks-android@mygod.be>  *
 *                                                                            *
 * This program is free software: you can redistribute it and/or modify       *
 * it under the terms of the GNU General Public License as published by       *
 * the Free Software Foundation, either version 3 of the License, or          *
 *  (at your option) any later version.                                       *
 *                                                                            *
 * This program is distributed in the hope that it will be useful,            *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the              *
 * GNU General Public License for more details.                               *
 *                                                                            *
 * You should have received a copy of the GNU General Public License          *
 * along with this program. If not, see <http://www.gnu.org/licenses/>.       *
 *                                                                            *
 ******************************************************************************/

@file:Suppress("SpellCheckingInspection")

package io.nekohasekai.sagernet.ktx

import android.os.Build
import io.nekohasekai.sagernet.SagerNet
import io.nekohasekai.sagernet.bg.VpnService
import io.nekohasekai.sagernet.database.DataStore
import io.nekohasekai.sagernet.fmt.AbstractBean
import io.nekohasekai.sagernet.fmt.LOCALHOST
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.util.NetUtil
import okhttp3.ConnectionSpec
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.Socket

val okHttpClient = OkHttpClient.Builder()
    .followRedirects(true)
    .followSslRedirects(true)
    .connectionSpecs(listOf(ConnectionSpec.CLEARTEXT, ConnectionSpec.RESTRICTED_TLS))
    .build()

private lateinit var proxyClient: OkHttpClient
fun createProxyClient(): OkHttpClient {
    if (!SagerNet.started) {
        if (DataStore.startedProfile == 0L) {
            return okHttpClient
        }
    }

    if (!::proxyClient.isInitialized) {
        proxyClient = okHttpClient.newBuilder().proxy(requireProxy()).build()
    }
    return proxyClient
}


fun requireProxy(): Proxy {
    return if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
        Proxy(Proxy.Type.SOCKS, InetSocketAddress(LOCALHOST, DataStore.socksPort))
    } else {
        Proxy(Proxy.Type.HTTP, InetSocketAddress(LOCALHOST, DataStore.httpPort))
    }
}

fun linkBuilder() = HttpUrl.Builder().scheme("https")

fun HttpUrl.Builder.toLink(scheme: String, appendDefaultPort: Boolean = true): String {
    var url = build()
    val defaultPort = HttpUrl.defaultPort(url.scheme)
    var replace = false
    if (appendDefaultPort && url.port == defaultPort) {
        url = url.newBuilder().port(14514).build()
        replace = true
    }
    return url.toString()
        .replace("${url.scheme}://", "$scheme://").let {
            if (replace) it.replace("${url.host}:14514", "${url.host}:$defaultPort") else it
        }
}

fun String.isIpAddress(): Boolean {
    return NetUtil.isValidIpV4Address(this) || NetUtil.isValidIpV6Address(this)
}

fun String.unwrapHost(): String {
    if (startsWith("[") && endsWith("]")) {
        return substring(1, length - 1).unwrapHost()
    }
    return this
}

fun AbstractBean.wrapUri(): String {
    return if (NetUtil.isValidIpV6Address(finalAddress)) {
        "[$finalAddress]:$finalPort"
    } else {
        "$finalAddress:$finalPort"
    }
}

fun parseAddress(addressArray: ByteArray) = InetAddress.getByAddress(addressArray)
val INET_TUN = InetAddress.getByName(VpnService.PRIVATE_VLAN4_CLIENT)
val INET6_TUN = InetAddress.getByName(VpnService.PRIVATE_VLAN6_CLIENT)

fun mkPort(): Int {
    val socket = Socket()
    socket.reuseAddress = true
    socket.bind(InetSocketAddress(0))
    val port = socket.localPort
    socket.close()
    return port
}

fun ByteBuf.toByteArray(): ByteArray {
    if (!hasArray()) {
        return Unpooled.copiedBuffer(this).array()
    }
    return array()
}

const val IPPROTO_ICMP = 1
const val IPPROTO_ICMPv6 = 58

const val IPPROTO_TCP = 6
const val IPPROTO_UDP = 17