package org.hisp.dhis.reports.md.action;

import static org.hisp.dhis.system.util.ConversionUtils.getIdentifiers;
import static org.hisp.dhis.system.util.TextUtils.getCommaDelimitedString;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.format.VerticalAlignment;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;

import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hisp.dhis.common.comparator.IdentifiableObjectNameComparator;
import org.hisp.dhis.config.Configuration_IN;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodService;
import org.hisp.dhis.reports.ReportService;
import org.hisp.dhis.system.util.MathUtils;

import com.opensymphony.xwork2.Action;

public class MDReportResultAction_With_POI
    implements Action
{

    private final String GENERATEAGGDATA = "generateaggdata";

    private final String USEEXISTINGAGGDATA = "useexistingaggdata";

    private final String USECAPTUREDDATA = "usecaptureddata";

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------
    /*
     * private StatementManager statementManager;
     * 
     * public void setStatementManager( StatementManager statementManager ) {
     * this.statementManager = statementManager; }
     */
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

    // -------------------------------------------------------------------------
    // Input/Output
    // -------------------------------------------------------------------------

    private InputStream inputStream;

    public InputStream getInputStream()
    {
        return inputStream;
    }

    private String fileName;

    public String getFileName()
    {
        return fileName;
    }

    private Integer selectedStartPeriodId;

    public void setSelectedStartPeriodId( Integer selectedStartPeriodId )
    {
        this.selectedStartPeriodId = selectedStartPeriodId;
    }

    private Integer selectedEndPeriodId;

    public void setSelectedEndPeriodId( Integer selectedEndPeriodId )
    {
        this.selectedEndPeriodId = selectedEndPeriodId;
    }

    private int ouIDTB;

    public void setOuIDTB( int ouIDTB )
    {
        this.ouIDTB = ouIDTB;
    }

    private String aggData;

    public void setAggData( String aggData )
    {
        this.aggData = aggData;
    }

    private Integer orgUnitLevelCB;

    public void setOrgUnitLevelCB( Integer orgUnitLevelCB )
    {
        this.orgUnitLevelCB = orgUnitLevelCB;
    }

    private List<String> selectedDataElements;

    public void setSelectedDataElements( List<String> selectedDataElements )
    {
        this.selectedDataElements = selectedDataElements;
    }

    private String excludeZeroData;

    public void setExcludeZeroData( String excludeZeroData )
    {
        this.excludeZeroData = excludeZeroData;
    }

    private List<OrganisationUnit> orgUnitList;

    private String raFolderName;

    private SimpleDateFormat simpleDateFormat;

    // private SimpleDateFormat monthFormat;

    // private SimpleDateFormat yearFormat;

    // private SimpleDateFormat simpleMonthFormat;

    private String reportFileNameTB;

    // private String reportModelTB;

    private Date sDate;

    private Date eDate;

    private OrganisationUnit tempSelOrgUnit;

    // -------------------------------------------------------------------------
    // Action Implementation
    // -------------------------------------------------------------------------
    public String execute()
        throws Exception
    {
        // Initialization
        // statementManager.initialise();

        raFolderName = reportService.getRAFolderName();
        simpleDateFormat = new SimpleDateFormat( "MMM-yyyy" );
        reportFileNameTB = "MDReport.xlsx";

        tempSelOrgUnit = organisationUnitService.getOrganisationUnit( ouIDTB );

        if ( aggData.equalsIgnoreCase( USECAPTUREDDATA ) && excludeZeroData != null )
        {
            System.out.println( " MD Report Generation Start Time is : " + new Date() );
            generateUseCapturedDataExcludeZeroData();
        }

        else
        {
            System.out.println( " MD Report Generation Start Time is : " + new Date() );
            generateData();
        }

        // statementManager.destroy();

        System.out.println( " MD Report Generation End Time is : \t" + new Date() );

        return SUCCESS;
    }

    public void generateUseCapturedDataExcludeZeroData()
        throws Exception
    {
        // monthFormat = new SimpleDateFormat( "MMMM" );
        // yearFormat = new SimpleDateFormat( "yyyy" );
        // simpleMonthFormat = new SimpleDateFormat( "MMM" );
        // String parentUnit = "";
        int startRow = 0;
        int headerRow = 1;
        int headerCol = 0;

        String outputReportPath = System.getenv( "DHIS2_HOME" ) + File.separator + Configuration_IN.DEFAULT_TEMPFOLDER;
        File newdir = new File( outputReportPath );
        if ( !newdir.exists() )
        {
            newdir.mkdirs();
        }
        outputReportPath += File.separator + UUID.randomUUID().toString() + ".xlsx";

        // FileInputStream tempFile = new FileInputStream( new File(
        // outputReportPath ) );

        // HSSFWorkbook apachePOIWorkbook = new HSSFWorkbook();

        /*
         * WritableWorkbook outputReportWorkbook = Workbook.createWorkbook( new
         * File( outputReportPath ) ); WritableSheet sheet0 =
         * outputReportWorkbook.createSheet( "MDReport", 0 );
         * sheet0.getSettings().setDefaultColumnWidth( 12 );
         * 
         * WritableCellFormat wCellformat = new WritableCellFormat();
         * wCellformat.setBorder( Border.ALL, BorderLineStyle.THIN );
         * wCellformat.setAlignment( Alignment.CENTRE );
         * wCellformat.setVerticalAlignment( VerticalAlignment.CENTRE );
         * wCellformat.setWrap( true );
         */

        // Period Info
        Period selectedStartPeriod = periodService.getPeriod( selectedStartPeriodId );
        Period selectedEndPeriod = periodService.getPeriod( selectedEndPeriodId );
        sDate = selectedStartPeriod.getStartDate();
        eDate = selectedEndPeriod.getEndDate();

        List<Period> periodList = new ArrayList<Period>( periodService.getIntersectingPeriods(
            selectedStartPeriod.getStartDate(), selectedEndPeriod.getEndDate() ) );
        Collection<Integer> tempPeriodIds = new ArrayList<Integer>( getIdentifiers( Period.class, periodList ) );
        String periodIdsByComma = getCommaDelimitedString( tempPeriodIds );

        // Getting selected orgunit and its immediate children
        OrganisationUnit selOrgUnit = organisationUnitService.getOrganisationUnit( ouIDTB );
        List<OrganisationUnit> selOUList = new ArrayList<OrganisationUnit>(
            organisationUnitService.getOrganisationUnitWithChildren( selOrgUnit.getId() ) );
        Map<Integer, Integer> orgunitLevelMap = new HashMap<Integer, Integer>( reportService.getOrgunitLevelMap() );
        Iterator<OrganisationUnit> ouIterator = selOUList.iterator();
        while ( ouIterator.hasNext() )
        {
            OrganisationUnit orgU = ouIterator.next();

            Integer level = orgunitLevelMap.get( orgU.getId() );
            if ( level == null )
                level = organisationUnitService.getLevelOfOrganisationUnit( orgU.getId() );
            if ( level > orgUnitLevelCB )
            {
                ouIterator.remove();
            }
        }
        int minOULevel = 1;
        int maxOuLevel = 1;
        if ( selOUList != null && selOUList.size() > 0 )
        {
            minOULevel = organisationUnitService.getLevelOfOrganisationUnit( selOUList.get( 0 ).getId() );
        }
        maxOuLevel = orgUnitLevelCB;

        /*
         * int tempLavelCount = 2;
         * 
         * for ( int i = minOULevel; i <= maxOuLevel; i++ ) {
         * 
         * tempLavelCount++; }
         */

        // HSSFSheet sheetName = apachePOIWorkbook.createSheet( "MDReport" );
        // HSSFSheet sheet0 = apachePOIWorkbook.getSheetAt( 0 );

        // create a new workbook
        XSSFWorkbook apachePOIWorkbook = new XSSFWorkbook();

        // add a new sheet to the workbook
        Sheet sheet0 = apachePOIWorkbook.createSheet( "MDReport" );
        
        System.out.println( " -- Size of selected DataElements -- " + selectedDataElements.size() );
        int deCount = 0;
        for ( String selDe : selectedDataElements )
        {
            deCount++;
        }

        String mainHeaderInfo = "OrgUnit Name is " + selOrgUnit.getShortName() + " From : "
            + simpleDateFormat.format( sDate ) + " To : " + simpleDateFormat.format( eDate );

        /* Create Workbook and Worksheet */
        // HSSFWorkbook my_workbook = new HSSFWorkbook();
        // HSSFSheet my_sheet = my_workbook.createSheet("Merge Cells");
        // Row row = my_sheet.createRow((short) 1);
        // Cell cell = row.createCell((short) 1);
        // cell.setCellValue("Merge Across Rows and Columns - Example");
        // We want the Cell Data to be distributed across B2 to D5 range
        // We use static method valueOf in CellRangeAddress, to specify range
        // my_sheet.addMergedRegion(CellRangeAddress.valueOf("B2:D5"));
        /* Write changes to the workbook */
        // FileOutputStream out = new FileOutputStream(new
        // File("C:\\Merge_Rows_Columns.xls"));
        // my_workbook.write(out);
        // out.close();

        // String mergeCell = "A1:" + "A" + 2 + deCount;
        // Row row = sheet0.getRow( 0 );
        // Cell cell = row.getCell( 0 );

        // sheet0.addMergedRegion(rowFrom,rowTo,colFrom,colTo);

        // add 2 row to the sheet
        Row row1 = sheet0.createRow( 0 );
        Cell row1col1 = row1.createCell( 0 );
        row1col1.setCellValue( mainHeaderInfo );
        sheet0.addMergedRegion( new CellRangeAddress( 0, 0, 2 + deCount, 0 ) );
        row1col1.setCellStyle( getCellFormatPOIExtended( apachePOIWorkbook ) );

        // sheet0.mergeCells( 0, 0, 2 + deCount, 0 );
        // sheet0.addCell( new Label( 0, 0, mainHeaderInfo, getCellFormat2() )
        // );

        // sheet0.addCell( new Label( headerCol, headerRow, "Sl.No.",
        // getCellFormat2() ) );
        Row row2 = sheet0.createRow( headerRow );
        Cell row2col1 = row2.createCell( headerCol );
        row2col1.setCellValue( "Sl.No." );
        row2col1.setCellStyle( getCellFormatPOIExtended( apachePOIWorkbook ) );

        int c1 = headerCol + 1;
        Cell row3ccol1 = row2.createCell( c1 );
        row3ccol1.setCellValue( "Parent Hierarcy" );
        row3ccol1.setCellStyle( getCellFormatPOIExtended( apachePOIWorkbook ) );

        Cell row3ccol2 = row2.createCell( c1 + 1 );
        row3ccol2.setCellValue( "Facility" );
        row3ccol2.setCellStyle( getCellFormatPOIExtended( apachePOIWorkbook ) );

        // sheet0.addCell( new Label( c1, headerRow, "Parent Hierarcy",
        // getCellFormat2() ) );
        // sheet0.addCell( new Label( c1 + 1, headerRow, "Facility",
        // getCellFormat2() ) );

        /*
         * for ( int i = minOULevel; i <= maxOuLevel; i++ ) { sheet0.addCell(
         * new Label( c1, headerRow, "Level " + i, getCellFormat2() ) ); c1++; }
         */
        int rowCount = 1;
        int colCount = 0;
        /*
         * for( OrganisationUnit ou : selOUList ) { sheet0.addCell( new Number(
         * headerCol, headerRow + rowCount, rowCount, getCellFormat2() ) );
         * 
         * Integer level = orgunitLevelMap.get( ou.getId() ); if( level == null
         * ) level = organisationUnitService.getLevelOfOrganisationUnit(
         * ou.getId() );
         * 
         * colCount = 1 + level - minOULevel; sheet0.addCell( new Label(
         * colCount, headerRow + rowCount, ou.getName(), getCellFormat2() ) );
         * 
         * colCount = c1; }
         */
        /*
         * List<Integer> orgUnitIds = new ArrayList<Integer>(
         * getIdentifiers(OrganisationUnit.class, selOUList ) ); String
         * orgUnitIdsByComma = getCommaDelimitedString( orgUnitIds );
         */
        orgUnitList = new ArrayList<OrganisationUnit>( selOrgUnit.getChildren() );
        Collections.sort( orgUnitList, new IdentifiableObjectNameComparator() );

        // collect dataElementIDs by commaSepareted
        String dataElmentIdsByComma = getDataelementIdsFromSelectedList();

        // Map<String, String> aggDataMap = null;
        /*
         * if( aggData.equalsIgnoreCase( USEEXISTINGAGGDATA ) ) { aggDataMap =
         * new HashMap<String, String>(
         * reportService.getResultDataValueFromAggregateTableByPeriodAgg(
         * orgUnitIdsByComma, dataElmentIdsByComma, periodIdsByComma ) ); }
         * 
         * if( aggData.equalsIgnoreCase( USECAPTUREDDATA ) ) { aggDataMap = new
         * HashMap<String, String>(
         * reportService.getDataFromDataValueTableByPeriodAgg(
         * orgUnitIdsByComma, dataElmentIdsByComma, periodIdsByComma ) ); }
         */
        // System.out.println( "MDReport : " + selOrgUnit.getName()+ " : " +
        // " Report Generation Start Time is : " + new Date() );

        int orgUnitCount = 0;
        int rowIncr = 0;
        int tempOrgUnitCount = 0;

        int rowStart = 5;
        int colStart = 0;

        int rowInc = 0;
        int slno = 1;

        Iterator<OrganisationUnit> it = selOUList.iterator();

        while ( it.hasNext() )
        {
            List<String> resultList = new ArrayList<String>();
            int flag = 0;
            OrganisationUnit tempOrgUnit = (OrganisationUnit) it.next();

            Map<String, String> aggDataMap = new HashMap<String, String>();

            if ( aggData.equalsIgnoreCase( USECAPTUREDDATA ) )
            {

                aggDataMap.putAll( reportService.getAggDataFromDataValueTable( "" + tempOrgUnit.getId(),
                    dataElmentIdsByComma, periodIdsByComma ) );
            }

            Integer level = orgunitLevelMap.get( tempOrgUnit.getId() );
            if ( level == null )
                level = organisationUnitService.getLevelOfOrganisationUnit( tempOrgUnit.getId() );

            colCount = c1 + 2;

            resultList.add( getHierarchyOrgunit( tempOrgUnit ) );
            resultList.add( tempOrgUnit.getName() );

            // colCount = c1;
            int dataElementCount = 0;
            for ( String selDeExp : selectedDataElements )
            {
                String[] selDeExpParts = selDeExp.split( "#@#" );
                String formula = selDeExpParts[0];

                if ( slno == 1 )
                {
                    // sheet0.addCell( new Label( colCount, headerRow,
                    // selDeExpParts[1], getCellFormat2() ) );
                    //System.out.println( selDeExpParts[0] + " -- " + selDeExpParts[1] );
                    //Row tempRow = sheet0.createRow( headerRow );
                    Cell tempRowcol = row2.createCell( colCount );
                    tempRowcol.setCellValue( selDeExpParts[1] );
                    tempRowcol.setCellStyle( getCellFormatPOIExtended( apachePOIWorkbook ) );
                }

                String tempStr = getAggVal( formula, tempOrgUnit.getId(), aggDataMap );

                resultList.add( tempStr );
                Double tempDouble = 0.0;
                try
                {
                    tempDouble = Double.parseDouble( tempStr );

                }
                catch ( Exception e )
                {
                    tempDouble = 0.0;

                }

                if ( tempDouble > 0.0 )
                {
                    flag = 1;
                }

                colCount++;
                dataElementCount++;
            }

            if ( flag == 1 )
            {
                // sheet0.addCell( new Number( headerCol, headerRow + rowCount, rowCount, getCellFormat1() ) );
                Row tempRow = sheet0.createRow( headerRow + rowCount );
                Cell tempRowcol = tempRow.createCell( headerCol );
                tempRowcol.setCellValue( rowCount );
                tempRowcol.setCellStyle( getCellFormatPOIBasic( apachePOIWorkbook ) );

                colCount = 1;

                int tempCount = 0;

                for ( String tempValue : resultList )
                {

                    try
                    {
                        // sheet0.addCell( new Number( colCount, headerRow +
                        // rowCount, Double.parseDouble( tempValue ),
                        // getCellFormat1() ) );
                        //Row rowTemp = sheet0.createRow( headerRow + rowCount );
                        Cell colTemp = tempRow.createCell( colCount );
                        colTemp.setCellValue( Double.parseDouble( tempValue ) );
                        colTemp.setCellStyle( getCellFormatPOIBasic( apachePOIWorkbook ) );

                    }
                    catch ( Exception e )
                    {
                        // sheet0.addCell( new Label( colCount, headerRow +
                        // rowCount, tempValue, getCellFormat1() ) );

                        //Row rowTemp = sheet0.createRow( headerRow + rowCount );
                        Cell colTemp = tempRow.createCell( colCount );
                        colTemp.setCellValue( tempValue );
                        colTemp.setCellStyle( getCellFormatPOIBasic( apachePOIWorkbook ) );
                    }

                    if ( tempCount == 0 )
                    {

                        colCount = 2;
                    }
                    else
                    {
                        colCount++;
                    }
                    tempCount++;

                }
                rowCount++;
            }

            rowIncr++;
            orgUnitCount++;
            slno++;
        }

        // outputReportWorkbook.write();
        // outputReportWorkbook.close();

        // tempFile.close(); //Close the InputStream

        /*
         * FileOutputStream output_file = new FileOutputStream( new File(
         * outputReportPath ) ); //Open FileOutputStream to write updates
         * 
         * apachePOIWorkbook.write( output_file ); //write changes
         * output_file.close(); //close the stream
         * 
         * fileName = reportFileNameTB.replace( ".xlsx", "" ); fileName += "_" +
         * selOrgUnit.getShortName() + "_"; fileName += simpleDateFormat.format(
         * sDate ) + "_"; fileName += simpleDateFormat.format( eDate ) +
         * ".xlsx";
         * 
         * File outputReportFile = new File( outputReportPath ); inputStream =
         * new BufferedInputStream( new FileInputStream( outputReportFile ) );
         */

        try
        {
            FileOutputStream output_file = new FileOutputStream( new File( outputReportPath ) ); // Open
                                                                                                 // FileOutputStream
                                                                                                 // to
                                                                                                 // write
                                                                                                 // updates

            apachePOIWorkbook.write( output_file ); // write changes
            output_file.close(); // close the stream

            fileName = reportFileNameTB.replace( ".xlsx", "" );
            fileName += "_" + selOrgUnit.getShortName() + "_";
            fileName += simpleDateFormat.format( sDate ) + "_";
            fileName += simpleDateFormat.format( eDate ) + ".xlsx";

            File outputReportFile = new File( outputReportPath );
            inputStream = new BufferedInputStream( new FileInputStream( outputReportFile ) );
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }

    }

    public void generateData()
        throws Exception
    {

        // monthFormat = new SimpleDateFormat( "MMMM" );
        // yearFormat = new SimpleDateFormat( "yyyy" );
        // simpleMonthFormat = new SimpleDateFormat( "MMM" );
        // String parentUnit = "";
        int startRow = 0;
        int headerRow = 1;
        int headerCol = 0;

        String outputReportPath = System.getenv( "DHIS2_HOME" ) + File.separator + Configuration_IN.DEFAULT_TEMPFOLDER;
        File newdir = new File( outputReportPath );
        if ( !newdir.exists() )
        {
            newdir.mkdirs();
        }
        outputReportPath += File.separator + UUID.randomUUID().toString() + ".xlsx";
        
        FileOutputStream output_file = new FileOutputStream( new File( outputReportPath ) );
        
        /*
         * WritableWorkbook outputReportWorkbook = Workbook.createWorkbook( new
         * File( outputReportPath ) ); WritableSheet sheet0 =
         * outputReportWorkbook.createSheet( "MDReport", 0 );
         * 
         * sheet0.getSettings().setDefaultColumnWidth( 12 );
         * 
         * WritableCellFormat wCellformat = new WritableCellFormat();
         * wCellformat.setBorder( Border.ALL, BorderLineStyle.THIN );
         * wCellformat.setAlignment( Alignment.CENTRE );
         * wCellformat.setVerticalAlignment( VerticalAlignment.CENTRE );
         * wCellformat.setWrap( true );
         */

        // Period Info
        Period selectedStartPeriod = periodService.getPeriod( selectedStartPeriodId );
        Period selectedEndPeriod = periodService.getPeriod( selectedEndPeriodId );
        sDate = selectedStartPeriod.getStartDate();
        eDate = selectedEndPeriod.getEndDate();

        List<Period> periodList = new ArrayList<Period>( periodService.getIntersectingPeriods(
            selectedStartPeriod.getStartDate(), selectedEndPeriod.getEndDate() ) );
        Collection<Integer> tempPeriodIds = new ArrayList<Integer>( getIdentifiers( Period.class, periodList ) );
        String periodIdsByComma = getCommaDelimitedString( tempPeriodIds );

        // Getting selected orgunit and its immediate children
        OrganisationUnit selOrgUnit = organisationUnitService.getOrganisationUnit( ouIDTB );
        List<OrganisationUnit> selOUList = new ArrayList<OrganisationUnit>(
            organisationUnitService.getOrganisationUnitWithChildren( selOrgUnit.getId() ) );
        Map<Integer, Integer> orgunitLevelMap = new HashMap<Integer, Integer>( reportService.getOrgunitLevelMap() );
        Iterator<OrganisationUnit> ouIterator = selOUList.iterator();
        while ( ouIterator.hasNext() )
        {
            OrganisationUnit orgU = ouIterator.next();

            Integer level = orgunitLevelMap.get( orgU.getId() );
            if ( level == null )
                level = organisationUnitService.getLevelOfOrganisationUnit( orgU.getId() );
            if ( level > orgUnitLevelCB )
            {
                ouIterator.remove();
            }
        }
        int minOULevel = 1;
        int maxOuLevel = 1;
        if ( selOUList != null && selOUList.size() > 0 )
        {
            minOULevel = organisationUnitService.getLevelOfOrganisationUnit( selOUList.get( 0 ).getId() );
        }
        maxOuLevel = orgUnitLevelCB;

        int tempLavelCount = 0;
        for ( int i = minOULevel; i <= maxOuLevel; i++ )
        {

            tempLavelCount++;
        }
        System.out.println( " -- Size of selected DataElements -- " + selectedDataElements.size() );
        
        int deCount = 0;
        for ( String selDe : selectedDataElements )
        {
            deCount++;
        }
        
        // create a new workbook
        XSSFWorkbook apachePOIWorkbook = new XSSFWorkbook();
        // add a new sheet to the workbook
        Sheet sheet0 = apachePOIWorkbook.createSheet( "MDReport" );

        String mainHeaderInfo = "OrgUnit Name is " + selOrgUnit.getShortName() + " From : "
            + simpleDateFormat.format( sDate ) + " To : " + simpleDateFormat.format( eDate );

        /*
         * sheet0.mergeCells( 0, 0, tempLavelCount + deCount, 0 );
         * sheet0.addCell( new Label( 0, 0, mainHeaderInfo, getCellFormat2() ) ); 
         * sheet0.addCell( new Label( headerCol, headerRow, "Sl.No.", getCellFormat2() ) );
         */

        // add 2 row to the sheet
        Row row1 = sheet0.createRow( 0 );
        Cell row1col1 = row1.createCell( 0 );
        row1col1.setCellValue( mainHeaderInfo );
        sheet0.addMergedRegion( new CellRangeAddress( 0, 0, tempLavelCount + deCount, 0 ) );
        row1col1.setCellStyle( getCellFormatPOIExtended( apachePOIWorkbook ) );

        // sheet0.addCell( new Label( headerCol, headerRow, "Sl.No.", getCellFormat2() ) );
        Row row2 = sheet0.createRow( headerRow );
        Cell row2col1 = row2.createCell( headerCol );
        row2col1.setCellValue( "Sl.No." );
        row2col1.setCellStyle( getCellFormatPOIExtended( apachePOIWorkbook ) );
        
        //Row row2Level = sheet0.createRow( headerRow );
        int c1 = headerCol + 1;
        for ( int i = minOULevel; i <= maxOuLevel; i++ )
        {
            // sheet0.addCell( new Label( c1, headerRow, "Level " + i, getCellFormat2() ) );
            //Row row2Level = sheet0.createRow( headerRow );
            Cell row2colLevel = row2.createCell( c1 );
            row2colLevel.setCellValue( "Level " + i );
            //System.out.println( row2.getRowNum() + " -- " +  row2colLevel.getStringCellValue());
            row2colLevel.setCellStyle( getCellFormatPOIExtended( apachePOIWorkbook ) );
            //System.out.println( i + " -- " +  c1 + " -- " + headerRow );
            c1++;
            
        }
        
        int rowCount = 1;
        int colCount = 0;

        List<Integer> orgUnitIds = new ArrayList<Integer>( getIdentifiers( OrganisationUnit.class, selOUList ) );
        String orgUnitIdsByComma = getCommaDelimitedString( orgUnitIds );

        orgUnitList = new ArrayList<OrganisationUnit>( selOrgUnit.getChildren() );
        Collections.sort( orgUnitList, new IdentifiableObjectNameComparator() );

        // collect dataElementIDs by commaSepareted
        String dataElmentIdsByComma = getDataelementIdsFromSelectedList();

        int orgUnitCount = 0;
        int rowIncr = 0;
        int tempOrgUnitCount = 0;

        int rowStart = 5;
        int colStart = 0;

        int rowInc = 0;
        int slno = 1;

        Iterator<OrganisationUnit> it = selOUList.iterator();
        // for( OrganisationUnit tempOrgUnit : selOUList )
        
        while ( it.hasNext() )
        {
            OrganisationUnit tempOrgUnit = (OrganisationUnit) it.next();
            List<OrganisationUnit> orgUnitWithChildTree = new ArrayList<OrganisationUnit>(
                organisationUnitService.getOrganisationUnitWithChildren( tempOrgUnit.getId() ) );
            List<Integer> childOrgUnitTreeIds = new ArrayList<Integer>( getIdentifiers( OrganisationUnit.class,
                orgUnitWithChildTree ) );
            String childOrgUnitsByComma = getCommaDelimitedString( childOrgUnitTreeIds );

            Map<String, String> aggDataMap = new HashMap<String, String>();

            if ( aggData.equalsIgnoreCase( GENERATEAGGDATA ) )
            {
                aggDataMap.putAll( reportService.getAggDataFromDataValueTable( childOrgUnitsByComma,
                    dataElmentIdsByComma, periodIdsByComma ) );
            }

            else if ( aggData.equalsIgnoreCase( USEEXISTINGAGGDATA ) )
            {
                aggDataMap = new HashMap<String, String>(
                    reportService.getResultDataValueFromAggregateTableByPeriodAgg( orgUnitIdsByComma,
                        dataElmentIdsByComma, periodIdsByComma ) );
            }

            else if ( aggData.equalsIgnoreCase( USECAPTUREDDATA ) )
            {
                aggDataMap.putAll( reportService.getAggDataFromDataValueTable( "" + tempOrgUnit.getId(),
                    dataElmentIdsByComma, periodIdsByComma ) );
            }

            // sheet0.addCell( new Number( headerCol, headerRow + rowCount, rowCount, getCellFormat1() ) );
            Row tempDataRow = sheet0.createRow( headerRow + rowCount );
            
            Cell tempRowcolSlNo = tempDataRow.createCell( headerCol );
            tempRowcolSlNo.setCellValue( rowCount );
            tempRowcolSlNo.setCellStyle( getCellFormatPOIBasic( apachePOIWorkbook ) );

            // resultList.add( ""+ rowCount );
            Integer level = orgunitLevelMap.get( tempOrgUnit.getId() );
            if ( level == null )
                level = organisationUnitService.getLevelOfOrganisationUnit( tempOrgUnit.getId() );

            colCount = 1 + level - minOULevel;

            // sheet0.addCell( new Label( colCount, headerRow + rowCount, tempOrgUnit.getName(), getCellFormat1() ) );
            //Row tempRowOrgUnit = sheet0.createRow( headerRow + rowCount );
            
            Cell tempRowcolOrgUnit = tempDataRow.createCell( colCount );
            tempRowcolOrgUnit.setCellValue( tempOrgUnit.getName() );
            tempRowcolOrgUnit.setCellStyle( getCellFormatPOIBasic( apachePOIWorkbook ) );

            // resultList.add( tempOrgUnit.getName() );
            colCount = c1;
            int dataElementCount = 0;
            for ( String selDeExp : selectedDataElements )
            {
                String[] selDeExpParts = selDeExp.split( "#@#" );
                String formula = selDeExpParts[0];

                // System.out.println( "DE_Expression : " + formula );

                if ( slno == 1 )
                {
                    // sheet0.addCell( new Label( colCount, headerRow, selDeExpParts[1], getCellFormat2() ) );
                    //System.out.println( selDeExpParts[0] + " -- " + selDeExpParts[1] );
                    //Row tempRow = sheet0.createRow( headerRow );
                    Cell tempRowcol = row2.createCell( colCount );
                    tempRowcol.setCellValue( selDeExpParts[1] );
                    tempRowcol.setCellStyle( getCellFormatPOIExtended( apachePOIWorkbook ) );
                }

                String tempStr = getAggVal( formula, tempOrgUnit.getId(), aggDataMap );

                try
                {
                    // tempDouble = Double.parseDouble( tempStr );
                    // sheet0.addCell( new Number( colCount, headerRow + rowCount, Double.parseDouble( tempStr ), getCellFormat1() ) );
                    //Row rowTemp = sheet0.createRow( headerRow + rowCount );
                    Cell colTemp = tempDataRow.createCell( colCount );
                    colTemp.setCellValue( Double.parseDouble( tempStr ) );
                    colTemp.setCellStyle( getCellFormatPOIBasic( apachePOIWorkbook ) );

                }
                catch ( Exception e )
                {
                    // tempDouble = 0.0;
                    // sheet0.addCell( new Label( colCount, headerRow + rowCount, tempStr, getCellFormat1() ) );
                    //Row rowTemp = sheet0.createRow( headerRow + rowCount );
                    Cell colTemp = tempDataRow.createCell( colCount );
                    colTemp.setCellValue( tempStr );
                    colTemp.setCellStyle( getCellFormatPOIBasic( apachePOIWorkbook ) );
                }
                colCount++;
                dataElementCount++;
            }
          
            rowCount++;
            rowIncr++;
            orgUnitCount++;
            slno++;
        }
        
        
        try
        {
            apachePOIWorkbook.write( output_file ); // write changes
            output_file.close(); // close the stream
            
            fileName = reportFileNameTB.replace( ".xlsx", "" );
            fileName += "_" + selOrgUnit.getShortName() + "_";
            fileName += simpleDateFormat.format( sDate ) + "_";
            fileName += simpleDateFormat.format( eDate ) + ".xlsx";

            File outputReportFile = new File( outputReportPath );
            inputStream = new BufferedInputStream( new FileInputStream( outputReportFile ) );
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
    }

    // Returns the OrgUnitTree for which Root is the orgUnit
    @SuppressWarnings( "unchecked" )
    public List<OrganisationUnit> getChildOrgUnitTree( OrganisationUnit orgUnit )
    {
        List<OrganisationUnit> orgUnitTree = new ArrayList<OrganisationUnit>();
        orgUnitTree.add( orgUnit );

        List<OrganisationUnit> children = new ArrayList<OrganisationUnit>( orgUnit.getChildren() );
        Collections.sort( children, new IdentifiableObjectNameComparator() );

        Iterator childIterator = children.iterator();
        OrganisationUnit child;
        while ( childIterator.hasNext() )
        {
            child = (OrganisationUnit) childIterator.next();
            orgUnitTree.addAll( getChildOrgUnitTree( child ) );
        }
        return orgUnitTree;
    }// getChildOrgUnitTree end

    // getting data value using Map
    private String getAggVal( String expression, Integer orgUnitID, Map<String, String> aggDeMap )
    {
        try
        {
            Pattern pattern = Pattern.compile( "(\\[\\d+\\.\\d+\\])" );

            Matcher matcher = pattern.matcher( expression );
            StringBuffer buffer = new StringBuffer();

            String resultValue = "";

            while ( matcher.find() )
            {
                String replaceString = matcher.group();

                replaceString = replaceString.replaceAll( "[\\[\\]]", "" );

                // replaceString = aggDeMap.get( replaceString +":"+ orgUnitID
                // );

                replaceString = aggDeMap.get( replaceString );
                if ( replaceString == null )
                {
                    replaceString = "0";
                }

                matcher.appendReplacement( buffer, replaceString );

                resultValue = replaceString;
            }

            matcher.appendTail( buffer );

            double d = 0.0;
            try
            {
                d = MathUtils.calculateExpression( buffer.toString() );
            }
            catch ( Exception e )
            {
                d = 0.0;
                resultValue = "";
            }

            resultValue = "" + (double) d;

            return resultValue;
        }
        catch ( NumberFormatException ex )
        {
            throw new RuntimeException( "Illegal DataElement id", ex );
        }
    }

    private String getHierarchyOrgunit( OrganisationUnit orgunit )
    {
        String hierarchyOrgunit = "";
        // String hierarchyOrgunit = orgunit.getName();

        while ( orgunit.getParent() != null )
        {

            if ( orgunit.getId() == tempSelOrgUnit.getId() )
            {
                break;
            }
            hierarchyOrgunit = orgunit.getParent().getName() + " - > " + hierarchyOrgunit;

            orgunit = orgunit.getParent();
        }

        return hierarchyOrgunit;
    }

    public WritableCellFormat getCellFormat1()
        throws Exception
    {
        WritableCellFormat wCellformat = new WritableCellFormat();
        wCellformat.setBorder( Border.ALL, BorderLineStyle.THIN );
        wCellformat.setAlignment( Alignment.CENTRE );
        wCellformat.setVerticalAlignment( VerticalAlignment.CENTRE );
        wCellformat.setWrap( true );

        return wCellformat;
    }

    public WritableCellFormat getCellFormat2()
        throws Exception
    {
        WritableFont arialBold = new WritableFont( WritableFont.ARIAL, 10, WritableFont.BOLD );
        WritableCellFormat wCellformat = new WritableCellFormat( arialBold );

        wCellformat.setBorder( Border.ALL, BorderLineStyle.THIN );
        wCellformat.setAlignment( Alignment.CENTRE );
        wCellformat.setVerticalAlignment( VerticalAlignment.CENTRE );
        wCellformat.setBackground( Colour.ICE_BLUE );
        wCellformat.setWrap( true );
        wCellformat.setShrinkToFit( true );
        return wCellformat;
    }

    public XSSFCellStyle getCellFormatPOIBasic( XSSFWorkbook apachePOIWorkbook )
        throws Exception
    {

        XSSFCellStyle my_style = apachePOIWorkbook.createCellStyle();
        /* First, let us draw a thick border so that the color is visible */
        my_style.setBorderLeft( XSSFCellStyle.BORDER_THIN );
        my_style.setBorderRight( XSSFCellStyle.BORDER_THIN );
        my_style.setBorderTop( XSSFCellStyle.BORDER_THIN );
        my_style.setBorderBottom( XSSFCellStyle.BORDER_THIN );

        my_style.setAlignment( HorizontalAlignment.CENTER );
        my_style.setVerticalAlignment( org.apache.poi.ss.usermodel.VerticalAlignment.CENTER );
        my_style.setWrapText( true );

        return my_style;
    }

    public XSSFCellStyle getCellFormatPOIExtended( XSSFWorkbook apachePOIWorkbook )
        throws Exception
    {
        /* Get access to XSSFCellStyle */
        /*
         * ExtendedFormatRecord e = new ExtendedFormatRecord();
         * e.setShrinkToFit(true);
         */

        XSSFCellStyle my_style = apachePOIWorkbook.createCellStyle();
        /* First, let us draw a thick border so that the color is visible */
        my_style.setBorderLeft( XSSFCellStyle.BORDER_THIN );
        my_style.setBorderRight( XSSFCellStyle.BORDER_THIN );
        my_style.setBorderTop( XSSFCellStyle.BORDER_THIN );
        my_style.setBorderBottom( XSSFCellStyle.BORDER_THIN );

        my_style.setAlignment( HorizontalAlignment.CENTER );
        my_style.setVerticalAlignment( org.apache.poi.ss.usermodel.VerticalAlignment.CENTER );

        // my_style.setFillBackgroundColor( IndexedColors.LIGHT_GREEN.getIndex()
        // );
        my_style.setFillForegroundColor( IndexedColors.LIGHT_GREEN.getIndex() );
        my_style.setFillPattern( FillPatternType.SOLID_FOREGROUND );
        my_style.setWrapText( true );

        XSSFFont my_font = apachePOIWorkbook.createFont();
        my_font.setBoldweight( HSSFFont.BOLDWEIGHT_BOLD );
        /* attach the font to the style created earlier */
        my_style.setFont( my_font );

        return my_style;

    }

    private String getAggVal( String expression, Map<String, String> aggDeMap )
    {
        try
        {
            Pattern pattern = Pattern.compile( "(\\[\\d+\\.\\d+\\])" );

            Matcher matcher = pattern.matcher( expression );
            StringBuffer buffer = new StringBuffer();

            String resultValue = "";

            while ( matcher.find() )
            {
                String replaceString = matcher.group();

                replaceString = replaceString.replaceAll( "[\\[\\]]", "" );

                replaceString = aggDeMap.get( replaceString );

                if ( replaceString == null )
                {
                    replaceString = "0";
                }

                matcher.appendReplacement( buffer, replaceString );

                resultValue = replaceString;
            }

            matcher.appendTail( buffer );

            double d = 0.0;
            try
            {
                d = MathUtils.calculateExpression( buffer.toString() );
            }
            catch ( Exception e )
            {
                d = 0.0;
                resultValue = "";
            }

            resultValue = "" + (double) d;

            return resultValue;
        }
        catch ( NumberFormatException ex )
        {
            throw new RuntimeException( "Illegal DataElement id", ex );
        }
    }

    public String getDataelementIdsFromSelectedList()
    {
        String dataElmentIdsByComma = "-1";

        try
        {
            for ( String selDeExp : selectedDataElements )
            {
                String[] selDeExpParts = selDeExp.split( "#@#" );
                String formula = selDeExpParts[0];

                try
                {
                    Pattern pattern = Pattern.compile( "(\\[\\d+\\.\\d+\\])" );

                    Matcher matcher = pattern.matcher( formula );
                    StringBuffer buffer = new StringBuffer();

                    while ( matcher.find() )
                    {
                        String replaceString = matcher.group();

                        replaceString = replaceString.replaceAll( "[\\[\\]]", "" );
                        replaceString = replaceString.substring( 0, replaceString.indexOf( '.' ) );

                        int dataElementId = Integer.parseInt( replaceString );
                        dataElmentIdsByComma += "," + dataElementId;
                        replaceString = "";
                        matcher.appendReplacement( buffer, replaceString );
                    }
                }
                catch ( Exception e )
                {
                    e.printStackTrace();
                }

            }// end of for loop
        }// try block end
        catch ( Exception e )
        {
            e.printStackTrace();
        }

        return dataElmentIdsByComma;
    }
}
