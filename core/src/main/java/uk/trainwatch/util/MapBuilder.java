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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;

/**
 * A builder of maps
 *
 * @author peter
 * @param <K>
 * @param <V>
 */
public interface MapBuilder<K, V>
        extends Consumer<V>,
                BiConsumer<K, V>
{

    /**
     * Add a new entry, replacing any existing one
     *
     * @param k key
     * @param v value
     *
     * @return
     */
    MapBuilder<K, V> add( K k, V v );

    /**
     * Add all values.
     *
     * @param k      function to extract the key
     * @param values values to add
     *
     * @return
     */
    MapBuilder<K, V> addAll( Function<V, K> k, V... values );

    /**
     * Add all values. This method will not replace any existing key
     *
     * @param k      function to extract the key
     * @param values values to add
     *
     * @return
     */
    MapBuilder<K, V> addAll( Function<V, K> k, Collection<V> values );

    /**
     * Add all entries from an existing map
     *
     * @param map Map to add
     *
     * @return
     */
    MapBuilder<K, V> addAll( Map<K, V> map );

    /**
     * Adds a value to this builder.
     *
     * @param v Value to add
     *
     * @return
     *
     * @throws IllegalStateException if no keyExtractor has been configured
     */
    MapBuilder<K, V> add( V v );

    /**
     * Add all values.
     *
     * @param values values to add
     *
     * @return
     */
    MapBuilder<K, V> addAll( V... values );

    /**
     * Add all values. This method will not replace any existing key
     *
     * @param values values to add
     *
     * @return
     */
    MapBuilder<K, V> addAll( Collection<V> values );

    /**
     * Make the final map concurrent
     *
     * @return
     */
    MapBuilder<K, V> concurrent();

    /**
     * Make the final map linked
     *
     * @return
     */
    MapBuilder<K, V> linked();

    /**
     * Make the final map unmodifiable.
     *
     * @return
     */
    MapBuilder<K, V> readonly();

    /**
     * Make the final map synchronized.
     * <p>
     * This has no effect if the map is readonly or concurrent
     *
     * @return
     */
    MapBuilder<K, V> _synchronized();

    /**
     * Make the map apply a mapping function on it's keys when performing lookup's, puts etc
     *
     * @param keyMapper key mapper
     *
     * @return
     */
    MapBuilder<K, V> keyMapper( UnaryOperator<K> keyMapper );

    /**
     * Tells the builder how to get the key from the value.
     * <p>
     * Once this has been called you can use the builder as a consumer, or use the {@link #add(java.lang.Object)} method.
     *
     * @param keyExtractor function to extract keys from values
     *
     * @return
     */
    MapBuilder<K, V> key( Function<V, K> keyExtractor );

    /**
     * Build the final map
     *
     * @return
     */
    Map<K, V> build();

    default <A> MapBuilder<K, V> addSupplier( K key, Supplier<A> f )
    {
        return add( key, (V) f );
    }

    default <A> MapBuilder<K, V> addConsumer( K key, Consumer<A> f )
    {
        return add( key, (V) f );
    }

    default <A, B> MapBuilder<K, V> addFunction( K key, Function<A, B> f )
    {
        return add( key, (V) f );
    }

    default <A, B, C> MapBuilder<K, V> addBiFunction( K key, BiFunction<A, B, C> f )
    {
        return add( key, (V) f );
    }

    default <A, B> MapBuilder<K, V> addBiConsumer( K key, BiConsumer<A, B> f )
    {
        return add( key, (V) f );
    }

    /**
     * Add all properties from a properties object
     *
     * @param p           Properties
     * @param keyMapper   function to convert key
     * @param valueMapper function to convert value
     *
     * @return
     */
    default MapBuilder<K, V> addProperties( Properties p, Function<Object, K> keyMapper, Function<Object, V> valueMapper )
    {
        p.forEach( ( k, v ) -> add( keyMapper.apply( k ), valueMapper.apply( v ) ) );
        return this;
    }

    /**
     * Add all properties from a File
     *
     * @param f           File
     * @param keyMapper   function to convert key
     * @param valueMapper function to convert value
     *
     * @return
     *
     * @throws IOException
     */
    default MapBuilder<K, V> addProperties( File f, Function<Object, K> keyMapper, Function<Object, V> valueMapper )
            throws IOException
    {
        try( Reader r = new FileReader( f ) ) {
            return addProperties( r, keyMapper, valueMapper );
        }
    }

    /**
     * Add properties from a Reader
     *
     * @param r           Reader
     * @param keyMapper   function to convert key
     * @param valueMapper function to convert value
     *
     * @return
     *
     * @throws IOException
     */
    default MapBuilder<K, V> addProperties( Reader r, Function<Object, K> keyMapper, Function<Object, V> valueMapper )
            throws IOException
    {
        Properties p = new Properties();
        p.load( r );
        return addProperties( p, keyMapper, valueMapper );
    }

    /**
     * Create a builder of an non-concurrent map.
     *
     * @param <K> type of key
     * @param <V> type of value
     *
     * @return
     */
    static <K, V> MapBuilder<K, V> builder()
    {
        return builder( HashMap::new );
    }

    /**
     * Utility to return an unmodifiable map who's values are the constants of an Enum.
     * The keys used here are the constant names;
     *
     * @param <E> Type of Enum
     * @param c   Class of the enum
     *
     * @return
     */
    static <E extends Enum<E>> Map<String, E> enumLookupMap( Class<E> c )
    {
        return enumLookupMap( Enum::name, c );
    }

    /**
     * Utility to return an unmodifiable map who's values are the constants of an Enum.
     *
     * @param <E>          Type of Enum
     * @param keyExtractor Function to get the key from the enum constant
     * @param c            Class of the enum
     *
     * @return
     */
    static <E extends Enum<E>> Map<String, E> enumLookupMap( Function<E, String> keyExtractor, Class<E> c )
    {
        return MapBuilder.<String, E>builder()
                .readonly()
                .concurrent()
                .key( keyExtractor )
                .addAll( c.getEnumConstants() )
                .build();
    }

    /**
     * Utility to return an unmodifiable map who's values are the constants of an Enum.
     *
     * @param <E>          Type of Enum
     * @param keyExtractor Function to get the key from the enum constant
     * @param keyMapper    Function to transform keys on lookup
     * @param c            Class of the enum
     *
     * @return
     */
    static < E extends Enum<E>> Map<String, E> enumLookupMap( Function<E, String> keyExtractor, UnaryOperator<String> keyMapper, Class<E> c )
    {
        return MapBuilder.<String, E>builder()
                .readonly()
                .concurrent()
                .key( keyExtractor )
                .keyMapper( keyMapper )
                .addAll( c.getEnumConstants() )
                .build();
    }

    static MapBuilder<String, Object> fromJsonObject( JsonObject o )
    {
        MapBuilder<String, Object> b = MapBuilder.<String, Object>builder();
        o.forEach( ( k, v ) -> {
            switch( v.getValueType() ) {
                case STRING:
                    b.add( k, ((JsonString) v).getString() );
                    break;

                case NUMBER:
                    b.add( k, ((JsonNumber) v).bigDecimalValue() );
                    break;

                case OBJECT:
                    b.add( k, fromJsonObject( (JsonObject) v ).build() );
                    break;

                case ARRAY:
                    b.add( k, CollectionBuilder.fromJsonArray( (JsonArray) v ).build() );
                    break;

                case FALSE:
                    b.add( k, false );
                    break;

                case TRUE:
                    b.add( k, true );
                    break;

                case NULL:
                default:
                    break;
            }
        } );
        return b;
    }

    /**
     * Create a builder with a supplied map
     *
     * @param <K>
     * @param <V>
     * @param s   Supplier of a map
     *
     * @return
     */
    static <K, V> MapBuilder<K, V> builder( Supplier<Map<K, V>> s )
    {
        return new MapBuilder<K, V>()
        {
            private boolean concurrent = false;
            private boolean linked = false;
            private boolean readOnly = false;
            private boolean _synchronized = false;
            private UnaryOperator<K> keyMapper;
            private Function<V, K> keyExtractor;

            Map<K, V> map;

            private void assertMap()
            {
                if( map != null ) {
                    throw new IllegalStateException( "Cannot change type once the map is being built" );
                }
            }

            private Map<K, V> getMap()
            {
                if( map == null ) {
                    if( concurrent ) {
                        map = new ConcurrentHashMap<>();
                    }
                    else if( linked ) {
                        map = new LinkedHashMap<>();
                    }
                    else {
                        map = new HashMap<>();
                    }
                }
                return map;
            }

            @Override
            public void accept( K k, V v )
            {
                getMap().put( k, v );
            }

            @Override
            public MapBuilder<K, V> add( K k, V v )
            {
                getMap().put( k, v );
                return this;
            }

            @Override
            public MapBuilder<K, V> addAll( Function<V, K> k, V... values )
            {
                Map<K, V> m = getMap();
                for( V v: values ) {
                    m.putIfAbsent( k.apply( v ), v );
                }
                return this;
            }

            @Override
            public MapBuilder<K, V> addAll( Function<V, K> k, Collection<V> values )
            {
                Map<K, V> m = getMap();
                values.stream().
                        forEach( v -> m.putIfAbsent( k.apply( v ), v ) );
                return this;
            }

            @Override
            public MapBuilder<K, V> addAll( Map<K, V> map )
            {
                Map<K, V> m = getMap();
                map.entrySet().stream().forEach( e -> m.put( e.getKey(), e.getValue() ) );
                return this;
            }

            @Override
            public MapBuilder<K, V> concurrent()
            {
                assertMap();
                concurrent = true;
                return this;
            }

            @Override
            public MapBuilder<K, V> linked()
            {
                assertMap();
                linked = true;
                return this;
            }

            @Override
            public MapBuilder<K, V> readonly()
            {
                assertMap();
                readOnly = true;
                return this;
            }

            @Override
            public MapBuilder<K, V> _synchronized()
            {
                assertMap();
                _synchronized = true;
                return this;
            }

            @Override
            public MapBuilder<K, V> keyMapper( UnaryOperator<K> keyMapper )
            {
                this.keyMapper = keyMapper;
                return this;
            }

            private void assertKeyExtractor()
            {
                if( keyExtractor == null ) {
                    throw new IllegalStateException( "No key extraction function has been defined" );
                }
            }

            @Override
            public MapBuilder<K, V> key( Function<V, K> keyExtractor )
            {
                this.keyExtractor = keyExtractor;
                return this;
            }

            @Override
            public MapBuilder<K, V> add( V v )
            {
                assertKeyExtractor();
                return add( keyExtractor.apply( v ), v );
            }

            @Override
            public void accept( V t )
            {
                assertKeyExtractor();
                add( keyExtractor.apply( t ), t );
            }

            @Override
            public MapBuilder<K, V> addAll( V... values )
            {
                assertKeyExtractor();
                Map<K, V> m = getMap();
                for( V v: values ) {
                    m.putIfAbsent( keyExtractor.apply( v ), v );
                }
                return this;
            }

            @Override
            public MapBuilder<K, V> addAll( Collection<V> values )
            {
                assertKeyExtractor();
                Map<K, V> m = getMap();
                values.stream().
                        forEach( v -> m.putIfAbsent( keyExtractor.apply( v ), v ) );
                return this;
            }

            @Override
            public Map<K, V> build()
            {
                // Special case, if a readonly empty map then return just that
                if( (map == null || map.isEmpty()) && readOnly ) {
                    return Collections.emptyMap();
                }

                Map<K, V> m = getMap();

                if( keyMapper != null ) {
                    m = new KeyTransformMap<>( keyMapper, m );
                }

                // readonly trumps _synchronized but _synchronized is ignored if map is Concurrent
                // Also check for concurrency using interface not the flag here
                if( readOnly ) {
                    m = Collections.unmodifiableMap( m );
                }
                else if( _synchronized && !(map instanceof ConcurrentMap) ) {
                    m = Collections.synchronizedMap( m );
                }

                return m;
            }
        };
    }

}
