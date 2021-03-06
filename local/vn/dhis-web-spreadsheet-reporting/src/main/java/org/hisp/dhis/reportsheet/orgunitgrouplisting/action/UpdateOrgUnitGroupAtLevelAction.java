/*
 * Copyright (c) 2004-2011, University of Oslo
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
package org.hisp.dhis.reportsheet.orgunitgrouplisting.action;

import java.util.HashMap;
import java.util.Map;

import org.hisp.dhis.organisationunit.OrganisationUnitGroup;
import org.hisp.dhis.organisationunit.OrganisationUnitGroupService;
import org.hisp.dhis.organisationunit.OrganisationUnitLevel;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.reportsheet.ExportReportService;
import org.hisp.dhis.reportsheet.ExportReportOganiztionGroupListing;

import com.opensymphony.xwork2.Action;

/**
 * @author Tran Thanh Tri
 * @version $Id$
 */
public class UpdateOrgUnitGroupAtLevelAction
    implements Action
{
    // -------------------------------------------------------------------------
    // Dependency
    // -------------------------------------------------------------------------

    private ExportReportService exportReportService;

    public void setExportReportService( ExportReportService exportReportService )
    {
        this.exportReportService = exportReportService;
    }

    private OrganisationUnitService organisationUnitService;

    public void setOrganisationUnitService( OrganisationUnitService organisationUnitService )
    {
        this.organisationUnitService = organisationUnitService;
    }

    private OrganisationUnitGroupService organisationUnitGroupService;

    public void setOrganisationUnitGroupService( OrganisationUnitGroupService organisationUnitGroupService )
    {
        this.organisationUnitGroupService = organisationUnitGroupService;
    }

    // -------------------------------------------------------------------------
    // Input & Output
    // -------------------------------------------------------------------------

    private Integer exportReportId;

    public void setExportReportId( Integer exportReportId )
    {
        this.exportReportId = exportReportId;
    }

    private Integer orgUnitGroupId;

    public void setOrgUnitGroupId( Integer orgUnitGroupId )
    {
        this.orgUnitGroupId = orgUnitGroupId;
    }

    private Integer levelId;

    public void setLevelId( Integer levelId )
    {
        this.levelId = levelId;
    }

    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------

    public String execute()
        throws Exception
    {
        ExportReportOganiztionGroupListing exportReport = (ExportReportOganiztionGroupListing) exportReportService
            .getExportReport( exportReportId );

        Map<OrganisationUnitGroup, OrganisationUnitLevel> orgUniGroupAtLevels = new HashMap<OrganisationUnitGroup, OrganisationUnitLevel>(
            exportReport.getOrganisationUnitLevels() );

        OrganisationUnitGroup organisationUnitGroup = organisationUnitGroupService
            .getOrganisationUnitGroup( orgUnitGroupId );

        if ( levelId != null && levelId != -1 )
        {
            OrganisationUnitLevel organisationUnitLevel = organisationUnitService.getOrganisationUnitLevel( levelId );

            orgUniGroupAtLevels.put( organisationUnitGroup, organisationUnitLevel );
        }
        else
        {
            orgUniGroupAtLevels.remove( organisationUnitGroup );
        }

        exportReport.setOrganisationUnitLevels( orgUniGroupAtLevels );

        exportReportService.updateExportReport( exportReport );

        return SUCCESS;
    }

}
