package org.hisp.dhis.reports.nbits.action;

import java.util.ArrayList;
import java.util.List;

import org.hisp.dhis.program.Program;
import org.hisp.dhis.program.ProgramService;

import com.opensymphony.xwork2.Action;

public class NBITSReportFormAction implements Action
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private ProgramService programService;
    
    public void setProgramService( ProgramService programService )
    {
        this.programService = programService;
    }
    
    // -------------------------------------------------------------------------
    // Getter & Setter
    // -------------------------------------------------------------------------

    private List<Program> programList;
    
    public List<Program> getProgramList()
    {
        return programList;
    }

    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------
    public String execute() throws Exception
    {
        programList = new ArrayList<Program>( programService.getAllPrograms() );
        
        return SUCCESS;
    }
}
