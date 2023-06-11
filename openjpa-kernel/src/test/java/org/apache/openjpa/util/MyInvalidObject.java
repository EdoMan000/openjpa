/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.openjpa.util;

import java.util.Random;

public class MyInvalidObject {
    /**
     * The hashCode() method in Java returns an integer value that <br>
     * represents the unique identifier of an object. <br>
     * The hashCode() method is required to follow certain rules: <br>
     * <p>
     * Consistency: <br>
     * If an object does not change its internal state, <br>
     * calling hashCode() multiple times should consistently return the same value. <br>
     * <p>
     * Equality: <br>
     * If two objects are equal according to the equals() method, <br>
     * their hashCode() values should be the same. <br>
     * However, the reverse is not necessarily true. <br>
     * <p>
     * Uniqueness: <br>
     * Ideally, each distinct object should have a unique hashCode() value, <br>
     * but due to the limited range of integers, collisions may occur. <br>
     * <p>
     * Using random int returns we are violating what was just explained here making <br>
     * it an invalid instance for Object
     */
    @Override
    public int hashCode() {
        Random random = new Random(System.currentTimeMillis());
        return random.nextInt();
    }
}
