/*
 * Copyright 2014 Peter T Mount.
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
package uk.trainwatch.util.sql;

import uk.trainwatch.util.AbstractSpliterator;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 *
 * @author Peter T Mount
 */
public class ResultSetSpliterator<T>
        extends AbstractSpliterator<T>
{

    private final ResultSet resultSet;
    private final SQLFunction<ResultSet, T> factory;

    /**
     *
     * @param resultSet {@link ResultSet} to iterate over
     * @param factory   {@link Function} that will take a ResultSet row and produce some object
     */
    public ResultSetSpliterator( ResultSet resultSet, SQLFunction<ResultSet, T> factory )
    {
        this.resultSet = resultSet;
        this.factory = factory;
    }

    @Override
    public boolean tryAdvance( Consumer<? super T> action )
    {
        try
        {
            if( resultSet.next() )
            {
                action.accept( factory.apply( resultSet ) );
                return true;
            }
            return false;
        }
        catch( SQLException ex )
        {
            throw new UncheckedSQLException( ex );
        }
    }
}
