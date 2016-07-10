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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 *
 * @author peter
 */
public interface ListBuilder<T>
{

    /**
     * Add an entry to the list
     *
     * @param v
     * @return
     */
    ListBuilder<T> add( T v );

    /**
     * Mark the list as readonly
     *
     * @return
     */
    ListBuilder<T> readonly();

    /**
     * Mark the list as synchronized
     *
     * @return
     */
    ListBuilder<T> _synchronized();

    /**
     * Mark the list as concurrent
     *
     * @return
     */
    ListBuilder<T> concurrent();

    /**
     * Create the final list
     *
     * @return
     */
    List<T> build();

    /**
     * Create a new builder
     *
     * @param <T>
     * @return
     */
    static <T> ListBuilder<T> builder()
    {
        return new ListBuilder<T>()
        {
            private boolean readonly;
            private boolean sync;
            private boolean concurrent;
            private List<T> list;
            private boolean built;

            private List<T> getList()
            {
                if( list == null ) {
                    if( concurrent ) {
                        list = new CopyOnWriteArrayList<>();
                    }
                    else {
                        list = new ArrayList<>();
                    }
                }
                else if( built ) {
                    if( concurrent ) {
                        list = new CopyOnWriteArrayList<>( list );
                    }
                    else {
                        list = new ArrayList<>( list );
                    }
                    built = false;
                }
                return list;
            }

            @Override
            public ListBuilder<T> add( T v )
            {
                getList().add( v );
                return this;
            }

            @Override
            public ListBuilder<T> readonly()
            {
                if( sync || concurrent ) {
                    throw new IllegalArgumentException( "Cannot make list read only" );
                }
                readonly = true;
                return this;
            }

            @Override
            public ListBuilder<T> _synchronized()
            {
                if( readonly || concurrent ) {
                    throw new IllegalArgumentException( "Cannot make list synchronized" );
                }
                sync = true;
                return this;
            }

            @Override
            public ListBuilder<T> concurrent()
            {
                if( readonly || sync ) {
                    throw new IllegalArgumentException( "Cannot make list concurrent" );
                }
                if( !concurrent && list != null ) {
                    throw new IllegalArgumentException( "Cannot convert list to concurrent" );
                }
                concurrent = true;
                return this;
            }

            @Override
            public List<T> build()
            {
                List<T> l = getList();
                if( readonly ) {
                    l = Collections.unmodifiableList( l );
                }
                else if( sync ) {
                    l = Collections.synchronizedList( l );
                }
                built = true;
                return l;
            }
        };
    }
}
