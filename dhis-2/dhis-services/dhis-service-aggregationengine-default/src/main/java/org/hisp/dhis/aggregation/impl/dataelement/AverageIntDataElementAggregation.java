package org.hisp.dhis.aggregation.impl.dataelement;

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

import static org.hisp.dhis.system.util.DateUtils.getDaysInclusive;

import java.util.Collection;
import java.util.Date;

import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementCategoryOptionCombo;
import org.hisp.dhis.datavalue.DataValue;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitHierarchy;
import org.hisp.dhis.period.Period;

/**
 * @author Lars Helge Overland
 * @version $Id: AverageIntDataElementAggregation.java 4753 2008-03-14 12:48:50Z larshelg $
 */
public class AverageIntDataElementAggregation
    extends AbstractDataElementAggregation
{
    public Double getAggregatedValue( DataElement dataElement, DataElementCategoryOptionCombo optionCombo, Date aggregationStartDate, Date aggregationEndDate,
        OrganisationUnit organisationUnit )
    {
        double totalSum = 0;

        boolean valuesRegistered = false;

        OrganisationUnitHierarchy hierarchy = aggregationCache.getOrganisationUnitHierarchy();
        
        Collection<Integer> organisationUnitIds = hierarchy.getChildren( organisationUnit.getId() );
        
        for ( Integer id : organisationUnitIds )
        {
            double[] sums = getSumAndRelevantDays( dataElement, optionCombo, aggregationStartDate, aggregationEndDate, id );
    
            if ( sums[1] > 0 )
            {
                double average = sums[0] / sums[1];
    
                totalSum += average;
    
                valuesRegistered = true;
            }
        }

        return valuesRegistered ? totalSum : null;
    }

    /**
     * Retrieves the datavalues registered for the given dataelement, source and
     * hierarchy.
     * 
     * @param dataElement the dataelement
     * @param organisationUnit The organisationUnit
     * @param hierarchyId Id of the hierarchy
     * @return Datavalues registered for the given dataelement, organisationUnit
     *         and hierarchy.
     * @throws AggregationStoreException
     */
    protected Collection<DataValue> getDataValues( Integer dataElementId, Integer optionComboId, Integer organisationUnitId,
        Date startDate, Date endDate )
    {   
        Collection<Integer> periods = aggregationCache.getIntersectingPeriodIds( startDate, endDate );
        
        Collection<DataValue> values = aggregationStore.getDataValues( organisationUnitId, dataElementId, optionComboId, periods );
        
        return values;
    }

    /**
     * Calculates the AVERAGE for DataValues which period is within or
     * overlapping startDate and / or endDate. This method is typically used for
     * semi-permanent DataValues. It handles the four situations where a
     * DataValue will count for the period that startDate to endDate constitutes (
     * referred to as d1 and d2 ):
     * <p>
     * 1. startDate and endDate is between d1 - d2<br>
     * 2. startDate is before d1 and endDate is between d1 - d2<br>
     * 3. startDate is between d1 - d2 and endDate is after d2<br>
     * 4. startDate is before d1 and endDate is after d2<br>
     * <p>
     * The method iterates over every DataValue in the passed Collection. The
     * number of days in each DataValue's period between d1 - d2 is multiplied
     * with the DataValue's value, summarized and returned at index 0. The
     * number of days in each DataValue's period between d1 - d2 is summarized
     * and returned at index 1.
     * 
     * @param dataValues The datavalues to aggregate
     * @param startDate Start date of the period to aggregate over
     * @param endDate End date of the period to aggregate over
     * @param aggregationStartDate The original start date of the entire
     *        aggregation period
     * @param aggregationEndDate The original end date of the entire aggregation
     *        period
     * @return The AVERAGE value.
     */
    protected double[] getAggregateOfValues( Collection<DataValue> dataValues, Date startDate, Date endDate,
        Date aggregationStartDate, Date aggregationEndDate )
    {
        double totalSum = 0;
        double totalRelevantDays = 0;

        for ( DataValue dataValue : dataValues )
        {
            final Period currentPeriod = aggregationCache.getPeriod( dataValue.getPeriod().getId() );
            final Date currentStartDate = currentPeriod.getStartDate();
            final Date currentEndDate = currentPeriod.getEndDate();

            double value = 0;

            try
            {
                value = Double.parseDouble( dataValue.getValue() );
            }
            catch ( Exception ex )
            {
            }

            long relevantDays = 0;

            if ( currentStartDate.compareTo( startDate ) >= 0 && currentEndDate.compareTo( endDate ) <= 0 )
            {
                relevantDays = getDaysInclusive( currentStartDate, currentEndDate );
            }
            else if ( currentStartDate.compareTo( startDate ) <= 0 && currentEndDate.compareTo( startDate ) >= 0
                && currentEndDate.compareTo( endDate ) <= 0 )
            {
                relevantDays = getDaysInclusive( startDate, currentEndDate );
            }
            else if ( currentStartDate.compareTo( startDate ) >= 0 && currentStartDate.compareTo( endDate ) <= 0
                && currentEndDate.compareTo( endDate ) >= 0 )
            {
                relevantDays = getDaysInclusive( currentStartDate, endDate );
            }
            else if ( currentStartDate.compareTo( startDate ) <= 0 && currentEndDate.compareTo( endDate ) >= 0 )
            {
                relevantDays = getDaysInclusive( startDate, endDate );
            }

            totalSum += value * relevantDays;
            totalRelevantDays += relevantDays;
        }

        double[] fraction = { totalSum, totalRelevantDays };

        return fraction;
    }
}
