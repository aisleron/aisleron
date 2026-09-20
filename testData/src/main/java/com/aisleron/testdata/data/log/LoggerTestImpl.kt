/*
 * Copyright (C) 2026 aisleron.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.aisleron.testdata.data.log

import com.aisleron.domain.log.Logger

class LoggerTestImpl : Logger {
    private var dParameters = LogParameters()
    private var eParameters = LogParameters()
    private var wParameters = LogParameters()

    fun getDParameters(): LogParameters = dParameters

    fun getEParameters(): LogParameters = eParameters

    fun getWParameters(): LogParameters = wParameters


    override fun d(tag: String, message: String) {
        dParameters = LogParameters(
            tag = tag,
            message = message
        )
    }

    override fun e(tag: String, message: String, throwable: Throwable?) {
        eParameters = LogParameters(
            tag = tag,
            message = message,
            throwable = throwable
        )
    }

    override fun w(tag: String, message: String, throwable: Throwable?) {
        wParameters = LogParameters(
            tag = tag,
            message = message,
            throwable = throwable
        )
    }

    data class LogParameters(
        val tag: String = "",
        val message: String = "",
        val throwable: Throwable? = null
    )
}