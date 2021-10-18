/* Copyright (c) 2021 OceanBase and/or its affiliates. All rights reserved.
oblogclient is licensed under Mulan PSL v2.
You can use this software according to the terms and conditions of the Mulan PSL v2.
You may obtain a copy of Mulan PSL v2 at:
         http://license.coscl.org.cn/MulanPSL2
THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
See the Mulan PSL v2 for more details. */

package com.oceanbase.clogproxy.client.util;

import com.oceanbase.clogproxy.client.fliter.DataFilterBase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataFilterUtil {
    /**
     *
     * @param db db name
     * @param tb table name
     * @param cols column names
     * @param dataFilterBase DataFilterBase
     */
    public static void putColNames(String db, String tb, List<String> cols,
                                   DataFilterBase dataFilterBase) {

        if (tb == null) {
            return;
        }
        Map<String, Map<String, List<String>>> dbAndTablePair = dataFilterBase.getRequireMap();
        boolean founded = false;
        for (Map.Entry<String, Map<String, List<String>>> dbEntry : dbAndTablePair.entrySet()) {
            if (db == null || db.equalsIgnoreCase(dbEntry.getKey())) {
                for (Map.Entry<String, List<String>> entry : dbEntry.getValue().entrySet()) {
                    if (tb.equalsIgnoreCase(entry.getKey())) {
                        founded = true;
                        entry.getValue().addAll(cols);
                    }
                }

                if (!founded) {
                    // db is already in the filter, but the table is not, so add the table
                    Map<String, List<String>> tabMap = dbEntry.getValue();
                    tabMap.put(tb, cols);
                    founded = true;
                }
            }
        }

        if (!founded) {
            // db is not in the filter, so add two maps
            Map<String, List<String>> tabMap = new HashMap<String, List<String>>();
            tabMap.put(tb, cols);
            dbAndTablePair.put(db, tabMap);
        }
    }

    /**
     * Use the give db and tb name to retrieve cols list
     * @param db db name
     * @param tb table name
     * @param dataFilterBase DataFilterBase
     * @return cols reference to corresponded db name and table name
     * Note: this function get cols from map in old DataFilter implementation
     */
    public static List<String> getColNamesWithMapping(String db, String tb,
                                                      DataFilterBase dataFilterBase) {
        if (tb == null) {
            return null;
        }
        Map<String, Map<String, List<String>>> dbAndTablePair = dataFilterBase.getReflectionMap();
        Map<String, List<String>> tableAndCols = dbAndTablePair.get(db);
        if (tableAndCols == null) {
            //if we don't find tableAndCols, that mean this dbName appears for the first time,
            //and we use getColNames to require the missing cols and update map;
            tableAndCols = new HashMap<String, List<String>>();
            List<String> cols = getColNames(db, tb, dataFilterBase);
            tableAndCols.put(tb, cols);
            dbAndTablePair.put(db, tableAndCols);
            return cols;
        } else {
            List<String> needCols = tableAndCols.get(tb);
            //we propose the cols can't be null, so we use null to determinate whether the cols we
            //needed has existed in the map
            if (needCols == null) {
                //the cols we needed is missing ,use getColNames to require the missing cols
                List<String> cols = getColNames(db, tb, dataFilterBase);
                tableAndCols.put(tb, cols);
                return cols;
            } else {
                //the cols existed, just return the value.
                return needCols;
            }
        }
    }

    /**
     * Use the give db and tb name to retrieve cols list
     * @param db  db name
     * @param tb  table name
     * @param dataFilterBase DataFilterBase
     * @return cols reference to corresponded db name and table name
     */
    public static List<String> getColNames(String db, String tb, DataFilterBase dataFilterBase) {

        if (tb == null) {
            return null;
        }
        Map<String, Map<String, List<String>>> requireMap = dataFilterBase.getRequireMap();
        for (Map.Entry<String, Map<String, List<String>>> dbEntry : requireMap.entrySet()) {
            StringBuffer buf = new StringBuffer(dbEntry.getKey());
            processStringToRegularExpress(buf);
            if (db == null || db.toLowerCase().matches(buf.toString().toLowerCase())) {
                for (Map.Entry<String, List<String>> entry : dbEntry.getValue().entrySet()) {
                    buf = new StringBuffer(entry.getKey());
                    processStringToRegularExpress(buf);
                    if (tb.toLowerCase().matches(buf.toString().toLowerCase())) {
                        return entry.getValue();
                    }
                }
            }
        }
        return null;
    }

    /**
     * This function will first replace all "." to "\.", then replace all "*" to ".*"
     *
     * @param stringBuffer original string buffer
     */
    public static void processStringToRegularExpress(StringBuffer stringBuffer) {
        int index;
        int beginIndex = 0;
        while (-1 != (index = stringBuffer.indexOf(".", beginIndex))) {
            stringBuffer.insert(index, '\\');
            beginIndex = index + 2;
        }
        beginIndex = 0;
        while (-1 != (index = stringBuffer.indexOf("*", beginIndex))) {
            stringBuffer.insert(index, '.');
            beginIndex = index + 2;
        }
    }

    /**
     * Judge if the given col name exists in col lists
     * @param col col to be judged
     * @param s   cols list
     * @return  true if exists in, else false
     */
    public static boolean isColInArray(final String col, final List<String> s) {
        for (int i = 0; i < s.size(); i++) {
            if ("*".equals(s.get(i)) || col.equalsIgnoreCase(s.get(i))) {
                return true;
            }
        }
        return false;
    }

}