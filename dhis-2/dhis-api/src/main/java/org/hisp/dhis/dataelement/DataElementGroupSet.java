package org.hisp.dhis.dataelement;

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

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.hisp.dhis.common.BaseIdentifiableObject;
import org.hisp.dhis.common.Dxf2Namespace;
import org.hisp.dhis.common.adapter.DataElementGroupXmlAdapter;
import org.hisp.dhis.common.comparator.IdentifiableObjectNameComparator;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * DataElementGroupSet is a set of DataElementGroups. It is by default
 * exclusive, in the sense that a DataElement can only be a member of one or
 * zero of the DataElementGroups in a DataElementGroupSet.
 *
 * @author Lars Helge Overland
 */
@XmlRootElement( name = "dataElementGroupSet", namespace = Dxf2Namespace.NAMESPACE )
@XmlAccessorType( value = XmlAccessType.NONE )
public class DataElementGroupSet extends BaseIdentifiableObject
{
    /**
     * Determines if a de-serialized file is compatible with this class.
     */
    private static final long serialVersionUID = -2118690320625221749L;

    private String description;

    private Boolean compulsory = false;

    private List<DataElementGroup> members = new ArrayList<DataElementGroup>();

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public DataElementGroupSet()
    {
    }

    public DataElementGroupSet( String name )
    {
        this.name = name;
        this.compulsory = false;
    }

    public DataElementGroupSet( String name, Boolean compulsory )
    {
        this.name = name;
        this.compulsory = compulsory;
    }

    public DataElementGroupSet( String name, String description, Boolean compulsory )
    {
        this.name = name;
        this.description = description;
        this.compulsory = compulsory;
    }

    // -------------------------------------------------------------------------
    // Logic
    // -------------------------------------------------------------------------

    public void addDataElementGroup( DataElementGroup dataElementGroup )
    {
        members.add( dataElementGroup );
        dataElementGroup.setGroupSet( this );
    }

    public Collection<DataElement> getDataElements()
    {
        List<DataElement> dataElements = new ArrayList<DataElement>();

        for ( DataElementGroup group : members )
        {
            dataElements.addAll( group.getMembers() );
        }

        return dataElements;
    }

    public DataElementGroup getGroup( DataElement dataElement )
    {
        for ( DataElementGroup group : members )
        {
            if ( group.getMembers().contains( dataElement ) )
            {
                return group;
            }
        }

        return null;
    }

    public Boolean isMemberOfDataElementGroups( DataElement dataElement )
    {
        for ( DataElementGroup group : members )
        {
            if ( group.getMembers().contains( dataElement ) )
            {
                return true;
            }
        }

        return false;
    }

    public Boolean hasDataElementGroups()
    {
        return members != null && members.size() > 0;
    }

    public List<DataElementGroup> getSortedGroups()
    {
        List<DataElementGroup> sortedGroups = new ArrayList<DataElementGroup>( members );

        Collections.sort( sortedGroups, new IdentifiableObjectNameComparator() );

        return sortedGroups;
    }

    // -------------------------------------------------------------------------
    // equals and hashCode
    // -------------------------------------------------------------------------

    @Override
    public int hashCode()
    {
        return name.hashCode();
    }

    @Override
    public boolean equals( Object o )
    {
        if ( this == o )
        {
            return true;
        }

        if ( o == null )
        {
            return false;
        }

        if ( !(o instanceof DataElementGroupSet) )
        {
            return false;
        }

        final DataElementGroupSet other = (DataElementGroupSet) o;

        return name.equals( other.getName() );
    }

    @Override
    public String toString()
    {
        return "[" + name + "]";
    }

    // -------------------------------------------------------------------------
    // Getters and setters
    // -------------------------------------------------------------------------

    @XmlElement
    @JsonProperty
    public String getDescription()
    {
        return description;
    }

    public void setDescription( String description )
    {
        this.description = description;
    }

    @XmlElement
    @JsonProperty
    public Boolean isCompulsory()
    {
        if ( compulsory == null )
        {
            return false;
        }

        return compulsory;
    }

    public void setCompulsory( Boolean compulsory )
    {
        this.compulsory = compulsory;
    }

    @XmlElementWrapper( name = "dataElementGroups" )
    @XmlElement( name = "dataElementGroup" )
    @XmlJavaTypeAdapter( DataElementGroupXmlAdapter.class )
    @JsonProperty( value = "dataElementGroups" )
    @JsonSerialize( contentAs = BaseIdentifiableObject.class )
    public List<DataElementGroup> getMembers()
    {
        return members;
    }

    public void setMembers( List<DataElementGroup> members )
    {
        this.members = members;
    }
}
