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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class Yml2PropertiesUtils {


    public static String castProperties(String yml) {
        List<YmlNode> nodeList = getNodeList(yml);
        return printNodeList(nodeList);
    }

    private static String printNodeList(List<YmlNode> nodeList) {
        StringBuilder sb = new StringBuilder();
        for (YmlNode node : nodeList) {
            if (node.getLast().equals(Boolean.FALSE)) {
                continue;
            }
            if (node.getEmptyLine().equals(Boolean.TRUE)) {
                sb.append("\r\n");
                continue;
            }
            // whether contains annotation
            if (node.getHeadRemark().length() > 0) {
                String s = "# " + node.getHeadRemark();
                sb.append(s).append("\r\n");
                continue;
            }
            if (node.getTailRemark().length() > 0) {
                String s = "# " + node.getTailRemark();
                sb.append(s).append("\r\n");
            }

            String kv = node.getKey() + "=" + node.getValue();
            sb.append(kv).append("\r\n");
        }
        return sb.toString();
    }

    private static List<YmlNode> getNodeList(String yml) {
        String[] lines = yml.split("\n");
        List<YmlNode> nodeList = new ArrayList<>();
        Map<Integer, String> keyMap = new HashMap<>();
        Set<String> keySet = new HashSet<>();
        for (String line : lines) {
            YmlNode node = getNode(line);
            if (node.getKey() != null && node.getKey().length() > 0) {
                int level = node.getLevel();
                if (level == 0) {
                    keyMap.clear();
                    keyMap.put(0, node.getKey());
                } else {
                    int parentLevel = level - 1;
                    String parentKey = keyMap.get(parentLevel);
                    String currentKey = parentKey + "." + node.getKey();
                    keyMap.put(level, currentKey);
                    node.setKey(currentKey);
                }
            }
            keySet.add(node.getKey() + ".");
            nodeList.add(node);
        }

        for (YmlNode each : nodeList) {
            each.setLast(getNodeLast(each.getKey(), keySet));
        }
        return nodeList;
    }

    private static boolean getNodeLast(String key, Set<String> keySet) {
        if (key.isEmpty()) {
            return true;
        }
        key = key + ".";
        int count = 0;
        for (String each : keySet) {
            if (each.startsWith(key)) {
                count++;
            }
        }
        return count == 1;
    }

    private static YmlNode getNode(String line) {
        YmlNode node = new YmlNode();

        node.setEffective(Boolean.FALSE);
        node.setEmptyLine(Boolean.FALSE);
        node.setHeadRemark("");
        node.setKey("");
        node.setValue("");
        node.setTailRemark("");
        node.setLast(Boolean.FALSE);
        node.setLevel(0);

        String trimStr = line.trim();
        if (trimStr.isEmpty()) {
            node.setEmptyLine(Boolean.TRUE);
            return node;
        }

        if (trimStr.startsWith("#")) {
            node.setHeadRemark(trimStr.replaceFirst("#", "").trim());
            return node;
        }

        String[] strs = line.split(":", 2);

        if (strs.length == 0) {
            return node;
        }
        node.setKey(strs[0].trim());

        String value;
        if (strs.length == 2) {
            value = strs[1];
        } else {
            value = "";
        }
        // get the last annotation
        String tailRemark = "";
        if (value.contains(" #")) {
            String[] values = value.split("#", 2);
            if (values.length == 2) {
                value = values[0];
                tailRemark = values[1];
            }
        }
        node.setTailRemark(tailRemark.trim());
        node.setValue(value.trim());

        int level = getNodeLevel(line);
        node.setLevel(level);
        node.setEffective(Boolean.TRUE);
        return node;
    }

    private static int getNodeLevel(String line) {
        if (line.trim().isEmpty()) {
            return 0;
        }
        char[] chars = line.toCharArray();
        int count = 0;
        for (char c : chars) {
            if (c != ' ') {
                break;
            }
            count++;
        }
        return count / 2;
    }

    static class YmlNode {

        /**
         * relation of levels
         */
        private Integer level;
        /**
         * key
         */
        private String key;
        /**
         * value
         */
        private String value;
        /**
         * is empty line or not
         */
        private Boolean emptyLine;
        /**
         * is effective or not
         */
        private Boolean effective;
        /**
         *  head comment or line comment
         */
        private String headRemark;
        /**
         * the last comment
         */
        private String tailRemark;
        /**
         * is last or not
         */
        private Boolean last;

        public Boolean getLast() {
            return last;
        }

        public void setLast(Boolean last) {
            this.last = last;
        }

        public Integer getLevel() {
            return level;
        }

        public void setLevel(Integer level) {
            this.level = level;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public Boolean getEmptyLine() {
            return emptyLine;
        }

        public void setEmptyLine(Boolean emptyLine) {
            this.emptyLine = emptyLine;
        }

        public Boolean getEffective() {
            return effective;
        }

        public void setEffective(Boolean effective) {
            this.effective = effective;
        }

        public String getHeadRemark() {
            return headRemark;
        }

        public void setHeadRemark(String headRemark) {
            this.headRemark = headRemark;
        }

        public String getTailRemark() {
            return tailRemark;
        }

        public void setTailRemark(String tailRemark) {
            this.tailRemark = tailRemark;
        }

    }

}

