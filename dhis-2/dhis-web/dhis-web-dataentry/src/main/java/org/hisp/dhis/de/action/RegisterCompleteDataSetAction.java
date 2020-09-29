package org.hisp.dhis.de.action;

/*
 * Copyright (c) 2004-2012, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * * Neither the name of the HISP project nor the names of its contributors may
 *   be used to endorse or promote products derived from this software without
 *   specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hisp.dhis.aggregation.AggregationService;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementCategoryOptionCombo;
import org.hisp.dhis.dataelement.DataElementCategoryService;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.dataset.CompleteDataSetRegistration;
import org.hisp.dhis.dataset.CompleteDataSetRegistrationService;
import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.dataset.DataSetService;
import org.hisp.dhis.datavalue.DataValue;
import org.hisp.dhis.datavalue.DataValueService;
import org.hisp.dhis.i18n.I18nFormat;
import org.hisp.dhis.indicator.Indicator;
import org.hisp.dhis.message.MessageService;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodType;
import org.hisp.dhis.user.CurrentUserService;

import com.opensymphony.xwork2.Action;

/**
 * @author Lars Helge Overland
 */
public class RegisterCompleteDataSetAction
    implements Action
{
    private static final Log log = LogFactory.getLog( RegisterCompleteDataSetAction.class );

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private CompleteDataSetRegistrationService registrationService;

    public void setRegistrationService( CompleteDataSetRegistrationService registrationService )
    {
        this.registrationService = registrationService;
    }

    private DataSetService dataSetService;

    public void setDataSetService( DataSetService dataSetService )
    {
        this.dataSetService = dataSetService;
    }

    private OrganisationUnitService organisationUnitService;

    public void setOrganisationUnitService( OrganisationUnitService organisationUnitService )
    {
        this.organisationUnitService = organisationUnitService;
    }

    private CurrentUserService currentUserService;

    public void setCurrentUserService( CurrentUserService currentUserService )
    {
        this.currentUserService = currentUserService;
    }

    private MessageService messageService;

    public void setMessageService( MessageService messageService )
    {
        this.messageService = messageService;
    }

    //
    private DataValueService dataValueService;

    public void setDataValueService( DataValueService dataValueService )
    {
        this.dataValueService = dataValueService;
    }
    
    private DataElementService dataElementService;

    public void setDataElementService( DataElementService dataElementService )
    {
        this.dataElementService = dataElementService;
    }

    private AggregationService aggregationService;

    public void setAggregationService( AggregationService aggregationService )
    {
        this.aggregationService = aggregationService;
    }
    
    private DataElementCategoryService categoryService;

    public void setCategoryService( DataElementCategoryService categoryService )
    {
        this.categoryService = categoryService;
    }
    
    
    private I18nFormat format;

    public void setFormat( I18nFormat format )
    {
        this.format = format;
    }

    // -------------------------------------------------------------------------
    // Input
    // -------------------------------------------------------------------------

    private String periodId;

    public void setPeriodId( String periodId )
    {
        this.periodId = periodId;
    }

    private Integer dataSetId;

    public void setDataSetId( Integer dataSetId )
    {
        this.dataSetId = dataSetId;
    }

    private Integer organisationUnitId;

    public void setOrganisationUnitId( Integer organisationUnitId )
    {
        this.organisationUnitId = organisationUnitId;
    }

    private int statusCode;

    public int getStatusCode()
    {
        return statusCode;
    }

    private Map<Integer, Integer> indicatorDeMap;
    
    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------

    public String execute()
    {
        CompleteDataSetRegistration registration = new CompleteDataSetRegistration();

        DataSet dataSet = dataSetService.getDataSet( dataSetId );
        Period period = PeriodType.createPeriodExternalId( periodId );
        OrganisationUnit organisationUnit = organisationUnitService.getOrganisationUnit( organisationUnitId );

        String storedBy = currentUserService.getCurrentUsername();

        // ---------------------------------------------------------------------
        // Check locked status
        // ---------------------------------------------------------------------

        if ( dataSetService.isLocked( dataSet, period, organisationUnit, null ) )
        {
            return logError( "Entry locked for combination: " + dataSet + ", " + period + ", " + organisationUnit, 2 );
        }

        // ---------------------------------------------------------------------
        // Register as completed dataSet
        // ---------------------------------------------------------------------

        if ( registrationService.getCompleteDataSetRegistration( dataSet, period, organisationUnit ) == null )
        {
            registration.setDataSet( dataSet );
            registration.setPeriod( period );
            registration.setSource( organisationUnit );
            registration.setDate( new Date() );
            registration.setStoredBy( storedBy );

            registrationService.saveCompleteDataSetRegistration( registration );

            log.info( "DataSet registered as complete: " + registration );

            registration.getPeriod().setName( format.formatPeriod( registration.getPeriod() ) );

            messageService.sendCompletenessMessage( registration );
        }
        
        Date now = new Date();
        initializeIndicatorDeMap();
        
        List<Indicator> indicators = new ArrayList<Indicator>(  dataSet.getIndicators());
        
        //dataValues = dataValueService.getDataValues( organisationUnit, period, dataSet.getDataElements() );
        
        String[] calulatePeriod = periodId.split( "_" );
        String nextPeriod = null;
        
        if ( calulatePeriod[0].equalsIgnoreCase( "monthly" ) )
        {
            String[] splitperiod = calulatePeriod[1].split( "-" );
            if ( Integer.parseInt( splitperiod[1] ) > 1 || Integer.parseInt( splitperiod[1] ) < 12 )
            {
                int month = Integer.parseInt( splitperiod[1] ) + 1;
                nextPeriod = calulatePeriod[0] + "_" + splitperiod[0] + "-" + month + "-" + splitperiod[2];
            }
            else if ( Integer.parseInt( splitperiod[1] ) == 12 )
            {
                int year = Integer.parseInt( splitperiod[2] ) + 1;
                nextPeriod = calulatePeriod[0] + "_" + splitperiod[0] + "-" + 1 + "-" + year;
            }
        }
        
        Period nextperiodId = PeriodType.createPeriodExternalId( nextPeriod );
        
        System.out.println( " indicator size " + indicators.size() + " deMap size -- "  + indicatorDeMap.size() );
        
        /*
        List<Integer> indicators = new ArrayList<Integer>();
        indicators.add(211); indicators.add(212); indicators.add(213); indicators.add(214);
        indicators.add(215); indicators.add(216); indicators.add(217); indicators.add(218);
        indicators.add(219); indicators.add(220); indicators.add(221); indicators.add(222);
        indicators.add(223); indicators.add(224); indicators.add(225); indicators.add(226);
        indicators.add(227); indicators.add(228); indicators.add(229); indicators.add(230);
        indicators.add(231); indicators.add(232); indicators.add(233); indicators.add(234);
        indicators.add(235); indicators.add(236); indicators.add(237); indicators.add(238);
        indicators.add(239); indicators.add(240); indicators.add(241); indicators.add(242);
        indicators.add(243); indicators.add(244); indicators.add(245); indicators.add(246);
        indicators.add(247); indicators.add(248); indicators.add(249); indicators.add(250);
        indicators.add(251); indicators.add(252);
        */
        
        int optionComboId = 2;
        DataElementCategoryOptionCombo optionCombo = categoryService.getDataElementCategoryOptionCombo( optionComboId );
        
        for( Indicator selIndicator : indicators )
        {
            //Indicator selIndicator = indicatorService.getIndicator( ind );
            
            double indicatorValue = 0.0;
            Double indValue = aggregationService.getAggregatedIndicatorValue( selIndicator, registration.getPeriod().getStartDate(), registration.getPeriod().getEndDate(), organisationUnit );
            
            //&& indValue != 0.0
            if( indValue != null  )
            {
                //System.out.println( selIndicator.getName() + "  indicator value -- " + indValue );
                indicatorValue = indValue;
                String value = ""+(int)indicatorValue;
                if ( value != null && value.trim().length() == 0 )
                {
                    value = null;
                }
                
                if ( value != null )
                {
                    value = value.trim();
                }
                
                DataElement dataElement = dataElementService.getDataElement( indicatorDeMap.get( selIndicator.getId() ) );
                
                DataValue dataValue = dataValueService.getDataValue( organisationUnit, dataElement, nextperiodId, optionCombo );

                if ( dataValue == null )
                {
                    if ( value != null )
                    {
                        dataValue = new DataValue( dataElement, nextperiodId, organisationUnit, value, storedBy, now, null, optionCombo );
                        dataValueService.addDataValue( dataValue );
                    }
                }
                else
                {
                    dataValue.setValue( value );
                    dataValue.setTimestamp( now );
                    dataValue.setStoredBy( storedBy );

                    dataValueService.updateDataValue( dataValue );
                }
            }

        }
        
        /*
        double numVal = 0.0;
        double denVal = 0.0;
        double indValue = 0.0;
        Double numValue = 0.0;
        Double denValue = 0.0;
        
        Indicator selIndicator = indicatorService.getIndicator( 211 );
        Double temp1 = aggregationService.getAggregatedIndicatorValue( selIndicator, registration.getPeriod().getStartDate(), registration.getPeriod().getEndDate(), organisationUnit );
        if( temp1 == null ) temp1 = 0.0;
       
        System.out.println( "indicator value 1 -- " + temp1 );
        
        Double temp2 = aggregationService.getAggregatedNumeratorValue( selIndicator, registration.getPeriod().getStartDate(), registration.getPeriod().getEndDate(), organisationUnit );
        if( temp2 == null ) temp2 = 0.0;
        numVal = temp2;
        Double temp3 = aggregationService.getAggregatedDenominatorValue( selIndicator, registration.getPeriod().getStartDate(), registration.getPeriod().getEndDate(), organisationUnit );
        if( temp3 == null ) temp3 = 0.0;
        denVal = temp3;
        
        
        try
        {
            if( denVal != 0.0 )
            {
                indValue = ( numVal / denVal ) * selIndicator.getIndicatorType().getFactor();
            }
            else
            {
                indValue = 0.0;
            }
        }
        catch( Exception e )
        {
            indValue = 0.0;
        }
        
        System.out.println( "indicator value 2 -- " + indValue );
        
        indValue = Math.round( indValue * Math.pow( 10, 1 ) ) / Math.pow( 10, 1 );
        
        System.out.println( "indicator value 3 -- " + (int)indValue );
       */         
        
        return SUCCESS;
    }

    // -------------------------------------------------------------------------
    // Supportive methods
    // -------------------------------------------------------------------------

    private String logError( String message, int statusCode )
    {
        log.info( message );

        this.statusCode = statusCode;

        return SUCCESS;
    }
    
    public void initializeIndicatorDeMap()
    {
        indicatorDeMap = new HashMap<Integer, Integer>();
    
        indicatorDeMap.put( 211, 344 );
        indicatorDeMap.put( 212, 5939 );
        indicatorDeMap.put( 213, 345 );
        indicatorDeMap.put( 214, 346 );
        indicatorDeMap.put( 215, 347 );
        indicatorDeMap.put( 216, 348 );
        indicatorDeMap.put( 217, 349 );
        indicatorDeMap.put( 218, 350 );
        indicatorDeMap.put( 219, 351 );
        indicatorDeMap.put( 220, 6163 );
        indicatorDeMap.put( 221, 6164 );
        indicatorDeMap.put( 222, 6165 );
        indicatorDeMap.put( 223, 6166 );
        indicatorDeMap.put( 224, 6167 );
        indicatorDeMap.put( 225, 352 );
        indicatorDeMap.put( 226, 6168 );
        indicatorDeMap.put( 227, 353 );
        indicatorDeMap.put( 228, 354 );
        indicatorDeMap.put( 229, 355 );
        indicatorDeMap.put( 230, 6169 );
        indicatorDeMap.put( 231, 6170 );
        indicatorDeMap.put( 232, 356 );
        indicatorDeMap.put( 233, 6171 );
        indicatorDeMap.put( 234, 358 );
        indicatorDeMap.put( 235, 359 );
        indicatorDeMap.put( 236, 360 );
        indicatorDeMap.put( 237, 361 );
        indicatorDeMap.put( 238, 362 );
        indicatorDeMap.put( 239, 363 );
        indicatorDeMap.put( 240, 6172 );
        indicatorDeMap.put( 241, 6173 );
        indicatorDeMap.put( 242, 364 );
        indicatorDeMap.put( 243, 6174 );
        indicatorDeMap.put( 244, 366 );
        indicatorDeMap.put( 245, 367 );
        indicatorDeMap.put( 246, 6175 );
        indicatorDeMap.put( 247, 6176 );
        indicatorDeMap.put( 248, 6177 );
        indicatorDeMap.put( 249, 6178 );
        indicatorDeMap.put( 250, 372 );
        indicatorDeMap.put( 251, 373 );
        indicatorDeMap.put( 252, 374 );
        
        
    }

    
    
    
    
}
