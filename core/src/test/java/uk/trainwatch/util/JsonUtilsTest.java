/*
 * Copyright 2016 peter.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.trainwatch.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.json.JsonArray;
import javax.json.JsonObject;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author peter
 */
public class JsonUtilsTest
{

    /**
     * Test of decode method, of class JsonUtils.
     */
    @Test
    public void collectJsonArray()
    {
        List<Integer> src = Arrays.asList( 1, 2, 3, 4, 5, 6, 7, 8 );
        JsonArray ary = src.stream().collect( JsonUtils.collectJsonArray() ).build();
        assertEquals( src.size(), ary.size() );

        Set<Integer> tmp = new HashSet<>();
        for( int i = 0; i < ary.size(); i++ ) {
            tmp.add( ary.getInt( i ) );
        }
        // Check as JsonArray can have duplicate entries
        assertEquals( src.size(), tmp.size() );

        // Remove all entries from src - should now be empty
        tmp.removeAll( src );
        assertTrue( tmp.isEmpty() );
    }

    @Test
    public void collectJsonMap()
    {
        Map<String, Integer> src = MapBuilder.<String, Integer>builder()
                .add( "a", 1 )
                .add( "b", 2 )
                .add( "c", 3 )
                .add( "d", 4 )
                .add( "e", 5 )
                .build();

        JsonObject o = src.entrySet()
                .stream()
                .collect( JsonUtils.collectJsonObject( Map.Entry::getKey, Map.Entry::getValue ) )
                .build();

        assertEquals( src.size(), o.size() );
        src.forEach( ( k, v ) -> {
            assertTrue( k, o.containsKey( k ) );
            assertEquals( src.get( k ), (Integer) o.getInt( k ) );
        } );

        o.keySet().forEach( k -> {
            Integer i = o.getInt( k );
            assertEquals( k, src.get( k ), i );
        } );

    }
}
