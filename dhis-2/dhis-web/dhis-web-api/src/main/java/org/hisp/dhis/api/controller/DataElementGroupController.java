package org.hisp.dhis.api.controller;

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

import org.hisp.dhis.api.utils.IdentifiableObjectParams;
import org.hisp.dhis.api.utils.ObjectPersister;
import org.hisp.dhis.api.utils.WebLinkPopulator;
import org.hisp.dhis.api.view.Jaxb2Utils;
import org.hisp.dhis.common.Pager;
import org.hisp.dhis.dataelement.DataElementGroup;
import org.hisp.dhis.dataelement.DataElementGroups;
import org.hisp.dhis.dataelement.DataElementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping( value = DataElementGroupController.RESOURCE_PATH )
public class DataElementGroupController
{
    public static final String RESOURCE_PATH = "/dataElementGroups";

    @Autowired
    private DataElementService dataElementService;

    @Autowired
    private ObjectPersister objectPersister;

    //-------------------------------------------------------------------------------------------------------
    // GET
    //-------------------------------------------------------------------------------------------------------

    @RequestMapping( method = RequestMethod.GET )
    public String getDataElementGroups( IdentifiableObjectParams params, Model model, HttpServletRequest request )
    {
        DataElementGroups dataElementGroups = new DataElementGroups();

        if ( params.isPaging() )
        {
            int total = dataElementService.getDataElementGroupCount();

            Pager pager = new Pager( params.getPage(), total );
            dataElementGroups.setPager( pager );

            List<DataElementGroup> dataElementGroupList = new ArrayList<DataElementGroup>(
                dataElementService.getDataElementGroupsBetween( pager.getOffset(), pager.getPageSize() ) );

            dataElementGroups.setDataElementGroups( dataElementGroupList );
        }
        else
        {
            dataElementGroups.setDataElementGroups( new ArrayList<DataElementGroup>( dataElementService.getAllDataElementGroups() ) );
        }

        if ( params.hasLinks() )
        {
            WebLinkPopulator listener = new WebLinkPopulator( request );
            listener.addLinks( dataElementGroups );
        }

        model.addAttribute( "model", dataElementGroups );

        return "dataElementGroups";
    }

    @RequestMapping( value = "/{uid}", method = RequestMethod.GET )
    public String getDataElementGroup( @PathVariable( "uid" ) String uid, IdentifiableObjectParams params, Model model, HttpServletRequest request )
    {
        DataElementGroup dataElementGroup = dataElementService.getDataElementGroup( uid );

        if ( params.hasLinks() )
        {
            WebLinkPopulator listener = new WebLinkPopulator( request );
            listener.addLinks( dataElementGroup );
        }

        model.addAttribute( "model", dataElementGroup );

        return "dataElementGroup";
    }

    //-------------------------------------------------------------------------------------------------------
    // POST
    //-------------------------------------------------------------------------------------------------------

    @RequestMapping( method = RequestMethod.POST, headers = {"Content-Type=application/xml, text/xml"} )
    @PreAuthorize( "hasRole('ALL') or hasRole('F_WEBAPI_CREATE')" )
    public void postDataElementGroupXML( HttpServletResponse response, InputStream input ) throws Exception
    {
        DataElementGroup dataElementGroup = Jaxb2Utils.unmarshal( DataElementGroup.class, input );
        postDataElementGroup( dataElementGroup, response );
    }

    @RequestMapping( method = RequestMethod.POST, headers = {"Content-Type=application/json"} )
    @PreAuthorize( "hasRole('ALL') or hasRole('F_WEBAPI_CREATE')" )
    public void postDataElementGroupJSON( HttpServletResponse response, InputStream input ) throws Exception
    {
        throw new HttpRequestMethodNotSupportedException( RequestMethod.POST.toString() );
    }

    public void postDataElementGroup( DataElementGroup dataElementGroup, HttpServletResponse response )
    {
        if ( dataElementGroup == null )
        {
            response.setStatus( HttpServletResponse.SC_NOT_IMPLEMENTED );
        }
        else
        {
            try
            {
                dataElementGroup = objectPersister.persistDataElementGroup( dataElementGroup );

                if ( dataElementGroup.getUid() == null )
                {
                    response.setStatus( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
                }
                else
                {
                    response.setStatus( HttpServletResponse.SC_CREATED );
                    response.setHeader( "Location", DataElementController.RESOURCE_PATH + "/" + dataElementGroup.getUid() );
                }
            } catch ( Exception e )
            {
                response.setStatus( HttpServletResponse.SC_CONFLICT );
            }
        }
    }

    //-------------------------------------------------------------------------------------------------------
    // PUT
    //-------------------------------------------------------------------------------------------------------

    @RequestMapping( value = "/{uid}", method = RequestMethod.PUT, headers = {"Content-Type=application/xml, text/xml"} )
    @ResponseStatus( value = HttpStatus.NO_CONTENT )
    @PreAuthorize( "hasRole('ALL') or hasRole('F_WEBAPI_UPDATE')" )
    public void putDataElementGroupXML( @PathVariable( "uid" ) String uid, InputStream input ) throws Exception
    {
        throw new HttpRequestMethodNotSupportedException( RequestMethod.DELETE.toString() );
    }

    @RequestMapping( value = "/{uid}", method = RequestMethod.PUT, headers = {"Content-Type=application/json"} )
    @ResponseStatus( value = HttpStatus.NO_CONTENT )
    @PreAuthorize( "hasRole('ALL') or hasRole('F_WEBAPI_UPDATE')" )
    public void putDataElementGroupJSON( @PathVariable( "uid" ) String uid, InputStream input ) throws Exception
    {
        throw new HttpRequestMethodNotSupportedException( RequestMethod.PUT.toString() );
    }

    //-------------------------------------------------------------------------------------------------------
    // DELETE
    //-------------------------------------------------------------------------------------------------------

    @RequestMapping( value = "/{uid}", method = RequestMethod.DELETE )
    @ResponseStatus( value = HttpStatus.NO_CONTENT )
    @PreAuthorize( "hasRole('ALL') or hasRole('F_WEBAPI_DELETE')" )
    public void deleteDataElementGroup( @PathVariable( "uid" ) String uid ) throws Exception
    {
        // throw new HttpRequestMethodNotSupportedException( RequestMethod.DELETE.toString() );

        DataElementGroup dataElementGroup = dataElementService.getDataElementGroup( uid );

        if ( dataElementGroup != null )
        {
            dataElementService.deleteDataElementGroup( dataElementGroup );
        }
    }
}
