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
package uk.trainwatch.io.ftp;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import uk.trainwatch.io.IOAction;
import uk.trainwatch.io.IOBiConsumer;
import uk.trainwatch.io.IOConsumer;
import uk.trainwatch.io.IOFunction;
import uk.trainwatch.io.IOPredicate;
import uk.trainwatch.io.IOSupplier;
import uk.trainwatch.util.Consumers;

public class FTPClientBuilder
{

    String proxy = null;
    int proxyPort;
    String proxyUser;
    String proxyPass;

    IOConsumer<DefaultFTPClient> connect;
    IOPredicate<DefaultFTPClient> login;

    Consumer<String> consumer;
    boolean printCommands;
    boolean debuggingEnabled;

    int keepAliveTimeout = 0;
    int controlKeepAliveReplyTimeout = 0;
    boolean useEpsvWithIPv4 = false;
    boolean localActive = false;

    boolean binaryTransfer = true;
    boolean listHiddenFiles = false;
    final Map<String, Object> attributes = new HashMap<>();
    IOBiConsumer<FTPClient, IOAction.Builder> action = null;

    public FTPClientBuilder addAction( IOBiConsumer<FTPClient, IOAction.Builder> action )
    {
        this.action = this.action == null ? action : this.action.andThen( action );
        return this;
    }

    /**
     * Perform some action on the FTPClient
     *
     * @param action
     *
     * @return
     */
    public FTPClientBuilder invoke( IOConsumer<FTPClient> action )
    {
        return addAction( ( c, b ) -> action.accept( c ) );
    }

    /**
     * Apply a mapping function on the FTPClient (possibly performing some action on it) and if it returns an IOAction add that to the actions to be
     * invoked after the client has been closed.
     *
     * @param action
     *
     * @return
     */
    public FTPClientBuilder invokeLater( IOFunction<FTPClient, IOAction> action )
    {
        return addAction( ( c, b ) -> b.add( action.apply( c ) ) );
    }

    public FTPClientBuilder setLogger( Consumer<String> consumer )
    {
        this.consumer = consumer;
        return this;
    }

    public FTPClientBuilder logger( Consumer<String> consumer )
    {
        this.consumer = Consumers.andThen( this.consumer, consumer );
        return this;
    }

    public FTPClientBuilder enableDebugging()
    {
        debuggingEnabled = true;
        return this;
    }

    public FTPClientBuilder printCommands()
    {
        printCommands = true;
        return this;
    }

    /**
     * HTTP proxy
     * <p>
     * @param proxy
     *              <p>
     * @return
     */
    public FTPClientBuilder proxy( String proxy )
    {
        return proxy( proxy, 80 );
    }

    public FTPClientBuilder proxy( String proxy, int proxyPort )
    {
        this.proxy = proxy;
        this.proxyPort = proxyPort;
        return this;
    }

    public FTPClientBuilder proxyPort( int proxyPort )
    {
        this.proxyPort = proxyPort;
        return this;
    }

    public FTPClientBuilder proxyUser( String proxyUser )
    {
        this.proxyUser = proxyUser;
        return this;
    }

    public FTPClientBuilder proxyPass( String proxyPass )
    {
        this.proxyPass = proxyPass;
        return this;
    }

    /**
     * TCP Keep Alive Timeout
     * <p>
     * @param keepAliveTimeout
     *                         <p>
     * @return
     */
    public FTPClientBuilder keepAliveTimeout( int keepAliveTimeout )
    {
        this.keepAliveTimeout = keepAliveTimeout;
        return this;
    }

    /**
     * TCP Keep Alive Timeout for replies
     * <p>
     * @param controlKeepAliveReplyTimeout
     *                                     <p>
     * @return
     */
    public FTPClientBuilder controlKeepAliveReplyTimeout( int controlKeepAliveReplyTimeout )
    {
        this.controlKeepAliveReplyTimeout = controlKeepAliveReplyTimeout;
        return this;
    }

    public FTPClientBuilder useEpsvWithIPv4()
    {
        useEpsvWithIPv4 = true;
        return this;
    }

    /**
     * Local or remote (behind firewall) server
     * <p>
     * @return
     */
    public FTPClientBuilder localActive()
    {
        localActive = true;
        return this;
    }

    /**
     * Use Passive mode (default) for when behind a firewall
     * <p>
     * @return
     */
    public FTPClientBuilder passive()
    {
        localActive = false;
        return this;
    }

    /**
     * Set binary mode (default)
     * <p>
     * @return
     */
    public FTPClientBuilder binary()
    {
        binaryTransfer = true;
        return this;
    }

    public FTPClientBuilder ascii()
    {
        binaryTransfer = false;
        return this;
    }

    /**
     * List hidden files
     * <p>
     * @param listHiddenFiles
     *                        <p>
     * @return
     */
    public FTPClientBuilder listHiddenFiles()
    {
        listHiddenFiles = true;
        return this;
    }

    public FTPClientBuilder connect( String s )
    {
        connect = c -> c.getDelegate().connect( s );
        return this;
    }

    public FTPClientBuilder connect( String s, int p )
    {
        connect = c -> c.getDelegate().connect( s, p );
        return this;
    }

    public FTPClientBuilder login( String user, String password )
    {
        login = c -> c.getDelegate().login( user, password );
        return this;
    }

    public FTPClientBuilder login( String user, String password, String account )
    {
        login = c -> c.getDelegate().login( user, password, account );
        return this;
    }

    public FTPClientBuilder setAttribute( String n, Object v )
    {
        attributes.put( n, v );
        return this;
    }

    /**
     * Build an FTPClient instance
     *
     * @return
     */
    public FTPClient build()
    {
        return new DefaultFTPClient( this );
    }

    /**
     * Builds a lazy FTPClient instance. This client will only connect if an action is required upon it.
     *
     * @return
     */
    public FTPClient buildLazyClient()
    {
        FTPClientBuilder b = this;
        return new LazyFTPClient( () -> b.build() );
    }

    public IOConsumer<IOAction.Builder> buildIOActionChain()
            throws IOException
    {
        Objects.requireNonNull( action, "No action chain defined" );
        return b -> {
            try( FTPClient client = buildLazyClient() ) {
                action.accept( client, b );
            }
        };
    }

    /**
     * Builds an IOAction which will execute any actions applied to this builder and return an IOAction of any actions to run once the client has closed.
     *
     * @return
     *
     * @throws IOException
     */
    public IOSupplier<IOAction> buildIOSupplier()
            throws IOException
    {
        Objects.requireNonNull( action, "No action chain defined" );
        return () -> {
            try( FTPClient client = buildLazyClient() ) {
                IOAction.Builder b = IOAction.builder();
                action.accept( client, b );
                return b.build();
            }
        };
    }

    public void execute()
            throws IOException
    {
        buildIOSupplier().get().invoke();
    }
}
