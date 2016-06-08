/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.util;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 *
 * @author Peter T Mount
 */
public class Predicates
{

    /**
     * Always true
     */
    public static final Predicate<?> TRUE = o -> true;
    /**
     * Always false
     */
    public static final Predicate<?> FALSE = o -> false;

    /**
     * Equivalent to {@code o-> o instanceof class}
     * <p>
     * @param <T>
     * @param clazz <p>
     * @return
     */
    public static final <T> Predicate<T> instanceOf( Class clazz )
    {
        return o -> o == null ? null : clazz.isAssignableFrom( o.getClass() );
    }

    public static final <T> Predicate<T> and( Predicate<T> a, Predicate<T> b )
    {
        return a == null ? b : b == null ? a : a.and( b );
    }

    public static final <T> Predicate<T> and( Predicate<T>... p )
    {
        return Stream.of( p ).reduce( Predicates::and ).orElse( v -> false );
    }

    public static final <T> Predicate<T> or( Predicate<T> a, Predicate<T> b )
    {
        return a == null ? b : b == null ? a : a.or( b );
    }

    public static final <T> Predicate<T> or( Predicate<T>... p )
    {
        return Stream.of( p ).reduce( Predicates::or ).orElse( v -> false );
    }

    /**
     * Returns p unless its null in which it returns a predicate which returns false
     *
     * @param <T>
     * @param p
     *
     * @return
     */
    public static final <T> Predicate<T> ensureNotNull( Predicate<T> p )
    {
        return p == null ? o -> false : p;
    }

    /**
     * Returns a predicate which tests an array of values
     *
     * @param <T>
     * @param t
     *
     * @return
     */
    public static <T> Predicate<T> in( T... t )
    {
        if( t == null || t.length == 0 ) {
            return v -> false;
        }

        if( t.length == 1 ) {
            T t0 = t[0];
            return v -> t0.equals( v );
        }

        T ary[] = Arrays.copyOf( t, t.length );

        return v -> {
            for( int i = 0; i < ary.length; i++ ) {
                if( Objects.equals( ary[i], v ) ) {
                    return true;
                }
            }
            return false;
        };
    }

}
