package goodr0ne;

import picocli.CommandLine;

@CommandLine.Command(name = "filescan", mixinStandardHelpOptions = true, version = "goodr0ne.filescan 0.47")
public class FileScan implements Runnable {

  @CommandLine.Option(names = "-readDrives" , description = "Outputs all available drives list.")
  private static boolean isReadDrives = false;

  private static void readDrives() {
    System.out.println("-readDrives");
  }

  public void run() {
    System.out.println("Hello World!");
    if (isReadDrives) {
      readDrives();
    }
  }

  public static void main(String[] args) {
    CommandLine.run(new FileScan(), args);
  }
}
