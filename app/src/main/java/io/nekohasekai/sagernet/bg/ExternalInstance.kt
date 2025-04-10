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

package io.nekohasekai.sagernet.bg

import io.nekohasekai.sagernet.database.ProxyEntity
import io.nekohasekai.sagernet.fmt.buildCustomConfig
import io.nekohasekai.sagernet.ktx.Logs
import io.netty.channel.EventLoopGroup
import libv2ray.Libv2ray
import libv2ray.V2RayVPNServiceSupportsSet

class ExternalInstance(
    val supportSet: V2RayVPNServiceSupportsSet,
    profile: ProxyEntity,
    val port: Int,
    override val eventLoopGroup: EventLoopGroup
) : V2RayInstance(profile) {

    override fun init() {
        super.init()

        Logs.d(config.config)
        pluginConfigs.forEach { (_, plugin) ->
            val (_, content) = plugin
            Logs.d(content)
        }
    }

    override fun initInstance() {
        v2rayPoint = Libv2ray.newV2RayPoint(supportSet, false)
    }

    override fun buildConfig() {
        config = buildCustomConfig(profile, port)
    }

}