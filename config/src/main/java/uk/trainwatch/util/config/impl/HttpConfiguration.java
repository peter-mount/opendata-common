/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.util.config.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import uk.trainwatch.util.MapBuilder;
import uk.trainwatch.util.config.Configuration;

/**
 * Configuration from a remote http server. Authentication is done
 *
 * @author peter
 */
public class HttpConfiguration
        extends ConfigurationWrapper
{

    private static final Logger LOG = Logger.getLogger( "HttpConfiguration" );

    private Configuration configuration;
    private final URI uri;

    public HttpConfiguration( URI uri )
    {
        this.uri = uri;
    }

    @Override
    protected Configuration getConfiguration()
    {
        if( configuration == null )
        {
            // Don't write uri as is as we may expose security details
            LOG.log( Level.INFO, () -> "Retrieving config " + uri.getScheme() + "://" + uri.getHost() + uri.getPath() );

            try( CloseableHttpClient client = HttpClients.createDefault() )
            {
                HttpGet get = new HttpGet( uri );
                get.setHeader( "User-Agent", "Area51 Configuration/1.0" );
                HttpResponse response = client.execute( get );
                switch( response.getStatusLine().getStatusCode() )
                {
                    case 200:
                    case 304:
                        try( InputStream is = response.getEntity().getContent() )
                        {
                            try( JsonReader r = Json.createReader( new InputStreamReader( is ) ) )
                            {
                                configuration = new MapConfiguration( MapBuilder.fromJsonObject( r.readObject() ).build() );
                            }
                        }
                        break;
                        
                    default:
                        LOG.log( Level.WARNING, () -> "Error " + uri.getScheme() + "://" + uri.getHost() + uri.getPath()
                                 + " " + response.getStatusLine().getStatusCode() );
                        configuration = EmptyConfiguration.INSTANCE;
                }
            } catch( IOException ex )
            {
                throw new UncheckedIOException( ex );
            }
        }
        return configuration;
    }

}