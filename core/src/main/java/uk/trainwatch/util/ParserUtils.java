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
package uk.trainwatch.util;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Properties;
import java.util.function.Function;

/**
 *
 * @author peter
 */
public class ParserUtils
{

    public static int parseInt( String v )
    {
        return parseInt( v, 0 );
    }

    public static int parseInt( String v, int defaultValue )
    {
        return parse( v, Integer::parseInt, defaultValue );
    }

    public static long parseLong( String v )
    {
        return parseLong( v, 0L );
    }

    public static Long parseLong( String v, Long defaultValue )
    {
        return parse( v, Long::parseLong, defaultValue );
    }

    public static double parseDouble( String v )
    {
        return parseDouble( v, 0.0 );
    }

    public static Double parseDouble( String v, Double defaultValue )
    {
        return parse( v, Double::parseDouble, defaultValue );
    }

    public static <T> T parse( String v, Function<String, T> f, T defaultValue )
    {
        if( v == null || v.isEmpty() ) {
            return defaultValue;
        }
        String s = v.trim();
        if( s.isEmpty() ) {
            return defaultValue;
        }
        try {
            return f.apply( s );
        }
        catch( Exception ex ) {
            return defaultValue;
        }
    }

    public static Properties readProperties( File file )
            throws IOException
    {
        try( Reader r = new FileReader( file ) ) {
            Properties p = new Properties();
            p.load( r );
            return p;
        }
    }

    public static Properties readProperties( Path path )
            throws IOException
    {
        try( InputStream is = Files.newInputStream( path, StandardOpenOption.READ ) ) {
            Properties p = new Properties();
            p.load( is );
            return p;
        }
    }
}
