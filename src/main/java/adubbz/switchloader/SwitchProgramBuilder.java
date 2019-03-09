package adubbz.switchloader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import ghidra.app.util.MemoryBlockUtil;
import ghidra.app.util.bin.ByteProvider;
import ghidra.app.util.importer.MemoryConflictHandler;
import ghidra.framework.store.LockException;
import ghidra.program.model.address.AddressOutOfBoundsException;
import ghidra.program.model.address.AddressOverflowException;
import ghidra.program.model.address.AddressSpace;
import ghidra.program.model.listing.Program;
import ghidra.util.task.TaskMonitor;

public abstract class SwitchProgramBuilder 
{
    protected ByteProvider provider;
    protected Program program;
    protected MemoryBlockUtil mbu;
    
    protected byte[] text;
    protected byte[] rodata;
    protected byte[] data;
    
    protected int textOffset;
    protected int rodataOffset;
    protected int dataOffset;
    protected int bssSize;
    
    // MOD0 Stuff
    protected int bssOffset;
    
    protected SwitchProgramBuilder(ByteProvider provider, Program program, MemoryConflictHandler handler)
    {
        this.provider = provider;
        this.program = program;
        this.mbu = new MemoryBlockUtil(program, handler);
    }
    
    protected void load(TaskMonitor monitor)
    {
        long baseAddress = 0x7100000000L;
        AddressSpace aSpace = program.getAddressFactory().getDefaultAddressSpace();
        
        try 
        {
            // Set the base address
            this.program.setImageBase(aSpace.getAddress(baseAddress), true);
            this.loadDefaultSegments(monitor);
            
            // Setup memory blocks
            InputStream textInputStream = new ByteArrayInputStream(this.text);
            InputStream rodataInputStream = new ByteArrayInputStream(this.rodata);
            InputStream dataInputStream = new ByteArrayInputStream(this.data);
            
            this.mbu.createInitializedBlock(".text", aSpace.getAddress(baseAddress + this.textOffset), textInputStream, this.text.length, "", null, true, false, true, monitor);
            this.mbu.createInitializedBlock(".rodata", aSpace.getAddress(baseAddress + this.rodataOffset), rodataInputStream, this.rodata.length, "", null, true, false, false, monitor);
            this.mbu.createInitializedBlock(".data", aSpace.getAddress(baseAddress + this.dataOffset), dataInputStream, this.data.length, "", null, true, true, false, monitor);
            this.mbu.createUninitializedBlock(false, ".bss", aSpace.getAddress(baseAddress + this.bssOffset), this.bssSize, "", null, true, true, false);
        } 
        catch (AddressOverflowException | LockException | IllegalStateException | AddressOutOfBoundsException | IOException e) 
        {
            
        }
    }
    
    protected abstract void loadDefaultSegments(TaskMonitor monitor) throws IOException, AddressOverflowException, AddressOutOfBoundsException;
}
