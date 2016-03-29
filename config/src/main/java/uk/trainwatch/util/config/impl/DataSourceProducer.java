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
package uk.trainwatch.util.config.impl;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.sql.DataSource;
import org.postgresql.ds.PGPoolingDataSource;
import uk.trainwatch.util.config.Configuration;
import uk.trainwatch.util.config.ConfigurationService;
import uk.trainwatch.util.config.Database;

/**
 *
 * @author peter
 */
@ApplicationScoped
public class DataSourceProducer
{

    private final Map<String, DataSource> dataSources = new ConcurrentHashMap<>();

    @Inject
    private ConfigurationService configurationService;

    @Produces
    @Database("")
    DataSource getDataSource( InjectionPoint injectionPoint )
    {
        for( Annotation a: injectionPoint.getQualifiers() ) {
            if( a instanceof Database ) {
                return getDataSource( ((Database) a).value() );
            }
        }
        throw new IllegalArgumentException( "Unable to inject unnamed DataSource" );
    }

    public DataSource getDataSource( String name )
    {
        return dataSources.computeIfAbsent( name, this::lookup );
    }

    private DataSource lookup( String name )
    {
        Configuration map = configurationService.getConfiguration( name );

        PGPoolingDataSource ds = new PGPoolingDataSource();
        ds.setApplicationName( "OpenData" );
        ds.setDataSourceName( map.getString( "name", () -> map.getString( "database" ) ) );
        ds.setMaxConnections( map.getInt( "connections.max", 10 ) );
        ds.setInitialConnections( map.getInt( "connections.initial", 0 ) );

        ds.setUser( Objects.requireNonNull( map.getString( "username" ), "username required" ) );
        ds.setPassword( Objects.requireNonNull( map.getString( "password" ), "password required" ) );
        ds.setServerName( Objects.requireNonNull( map.getString( "hostname" ), "hostname required" ) );
        ds.setDatabaseName( map.getString( "database", () -> map.getString( "name" ) ) );
        ds.setPortNumber( map.getInt( "port", 0 ) );

        return ds;
    }
}
