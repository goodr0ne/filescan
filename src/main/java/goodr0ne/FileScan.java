package goodr0ne;

import org.apache.commons.lang3.StringUtils;
import picocli.CommandLine;

import javax.swing.filechooser.FileSystemView;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;

@CommandLine.Command(name = "filescan",
        mixinStandardHelpOptions = true, version = "goodr0ne.filescan 1.337")
public class FileScan implements Runnable {

  @CommandLine.Option(names = "-readDrives",
          description = "Outputs all available drives list.")
  private static boolean isReadDrives = false;

  @CommandLine.Option(names = "-searchFile", arity = "1..2",
          description = "Searches for a file in specified path (1st param - path, 2nd - file) "
                  + "or by exact name match (single param - file name)")
  private static String[] searchFileArgs = {""};

  @CommandLine.Option(names = "-appendLine",
          description = "Add specified string to searched file")
  private static String appendLineArg = "";

  private static File foundedFile;

  private static void readDrives() {
    System.out.println("-readDrives option execution:");
    FileSystemView fsv = FileSystemView.getFileSystemView();
    File[] paths = File.listRoots();
    for (File path:paths) {
      System.out.println("Drive Name: "+path);
      System.out.println("Description: "+fsv.getSystemTypeDescription(path));
    }
    System.out.println();
  }

  private static int searchFile() {
    int status = 0;
    System.out.println("-searchFile option execution:");
    if (searchFileArgs.length > 1) {
      System.out.println("Launching partial filename match search in directory");
      System.out.println("Searching in path - " + searchFileArgs[0]);
      System.out.println("Searching filename - " + searchFileArgs[1]);
      try {
        File path = new File(searchFileArgs[0]);
        File[] matchingFiles = path.listFiles(new FilenameFilter() {
          public boolean accept(File dir, String name) {
            return name.contains(searchFileArgs[1]);
          }
        });
        if ((matchingFiles != null) && (matchingFiles.length > 0)) {
          System.out.println("File(s) found! Total quantity - " + matchingFiles.length);
          foundedFile = matchingFiles[0];
          status = 1;
          for (File file:matchingFiles) {
            System.out.println("Founded file - " + file.getName());
          }
        } else {
          System.out.println("No File(s) found!");
        }
      } catch (Exception e) {
        System.out.println("Exception arrived - " + e.toString());
        status = -1;
      }
    } else {
      System.out.println("Launching exact filename match search in all directories");
      System.out.println("Searching filename - " + searchFileArgs[0]);
      status = 0;
    }
    System.out.println();
    return status;
  }

  private static void appendLine() {
    System.out.println("-appendLine option execution:");
    System.out.println("Appending line - " + appendLineArg);
    System.out.println("To file - " + foundedFile.getName());
    long size = foundedFile.length() / 1024;
    System.out.println("File size - " + size + "kb");
    System.out.println("Placed in path - " + foundedFile.getAbsolutePath());
    try {
      if (appendLineArg.length() > 255) {
        System.out.println("Please, use less than 256 chars in desired appended line\n");
        return;
      }
      if (!appendLineArg.matches("[a-zA-Z\\Q,.-!?_ \\E]*")) {
        System.out.println("Please, use only allowed chars in desired appended line, " +
                "that's [a-zA-Z,.-!?_ ]\n");
        return;
      }
      if (!(foundedFile.getName().endsWith(".txt")
              || foundedFile.getName().endsWith(".md"))) {
        System.out.println("Only .txt or .md files are allowed for line appending\n");
        return;
      }
      if (size > 10) {
        System.out.println("Files with size more than 10kb are not allowed for appending\n");
        return;
      }
      try (BufferedWriter writer = new BufferedWriter(new FileWriter(foundedFile, true))) {
        writer.append(appendLineArg).append("\n");
      }
    } catch (Exception e) {
      System.out.println("Exception arrived - " + e.toString());
    }
    System.out.println();
  }

  public void run() {
    System.out.println("\nFileScan is launched, fasten your seat belt!\n");
    if (isReadDrives) {
      readDrives();
    }
    if (!StringUtils.isBlank(searchFileArgs[0])) {
      int fileFound = searchFile();
      if (!StringUtils.isBlank(appendLineArg) && fileFound == 1) {
        appendLine();
      }
    }
    System.out.println("Thanks for using, enjoy your day!");
  }

  public static void main(String[] args) {
    CommandLine.run(new FileScan(), args);
  }
}
