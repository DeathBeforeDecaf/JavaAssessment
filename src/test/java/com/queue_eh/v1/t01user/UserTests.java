package com.queue_eh.v1.t01user;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.List;

import org.testng.annotations.Test;

import com.queue_eh.framework.v1.HttpsClientNonVerifying;
import com.queue_eh.framework.v1.HttpsClientNonVerifying.HttpsResponse;
import com.queue_eh.v1.BaseRequest;

public class UserTests extends BaseRequest
{
   @Test( enabled = true, groups = { "t01v1" } )
   public void getWeatherAPIGlossary()
   {
      // GET the Weather API glossary
      // Expect response status 200 and specific response content-type/body

      HttpsClientNonVerifying client = new HttpsClientNonVerifying();

      HttpsResponse httpsRS =
         client.submitRequest( "GET",
                               GLOSSARYV1_URI,
                               buildHeaders( ACCEPT_RSTYPE_JSONLD ),
                               null,
                               null,
                               20000,
                               30000,
                               5 );

      assertNotNull( httpsRS );

      // verify response status code
      assertEquals( "ERROR: Response status code not OK", "200", httpsRS.status_code );

      assertNotNull( httpsRS.header );

      assertTrue( httpsRS.header.containsKey( "content-type" ) );

      List< String > content_type = httpsRS.header.get( "content-type" );

      assertNotNull( content_type );

      assertEquals( 1, content_type.size() );

      assertEquals( "ERROR: Response Content-Type doesn't match requested value",
                    ACCEPT_RSTYPE_JSONLD, content_type.get( 0 ) );

      assertTrue( "ERROR: Expected response body larger than 1 byte", 1 < httpsRS.content.length() );

/*
      for ( String header_str : httpsRS.header.keySet() )
      {
         for ( String value : httpsRS.header.get( header_str ) )
         {
            System.out.println( header_str + ": " + value );
         }
      }

      System.out.println( httpsRS.content );
*/
   }

   @Test( enabled = true, groups = { "t01v1" } )
   public void getWeatherReportAirportLGA()
   {
      // Get the grid position for LGA
      // Get the forecast for the resulting grid position
      // Expect response status 200 and specific response content-type/body

      HttpsClientNonVerifying client = new HttpsClientNonVerifying();

      HttpsResponse httpsRS =
         client.submitRequest( "GET",
                               POINTSV1_URI + "/40.7731,-73.8756",   // LGA lat,long
                               buildHeaders( ACCEPT_RSTYPE_JSONLD ),
                               null,
                               null,
                               20000,
                               30000,
                               5 );

      assertNotNull( httpsRS );

      // verify response status code
      assertEquals( "ERROR: Response status code not OK", "200", httpsRS.status_code );

      assertNotNull( httpsRS.header );

      assertTrue( httpsRS.header.containsKey( "content-type" ) );

      List< String > content_type = httpsRS.header.get( "content-type" );

      assertNotNull( content_type );

      assertEquals( 1, content_type.size() );

      assertEquals( "ERROR: Response Content-Type doesn't match requested value",
                    ACCEPT_RSTYPE_JSONLD, content_type.get( 0 ) );

      assertTrue( "ERROR: Expected response body larger than 1 byte", 1 < httpsRS.content.length() );


      int forecastIdx = httpsRS.content.indexOf( "\"forecast\":" );

      assertTrue( "ERROR: Could not find 'forecast' URI", -1 != forecastIdx );

      int forecastValStart = httpsRS.content.indexOf( "\"", forecastIdx + "\"forecast\":".length() );

      assertTrue( "ERROR: Could not find 'forecast' URI value", -1 != forecastValStart );

      int forecastValEnd = httpsRS.content.indexOf( "\"", forecastValStart + 1 );

      assertTrue( "ERROR: Could not find 'forecast' URI termination", -1 != forecastValEnd );

      String forecast_uri = httpsRS.content.substring( forecastValStart + 1, forecastValEnd );

      assertNotNull( forecast_uri );

      assertTrue( forecast_uri.length() > WEATHER_API_HOST.length() );
/*
      for ( String header_str : httpsRS.header.keySet() )
      {
         for ( String value : httpsRS.header.get( header_str ) )
         {
            System.out.println( header_str + ": " + value );
         }
      }

      System.out.println( httpsRS.content );
*/


      // Test Step 2: Get the forecast for the resulting grid position

      httpsRS =
         client.submitRequest( "GET",
                               forecast_uri,   // /gridpoints/OKX/36,37/forecast
                               buildHeaders( ACCEPT_RSTYPE_JSONLD ),
                               null,
                               null,
                               20000,
                               30000,
                               5 );

      // verify response status code
      assertEquals( "ERROR: Response status code not OK", "200", httpsRS.status_code );

      assertNotNull( httpsRS.header );

      assertTrue( httpsRS.header.containsKey( "content-type" ) );

      content_type = httpsRS.header.get( "content-type" );

      assertNotNull( content_type );

      assertEquals( 1, content_type.size() );

      assertEquals( "ERROR: Response Content-Type doesn't match requested value",
                    ACCEPT_RSTYPE_JSONLD, content_type.get( 0 ) );

      assertTrue( "ERROR: Expected response body larger than 1 byte", 1 < httpsRS.content.length() );

      for ( String header_str : httpsRS.header.keySet() )
      {
         for ( String value : httpsRS.header.get( header_str ) )
         {
            System.out.println( header_str + ": " + value );
         }
      }

      System.out.println( httpsRS.content );
   }
}
