/*
 * Copyright (c) 2008-2012, Hazel Bilisim Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.query.impl;

import com.hazelcast.query.EntryObject;
import com.hazelcast.query.PredicateBuilder;
import com.hazelcast.query.SqlPredicate;
import com.hazelcast.util.Clock;
import com.hazelcast.util.TestUtil;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashSet;
import java.util.Set;

import static junit.framework.Assert.assertEquals;

@RunWith(com.hazelcast.util.RandomBlockJUnit4ClassRunner.class)
public class MapIndexServiceTest extends TestUtil {

    @Test
    public void testAndWithSingleEntry() throws Exception {
        IndexService mapIndexService = new IndexService();
        mapIndexService.addOrGetIndex("name", false);
        mapIndexService.addOrGetIndex("age", true);
        mapIndexService.addOrGetIndex("salary", true);
        for (int i = 0; i < 20000; i++) {
            Employee employee = new Employee(i + "Name", i % 80, (i % 2 == 0), 100 + (i % 1000));
            mapIndexService.saveEntryIndex(new QueryEntry(null, i, i, employee));
        }
        int count = 1000;
        Set<String> ages = new HashSet<String>(count);
        for (int i = 0; i < count; i++) {
            ages.add(String.valueOf(i));
        }
        final EntryObject entryObject = new PredicateBuilder().getEntryObject();
        final PredicateBuilder predicate = entryObject.get("name").equal("140Name").and(entryObject.get("age").in(ages.toArray(new String[0])));
        long total = Runtime.getRuntime().totalMemory();
        long free = Runtime.getRuntime().freeMemory();
        System.out.println("Used Memory:" + ((total - free) / 1024 / 1024));
        long start = Clock.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            Set<QueryableEntry> results = mapIndexService.query(predicate, null);
            assertEquals(1, results.size());
        }
        System.out.println("Took " + (Clock.currentTimeMillis() - start));
    }

    @Test
    public void testIndex() throws Exception {
        IndexService indexService = new IndexService();
        indexService.addOrGetIndex("name", false);
        indexService.addOrGetIndex("age", true);
        indexService.addOrGetIndex("salary", true);
        for (int i = 0; i < 20000; i++) {
            Employee employee = new Employee(i + "Name", i % 80, (i % 2 == 0), 100 + (i % 1000));
            indexService.saveEntryIndex(new QueryEntry(null, i, i, employee));
        }
        long total = Runtime.getRuntime().totalMemory();
        long free = Runtime.getRuntime().freeMemory();
        System.out.println("Used Memory:" + ((total - free) / 1024 / 1024));
        long start = Clock.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            SqlPredicate predicate = new SqlPredicate("salary=161 and age >20 and age <23");
            Set<QueryableEntry> results = new HashSet<QueryableEntry>(indexService.query(predicate, null));
            assertEquals(10, results.size());
        }
        System.out.println("Took " + (Clock.currentTimeMillis() - start));
    }
}