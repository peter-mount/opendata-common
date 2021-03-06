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
package uk.trainwatch.util.sql;

import uk.trainwatch.util.AbstractSpliterator;
import java.sql.SQLException;
import java.util.function.Consumer;

/**
 *
 * @author peter
 * @param <T>
 */
public class SQLSupplierSpliterator<T>
        extends AbstractSpliterator<T>
{

    private final SQLSupplier<T> supplier;

    public SQLSupplierSpliterator( SQLSupplier<T> supplier )
    {
        this.supplier = supplier;
    }

    @Override
    public boolean tryAdvance( Consumer<? super T> action )
    {
        try
        {
            T v = supplier.get();
            if( v == null )
            {
                return false;
            }

            action.accept( v );
            return true;
        }
        catch( SQLException ex )
        {
            throw new UncheckedSQLException( ex );
        }
    }
}
