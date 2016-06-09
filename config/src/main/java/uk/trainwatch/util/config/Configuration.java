/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.util.config;

import java.net.URI;
import uk.trainwatch.util.config.impl.MapConfiguration;
import uk.trainwatch.util.config.impl.EmptyConfiguration;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.json.JsonObject;
import uk.trainwatch.util.MapBuilder;

/**
 * Handles common configuration options
 *
 * @author peter
 */
public interface Configuration
        extends Map<String, Object>
{

    String getString( String key );

    Collection<String> getKeys();

    Stream<String> keys();

    default Collection<Object> getCollection( String key )
    {
        return collection( key ).collect( Collectors.toList() );
    }

    default Stream<Object> collection( String key )
    {
        return Stream.empty();
    }

    default URI getURI( String key )
    {
        return getURI( key, () -> null );
    }

    default URI getURI( String key, Supplier<URI> defaultValue )
    {
        String s = getString( key );
        return s == null ? defaultValue.get() : URI.create( s );
    }

    default <E extends Enum<E>> E getEnum( String key, Class<E> clazz )
    {
        return getEnumOrDefault( key, clazz, () -> null );
    }

    default <E extends Enum<E>> E getEnum( String key, Class<E> clazz, E defaultValue )
    {
        return getEnumOrDefault( key, clazz, () -> defaultValue );
    }

    default <E extends Enum<E>> E getEnumOrDefault( String key, Class<E> clazz, Supplier<E> defaultValue )
    {
        String s = getString( key );
        if( s == null || s.isEmpty() ) {
            return defaultValue.get();
        }
        try {
            return Enum.valueOf( clazz, getString( key ) );
        }
        catch( IllegalArgumentException iae ) {
            return defaultValue.get();
        }
    }

    default String getString( String key, String defaultValue )
    {
        return getString( key, () -> defaultValue );
    }

    default String getString( String key, Supplier<String> defaultValue )
    {
        String s = getString( key );
        return s == null ? defaultValue.get() : s;
    }

    default Configuration getConfiguration( String key )
    {
        return getConfiguration( key, EmptyConfiguration.INSTANCE );
    }

    default Configuration getConfiguration( String key, Configuration defaultValue )
    {
        return getConfiguration( key, () -> defaultValue );
    }

    @SuppressWarnings("unchecked")
    default Configuration getConfiguration( String key, Supplier<Configuration> defaultValue )
    {
        Object o = get( key );
        if( o instanceof Configuration ) {
            return (Configuration) o;
        }
        if( o instanceof JsonObject ) {
            return new MapConfiguration( MapBuilder.fromJsonObject( (JsonObject) o ).build() );
        }
        if( o instanceof Map ) {
            return new MapConfiguration( ((Map<String, Object>) o) );
        }
        return defaultValue.get();
    }

    default boolean getBoolean( String key )
    {
        return getBoolean( key, () -> false );
    }

    default boolean getBoolean( String key, boolean defaultValue )
    {
        return getBoolean( key, () -> defaultValue );
    }

    default boolean getBoolean( String key, Supplier<Boolean> defaultValue )
    {
        String s = getString( key );
        return s == null ? defaultValue.get() : Boolean.valueOf( getString( key ) );
    }

    default int getInt( String key )
    {
        return getInt( key, 0 );
    }

    default int getInt( String key, int defaultValue )
    {
        return getInt( key, () -> defaultValue );
    }

    default int getInt( String key, Supplier<Integer> defaultValue )
    {
        String s = getString( key );
        return s == null || s.isEmpty() ? defaultValue.get() : Integer.parseInt( s );
    }

    default long getLong( String key )
    {
        return getInt( key, 0 );
    }

    default long getLong( String key, long defaultValue )
    {
        return getLong( key, () -> defaultValue );
    }

    default long getLong( String key, Supplier<Long> defaultValue )
    {
        String s = getString( key );
        return s == null || s.isEmpty() ? defaultValue.get() : Long.parseLong( s );
    }

    default double getDouble( String key )
    {
        return getInt( key, 0 );
    }

    default double getDouble( String key, double defaultValue )
    {
        return getDouble( key, () -> defaultValue );
    }

    default double getDouble( String key, Supplier<Double> defaultValue )
    {
        String s = getString( key );
        return s == null || s.isEmpty() ? defaultValue.get() : Double.parseDouble( s );
    }

    /**
     * Get a value passing it to a mapping function if not null
     *
     * @param <T>
     * @param key
     * @param f
     *
     * @return
     */
    default <T> T get( String key, Function<String, T> f )
    {
        return get( key, f, () -> null );
    }

    /**
     * Get a value passing it to a mapping function if not null and then if the final result is null then return the output of the supplier.
     *
     * @param <T>
     * @param key
     * @param f
     * @param defaultValue
     *
     * @return
     */
    default <T> T get( String key, Function<String, T> f, Supplier<T> defaultValue )
    {
        T v = null;
        String s = getString( key );
        if( s != null ) {
            v = f.apply( s );
        }
        return v == null ? defaultValue.get() : v;
    }
}
