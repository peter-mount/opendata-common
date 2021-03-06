/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.util;

import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.Temporal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import javax.json.JsonWriter;

/**
 * A set of utility methods to support JSR353
 * <p>
 * @author Peter T Mount
 */
public class JsonUtils
{

    private static final Logger LOG = Logger.getLogger( JsonUtils.class.getName() );

    /**
     * Function to convert a {@link JsonValue} to a {@link JsonArray} or null if it's not valid
     */
    public static final Function<? super JsonValue, JsonArray> getArray = v -> v instanceof JsonArray ? (JsonArray) v : null;

    /**
     * Function to convert a {@link JsonValue} to a {@link JsonObject} or null if it's not valid
     */
    public static final Function<? super JsonValue, JsonObject> getObject = v -> v instanceof JsonObject ? (JsonObject) v : null;

    public static final Function<String, JsonStructure> parseJson = JsonUtils::decode;

    /**
     * Function to parse a {@link String} into a {@link JsonObject} or null if it's not valid
     */
    public static final Function<String, JsonObject> parseJsonObject = parseJson.andThen( getObject );

    /**
     * Function to parse a {@link String} into a {@link JsonArray} or null if it's not valid
     */
    public static final Function<String, JsonArray> parseJsonArray = parseJson.andThen( getArray );

    /**
     * Function to convert a {@link JsonStructure} into a String
     */
    public static final Function<? super JsonStructure, String> toString = JsonUtils::encode;
    public static final Function<JsonObject, String> jsonObjectToString = JsonUtils::encode;

    public static final JsonNumber ZERO = createJsonNumber( BigDecimal.ZERO );
    public static final JsonNumber ONE = createJsonNumber( BigDecimal.ONE );

    /**
     * Obtain a {@link JsonStructure} from a String.
     * <p>
     * @param s String
     * <p>
     * @return JsonStructure
     */
    public static JsonStructure decode( String s )
    {
        if( s == null || s.isEmpty() ) {
            return null;
        }
        final StringReader r = new StringReader( Objects.requireNonNull( s ) );
        try( JsonReader jr = Json.createReader( r ) ) {
            return jr.read();
        }
    }

    /**
     * Convert a {@link JsonStructure} to a String
     * <p>
     * @param s <p>
     * @return
     */
    public static String encode( JsonStructure s )
    {
        final StringWriter w = new StringWriter();
        try( JsonWriter jw = Json.createWriter( w ) ) {
            jw.write( s );
            return w.toString();
        }
    }

    /**
     * Convenience to build an empty JsonArray
     * <p>
     * @return
     */
    public static JsonArray emptyArray()
    {
        return Json.createArrayBuilder().build();
    }

    /**
     * Get the named JsonArray from a JsonObject. Unlike {@link JsonObject#getJsonArray(java.lang.String)} if null would be returned we return an empty instance
     * <p>
     * @param o    JsonObject
     * @param name name
     * <p>
     * @return JsonArray, never null
     */
    public static JsonArray getJsonArray( JsonObject o, String name )
    {
        JsonArray a = o == null ? null : o.getJsonArray( name );
        return a == null ? emptyArray() : a;
    }

    /**
     * Return a stream from a JsonArray.
     * <p>
     * If the array is null or empty then an empty stream is returned.
     * <p>
     * @param <T> type
     * @param a   array
     * <p>
     * @return Stream, never null
     */
    @Deprecated
    public static <T extends JsonValue> Stream<T> stream( JsonArray a )
    {
        if( a == null || a.isEmpty() ) {
            return Stream.empty();
        }
        return (Stream<T>) a.stream();
    }

    /**
     * Return a stream from a JsonArray.
     * <p>
     * If the array is null or empty then an empty stream is returned.
     * <p>
     * @param <T> type
     * @param o   array
     * <p>
     * @return Stream, never null
     */
    @Deprecated
    public static <T extends JsonValue> Stream<T> stream( JsonObject o )
    {
        if( o == null || o.isEmpty() ) {
            return Stream.empty();
        }
        return (Stream<T>) o.values().stream();
    }

