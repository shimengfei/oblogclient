/* Copyright (c) 2021 OceanBase and/or its affiliates. All rights reserved.
oblogclient is licensed under Mulan PSL v2.
You can use this software according to the terms and conditions of the Mulan PSL v2.
You may obtain a copy of Mulan PSL v2 at:
         http://license.coscl.org.cn/MulanPSL2
THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
See the Mulan PSL v2 for more details. */

package com.oceanbase.clogproxy.client.connection;

import com.oceanbase.clogproxy.client.config.ClientConf;
import com.oceanbase.clogproxy.client.message.LogMessage;
import com.oceanbase.clogproxy.common.packet.HeaderType;
import io.netty.handler.ssl.SslContext;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static com.oceanbase.clogproxy.common.packet.protocol.LogProxyProto.RuntimeStatus;

public class StreamContext {
    public static class TransferPacket {
        private HeaderType type;
        private LogMessage record;
        private RuntimeStatus status;

        public TransferPacket(LogMessage record) {
            this.type = HeaderType.DATA_CLIENT;
            this.record = record;
        }

        public TransferPacket(RuntimeStatus status) {
            this.type = HeaderType.STATUS;
            this.status = status;
        }

        public HeaderType getType() {
            return type;
        }

        public LogMessage getRecord() {
            return record;
        }

        public RuntimeStatus getStatus() {
            return status;
        }
    }

    private final BlockingQueue<TransferPacket> recordQueue = new LinkedBlockingQueue<>(ClientConf.TRANSFER_QUEUE_SIZE);

    private final ClientStream stream;
    ConnectionParams params;
    private final SslContext sslContext;

    public StreamContext(ClientStream stream, ConnectionParams params, SslContext sslContext) {
        this.stream = stream;
        this.params = params;
        this.sslContext = sslContext;
    }

    public ConnectionParams getParams() {
        return params;
    }

    public SslContext getSslContext() {
        return sslContext;
    }

    public ClientStream stream() {
        return stream;
    }

    public BlockingQueue<TransferPacket> recordQueue() {
        return recordQueue;
    }
}