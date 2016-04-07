/*
 * Copyright 2014 peter.
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
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * A suite of utilities to compliment {@link Collectors}
 * <p>
 * @author peter
 */
public class CollectorUtils
{

    /**
     * A collector that collects the last element on a stream. The output of this collector is an {@link Optional} as we may not have an entry.
     * 
     * @param <T> Type of value to collect
     *
     * @return Collector
     */
    public static <T> Collector<T, ?, Optional<T>> findLast()
    {
        class V
        {

            Optional opt = Optional.empty();
        }
        return Collector.<T, V, Optional<T>>of( () -> new V(),
                                                ( a, t ) -> a.opt = Optional.ofNullable( t ),
                                                ( a, b ) -> a.opt.isPresent() ? a : b,
                                                a -> a.opt );
    }

}
