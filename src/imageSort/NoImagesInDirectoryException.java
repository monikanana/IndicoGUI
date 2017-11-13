package imageSort;

public class NoImagesInDirectoryException extends Throwable {
    public void printException() {
        System.out.println("There is no images in the directory.");
    }
}