    /**
     * Return a stream from a named JsonArray within a JsonObject.
     * <p>
     * If the array is null or empty then an empty stream is returned.
     * <p>
     * @param <T> Type
     * @param o   JsonObject
     * @param n   Name of array
     * <p>
     * @return Stream, never null
     */
    @Deprecated
    public static <T extends JsonValue> Stream<T> stream( JsonObject o, String n )
    {
        return stream( o.getJsonArray( n ) );
    }

    @Deprecated
    public static void forEachJsonArray( JsonArray a, Consumer<? super JsonArray> c )
    {
        a.forEach( o -> {
            if( o instanceof JsonArray ) {
                c.accept( (JsonArray) o );
            }
        } );
    }

    @Deprecated
    public static void forEachJsonArray( JsonObject a, Consumer<? super JsonArray> c )
    {
        a.values()
                .forEach( o -> {
                    if( o instanceof JsonArray ) {
                        c.accept( (JsonArray) o );
                    }
                } );
    }

    @Deprecated
    public static void forEachJsonObject( JsonArray a, Consumer<? super JsonObject> c )
    {
        a.forEach( o -> {
            if( o instanceof JsonObject ) {
                c.accept( (JsonObject) o );
            }
        } );
    }

    @Deprecated
    public static void forEachJsonObject( JsonObject a, Consumer<? super JsonObject> c )
    {
        a.values()
                .forEach( o -> {
                    if( o instanceof JsonObject ) {
                        c.accept( (JsonObject) o );
                    }
                } );
    }

    /**
     * Return a value from a {@link JsonObject} allowing for nulls.
     * <p>
     * If the map contains null, an empty ("") string or a value that cannot be converted from a {@link JsonNumber} then the default value is returned.
     * <p>
     * If the value is a {@link JsonArray} or {@link JsonObject} then this will also return the default.
     *
     * @param <T>          Required type
     * @param o            {@link JsonObject}
     * @param n            key
     * @param num          mapping function for {@link JsonNumber} to T
     * @param str          mapping function for a String to T
     * @param t            supplier for value for boolean true
     * @param f            supplier for value for boolean false
     * @param defaultValue supplier of default value
     *
     * @return value
     */
    public static <T> T get( JsonObject o, String n, Function<JsonNumber, T> num, Function<String, T> str, Supplier<T> t, Supplier<T> f,
                             Supplier<T> defaultValue )
    {
        JsonValue v = o.get( n );
        if( v == null ) {
            return defaultValue.get();
        }
        switch( v.getValueType() ) {
            case NUMBER:
                return num.apply( (JsonNumber) v );

            case STRING:
                String s = ((JsonString) v).getString();
                if( s.isEmpty() ) {
                    return defaultValue.get();
                }
                try {
                    return str.apply( s );
                }
                catch( NumberFormatException ex ) {
                    LOG.log( Level.INFO, ex, () -> "" + s );
                    return defaultValue.get();
                }

            case TRUE:
                return t.get();

            case FALSE:
                return f.get();

            case NULL:
            case ARRAY:
            case OBJECT:
            default:
                return defaultValue.get();
        }
    }

    public static int getInt( JsonObject o, String n )
    {
        return getInt( o, n, () -> 0 );
    }

    public static Integer getInt( JsonObject o, String n, Integer defaultValue )
    {
        return getInt( o, n, () -> defaultValue );
    }

    public static Integer getInt( JsonObject o, String n, Supplier<Integer> defaultValue )
    {
        return get( o, n, JsonNumber::intValue, Integer::parseInt, () -> 1, () -> 0, defaultValue );
    }

    public static long getLong( JsonObject o, String n )
    {
        return getLong( o, n, () -> 0L );
    }

    public static Long getLong( JsonObject o, String n, Long defaultValue )
    {
        return getLong( o, n, () -> defaultValue );
    }

