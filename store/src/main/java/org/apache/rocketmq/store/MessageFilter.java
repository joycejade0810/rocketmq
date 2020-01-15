/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.rocketmq.store;

import java.nio.ByteBuffer;
import java.util.Map;

/**
 * 消息过滤API
 * 消息过滤原理：
 * RocketMQ消息过滤方式不同于其他消息中间件，是在订阅时做过滤。
 * 消息发送者在消息发送时如果设置了消息tags属性，存储在消息属性中，先存储commitlog文件中，然后转发到消息消费队列，消息消费队列会用8个字节存储消息tag的hashcode,
 * 之所以不直接存储tag字符串，是因为将ConsumeQueue设计为定长结构，加快加载性能
 * 在Broker端拉取消息时，遍历ConsumeQueue，只对比消息tag的hashcode，如果匹配则返回，否则忽略该消息。
 * ConsumeQueue在收到消息后，同样需要先对消息进行过滤，只是此时比较的是消息tag的值而不是hashcode
 */
public interface MessageFilter {
    /**
     * match by tags code or filter bit map which is calculated when message received
     * and stored in consume queue ext.
     * 根据ConsumeQueue判断消息是够匹配
     * @param tagsCode tagsCode ：消息tag的hashCode
     * @param cqExtUnit extend unit of consume queue：consumequeue条目的扩展属性
     */
    boolean isMatchedByConsumeQueue(final Long tagsCode,
        final ConsumeQueueExt.CqExtUnit cqExtUnit);

    /**
     * match by message content which are stored in commit log.
     * <br>{@code msgBuffer} and {@code properties} are not all null.If invoked in store,
     * {@code properties} is null;If invoked in {@code PullRequestHoldService}, {@code msgBuffer} is null.
     * 根据存储在commitlog文件中的内容判断消息是否匹配
     * @param msgBuffer message buffer in commit log, may be null if not invoked in store.  消息内容，如果为空，返回true
     * @param properties message properties, should decode from buffer if null by yourself. 消息属性，主要用于SQL92过滤模式
     */
    boolean isMatchedByCommitLog(final ByteBuffer msgBuffer,
        final Map<String, String> properties);
}
