/*
 * Copyright 2015 peter.
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
package uk.trainwatch.io;

import java.io.IOException;

/**
 * An extensible chain of actions to perform
 */
@FunctionalInterface
public interface IOAction
{

    /**
     *
     * @throws IOException
     */
    void invoke()
            throws IOException;

    static final IOAction NOP = () -> {
    };

    default IOAction andThen( IOAction after )
    {
        if( this == NOP ) {
            return after;
        }
        if( after == NOP ) {
            return this;
        }
        return () -> {
            invoke();
            after.invoke();
        };
    }

    default IOAction compose( IOAction before )
    {
        if( this == NOP ) {
            return before;
        }
        if( before == NOP ) {
            return this;
        }
        return () -> {
            before.invoke();
            invoke();
        };
    }

    static IOAction chain( IOAction before, IOAction after )
    {
        return before == null ? after : before.andThen( after );
    }

    static void invoke( IOAction action )
            throws IOException
    {
        if( action != null ) {
            action.invoke();
        }
    }

    static interface Builder
    {

        Builder add( IOAction action );

        default Builder add( IOSupplier<IOAction> supplier )
                throws IOException
        {
            return add( supplier.get() );
        }

        default <T> Builder add( T t, IOFunction<T, IOAction> mapper )
                throws IOException
        {
            return add( mapper.apply( t ) );
        }

        default Builder invoke( IOConsumer<Builder> c )
                throws IOException
        {
            c.accept( this );
            return this;
        }

        IOAction build();
    }

    static Builder builder()
    {
        return new Builder()
        {
            IOAction action = NOP;

            @Override
            public Builder add( IOAction action )
            {
                if( action != null ) {
                    this.action = this.action.andThen( action );
                }
                return this;
            }

            @Override
            public IOAction build()
            {
                return action;
            }
        };
    }
}