    public static Long getLong( JsonObject o, String n, Supplier<Long> defaultValue )
    {
        return get( o, n, JsonNumber::longValue, Long::parseLong, () -> 1L, () -> 0L, defaultValue );
    }

    public static double getDouble( JsonObject o, String n )
    {
        return getDouble( o, n, () -> 0.0 );
    }

    public static Double getDouble( JsonObject o, String n, Double defaultValue )
    {
        return getDouble( o, n, () -> defaultValue );
    }

    public static Double getDouble( JsonObject o, String n, Supplier<Double> defaultValue )
    {
        return get( o, n, JsonNumber::doubleValue, Double::parseDouble, () -> 1.0, () -> 0.0, defaultValue );
    }

    public static BigDecimal getBigDecimal( JsonObject o, String n )
    {
        return getBigDecimal( o, n, () -> BigDecimal.ZERO );
    }

    public static BigDecimal getBigDecimal( JsonObject o, String n, Supplier<BigDecimal> defaultValue )
    {
        return get( o, n, JsonNumber::bigDecimalValue, BigDecimal::new, () -> BigDecimal.ONE, () -> BigDecimal.ZERO, defaultValue );
    }

    public static BigInteger getBigInteger( JsonObject o, String n )
    {
        return getBigInteger( o, n, () -> BigInteger.ZERO );
    }

    public static BigInteger getBigInteger( JsonObject o, String n, Supplier<BigInteger> defaultValue )
    {
        return get( o, n, JsonNumber::bigIntegerValue, BigInteger::new, () -> BigInteger.ONE, () -> BigInteger.ZERO, defaultValue );
    }

    public static String getString( JsonValue v )
    {
        String s = null;
        if( v != null && v.getValueType() != JsonValue.ValueType.NULL ) {
            s = v.toString();
            if( s.length() > 1 && s.startsWith( "\"" ) && s.endsWith( "\"" ) ) {
                s = s.substring( 1, s.length() - 1 );
            }
            s = s.trim();
        }
        return s;
    }

    public static String getString( JsonObject o, String n )
    {
        return getString( o, n, null );
    }

    public static String getString( JsonObject o, String n, String defaultValue )
    {
        JsonValue v = o.get( n );
        if( v == null ) {
            return defaultValue;
        }
        switch( v.getValueType() ) {
            case NUMBER:
                JsonNumber jn = (JsonNumber) v;
                return jn.toString();
            case STRING:
                JsonString s = (JsonString) v;
                return s.getString();
            case TRUE:
                return "true";
            case FALSE:
                return "false";
            case NULL:
                return defaultValue;
            default:
                return v.toString();
        }
    }

    public static boolean getBoolean( JsonObject o, String n )
    {
        return getBoolean( o, n, false );
    }

    public static Boolean getBoolean( JsonObject o, String n, Boolean defaultValue )
    {
        JsonValue v = o.get( n );
        if( v == null ) {
            return defaultValue;
        }
        switch( v.getValueType() ) {
            case NUMBER:
                JsonNumber jn = (JsonNumber) v;
                return !jn.bigIntegerValue().
                        equals( BigInteger.ZERO );
            case STRING:
                JsonString s = (JsonString) v;
                return Boolean.parseBoolean( s.getString() );
            case TRUE:
                return true;
            case FALSE:
                return false;
            case NULL:
                return defaultValue;
            default:
                return Boolean.parseBoolean( v.toString() );
        }
    }

    public static Date getDate( JsonValue v )
    {
        if( v == null ) {
            return null;
        }
        switch( v.getValueType() ) {
            case NUMBER:
                return new Date( ((JsonNumber) v).longValue() );
            case STRING:
                return Date.valueOf( ((JsonString) v).getString() );
            case TRUE:
            case FALSE:
            case NULL:
                return null;
            default:
                return Date.valueOf( v.toString() );
        }
    }

    public static Date getDate( JsonObject map, String key )
    {
        return getDate( map.get( key ) );
    }

