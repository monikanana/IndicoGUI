package imageSort;

import io.indico.Indico;
import io.indico.api.results.BatchIndicoResult;
import io.indico.api.utils.IndicoException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class Sort {

    public void sort(String photoPath)
            throws IndicoException, NoImagesInDirectoryException, IOException,
            CannotCreateDirectoryException, FileNotMovedException {

        Indico indico = new Indico("1026d4b1cea8318b801f45df22bcb2b5");
        File photosFolder = new File(photoPath);
        File[] photosFiles = photosFolder.listFiles();

        if (photosFiles == null) {
            throw new FileNotFoundException();
        }

        /**
         * if there exists some directory in the photosFolder, we do not need it
         * that is why we create new LinkedList photosPatches
         * - this consists of only String paths to files which are not directories
         */

        LinkedList<String> photosPatches = new LinkedList<>();

        for (File photoFile : photosFiles) {
            if (!photoFile.isDirectory()) {
                photosPatches.add(photoFile.getPath());
            }
        }

        if (photosPatches.size() == 0) {
            throw new NoImagesInDirectoryException();
        }

        /*
        now we can start sorting images :)
         */

        BatchIndicoResult multiple = indico.imageRecognition.predict(photosPatches);
        List<Map<String, Double>> results = multiple.getImageRecognition();

        String maxKey;
        Double maxValue;

        File destinationFolder, sourceFile;

        /*
        we iterate through the list of map
        every element of the list concerns different image from our directory
         */
        for (int i = 0; i < results.size(); i++) {
            maxKey = "";
            maxValue = 0.0;

            /*
            we look for the best matches for single image
            Map.Entry<String, Double> is a match of our image to stork, dog, cat, cat etc...
             */
            for (Map.Entry<String, Double> iteratorMap : results.get(i).entrySet()) {

                if (iteratorMap.getValue() > maxValue) {
                    maxValue = iteratorMap.getValue();
                    maxKey = iteratorMap.getKey();
                }
            }

            sourceFile = new File(photosPatches.get(i));
            destinationFolder = new File(photosFolder.getPath() + "\\" + maxKey + "\\");

            if (!destinationFolder.exists()) {
                if (!destinationFolder.mkdir()) {
                    throw new CannotCreateDirectoryException();
                }
            }

            File destinationFile = new File(destinationFolder + "\\" + sourceFile.getName() + "\\");
            if (!sourceFile.renameTo(destinationFile)) {
                throw new FileNotMovedException();
            }
        }
    }
}