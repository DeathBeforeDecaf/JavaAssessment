package com.queue_eh.v1;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

public abstract class BaseRequest
{
   protected static String WEATHER_API_HOST = "https://api.weather.gov";

   protected static String ALERTSV1_URI = WEATHER_API_HOST + "/alerts";
   protected static String ALERTS_ACTIVEV1_URI = WEATHER_API_HOST + "/alerts/active";
   protected static String ALERTS_ACTIVE_COUNTV1_URI = WEATHER_API_HOST + "/alerts/active/count";

   protected static String ALERTS_TYPESV1_URI = WEATHER_API_HOST + "/alerts/types";
   protected static String GLOSSARYV1_URI = WEATHER_API_HOST + "/glossary";

   protected static String POINTSV1_URI = WEATHER_API_HOST + "/points";

   protected static String PRODUCTSV1_URI = WEATHER_API_HOST + "/products";
   protected static String PRODUCTS_LOCATIONSV1_URI = WEATHER_API_HOST + "/products/locations";

   protected static String RADAR_SERVERSV1_URI = WEATHER_API_HOST + "/radar/servers";

   protected static String RADAR_STATIONSV1_URI = WEATHER_API_HOST + "/radar/stations";

   protected static String STATIONSV1_URI = WEATHER_API_HOST + "/stations";

   protected static String ZONESV1_URI = WEATHER_API_HOST + "/zones";
/*
/alerts/active/zone/{zoneId}
/alerts/active/area/{area}
/alerts/active/region/{region}
/alerts/{id}

/points/{x},{y}

/gridpoints/{wfo}/{x},{y}
/gridpoints/{wfo}/{x},{y}/forecast
/gridpoints/{wfo}/{x},{y}/forecast/hourly
/gridpoints/{wfo}/{x},{y}/stations
/stations/{stationId}/observations
/stations/{stationId}/observations/latest
/stations/{stationId}/observations/{time}

/stations/{stationId}
/offices/{officeId}
/offices/{officeId}/headlines
/offices/{officeId}/headlines/{headlineId}
/points/{point}

/radar/servers/{id}

/radar/stations/{stationId}
/radar/stations/{stationId}/alarms
/radar/queues/{host}
/radar/profilers/{stationId}

/products/{productId}
/products/types/{typeId}
/products/types/{typeId}/locations
/products/types/{typeId}/locations/{locationId}

/zones/{type}
/zones/{type}/{zoneId}
/zones/{type}/{zoneId}/forecast
/zones/forecast/{zoneId}/observations
/zones/forecast/{zoneId}/stations
*/

   public static final String ACCEPT_RSTYPE_GEOJSON = "application/geo+json";
   public static final String ACCEPT_RSTYPE_JSONLD = "application/ld+json";
   public static final String ACCEPT_RSTYPE_DWML = "application/vnd.noaa.dwml+xml";
   public static final String ACCEPT_RSTYPE_OXML = "application/vnd.noaa.obs+xml";
   public static final String ACCEPT_RSTYPE_CAP = "application/cap+xml";
   public static final String ACCEPT_RSTYPE_ATOM = "application/atom+xml";

   public static final String ACCEPT_RSTYPE_JSON = "application/json";
   public static final String ACCEPT_RSTYPE_WILDCARD = "*/*";


   protected static PrintStream diagout = new PrintStream( OutputStream.nullOutputStream() );

   protected static Map< String, String[] > buildHeaders( String acceptType )
   {
      Map< String, String[] > result = new HashMap< String, String[] >();

      result.put( "Accept-Charset", new String[] { "UTF-8" } );
      result.put( "Accept", new String[] { acceptType } );

      return result;
   }
}
