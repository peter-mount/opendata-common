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

import java.util.Optional;
import java.util.stream.Stream;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author peter
 */
public class CollectorUtilsTest
{

    /**
     * Tests that we do return the last entry in a stream
     */
    @Test
    public void findLast_value_returned()
    {
        Optional<Integer> opt = Stream.of( 1, 2, 3, 4, 5 )
                .collect( CollectorUtils.findLast() );
        assertNotNull( "No optional", opt );
        assertTrue( "No entry returned", opt.isPresent() );
        assertEquals( "Incorrect value", Integer.valueOf( 5 ), opt.get() );
    }

    /**
     * Test we return an empty Optional when stream is empty
     */
    @Test
    public void findLast_no_value_returned()
    {
        // Standard empty stream
        Optional<Integer> opt = Stream.<Integer>empty()
                .collect( CollectorUtils.findLast() );
        assertNotNull( "No optional", opt );
        assertFalse( "Entry returned when not expecting any", opt.isPresent() );
    }

    /**
     * Test we return an empty optional when stream was filtered and no results were collected
     */
    @Test
    public void findLast_no_value_returned_filtered()
    {
        // Standard empty stream
        Optional<Integer> opt = Stream.of( 1, 2, 3, 4, 5 )
                .filter( v -> v > 5 )
                .collect( CollectorUtils.findLast() );
        assertNotNull( "No optional", opt );
        assertFalse( "Entry returned when not expecting any", opt.isPresent() );
    }

}
