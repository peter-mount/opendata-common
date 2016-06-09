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
package uk.trainwatch.util.ruleengine;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
import uk.trainwatch.util.Consumers;

/**
 * A Book of Rules
 *
 * @author peter
 */
public class RuleBook
        implements Serializable
{

    private static final long serialVersionUID = 1L;

    private final Map<Class<?>, RuleSet<?>> ruleSets = new ConcurrentHashMap<>();

    /**
     * Get a specific rule
     *
     * @param <T>
     * @param clazz
     * @param n
     *
     * @return
     */
    public <T> Consumer<T> getRule( Class<T> clazz, String n )
    {
        RuleSet<T> set = getRuleSet( clazz );
        return set == null ? null : set.getRule( n );
    }

    /**
     * Get the RuleSet for a class
     *
     * @param <T>
     * @param clazz
     *
     * @return RuleSet or null if not present
     */
    public <T> RuleSet<T> getRuleSet( Class<T> clazz )
    {
        Objects.requireNonNull( clazz );
        return (RuleSet<T>) ruleSets.get( clazz );
    }

    /**
     * Create a RuleSet for a class. If one already exists then it will be returned
     *
     * @param <T>
     * @param clazz
     *
     * @return RuleSet, never null
     */
    public <T> RuleSet<T> createRuleSet( Class<T> clazz )
    {
        Objects.requireNonNull( clazz );
        return (RuleSet<T>) ruleSets.computeIfAbsent( clazz, RuleSet::new );
    }

    /**
     * Remove a RuleSet for a class
     *
     * @param clazz
     */
    public void removeRuleSet( Class<?> clazz )
    {
        Objects.requireNonNull( clazz );
        ruleSets.remove( clazz );
    }

    /**
     * Adds a rule set replacing any existing one
     *
     * @param ruleSet
     */
    public void addRuleSet( RuleSet<?> ruleSet )
    {
        Objects.requireNonNull( ruleSet );
        Objects.requireNonNull( ruleSet.getClazz() );
        ruleSets.put( ruleSet.getClazz(), ruleSet );
    }

    /**
     * Returns a clone of an existing RuleSet, creating a new one if it's not present
     *
     * @param <T>
     * @param clazz
     *
     * @return
     */
    public <T> RuleSet<T> cloneRuleSet( Class<T> clazz )
    {
        RuleSet<T> s = getRuleSet( clazz );
        if( s == null ) {
            return new RuleSet<>( clazz );
        }

        try {
            return (RuleSet<T>) s.clone();
        }
        catch( CloneNotSupportedException ex ) {
            throw new IllegalArgumentException( ex );
        }
    }

    /**
     * Returns a {@link Consumer} that will accept a class and delegate to the specified class.
     * <p>
     * This is the preferred method to use rather than the more generic {@link #getConsumer()} method.
     *
     * @param <T>   Type the consumer is to accept
     * @param clazz Class of type T
     *
     * @return
     *
     * @see #getConsumer()
     */
    public <T> Consumer<T> getConsumer( Class<T> clazz )
    {
        Objects.requireNonNull( clazz );
        return Consumers.consumeIfNotNull( v -> {
            RuleSet rs = getRuleSet( clazz );
            if( rs != null ) {
                rs.accept( v );
            }
        } );
    }

    /**
     * Returns a {@link Consumer} that will accept a value and attempt to delegate to to a RuleSet based on it's class.
     * <p>
     * This is a more generic version of {@link #getConsumer(java.lang.Class)}.
     *
     * @param <T>
     *
     * @return Consumer
     *
     * @see #getConsumer(java.lang.Class)
     */
    public <T> Consumer<T> getConsumer()
    {
        return Consumers.consumeIfNotNull( v -> {
            RuleSet rs = getRuleSet( v.getClass() );
            if( rs != null ) {
                rs.accept( v );
            }
        } );
    }

    /**
     * Clears the RuleBook of all Rules
     */
    public void clear()
    {
        ruleSets.clear();
    }

    /**
     * Create a new Rule
     *
     * @param <T>
     * @param clazz Rule Class
     * @param n     Rule name
     * @param p     Predicate
     * @param c     Consumer
     *
     * @return Rule
     */
    public <T> RuleBook setRule( Class<T> clazz, String n, Predicate<T> p, Consumer<T> c )
    {
        createRuleSet( clazz ).setRule( n, p, c );
        return this;
    }

    /**
     * Create a new Rule
     *
     * @param <T>
     * @param clazz Rule Class
     * @param n     Rule name
     * @param c     Consumer
     *
     * @return Rule
     */
    public <T> RuleBook setRule( Class<T> clazz, String n, Consumer<T> c )
    {
        createRuleSet( clazz ).setRule( n, c );
        return this;
    }
}