    public static Time getTime( JsonValue v )
    {
        if( v == null ) {
            return null;
        }
        switch( v.getValueType() ) {
            case NUMBER:
                return new Time( ((JsonNumber) v).longValue() );
            case STRING:
                return Time.valueOf( ((JsonString) v).getString() );
            case TRUE:
            case FALSE:
            case NULL:
                return null;
            default:
                return Time.valueOf( v.toString() );
        }
    }

    public static Time getTime( JsonObject map, String key )
    {
        return getTime( map.get( key ) );
    }

    public static Timestamp getTimestamp( JsonObject map, String key )
    {
        String dt = getString( map, key );
        if( dt == null || dt.isEmpty() ) {
            return null;
        }

        try {
            return new Timestamp( Long.parseLong( dt ) );
        }
        catch( NumberFormatException nfe ) {
            // The database can send us time with a timezone offset so strip it out
            // FIXME later account for this or better still implement proper timestamp handling
            int i = dt.indexOf( "+" );
//            if( i == -1 )
//            {
//                i = dt.lastIndexOf( "-" );
//            }
            if( i > -1 ) {
                dt = dt.substring( 0, i );
            }

            // We need a time
            if( !dt.contains( " " ) || !dt.contains( ":" ) ) {
                dt = dt + " 00:00:00";
            }

            try {
                return Timestamp.valueOf( dt );
            }
            catch( Exception e ) {
                LOG.log( Level.SEVERE, "Timestamp " + dt, e );
                return null;
            }
        }
    }

    /**
     * If the JsonObject is not null apply the mapping function to create an object from it.
     * <p>
     * @param <T> Type of final object
     * @param o   JsonObject
     * @param f   mapping function
     * <p>
     * @return resultant object or null if not present.
     */
    public static <T> T computeIfPresent( JsonObject o, Function<JsonObject, T> f )
    {
        return o == null ? null : f.apply( o );
    }

    /**
     * Retrieve a JsonObject from another and if present apply the mapping function to create an object from it.
     * <p>
     * @param <T> Type of final object
     * @param o   JsonObject
     * @param n   name of sub-object
     * @param f   mapping function
     * <p>
     * @return resultant object or null if not present.
     */
    public static <T> T computeIfPresent( JsonObject o, String n, Function<JsonObject, T> f )
    {
        return computeIfPresent( o.getJsonObject( n ), f );
    }

    /**
     * If the JsonObject is not null apply the mapping function to create an object from it.
     * <p>
     * @param <T> Type of final object
     * @param o   JsonObject
     * @param s   supplier of the mapping function
     * <p>
     * @return resultant object or null if not present.
     */
    public static <T> T computeIfPresent( JsonObject o, Supplier<Function<JsonObject, T>> s )
    {
        return o == null ? null : s.get().
                apply( o );
    }

    /**
     * Retrieve a JsonObject from another and if present apply the mapping function to create an object from it.
     * <p>
     * @param <T> Type of final object
     * @param o   JsonObject
     * @param n   name of sub-object
     * @param s   supplier of the mapping function
     * <p>
     * @return resultant object or null if not present.
     */
    public static <T> T computeIfPresent( JsonObject o, String n, Supplier<Function<JsonObject, T>> s )
    {
        return computeIfPresent( o.getJsonObject( n ), s );
    }

    /**
     * Creates a {@link JsonObjectBuilder} based on the values of an existing {@link JsonObject}
     * <p>
     * @param o object to copy
     * <p>
     * @return builder populated from o
     */
    public static JsonObjectBuilder createObjectBuilder( JsonObject o )
    {
        return merge( Json.createObjectBuilder(), o );
    }

    /**
     * Merges an existing JsonObject into a JsonBuilder
     * <p>
     * @param b builder to add to
     * @param o object to merge
     * <p>
     * @return b
     */
    public static JsonObjectBuilder merge( JsonObjectBuilder b, JsonObject o )
    {
        Objects.requireNonNull( b );
        Objects.requireNonNull( o );
        o.forEach( ( k, v ) -> b.add( k, v ) );
        return b;
    }

