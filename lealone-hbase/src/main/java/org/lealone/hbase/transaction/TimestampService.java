/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.lealone.hbase.transaction;

import java.io.IOException;

import org.lealone.hbase.engine.HBaseConstants;
import org.lealone.hbase.metadata.TimestampServiceTable;
import org.lealone.hbase.util.HBaseUtils;
import org.lealone.message.DbException;

public class TimestampService {
    private static final long TIMESTAMP_BATCH = HBaseUtils.getConfiguration().getLong(HBaseConstants.TRANSACTION_TIMESTAMP_BATCH,
            HBaseConstants.DEFAULT_TRANSACTION_TIMESTAMP_BATCH);

    private final TimestampServiceTable timestampServiceTable;
    private long first;
    private long last;
    private long maxTimestamp;

    public TimestampService(String hostAndPort) {
        try {
            timestampServiceTable = new TimestampServiceTable(hostAndPort);
            first = last = maxTimestamp = timestampServiceTable.getLastMaxTimestamp();
            addBatch();
        } catch (IOException e) {
            throw DbException.convert(e);
        }
    }

    public long first() {
        return first;
    }

    private void addBatch() throws IOException {
        maxTimestamp += TIMESTAMP_BATCH;
        timestampServiceTable.updateLastMaxTimestamp(maxTimestamp);
    }

    public synchronized void reset() {
        try {
            first = last = maxTimestamp = 0;
            timestampServiceTable.updateLastMaxTimestamp(0);
            addBatch();
        } catch (IOException e) {
            throw DbException.convert(e);
        }
    }

    //事务用奇数版本号
    public synchronized long nextOdd() {
        try {
            if (last >= maxTimestamp)
                addBatch();

            long delta;
            if (last % 2 == 0)
                delta = 1;
            else
                delta = 2;

            last += delta;
            return last;
        } catch (IOException e) {
            throw DbException.convert(e);
        }
    }

    //非事务用偶数版本号
    public synchronized long nextEven() {
        try {
            if (last >= maxTimestamp)
                addBatch();

            long delta;
            if (last % 2 == 0)
                delta = 2;
            else
                delta = 1;
            last += delta;
            return last;
        } catch (IOException e) {
            throw DbException.convert(e);
        }
    }

    @Override
    public String toString() {
        return "TimestampService(first: " + first + ", last: " + last + ", max: " + maxTimestamp + ")";
    }
}
