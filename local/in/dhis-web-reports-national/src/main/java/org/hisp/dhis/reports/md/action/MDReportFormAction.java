package org.hisp.dhis.reports.md.action;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.hisp.dhis.organisationunit.OrganisationUnitLevel;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.period.MonthlyPeriodType;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodService;
import org.hisp.dhis.period.comparator.PeriodComparator;
import org.hisp.dhis.reports.ReportOption;
import org.hisp.dhis.reports.ReportService;
import org.hisp.dhis.reports.ReportType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.opensymphony.xwork2.Action;

public class MDReportFormAction implements Action
{
    //--------------------------------------------------------------------------
    //  Dependencies
    //--------------------------------------------------------------------------
    
    private PeriodService periodService;

    public void setPeriodService( PeriodService periodService )
    {
        this.periodService = periodService;
    }

    private ReportService reportService;

    public void setReportService( ReportService reportService )
    {
        this.reportService = reportService;
    }
    
    private OrganisationUnitService organisationUnitService;

    public void setOrganisationUnitService( OrganisationUnitService organisationUnitService )
    {
        this.organisationUnitService = organisationUnitService;
    }

    //--------------------------------------------------------------------------
    //Input/Output
    //--------------------------------------------------------------------------
    
    private SimpleDateFormat simpleDateFormat;
    
    public SimpleDateFormat getSimpleDateFormat()
    {
        return simpleDateFormat;
    }   

    private List<Period> periods;
    
    public List<Period> getPeriods()
    {
        return periods;
    }

    private String reportTypeName;
    
    public String getReportTypeName()
    {
        return reportTypeName;
    }
    
    private String periodTypeName;
    
    public String getPeriodTypeName()
    {
        return periodTypeName;
    }
    
    private List<ReportOption> reportOptionList;
    
    public List<ReportOption> getReportOptionList()
    {
        return reportOptionList;
    }
    
    private List<ReportOption> mdReportList;
    
    public List<ReportOption> getMdReportList()
    {
        return mdReportList;
    }

    private List<OrganisationUnitLevel> levels;

    public List<OrganisationUnitLevel> getLevels()
    {
        return levels;
    }
    
    
    private String raFolderName;
    //--------------------------------------------------------------------------
    //  Action Implementation
    //--------------------------------------------------------------------------
    
    
    public String execute() throws Exception
    {
        reportTypeName = ReportType.RT_MD_REPORT;
        periodTypeName = MonthlyPeriodType.NAME;
        raFolderName = reportService.getRAFolderName();
        
        //period information
        periods = new ArrayList<Period>( periodService.getPeriodsByPeriodType( new MonthlyPeriodType() ) );
        
        levels = organisationUnitService.getFilledOrganisationUnitLevels();
       
        Iterator<Period> periodIterator = periods.iterator();
        while ( periodIterator.hasNext() )
        {
            Period p1 = periodIterator.next();
            if ( p1.getStartDate().compareTo( new Date() ) > 0 )
            {
                periodIterator.remove();
            }                        
        }
        simpleDateFormat = new SimpleDateFormat( "MMM-yyyy" );
        Collections.sort( periods, new PeriodComparator() );
        
        //reportOptionList = new ArrayList<ReportOption>( getReportOptions() );
        
        mdReportList = new ArrayList<ReportOption>( getMDReportList() );
        
        
        return SUCCESS;
    }
 
    // Supportive Methods
    
    private List<ReportOption> getMDReportList( )
    {
        List<ReportOption> mdReportList = new ArrayList<ReportOption>();
        
        String newpath = "";
        try
        {
            newpath = System.getenv( "DHIS2_HOME" ) + File.separator + raFolderName + File.separator + "mdReportsList.xml";
        }
        catch ( NullPointerException npe )
        {
            System.out.println("DHIS_HOME is not set");
            return null;
        }
        
        String reportName = "";
        String reportXMLFileName = "";
        
        try
        {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse( new File( newpath ) );
            if ( doc == null )
            {
                System.out.println( "There is no MAP XML file in the DHIS2 Home" );
                return null;
            }

            NodeList listOfReports = doc.getElementsByTagName( "mdReport" );
            int totalReports = listOfReports.getLength();

            for( int s = 0; s < totalReports; s++ )
            {
                Node reportNode = listOfReports.item( s );
                Element reportElement = (Element) reportNode;
                
                NodeList reportNameList = reportElement.getElementsByTagName( "name" );
                Element reportNameElement = (Element) reportNameList.item( 0 );
                NodeList textreportNameList = reportNameElement.getChildNodes();
                reportName = ((Node) textreportNameList.item( 0 )).getNodeValue().trim();

                NodeList reportFileNameList = reportElement.getElementsByTagName( "filename" );
                Element reportFileNameElement = (Element) reportFileNameList.item( 0 );
                NodeList textreportFileNameList = reportFileNameElement.getChildNodes();
                reportXMLFileName = ((Node) textreportFileNameList.item( 0 )).getNodeValue().trim();
                
                if( reportName != null && reportXMLFileName != null )
                {
                    ReportOption reportOption = new ReportOption( reportName, reportXMLFileName );
                    mdReportList.add( reportOption );
                }
                
            }// end of for loop with s var
        }// try block end
        catch ( SAXParseException err )
        {
            System.out.println( "** Parsing error" + ", line " + err.getLineNumber() + ", uri " + err.getSystemId() );
            System.out.println( " " + err.getMessage() );
            return null;
        }
        catch ( SAXException e )
        {
            Exception x = e.getException();
            ((x == null) ? e : x).printStackTrace();
            return null;
        }
        catch ( Throwable t )
        {
            t.printStackTrace();
            return null;
        }
        
        return mdReportList;
    }    
    
    
    
    private List<ReportOption> getReportOptions( )
    {
        List<ReportOption> reportOptionList = new ArrayList<ReportOption>();
        
        String newpath = "";
        try
        {
            newpath = System.getenv( "DHIS2_HOME" ) + File.separator + raFolderName + File.separator + "mdreport.xml";
        }
        catch ( NullPointerException npe )
        {
            System.out.println("DHIS_HOME is not set");
            return null;
        }
        
        try
        {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse( new File( newpath ) );
            if ( doc == null )
            {
                System.out.println( "There is no MAP XML file in the DHIS2 Home" );
                return null;
            }

            NodeList listOfOption = doc.getElementsByTagName( "option" );
            int totalOptions = listOfOption.getLength();

            for( int s = 0; s < totalOptions; s++ )
            {
                Element element = (Element) listOfOption.item( s );
                String optiontext = element.getAttribute( "optiontext" );
                String optionvalue = element.getAttribute( "optionvalue" );

                optionvalue += "#@#" + optiontext;
                if( optiontext != null && optionvalue != null )
                {
                    ReportOption reportOption = new ReportOption( optiontext, optionvalue );
                    reportOptionList.add( reportOption );
                }
            }// end of for loop with s var
        }// try block end
        catch ( SAXParseException err )
        {
            System.out.println( "** Parsing error" + ", line " + err.getLineNumber() + ", uri " + err.getSystemId() );
            System.out.println( " " + err.getMessage() );
            return null;
        }
        catch ( SAXException e )
        {
            Exception x = e.getException();
            ((x == null) ? e : x).printStackTrace();
            return null;
        }
        catch ( Throwable t )
        {
            t.printStackTrace();
            return null;
        }
        
        return reportOptionList;
    }
    
}
