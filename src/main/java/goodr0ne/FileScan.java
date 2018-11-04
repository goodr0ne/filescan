package goodr0ne;

import org.apache.commons.lang3.StringUtils;
import picocli.CommandLine;

import javax.swing.filechooser.FileSystemView;
import java.io.*;
import java.util.ArrayList;
import java.util.Objects;

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
          description = "Add specified string to searched file (first found one)")
  private static String appendLineArg = "";

  //private static long fileCount = 0;
  private static long recursiveCount = 0;
  private static File foundFile;
  private static ArrayList<File> foundFiles = new ArrayList<>();
  //private static boolean[] isOutputted = {false, false, false, false};

  private static String readFile(File file, boolean isPreview) {
    StringBuilder output = new StringBuilder();
    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
      String line;
      int count = 0;
      while ((line = br.readLine()) != null) {
        output.append(line).append("\n");
        if (isPreview && (count > 6)) {
          break;
        }
        count++;
      }
    } catch (Exception e) {
      output = new StringBuilder("Exception arrived - " + e.toString());
    }
    return output.toString();
  }

  private static void readDrives() {
    System.out.println("-readDrives option execution:");
    FileSystemView fsv = FileSystemView.getFileSystemView();
    File[] paths = File.listRoots();
    for (File path:paths) {
      System.out.println("Drive Name: " + path);
      System.out.println("Description: " + fsv.getSystemTypeDescription(path));
    }
    System.out.println();
  }

  /*private static long fileCount(File dir) {
    try {
      File[] files = dir.listFiles();
      int count = 0;
      assert files != null;
      for (File f : files)
        if (f.isDirectory())
          count += fileCount(f);
        else
          count++;
      return count;
    } catch (Exception e) {
      return 0;
    }
  }*/

  private static void previewFoundFiles(File[] files) {
    int count = 1;
    for (File file:files) {
      if (count == 1) {
        System.out.println("\n" + count + "st Found file - " + file.getAbsolutePath());
      } else if (count == 2) {
        System.out.println("\n" + count + "nd Found file - " + file.getAbsolutePath());
      } else if (count == 3) {
        System.out.println("\n" + count + "rd Found file - " + file.getAbsolutePath());
      } else {
        System.out.println("\n" + count + "th Found file - " + file.getAbsolutePath());
      }
      System.out.println("File content preview (first 7 lines):");
      System.out.println(">>>FILE_BEGINS_HERE>>>" + readFile(file, true) + "<<<FILE_ENDS_HERE<<<");
      count++;
    }
  }

  private static void searchRecursively(File file) {
    recursiveCount++;
    /*if (!isOutputted[0] && (recursiveCount > (fileCount / 5))) {
      isOutputted[0] = true;
      System.out.println("Processed 20% of all files");
    } else if (!isOutputted[1] && (recursiveCount > 2 * (fileCount / 5))) {
      isOutputted[1] = true;
      System.out.println("Processed 40% of all files");
    } else if (!isOutputted[2] && (recursiveCount > 3 * (fileCount / 5))) {
      isOutputted[2] = true;
      System.out.println("Processed 60% of all files");
    } else if (!isOutputted[3] && (recursiveCount > 4 * (fileCount / 5))) {
      isOutputted[3] = true;
      System.out.println("Processed 80% of all files");
    }*/
    try {
      if (file.isDirectory()) {
        if (file.canRead()) {
          for (File temp : Objects.requireNonNull(file.listFiles())) {
            if (temp.isDirectory()) {
              searchRecursively(temp);
            } else {
              if (searchFileArgs[0].equals(temp.getName())) {
                System.out.println("File found! Path - " + temp.getAbsolutePath());
                foundFiles.add(temp);
              }
            }
          }
        }
      }
    } catch (Exception ignored) {}
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
        File[] matchingFiles = path.listFiles((dir, name) ->
                name.toLowerCase().contains(searchFileArgs[1].toLowerCase()));
        if ((matchingFiles != null) && (matchingFiles.length > 0)) {
          System.out.println("File(s) found! Total quantity - " + matchingFiles.length);
          foundFile = matchingFiles[0];
          status = 1;
          previewFoundFiles(matchingFiles);
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
      File[] drives = File.listRoots();
      /*System.out.println("Producing all files recount operation");
      for (File drive:drives) {
        long count = fileCount(new File(drive + "\\"));
        System.out.println("Drive " + drive + " files count - " + count);
        fileCount = fileCount + count;
      }
      System.out.println("Total files count - " + fileCount);*/
      for (File drive:drives) {
        System.out.println("Searching in drive " + drive);
        long time = System.currentTimeMillis();
        searchRecursively(new File(drive + "\\"));
        System.out.println("Drive search time - "
                + ((System.currentTimeMillis() - time) / 1000) + " seconds");
      }
      if (foundFiles.size() > 0) {
        System.out.println(foundFiles.size() + " files are found!");
        previewFoundFiles(foundFiles.toArray(new File[0]));
        foundFile = foundFiles.get(0);
        status = 1;
      } else {
        System.out.println("No files are found!");
      }
      System.out.println("Total number of recursive calls - " + recursiveCount);
    }
    System.out.println();
    return status;
  }

  private static void appendLine() {
    System.out.println("-appendLine option execution:");
    System.out.println("Appending line - " + appendLineArg);
    System.out.println("To file - " + foundFile.getName());
    long size = foundFile.length();
    if ((size / 1024) > 0) {
      System.out.println("File size - " + (size / 1024) + "kb");
    } else {
      System.out.println("File size - " + size + " bytes");
    }
    System.out.println("Placed in path - " + foundFile.getAbsolutePath());
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
      if (!(foundFile.getName().endsWith(".txt")
              || foundFile.getName().endsWith(".md"))) {
        System.out.println("Only .txt or .md files are allowed for line appending\n");
        return;
      }
      if (size > 10) {
        System.out.println("Files with size more than 10kb are not allowed for appending\n");
        return;
      }
      String content = readFile(foundFile, false);
      try (BufferedWriter writer = new BufferedWriter(new FileWriter(foundFile))) {
        writer.write(appendLineArg + "\n" + content);
      }
    } catch (Exception e) {
      System.out.println("Exception arrived - " + e.toString());
    }
    System.out.println();
  }

  public void run() {
    System.out.println("\nFileScan is launched, fasten your seat belt!\n");
    long time = System.currentTimeMillis();
    if (isReadDrives) {
      readDrives();
    }
    if (!StringUtils.isBlank(searchFileArgs[0])) {
      int fileFound = searchFile();
      if (!StringUtils.isBlank(appendLineArg) && fileFound == 1) {
        appendLine();
      }
    }
    System.out.println("Total spent time - "
            + ((System.currentTimeMillis() - time) / 1000) + " seconds");
    System.out.println("Thanks for using, enjoy your day!");
  }

  public static void main(String[] args) {
    CommandLine.run(new FileScan(), args);
  }
}
