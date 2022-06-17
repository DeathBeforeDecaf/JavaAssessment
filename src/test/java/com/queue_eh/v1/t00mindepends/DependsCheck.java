package com.queue_eh.v1.t00mindepends;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.List;

import org.testng.annotations.Test;

import com.queue_eh.framework.v1.HttpsClientNonVerifying;
import com.queue_eh.framework.v1.HttpsClientNonVerifying.HttpsResponse;

import com.queue_eh.v1.BaseRequest;

public class DependsCheck extends BaseRequest
{
   @Test( enabled = true, groups = { "t00v1" } )
   public void getWeatherAPIStatus()
   {
      // Submit a request to the Weather API to verify connectivity and availability to system under test
      // Expect response status 200 and non-trivial response content-type/body

      HttpsClientNonVerifying client = new HttpsClientNonVerifying();

      HttpsResponse httpsRS =
         client.submitRequest( "GET",
                               WEATHER_API_HOST,
                               buildHeaders( ACCEPT_RSTYPE_JSON ),
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
                    ACCEPT_RSTYPE_JSON, content_type.get( 0 ) );

      assertTrue( "ERROR: Expected response body larger than 1 byte", 1 < httpsRS.content.length() );

      assertTrue( "ERROR: Response must contain OK", httpsRS.content.contains( "OK" ) );
   }
}