    public static LocalTime getLocalTime( JsonObject o, String n )
    {
        JsonValue v = o.get( n );
        if( v == null ) {
            return null;
        }
        switch( v.getValueType() ) {
            case STRING:
                return TimeUtils.getLocalTime( ((JsonString) v).getString() );

            case NUMBER:
                return TimeUtils.getLocalTime( ((JsonNumber) v).longValue() );
            default:
                return null;
        }
    }

    public static LocalDate getLocalDate( JsonObject o, String n )
    {
        String s = getString( o, n );
        return s == null ? null : LocalDate.parse( s );
    }

    public static LocalDateTime getLocalDateTime( JsonObject o, String n )
    {
        String s = getString( o, n );
        return s == null ? null : LocalDateTime.parse( s );
    }

    public static <T extends Enum<T>> T getEnum( Class<T> c, JsonObject o, String n )
    {
        String s = getString( o, n );
        return s == null ? null : Enum.valueOf( c, s );
    }

    public static <T extends Enum<T>> T getEnum( Function<String, T> lookup, JsonObject o, String n )
    {
        String s = getString( o, n );
        return s == null ? null : lookup.apply( s );
    }

    public static <T extends Enum<T>> T[] getEnumArray( Class<T> c, JsonObject o, String n )
    {
        List<T> l = o.getJsonArray( n ).
                stream().
                map( e -> Enum.valueOf( c, ((JsonString) e).getString() ) ).
                collect( Collectors.toList() );

        return l.toArray( (T[]) Array.newInstance( c, l.size() ) );
    }

    /**
     * Returns a {@link JsonArrayBuilder} containing each enum value
     * <p>
     * @param ary <p>
     * @return
     */
    public static JsonArrayBuilder getArray( Enum<?> ary[] )
    {
        JsonArrayBuilder b = Json.createArrayBuilder();
        if( ary != null && ary.length > 0 ) {
            for( Enum<?> a: ary ) {
                b.add( a.toString() );
            }
        }
        return b;
    }

    /**
     * Create a {@link JsonValue{ of a String, handling nulls
     * <p>
     * @param s String
     * <p>
     * @return JsonValue
     */
    public static JsonValue createJsonValue( final String s )
    {
        return s == null ? JsonValue.NULL : createJsonString( s );
    }

    public static JsonNumber createJsonNumber( BigDecimal d )
    {
        return new JsonNumber()
        {
            @Override
            public boolean isIntegral()
            {
                return d.scale() == 0;
            }

            @Override
            public int intValue()
            {
                return d.intValue();
            }

            @Override
            public int intValueExact()
            {
                return d.intValueExact();
            }

            @Override
            public long longValue()
            {
                return d.longValue();
            }

            @Override
            public long longValueExact()
            {
                return d.longValueExact();
            }

            @Override
            public BigInteger bigIntegerValue()
            {
                return d.toBigInteger();
            }

            @Override
            public BigInteger bigIntegerValueExact()
            {
                return d.toBigIntegerExact();
            }

            @Override
            public double doubleValue()
            {
                return d.doubleValue();
            }

            @Override
            public BigDecimal bigDecimalValue()
            {
                return d;
            }

            @Override
            public JsonValue.ValueType getValueType()
            {
                return ValueType.NUMBER;
            }
        };
    }

    /**
     * Create a {@link JsonString}.
     * <p>
     * This is here mainly to support {@link #createJsonValue(java.lang.String)} as the {@link Json} class does not provide a way of generating this.
     * <p>
     * @param s String
     * <p>
     * @return JsonString
     * <p>
     * @throws NullPointerException if s is null
     */
    public static JsonString createJsonString( final String s )
    {
        Objects.requireNonNull( s );
        return new JsonString()
        {

            @Override
            public String getString()
            {
                return s;
            }

            @Override
            public CharSequence getChars()
            {
                return s;
            }

            @Override
            public JsonValue.ValueType getValueType()
            {
                return JsonValue.ValueType.STRING;
            }

        };
    }

