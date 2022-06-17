package com.queue_eh.framework.v1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PushbackInputStream;

import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class HttpsClientNonVerifying
{
   private static final Pattern RSStatus = Pattern.compile( "^HTTP/([0-9]\\.[0-9]) ([0-9]+) (.*)$" );
   private static final Pattern RSCookieCutter = Pattern.compile( "^([^=]+?)=(.*)?$" );

   private static final DateFormat ietfDateFormat =
      new SimpleDateFormat( "EEE, d MMM yyyy HH:mm:ss z", Locale.ENGLISH );

   private static final byte[] gzip_prefix =
   {
      ( byte )( GZIPInputStream.GZIP_MAGIC ),
      ( byte )( GZIPInputStream.GZIP_MAGIC >> 8 )
   };

   public static final String USER_AGENT_STR = "(java, antonw03@yahoo.com)";

   private static final PrintStream diagout = new PrintStream( OutputStream.nullOutputStream() );

   public class Cookie
   {
      public String name = null;
      public String value = null;
      public String expires = null;
      public String path = null;
      public String domain = null;

      boolean secure = false;
   }

   public class HttpsResponse
   {
      // status line
      public String http_version;
      public String status_code;
      public String reason_phrase;

      public Map< String, List< String > > header;

      public Map< String, Map< String, Cookie > > cookies;

      public String content;
   }


   private static class NullTrustManager implements X509TrustManager
   {
      @Override
      public void checkClientTrusted( X509Certificate[] chain, String authType ) throws CertificateException
      {
         // We could perform some rudimentary client store certificate checks here, if we weren't completely
         // ignoring security for this example.

         final DateFormat iso8601DateFormat =
            new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.ENGLISH );

         for ( int i = 0; i < chain.length; i++ )
         {
            diagout.println( "Client Certificate #" + ( i + 1 ) );
            diagout.println( "   Version: " + chain[ i ].getVersion() );
            diagout.println( "   Serial#: " + chain[ i ].getSerialNumber() );
            diagout.println( "   Issuer : " + chain[ i ].getIssuerX500Principal().getName() );
            diagout.println( "   ValidFrom: " + iso8601DateFormat.format( chain[ i ].getNotBefore() ) );
            diagout.println( "   ValidTo  : " + iso8601DateFormat.format( chain[ i ].getNotAfter() ) );
            diagout.println( "   Algorithm: " + chain[ i ].getSigAlgName() );
            diagout.println( "   Usage    : " + chain[ i ].getKeyUsage() );
         }
      }

      @Override
      public void checkServerTrusted( X509Certificate[] chain, String authType ) throws CertificateException
      {
         // Normally we would validate our chain of trust and that we fall within the date of the certificate.
         // Since we often must test with privately issued certificates, null out our trust manager checks and
         // test certificate information directly on public-facing hosts.

         final DateFormat iso8601DateFormat =
            new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.ENGLISH );

         for ( int i = 0; i < chain.length; i++ )
         {
            diagout.println( "Server Certificate #" + ( i + 1 ) );
            diagout.println( "   Version: " + chain[ i ].getVersion() );
            diagout.println( "   Serial#: " + chain[ i ].getSerialNumber() );
            diagout.println( "   Issuer : " + chain[ i ].getIssuerX500Principal().getName() );
            diagout.println( "   ValidFrom: " + iso8601DateFormat.format( chain[ i ].getNotBefore() ) );
            diagout.println( "   ValidTo  : " + iso8601DateFormat.format( chain[ i ].getNotAfter() ) );
            diagout.println( "   Algorithm: " + chain[ i ].getSigAlgName() );
            diagout.println( "   Usage    : " + chain[ i ].getKeyUsage() );
         }
      }

      @Override
      public X509Certificate[] getAcceptedIssuers()
      {
         return null;
      }
   }

   static
   {
      // configure the SSLContext with our non-verifying TrustManager
      SSLContext ctx = null;

      try
      {
         ctx = SSLContext.getInstance( "TLSv1.2" );

         ctx.init( new KeyManager[ 0 ], new TrustManager[] { new NullTrustManager() }, new SecureRandom() );

         SSLContext.setDefault( ctx );
      }
      catch ( NoSuchAlgorithmException nsae )
      {
         nsae.printStackTrace();
      }
      catch ( KeyManagementException kme )
      {
         kme.printStackTrace();
      }
   }


   public HttpsResponse submitRequest( String method,
                                       String targetUrl,
                                       Map< String, String[] > requestHeaders,
                                       String bodyContent,
                                       Map< String, Map< String, Cookie > > cookieMap,
                                       Integer connectTimeoutMS,
                                       Integer readTimeoutMS,
                                       Integer maxRedirectCount )
   {
      HttpsResponse result = null;

      StringBuffer buffer = new StringBuffer();

      StringBuffer cookie_content = new StringBuffer( 2000 );
      StringBuffer secure_cookie = new StringBuffer( 1000 );

      if ( null == cookieMap )
      {
         cookieMap = new HashMap< String, Map< String, Cookie > >(); //=== pair( cookie.path, pair( cookie.name, Cookie ) )
      }
      else if ( cookieMap.size() > 0 )  // compose cookie information
      {
         String[] path = new String[ cookieMap.size() ];

         path = cookieMap.keySet().toArray( path );

         Arrays.sort( path, new NaturalStringComparator() );

         for ( int i = 0; i < path.length; i++ )
         {
            Map< String, Cookie > box = cookieMap.get( path[ i ] );

            if ( ( null != box ) && ( 0 < box.size() ) )
            {
               String[] name = new String[ box.size() ];

               name = box.keySet().toArray( name );

               Arrays.sort( name, new NaturalStringComparator() );

               for ( int j = 0; j < name.length; j++ )
               {
                  Cookie biscuit = box.get( name[ j ] );

                  Date expiryDate = null;

                  if ( null != biscuit.expires )
                  {
                     try
                     {
                        expiryDate = ietfDateFormat.parse( biscuit.expires );

                        Date now = new Date();

                        if ( expiryDate.after( now ) )
                        {
                           expiryDate = null;
                        }
                     }
                     catch ( ParseException pe )
                     {
                        diagout.println( "cookie expiry of '" + biscuit.expires + "' doesn't appear to be valid." );

                        diagout.println( pe.getMessage() );
                     }
                  }

                  if ( null == expiryDate )
                  {
                     if ( !biscuit.secure )
                     {
                        if ( 0 == cookie_content.length() )
                        {
                           cookie_content.append( biscuit.name + "=" + biscuit.value );
                        }
                        else
                        {
                           cookie_content.append( "; " + biscuit.name + "=" + biscuit.value );
                        }
                     }
                     else
                     {
                        if ( 0 == secure_cookie.length() )
                        {
                           secure_cookie.append( biscuit.name + "=" + biscuit.value );
                        }
                        else
                        {
                           secure_cookie.append( "; " + biscuit.name + "=" + biscuit.value );
                        }
                     }
                  }
               }
            }
         }
      }

      int redirectCount = 0;

      if ( maxRedirectCount < 1 )
      {
         maxRedirectCount = 1;
      }

      URL url = null;

      try
      {
         url = new URL( targetUrl );
      }
      catch( MalformedURLException mue )
      {
         diagout.println( "targetUrl of '" + targetUrl + "' doesn't appear to be valid." );

         diagout.println( mue.getMessage() );

         return null;
      }

      HttpsURLConnection.setFollowRedirects( false );

      while ( redirectCount < maxRedirectCount )
      {
         try
         {
            HttpsURLConnection request = ( HttpsURLConnection )url.openConnection();

            request.setRequestProperty( "User-Agent", HttpsClientNonVerifying.USER_AGENT_STR );

            if ( "HTTPS".equalsIgnoreCase( url.getProtocol() ) )
            {
               if ( ( cookie_content.length() > 0 ) && ( secure_cookie.length() > 0 ) )
               {
                  request.setRequestProperty( "Cookie", cookie_content.toString() + "; " + secure_cookie.toString() );
               }
               else if ( cookie_content.length() > 0 )
               {
                  request.setRequestProperty( "Cookie", cookie_content.toString() );
               }
               else if ( secure_cookie.length() > 0 )
               {
                  request.setRequestProperty( "Cookie", secure_cookie.toString() );
               }
            }

            // GET, POST, HEAD, OPTIONS, PUT, DELETE, TRACE
            request.setRequestMethod( method );

            if ( null != requestHeaders )
            {
               for ( Iterator< String > keys = requestHeaders.keySet().iterator(); keys.hasNext(); )
               {
                  String name = keys.next();

                  if ( name != null )
                  {
                     String[] value = requestHeaders.get( name );

                     StringBuilder rq_property = new StringBuilder();

                     for ( int i = 0; i < value.length; i++ )
                     {
                        if ( i != 0 )
                        {
                           rq_property.append( ", " );
                        }

                        rq_property.append( value[ i ] );
                     }

                     request.setRequestProperty( name, rq_property.toString() );
                  }
               }
            }

            request.setDoInput( true );

            if ( "post".equals( method.toLowerCase() ) || "put".equals( method.toLowerCase() ) )
            {
               request.setDoOutput( true );
            }

            // Clamp connectTimeoutMS to workable values to prevent automatic java.net.SocketTimeoutException. 
            // A connectTimeoutMS of zero will block forever.
            if ( ( null != connectTimeoutMS )
                 && ( ( connectTimeoutMS.intValue() > 50 ) ) || ( 0 == connectTimeoutMS.intValue() ) )
            {
               request.setConnectTimeout( connectTimeoutMS.intValue() );
            }

            // Clamp readTimeoutMS to workable values to prevent automatic java.net.SocketTimeoutException.
            // A readTimeoutMS of zero will block forever.
            if ( ( null != readTimeoutMS )
                 && ( ( readTimeoutMS.intValue() > 50 ) || ( 0 == readTimeoutMS.intValue() ) ) )
            {
               request.setReadTimeout( readTimeoutMS.intValue() );
            }

            InputStreamReader reader = null;

            try
            {
               if ( null != bodyContent )
               {
                  OutputStream output = request.getOutputStream();

                  output.write( bodyContent.getBytes( "UTF-8" ) );

                  output.close();
               }

               InputStream rqStream = null;

               try
               {
                  rqStream = request.getInputStream();
               }
               catch ( IOException ioe )
               {
                  rqStream = request.getErrorStream();
               }

               PushbackInputStream input = new PushbackInputStream( rqStream, 2 );

               byte[] signature = new byte[ 2 ];

               int bytesRead = input.read( signature );

               if ( 2 == bytesRead )
               {
                  input.unread( signature );

                  if ( ( signature[ 0 ] == gzip_prefix[ 0 ] ) && ( signature[ 1 ] == gzip_prefix[ 1 ] ) )
                  {
                     reader = new InputStreamReader( new GZIPInputStream( input ) );
                  }
                  else
                  {
                     reader = new InputStreamReader( input, "UTF-8" );
                  }
               }
               else if ( 0 < bytesRead ) // 1 byte
               {
                  input.unread( signature, 0, bytesRead );

                  reader = new InputStreamReader( input, "UTF-8" );
               }
               else
               {
                  reader = new InputStreamReader( input, "UTF-8" );
               }
            }
            catch ( UnknownHostException uhe )
            {
               diagout.println( "submitRequest( targetUrl = '" + targetUrl + "' ): couldn't resolve host name in request" );

               diagout.println( uhe.getMessage() );

               return null;
            }
            catch ( ConnectException ce )
            {
               diagout.println( "submitRequest( targetURL = '" + targetUrl + "' ): couldn't receive answer from server" );

               diagout.println( ce.getMessage() );

               return null;
            }

            BufferedReader response = new BufferedReader( reader );

            String line;

            while ( null != ( line = response.readLine() ) )
            {
               buffer.append( line + "\n" );
            }

            response.close();

            Set< String > rsHeader = request.getHeaderFields().keySet();

            if ( null == result )
            {
               result = new HttpsResponse();
            }

            for ( Iterator< String > keys = rsHeader.iterator(); keys.hasNext(); )
            {
               String name = keys.next();

               if ( null != name )
               {
                  List< String > fields = request.getHeaderFields().get( name );

                  for ( int i = 0; i < fields.size(); i++ )
                  {
                     String value = fields.get( i );

                     value = value.trim();

                     if ( null == result.header )
                     {
                        result.header = new HashMap< String, List< String > >();
                     }

                     if ( null == result.header.get( name.toLowerCase() ) )
                     {
                        List< String > vector = new LinkedList< String >();

                        vector.add( value );

                        result.header.put( name.toLowerCase(), vector );
                     }
                     else
                     {
                        result.header.get( name.toLowerCase() ).add( value );
                     }

                     if ( "Set-Cookie".equalsIgnoreCase( name ) )
                     {
                        String cookie_components[] = value.split( ";" );

                        Cookie biscuit = new Cookie();

                        for ( int j = 0; j < cookie_components.length; j++ )
                        {
                           String component = cookie_components[ j ].trim();

                           Matcher segment = RSCookieCutter.matcher( component );

                           if ( segment.find() )
                           {
                              String segment_name = segment.group( 1 );
                              String segment_value = segment.group( 2 );

                              if ( "expires".equalsIgnoreCase( segment_name ) )
                              {
                                 biscuit.expires = segment_value;
                              }
                              else if ( "path".equalsIgnoreCase( segment_name ) )
                              {
                                 biscuit.path = segment_value;
                              }
                              else if ( "domain".equalsIgnoreCase( segment_name ) )
                              {
                                 biscuit.domain = segment_value;
                              }
                              else
                              {
                                 biscuit.name = segment_name;
                                 biscuit.value = segment_value;
                              }
                           }
                           else if ( "secure".equalsIgnoreCase( component ) )
                           {
                              biscuit.secure = true;
                           }
                        }

                        if ( ( null != biscuit.name ) && ( null != biscuit.value ) )
                        {
                           String path = "null";

                           if ( null != biscuit.path )
                           {
                              path = biscuit.path;
                           }

                           Map< String, Cookie > box = cookieMap.get( path );

                           if ( null == box )
                           {
                              box = new HashMap< String, Cookie >();

                              box.put( biscuit.name, biscuit );

                              cookieMap.put( path, box );
                           }
                           else
                           {
                              box.put( biscuit.name, biscuit );
                           }
                        }

                        List< String > hdr_content = null;

                        if ( null != result.header )
                        {
                           hdr_content = result.header.get( name );
                        }
                        else
                        {
                           result.header = new HashMap< String, List< String > >();
                        }

                        if ( null == hdr_content )
                        {
                           hdr_content = new LinkedList< String >();
                        }

                        hdr_content.add( value );

                        result.header.put( name, hdr_content );
                     }
                  }
               }
               else
               {
                  String firstLine = request.getHeaderField( name );  // name == null

                  Matcher status = RSStatus.matcher( firstLine );

                  if ( status.find() )
                  {
                     result.http_version = status.group( 1 );
                     result.status_code = status.group( 2 );
                     result.reason_phrase = status.group( 3 );
                  }
               }
            }

            result.content = buffer.toString();
         }
         catch ( IOException ioe )
         {
            diagout.println( "submitRequest( targetURL = '" + targetUrl
                          + "' ): is this a valid request to a working https server?" );

            diagout.println( ioe.getMessage() );

            return null;
         }

         // compose cookie information
         if ( cookieMap.size() > 0 )
         {
            cookie_content = new StringBuffer( 1000 );
            secure_cookie = new StringBuffer( 1000 );

            String[] path = new String[ cookieMap.size() ];

            path = cookieMap.keySet().toArray( path );

            Arrays.sort( path, new NaturalStringComparator() );

            for ( int i = 0; i < path.length; i++ )
            {
               Map< String, Cookie > box = cookieMap.get( path[ i ] );

               if ( ( null != box ) && ( 0 < box.size() ) )
               {
                  String[] name = new String[ box.size() ];

                  name = box.keySet().toArray( name );

                  Arrays.sort( name, new NaturalStringComparator() );

                  for ( int j = 0; j < name.length; j++ )
                  {
                     Cookie delicious = box.get( name[ j ] );

                     Date expiryDate = null;

                     if ( null != delicious.expires )
                     {
                        try
                        {
                           expiryDate = ietfDateFormat.parse( delicious.expires );

                           Date now = new Date();

                           if ( expiryDate.after( now ) )
                           {
                              expiryDate = null;
                           }
                        }
                        catch ( ParseException pe )
                        {
                           diagout.println( "cookie expiry of '" + delicious.expires + "' doesn't appear to be valid." );

                           diagout.println( pe.getMessage() );
                        }
                     }

                     if ( null == expiryDate )
                     {
                        if ( !delicious.secure )
                        {
                           if ( 0 == cookie_content.length() )
                           {
                              cookie_content.append( delicious.name + "=" + delicious.value );
                           }
                           else
                           {
                              cookie_content.append( "; " + delicious.name + "=" + delicious.value );
                           }
                        }
                        else
                        {
                           if ( 0 == secure_cookie.length() )
                           {
                              secure_cookie.append( delicious.name + "=" + delicious.value );
                           }
                           else
                           {
                              secure_cookie.append( "; " + delicious.name + "=" + delicious.value );
                           }
                        }
                     }
                  }
               }
            }
         }

         if ( ( null != result ) && ( null != result.status_code ) )
         {
            // http://www.w3.org/Protocols/rfc2616/rfc2626-sec10.html
            if ( ( "301".equals( result.status_code ) || "302".equals( result.status_code )
                   || "303".equals( result.status_code ) || "307".equals( result.status_code ) )
                 && ( "GET".equalsIgnoreCase( method )  || "HEAD".equalsIgnoreCase( method ) ) )
            {
               redirectCount++;

               String location = null;

               if ( null != result.header )
               {
                  List< String > location_list = result.header.get( "location" );

                  if ( null != location_list )
                  {
                     location = location_list.get( 0 );
                  }
               }

               if ( null != location )
               {
                  buffer = null;

                  diagout.println( "redirect to location: '" + location + "'" );

                  try
                  {
                    if ( location.toLowerCase().startsWith( "http" ) )
                     {
                        url = new URL( location );

                        result = null;
                    }
                  }
                  catch ( MalformedURLException mue )
                  {
                     diagout.println( "Could not redirect on HTTP " + result.status_code + " to location '"
                                      + location + "'" );

                     break;
                  }

                  if ( null != result )
                  {
                     // honor /location relative paths
                     try
                     {
                        url = new URL( url.getProtocol() + "://" + url.getHost() + location );

                        result = null;
                     }
                     catch ( MalformedURLException mue )
                     {
                        diagout.println( "Could not redirect on HTTP " + result.status_code + " to location '"
                                         + url.getProtocol() + "://" + url.getHost() + location + "'" );

                        break;
                     }
                  }

                  if ( null == result )
                  {
                     buffer = new StringBuffer();
                  }
               }
               else
               {
                  break;
               }
            }
            else
            {
               break;
            }
         }
         else
         {
            break;
         }
      }

      if ( cookieMap.size() > 0 )
      {
         result.cookies = cookieMap;
      }

      return result;
   }
}

