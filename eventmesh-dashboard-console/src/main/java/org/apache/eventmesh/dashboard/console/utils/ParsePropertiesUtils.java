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

package org.apache.eventmesh.dashboard.console.utils;

import java.util.ArrayList;
import java.util.stream.Collectors;


public class ParsePropertiesUtils {

    public static String[] parseProperties(String properties) {
        String[] strings = new String[2];
        ArrayList<String> keyList = new ArrayList<>();
        ArrayList<String> valueList = new ArrayList();
        String[] splits = properties.split("\\n");
        for (String split : splits) {
            if (split.equals("\n") || split.equals("\r") || split.equals("")) {
                continue;
            }
            String[] kvs = split.split("=");
            keyList.add(kvs[0]);
            valueList.add(kvs[1].split("\r")[0]);
        }
        strings[0] = keyList.stream().map(String::valueOf).collect(Collectors.joining(","));
        strings[1] = valueList.stream().map(String::valueOf).collect(Collectors.joining(","));

        return strings;
    }

}
