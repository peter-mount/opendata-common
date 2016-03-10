/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.trainwatch.io;

import java.io.File;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;
import java.util.function.Function;
import uk.trainwatch.util.TimeUtils;

/**
 *
 * @author Peter T Mount
 */
public abstract class AbstractPathMapper<T>
        implements Function<T, Path>
{

    private final String database;
    private final String prefix;
    private final String suffix;

    public AbstractPathMapper( String database, String prefix )
    {
        this( database, prefix, null );
    }

    public AbstractPathMapper( String database, String prefix, String suffix )
    {
        this.database = Objects.requireNonNull( database );
        this.prefix = Objects.requireNonNull( prefix );
        this.suffix = suffix == null ? "" : suffix;
    }

    @Deprecated
    public static final LocalDateTime getLocalDateTime()
    {
        return TimeUtils.getLocalDateTime();
    }

    @Deprecated
    public static final LocalDateTime getLocalDateTime( final long timestamp )
    {
        return TimeUtils.getLocalDateTime( timestamp );
    }

    @Deprecated
    public static final LocalDateTime getLocalDateTime( final Instant instant )
    {
        return TimeUtils.getLocalDateTime( instant );
    }

    protected final Path getPath( final LocalDateTime dateTime, final boolean hours )
    {
        return getFile( dateTime, hours ).
                toPath();
    }

    protected final File getFile( final LocalDateTime dateTime, final boolean hours )
    {
        File dir = getDir( dateTime, hours );
        int id = hours ? dateTime.getMinute() : dateTime.getDayOfMonth();
        return new File( dir, id + suffix );
    }

    protected final File getDir( final LocalDateTime dateTime, final boolean hours )
    {
        String fileName;
        if( hours )
        {
            fileName = String.join( "/",
                                    database,
                                    prefix,
                                    String.valueOf( dateTime.getYear() ),
                                    String.valueOf( dateTime.getMonth().
                                            getValue() ),
                                    String.valueOf( dateTime.getDayOfMonth() ),
                                    String.valueOf( dateTime.getHour() )
            );
        }
        else
        {
            fileName = String.join( "/",
                                    database,
                                    prefix,
                                    String.valueOf( dateTime.getYear() ),
                                    String.valueOf( dateTime.getMonth().
                                            getValue() )
            );
        }

        File file = new File( fileName.replaceAll( " ", "" ) );
        file.mkdirs();
        return file;
    }

}
