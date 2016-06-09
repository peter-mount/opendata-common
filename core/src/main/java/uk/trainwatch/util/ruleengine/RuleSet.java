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
import java.util.stream.Stream;
import uk.trainwatch.util.Consumers;

/**
 * A collection of Rule's
 *
 * @author peter
 * @param <T>
 */
public class RuleSet<T>
        implements Consumer<T>,
                   Cloneable,
                   Serializable
{

    private static final long serialVersionUID = 1L;

    private final Map<String, Consumer<T>> rules;

    private final Class<T> clazz;

    /**
     * Create a RuleSet for a specific class
     *
     * @param clazz
     */
    public RuleSet( Class<T> clazz )
    {
        this( clazz, new ConcurrentHashMap<>() );
    }

    private RuleSet( Class<T> clazz, Map<String, Consumer<T>> rules )
    {
        this.clazz = Objects.requireNonNull( clazz );
        this.rules = Objects.requireNonNull( rules );
    }

    /**
     * The class this rule set applies to
     *
     * @return
     */
    public Class<?> getClazz()
    {
        return clazz;
    }

    /**
     * Get the named rule
     *
     * @param n
     *
     * @return
     */
    public Consumer<T> getRule( String n )
    {
        Objects.requireNonNull( n, "Rule name" );
        return rules.get( n.toLowerCase() );
    }

    /**
     * Add a new rule
     *
     * @param rule
     *
     * @return true if added, false if already present under that name
     */
    public boolean addRule( String n, Consumer<T> rule )
    {
        Objects.requireNonNull( n, "Rule name" );
        return rules.putIfAbsent( n.toLowerCase(), rule ) == null;
    }

    /**
     * Set a rule, replacing any existing rule
     *
     * @param rule
     */
    public RuleSet<T> setRule( String n, Consumer<T> rule )
    {
        Objects.requireNonNull( n, "Rule name" );
        rules.put( n.toLowerCase(), rule );
        return this;
    }

    /**
     * Create a new Rule
     *
     * @param n Rule name
     * @param p Predicate
     * @param c Consumer
     */
    public RuleSet<T> setRule( String n, Predicate<T> p, Consumer<T> c )
    {
        Objects.requireNonNull( n );
        Objects.requireNonNull( p );
        Objects.requireNonNull( c );
        setRule( n, Consumers.ifThen( p, c ) );
        return this;
    }

    /**
     * Returns a stream of ruleNames
     *
     * @return
     */
    public Stream<String> ruleNames()
    {
        return rules.keySet().stream();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void accept( T t )
    {
        if( t != null ) {
            rules.values().forEach( r -> r.accept( t ) );
        }
    }

    /**
     * Clones this RuleSet. It will be linked to the RuleBook but not a part of it.
     * <p>
     * You would usually either use this method or {@link RuleBook#cloneRuleSet(java.lang.Class)} to clone a RuleSet, make modifications then use
     * {@link RuleBook#addRuleSet(onl.area51.ruleengine.RuleSet) } to make atomic changes to the RuleBook.
     *
     * @return
     *
     * @throws CloneNotSupportedException
     */
    @Override
    protected Object clone()
            throws CloneNotSupportedException
    {
        return new RuleSet<>( clazz, new ConcurrentHashMap<>( rules ) );
    }

}