    /**
     * Combine two JsonArrayBuilders - used in {@link Stream#reduce(java.lang.Object, java.util.function.BiFunction, java.util.function.BinaryOperator)}
     * operations.
     */
    public static BinaryOperator<JsonArrayBuilder> ARRAY_COMBINER = ( a, b ) -> {
        b.build().forEach( a::add );
        return a;
    };

    /**
     * Create a JsonObjectBuilder from a Map
     *
     * @param args Map. May be null
     *
     * @return JsonObjectBuilder. Never null but may be empty
     */
    public static JsonObjectBuilder createObjectBuilder( Map<String, Object> args )
    {
        JsonObjectBuilder b = Json.createObjectBuilder();
        if( args != null ) {
            args.forEach( ( k, v ) -> add( b, k, v ) );
        }
        return b;
    }

    /**
     * Add an object to a JsonObjectBuilder.
     * <p>
     * This will attempt to resolve the Json type
     *
     * @param b builder
     * @param k key
     * @param v value
     * @param f mapping function
     *
     * @return builder
     *
     * @throws NullPointerException if either builder or key is null.
     */
    public static <T> JsonObjectBuilder add( JsonObjectBuilder b, String k, T v, Function<T, Object> f )
    {
        Objects.requireNonNull( b );
        return v == null ? b.addNull( k ) : add( b, k, f.apply( v ) );
    }

    /**
     * Add an object to a JsonObjectBuilder.
     * <p>
     * This will attempt to resolve the Json type
     *
     * @param b builder
     * @param k key
     * @param v value
     *
     * @return builder
     *
     * @throws NullPointerException if either builder or key is null.
     */
    public static JsonObjectBuilder add( JsonObjectBuilder b, String k, Object v )
    {
        Objects.requireNonNull( b );
        Objects.requireNonNull( k );
        if( v == null ) {
            b.addNull( k );
        }
        else if( v instanceof CharSequence ) {
            b.add( k, String.valueOf( v ) );
        }
        else if( v instanceof Number ) {
            Number n = (Number) v;
            if( v instanceof Integer ) {
                b.add( k, n.intValue() );
            }
            else if( v instanceof Double || v instanceof Float ) {
                b.add( k, n.doubleValue() );
            }
            else if( v instanceof Long ) {
                b.add( k, n.longValue() );
            }
            else if( v instanceof Integer ) {
                b.add( k, n.intValue() );
            }
            else if( v instanceof BigDecimal ) {
                b.add( k, (BigDecimal) v );
            }
            else if( v instanceof BigInteger ) {
                b.add( k, (BigInteger) v );
            }
        }
        else if( v instanceof Boolean ) {
            b.add( k, (Boolean) v );
        }
        else if( v instanceof char[] ) {
            b.add( k, String.valueOf( (char[]) v ) );
        }
        else if( v instanceof JsonArrayBuilder ) {
            b.add( k, (JsonArrayBuilder) v );
        }
        else if( v instanceof JsonObjectBuilder ) {
            b.add( k, (JsonObjectBuilder) v );
        }
        else if( v instanceof Temporal ) {
            b.add( k, v.toString() );
        }
        return b;
    }

    public static <T> JsonArrayBuilder add( JsonArrayBuilder b, T v, Function<T, Object> f )
    {
        Objects.requireNonNull( b );
        return v == null ? b.addNull() : add( b, f.apply( v ) );
    }

