package com.queue_eh.framework.v1;

import java.util.Comparator;

public class NaturalStringComparator implements Comparator< String >
{
   // Orders a string containing natural numbers by ordinal value where the strings are otherwise equal

   public int compare( String lhs, String rhs )
   {
      int lhs_index = 0;
      int rhs_index = 0;

      if ( ( null == lhs ) && ( null != rhs ) )
      {
         return -1;
      }

      if (  ( null != lhs ) && ( null == rhs ) )
      {
         return 1;
      }

      if (  ( null == lhs ) && ( null == rhs ) )
      {
         return 0;
      }

      char lhs_content[] = lhs.toCharArray();
      char rhs_content[] = rhs.toCharArray();

      while ( ( lhs_index < lhs_content.length ) && ( rhs_index < rhs_content.length ) )
      {
         if ( ( ( '0' <= lhs_content[ lhs_index ] ) && ( '9' >= lhs_content[ lhs_index ] ) )
              && ( ( '0' <= rhs_content[ rhs_index ] ) && ( '9' >= rhs_content[ rhs_index ] ) ) )
         {
            // compare number segment
            char lhs_buffer[] = new char[ lhs_content.length - lhs_index + 1 ];
            char rhs_buffer[] = new char[ rhs_content.length - rhs_index + 1 ];

            int lhs_length = 0;
            int rhs_length = 0;

            // accumulate rhs content
            while ( ( rhs_index < ( rhs_content.length - 1 ) )
                    && ( ( '0' <= rhs_content[ rhs_index + 1 ] )
                    && ( '9' >= rhs_content[ rhs_index + 1 ] ) ) )
            {
               rhs_buffer[ rhs_length++ ] = rhs_content[ rhs_index++ ];
            }

            rhs_buffer[ rhs_length ] = rhs_content[ rhs_index ];
            rhs_buffer[ rhs_length + 1 ] = '\0';

            // accumulate lhs content
            while ( ( lhs_index < ( lhs_content.length - 1 ) )
                    && ( ( '0' <= lhs_content[ lhs_index + 1 ] )
                    && ( '9' >= lhs_content[ lhs_index + 1 ] ) ) )
            {
               lhs_buffer[ lhs_length++ ] = lhs_content[ lhs_index++ ];
            }

            lhs_buffer[ lhs_length ] = lhs_content[ lhs_index ];
            lhs_buffer[ lhs_length + 1 ] = '\0';

            if ( lhs_length > rhs_length )
            {
               return 1;
            }
            else if ( lhs_length < rhs_length )
            {
               return -1;
            }

            Long lhs_value = Long.parseLong( String.valueOf( lhs_buffer, 0, lhs_length + 1 ) );
            Long rhs_value = Long.parseLong( String.valueOf( rhs_buffer, 0, rhs_length + 1 ) );

            if ( lhs_value > rhs_value )
            {
               return 1;
            }
            else if ( lhs_value < rhs_value )
            {
               return -1;
            }
         }
         else
         {
            // compare non-numerical segment
            if ( lhs_content[ lhs_index ] > rhs_content[ rhs_index ] )
            {
               return 1;
            }
            else if ( lhs_content[ lhs_index ] < rhs_content[ rhs_index ] )
            {
               return -1;
            }
         }

         lhs_index++;
         rhs_index++;
      }

      return 0;
   }
}
