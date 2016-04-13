/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.io.ftp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.CopyOption;
import java.nio.file.Path;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;
import uk.trainwatch.io.IOConsumer;

/**
 *
 * @author peter
 */
class LazyFTPClient
        implements FTPClient
{

    private FTPClient client;

    private final Supplier<FTPClient> factory;

    public LazyFTPClient( Supplier<FTPClient> factory )
    {
        this.factory = factory;
    }

    private synchronized FTPClient getClient()
            throws IOException
    {
        if( client == null ) {
            client = factory.get();
            client.connect();
            client.login();
        }
        return client;
    }

    @Override
    public void close()
            throws IOException
    {
        if( client != null ) {
            client.close();
        }
    }

    @Override
    public void connect()
            throws IOException
    {
        getClient().connect();
    }

    @Override
    public synchronized boolean isConnected()
            throws IOException
    {
        return client != null && client.isConnected();
    }

    @Override
    public void login()
            throws IOException
    {
        getClient().login();
    }

    @Override
    public void log( Supplier<String> msg )
            throws IOException
    {
        getClient().log( msg );
    }

    @Override
    public void log( String msg )
            throws IOException
    {
        getClient().log( msg );
    }

    @Override
    public synchronized boolean isLoggedIn()
            throws IOException
    {
        return client != null && client.isLoggedIn();
    }

    @Override
    public boolean appendFile( String remote, InputStream local )
            throws IOException
    {
        return getClient().appendFile( remote, local );
    }

    @Override
    public boolean appendFile( FTPFile remote, InputStream local )
            throws IOException
    {
        return getClient().appendFile( remote, local );
    }

    @Override
    public OutputStream appendFileStream( String remote )
            throws IOException
    {
        return getClient().appendFileStream( remote );
    }

    @Override
    public Writer appendWriter( String remote )
            throws IOException
    {
        return getClient().appendWriter( remote );
    }

    @Override
    public boolean completePendingCommand()
            throws IOException
    {
        return getClient().completePendingCommand();
    }

    @Override
    public boolean retrieveFile( String remote, OutputStream local )
            throws IOException
    {
        return getClient().retrieveFile( remote, local );
    }

    @Override
    public boolean retrieveFile( FTPFile remote, OutputStream local )
            throws IOException
    {
        return getClient().retrieveFile( remote, local );
    }

    @Override
    public void retrieve( File file, CopyOption... options )
            throws IOException
    {
        getClient().retrieve( file, options );
    }

    @Override
    public void retrieve( FTPFile remote, File file, CopyOption... options )
            throws IOException
    {
        getClient().retrieve( remote, file, options );
    }

    @Override
    public void retrieve( String remote, File file, CopyOption... options )
            throws IOException
    {
        getClient().retrieve( remote, file, options );
    }

    @Override
    public void retrieve( Path target, CopyOption... options )
            throws IOException
    {
        getClient().retrieve( target, options );
    }

    @Override
    public Path retrieve( FTPFile f, Path target, CopyOption... options )
            throws IOException
    {
        return getClient().retrieve( f, target, options );
    }

    @Override
    public void retrieve( String remote, Path target, CopyOption... options )
            throws IOException
    {
        getClient().retrieve( remote, target, options );
    }

    @Override
    public InputStream retrieveFileStream( String remote )
            throws IOException
    {
        return getClient().retrieveFileStream( remote );
    }

    @Override
    public InputStream retrieveFileStream( FTPFile remote )
            throws IOException
    {
        return getClient().retrieveFileStream( remote );
    }

    @Override
    public Reader retrieveReader( String remote )
            throws IOException
    {
        return getClient().retrieveReader( remote );
    }

    @Override
    public Reader retrieveReader( FTPFile remote )
            throws IOException
    {
        return getClient().retrieveReader( remote );
    }

    @Override
    public boolean store( String remote, InputStream local )
            throws IOException
    {
        return getClient().store( remote, local );
    }

    @Override
    public void store( File file )
            throws IOException
    {
        getClient().store( file );
    }

    @Override
    public void store( String remote, File file )
            throws IOException
    {
        getClient().store( remote, file );
    }

    @Override
    public void store( Path target )
            throws IOException
    {
        getClient().store( target );
    }

    @Override
    public void store( String remote, Path target )
            throws IOException
    {
        getClient().store( remote, target );
    }

    @Override
    public OutputStream storeOutputStream( String remote )
            throws IOException
    {
        return getClient().storeOutputStream( remote );
    }

    @Override
    public Writer storeWriter( String remote )
            throws IOException
    {
        return getClient().storeWriter( remote );
    }

    @Override
    public boolean deleteFile( String pathname )
            throws IOException
    {
        return getClient().deleteFile( pathname );
    }

    @Override
    public boolean changeToParentDirectory()
            throws IOException
    {
        return getClient().changeToParentDirectory();
    }

    @Override
    public Collection<String> listNames( String pathname )
            throws IOException
    {
        return getClient().listNames( pathname );
    }

    @Override
    public Stream<String> names( String pathname )
            throws IOException
    {
        return getClient().names( pathname );
    }

    @Override
    public Collection<String> listNames()
            throws IOException
    {
        return getClient().listNames();
    }

    @Override
    public Stream<String> names()
            throws IOException
    {
        return getClient().names();
    }

    @Override
    public void forEachName( Consumer<String> c )
            throws IOException
    {
        getClient().forEachName( c );
    }

    @Override
    public void forEachName( String pathName, Consumer<String> c )
            throws IOException
    {
        getClient().forEachName( pathName, c );
    }

    @Override
    public Collection<FTPFile> listDirectories()
            throws IOException
    {
        return getClient().listDirectories();
    }

    @Override
    public Stream<FTPFile> directories()
            throws IOException
    {
        return getClient().directories();
    }

    @Override
    public Collection<FTPFile> listDirectories( String parent )
            throws IOException
    {
        return getClient().listDirectories( parent );
    }

    @Override
    public Stream<FTPFile> directories( String parent )
            throws IOException
    {
        return getClient().directories( parent );
    }

    @Override
    public Collection<FTPFile> listFiles( String pathname )
            throws IOException
    {
        return getClient().listFiles( pathname );
    }

    @Override
    public Stream<FTPFile> files( String pathname )
            throws IOException
    {
        return getClient().files( pathname );
    }

    @Override
    public Collection<FTPFile> listFiles()
            throws IOException
    {
        return getClient().listFiles();
    }

    @Override
    public Stream<FTPFile> files()
            throws IOException
    {
        return getClient().files();
    }

    @Override
    public Collection<FTPFile> listFiles( String pathname, FTPFileFilter filter )
            throws IOException
    {
        return getClient().listFiles( pathname, filter );
    }

    @Override
    public Stream<FTPFile> files( String pathname, FTPFileFilter filter )
            throws IOException
    {
        return getClient().files( pathname, filter );
    }

    @Override
    public Collection<FTPFile> listFiles( FTPFileFilter filter )
            throws IOException
    {
        return getClient().listFiles( filter );
    }

    @Override
    public Stream<FTPFile> files( FTPFileFilter filter )
            throws IOException
    {
        return getClient().files( filter );
    }

    @Override
    public void forEachFile( Consumer<FTPFile> c )
            throws IOException
    {
        getClient().forEachFile( c );
    }

    @Override
    public void forEach( IOConsumer<FTPFile> c )
            throws IOException
    {
        getClient().forEach( c );
    }

    @Override
    public void forEachFile( String pathname, Consumer<FTPFile> c )
            throws IOException
    {
        getClient().forEachFile( pathname, c );
    }

    @Override
    public void forEach( String pathname, IOConsumer<FTPFile> c )
            throws IOException
    {
        getClient().forEach( pathname, c );
    }

    @Override
    public void forEachFile( String pathname, FTPFileFilter filter, Consumer<FTPFile> c )
            throws IOException
    {
        getClient().forEachFile( pathname, filter, c );
    }

    @Override
    public void forEach( FTPFileFilter filter, IOConsumer<FTPFile> c )
            throws IOException
    {
        getClient().forEach( filter, c );
    }

    @Override
    public void forEach( String pathname, FTPFileFilter filter, IOConsumer<FTPFile> c )
            throws IOException
    {
        getClient().forEach( pathname, filter, c );
    }

    @Override
    public boolean makeDirectory( String pathname )
            throws IOException
    {
        return getClient().makeDirectory( pathname );
    }

    @Override
    public Collection<FTPFile> mlistDir()
            throws IOException
    {
        return getClient().mlistDir();
    }

    @Override
    public Collection<FTPFile> mlistDir( String pathname )
            throws IOException
    {
        return getClient().mlistDir( pathname );
    }

    @Override
    public Collection<FTPFile> mlistDir( String pathname, FTPFileFilter filter )
            throws IOException
    {
        return getClient().mlistDir( pathname, filter );
    }

    @Override
    public void forEachMlistDir( Consumer<FTPFile> c )
            throws IOException
    {
        getClient().forEachMlistDir( c );
    }

    @Override
    public void forEachMlist( IOConsumer<FTPFile> c )
            throws IOException
    {
        getClient().forEachMlist( c );
    }

    @Override
    public void forEachMlistDir( String pathname, Consumer<FTPFile> c )
            throws IOException
    {
        getClient().forEachMlistDir( pathname, c );
    }

    @Override
    public void forEachMlist( String pathname, IOConsumer<FTPFile> c )
            throws IOException
    {
        getClient().forEachMlist( pathname, c );
    }

    @Override
    public void forEachMlistDir( String pathname, FTPFileFilter filter, Consumer<FTPFile> c )
            throws IOException
    {
        getClient().forEachMlistDir( pathname, filter, c );
    }

    @Override
    public void forEachMlist( String pathname, FTPFileFilter filter, IOConsumer<FTPFile> c )
            throws IOException
    {
        getClient().forEachMlist( pathname, filter, c );
    }

    @Override
    public FTPFile mlistFile( String pathname )
            throws IOException
    {
        return getClient().mlistFile( pathname );
    }

    @Override
    public void mlistFile( String pathname, IOConsumer<FTPFile> c )
            throws IOException
    {
        getClient().mlistFile( pathname, c );
    }

    @Override
    public String printWorkingDirectory()
            throws IOException
    {
        return getClient().printWorkingDirectory();
    }

    @Override
    public boolean changeWorkingDirectory( String pathname )
            throws IOException
    {
        return getClient().changeWorkingDirectory( pathname );
    }

    @Override
    public <T> T getAttribute( String n )
            throws IOException
    {
        return getClient().<T>getAttribute( n );
    }

    @Override
    public void setAttribute( String n, Object v )
            throws IOException
    {
        getClient().setAttribute( n, v );
    }

    @Override
    public synchronized boolean isAttributePresent( String n )
            throws IOException
    {
        return client != null && client.isAttributePresent( n );
    }

}
