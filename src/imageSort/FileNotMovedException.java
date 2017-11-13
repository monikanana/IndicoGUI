package imageSort;

public class FileNotMovedException extends Throwable {
    public void printException() {
        System.out.println("File can not be moved to the directory.");
    }
}
