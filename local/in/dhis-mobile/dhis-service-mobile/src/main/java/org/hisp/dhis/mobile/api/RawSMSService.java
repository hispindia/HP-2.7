package org.hisp.dhis.mobile.api;

import java.util.Collection;

public interface RawSMSService
{
    String ID = RawSMSService.class.getName();
    
    // -------------------------------------------------------------------------
    // RawSMS
    // -------------------------------------------------------------------------

    void addRawSMS( RawSMS rawSMS );
    
    void updateRawSMS( RawSMS rawSMS );
    
    void deleteRawSMS( RawSMS rawSMS );
    
    Collection<RawSMS> getRawSMS( int start, int end );
    
    Collection<RawSMS> getAllRawSMS( );
    
    RawSMS getRawSMS( String senderInfo );
    
    long getRowCount();

    //String getRawSMS_CreateXML();
}