    public static JsonArrayBuilder add( JsonArrayBuilder b, Object v )
    {
        Objects.requireNonNull( b );
        if( v == null ) {
            b.addNull();
        }
        else if( v instanceof CharSequence ) {
            b.add( String.valueOf( v ) );
        }
        else if( v instanceof Number ) {
            Number n = (Number) v;
            if( v instanceof Integer ) {
                b.add( n.intValue() );
            }
            else if( v instanceof Double || v instanceof Float ) {
                b.add( n.doubleValue() );
            }
            else if( v instanceof Long ) {
                b.add( n.longValue() );
            }
            else if( v instanceof Integer ) {
                b.add( n.intValue() );
            }
            else if( v instanceof BigDecimal ) {
                b.add( (BigDecimal) v );
            }
            else if( v instanceof BigInteger ) {
                b.add( (BigInteger) v );
            }
        }
        else if( v instanceof Boolean ) {
            b.add( (Boolean) v );
        }
        else if( v instanceof char[] ) {
            b.add( String.valueOf( (char[]) v ) );
        }
        else if( v instanceof JsonArrayBuilder ) {
            b.add( (JsonArrayBuilder) v );
        }
        else if( v instanceof JsonObjectBuilder ) {
            b.add( (JsonObjectBuilder) v );
        }
        else if( v instanceof Temporal ) {
            b.add( v.toString() );
        }
        return b;
    }

    public static JsonObjectBuilder addAll( JsonObjectBuilder a, JsonObjectBuilder b )
    {
        Objects.requireNonNull( a );
        Objects.requireNonNull( b );
        return addAll( a, b.build() );
    }

    public static JsonObjectBuilder addAll( JsonObjectBuilder a, JsonObject b )
    {
        Objects.requireNonNull( a );
        Objects.requireNonNull( b );
        b.forEach( a::add );
        return a;
    }

    public static JsonArrayBuilder addAll( JsonArrayBuilder a, JsonArrayBuilder b )
    {
        Objects.requireNonNull( a );
        Objects.requireNonNull( b );
        return addAll( a, b.build() );
    }

    public static JsonArrayBuilder addAll( JsonArrayBuilder a, JsonArray b )
    {
        Objects.requireNonNull( a );
        Objects.requireNonNull( b );
        b.forEach( a::add );
        return a;
    }

    /**
     * Create a collector that will collect into a JsonArrayBuilder
     *
     * @return
     */
    public static Collector<Object, JsonArrayBuilder, JsonArrayBuilder> collectJsonArray()
    {
        return collectJsonArray( Json::createArrayBuilder );
    }

    /**
     * Create a collector that will collect into a JsonArrayBuilder
     *
     * @param supplier supplier of a JsonArrayBuilder
     *
     * @return
     */
    public static Collector<Object, JsonArrayBuilder, JsonArrayBuilder> collectJsonArray( Supplier<JsonArrayBuilder> supplier )
    {
        return Collector.of( supplier, JsonUtils::add, JsonUtils::addAll );
    }

    /**
     * Create a collector that collects into a JsonObjectBuilder
     *
     * @param <T>         Type to collect
     * @param keyMapper   map the key
     * @param valueMapper map the value
     *
     * @return
     */
    public static <T> Collector<T, JsonObjectBuilder, JsonObjectBuilder> collectJsonObject( Function<? super T, String> keyMapper,
                                                                                            Function<? super T, ? extends Object> valueMapper )
    {
        return collectJsonObject( Json::createObjectBuilder, keyMapper, valueMapper );
    }

    /**
     * Create a collector that collects into a JsonObjectBuilder
     *
     * @param <T>         Type to collect
     * @param supplier    supplies the JsonObjectBuilder
     * @param keyMapper   map the key
     * @param valueMapper map the value
     *
     * @return
     */
    public static <T> Collector<T, JsonObjectBuilder, JsonObjectBuilder> collectJsonObject( Supplier<JsonObjectBuilder> supplier,
                                                                                            Function<? super T, String> keyMapper,
                                                                                            Function<? super T, ? extends Object> valueMapper )
    {
        return Collector.of( supplier, ( a, t ) -> add( a, keyMapper.apply( t ), valueMapper.apply( t ) ), JsonUtils::addAll );
    }

}
