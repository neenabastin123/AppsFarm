package is.ejb.bl.monitoring.server;

import java.util.ArrayList;

import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.FileSystem;
import org.hyperic.sigar.FileSystemMap;
import org.hyperic.sigar.FileSystemUsage;
import org.hyperic.sigar.DiskUsage;

import org.hyperic.sigar.cmd.Shell;
import org.hyperic.sigar.cmd.SigarCommandBase;
import org.hyperic.sigar.shell.FileCompleter;
import org.hyperic.sigar.util.GetlineCompleter;

/**
 * Report filesytem disk space usage.
 */
public class StatsDisksIO extends SigarCommandBase {

	private long totalDiskReads = 0;
	private long totalDiskWrites = 0;

	private long totalDiskReadBytes = 0;
	private long totalDiskWriteBytes = 0;

    private static final String OUTPUT_FORMAT =
        "%-15s %-15s %10s %10s %7s %7s %5s %5s";

    private static final String[] HEADER = new String[] {
        "Filesystem",
        "Mounted on",
        "Reads",
        "Writes",
        "R-bytes",
        "W-bytes",
        "Queue",
        "Svctm",
    };

    private GetlineCompleter completer;

    public StatsDisksIO(Shell shell) {
        super(shell);
        setOutputFormat(OUTPUT_FORMAT);
        this.completer = new FileCompleter(shell);
    }

    public StatsDisksIO() {
        super();
        setOutputFormat(OUTPUT_FORMAT);
    }

    public GetlineCompleter getCompleter() {
        return this.completer;
    }

    protected boolean validateArgs(String[] args) {
        return args.length <= 1;
    }

    public String getSyntaxArgs() {
        return "[filesystem]";
    }

    public String getUsageShort() {
        return "Report filesystem disk i/o";
    }

    public void printHeader() {
        //printf(HEADER);
    }

    private String svctm(double val) {
        return sprintf("%3.2f", new Object[] { new Double(val) });
    }

    public void output(String[] args) throws SigarException {
        if (args.length == 1) {
            String arg = args[0];
            if ((arg.indexOf('/') != -1) || (arg.indexOf('\\') != -1)) {
                outputFileSystem(arg);
            }
            else {
                outputDisk(arg);
            }
        }
        else {
            FileSystem[] fslist = this.proxy.getFileSystemList();
            printHeader();
            for (int i=0; i<fslist.length; i++) {
                if (fslist[i].getType() == FileSystem.TYPE_LOCAL_DISK) {
                    output(fslist[i]);
                }
            }
        }
    }

    public void outputFileSystem(String arg) throws SigarException {
        FileSystemMap mounts = this.proxy.getFileSystemMap();
        String name = FileCompleter.expand(arg);
        FileSystem fs = mounts.getMountPoint(name);

        if (fs != null) {
            printHeader();
            output(fs);
            return;
        }

        throw new SigarException(arg + " No such file or directory");
    }

    public void outputDisk(String name) throws SigarException {
        DiskUsage disk = this.sigar.getDiskUsage(name);

        ArrayList items = new ArrayList();
        printHeader();
        items.add(name);
        items.add("-");
        items.add(String.valueOf(disk.getReads()));
        items.add(String.valueOf(disk.getWrites()));

        if (disk.getReadBytes() == Sigar.FIELD_NOTIMPL) {
            items.add("-");
            items.add("-");
        }
        else {
            //items.add(Sigar.formatSize(disk.getReadBytes()));
            //items.add(Sigar.formatSize(disk.getWriteBytes()));

        	//store unformatted vaules (bytes)
            items.add(disk.getReadBytes());
            items.add(disk.getWriteBytes());

            //store and format stats
            totalDiskReads = totalDiskReads + disk.getReads();
            totalDiskWrites = totalDiskWrites + disk.getWrites();
            
            totalDiskReadBytes = totalDiskReadBytes + disk.getReadBytes();
            totalDiskWriteBytes = totalDiskWriteBytes + disk.getWriteBytes();
        }

        if (disk.getQueue() == Sigar.FIELD_NOTIMPL) {
            items.add("-");
        }
        else {
            items.add(svctm(disk.getQueue()));
        }

        if (disk.getServiceTime() == Sigar.FIELD_NOTIMPL) {
            items.add("-");
        }
        else {
            items.add(svctm(disk.getServiceTime()));
        }

        printf(items);
    }

    public void output(FileSystem fs) throws SigarException {
        FileSystemUsage usage =
            this.sigar.getFileSystemUsage(fs.getDirName());

        ArrayList items = new ArrayList();

        items.add(fs.getDevName());
        items.add(fs.getDirName());
        items.add(String.valueOf(usage.getDiskReads()));
        items.add(String.valueOf(usage.getDiskWrites()));

        if (usage.getDiskReadBytes() == Sigar.FIELD_NOTIMPL) {
            items.add("-");
            items.add("-");
        }
        else {
            //items.add(Sigar.formatSize(usage.getDiskReadBytes()));
            //items.add(Sigar.formatSize(usage.getDiskWriteBytes()));

        	//store unformatted values
            items.add(usage.getDiskReadBytes());
            items.add(usage.getDiskWriteBytes());

            //store and format stats
            totalDiskReads = totalDiskReads + usage.getDiskReads();
            totalDiskWrites = totalDiskWrites + usage.getDiskWrites();
            
            totalDiskReadBytes = totalDiskReadBytes + usage.getDiskReadBytes();
            totalDiskWriteBytes = totalDiskWriteBytes + usage.getDiskWriteBytes();
        }

        if (usage.getDiskQueue() == Sigar.FIELD_NOTIMPL) {
            items.add("-");
        }
        else {
            items.add(svctm(usage.getDiskQueue()));
        }
        if (usage.getDiskServiceTime() == Sigar.FIELD_NOTIMPL) {
            items.add("-");
        }
        else {
            items.add(svctm(usage.getDiskServiceTime()));
        }

        //printf(items);
    }
    
    public long getTotalDiskReads() {
		return totalDiskReads;
	}

	public long getTotalDiskWrites() {
		return totalDiskWrites;
	}
	
	public long getTotalDiskReadBytes() {
		return totalDiskReadBytes;
	}

	public long getTotalDiskWriteBytes() {
		return totalDiskWriteBytes;
	}

	public void resetStats() {
		totalDiskReads = 0;
		totalDiskWrites = 0;
		
		totalDiskWriteBytes = 0;
		totalDiskReadBytes = 0;
	}
	
	public static void main(String[] args) throws Exception {
        new StatsDisksIO().processCommand(args);
    }
	
	
}
