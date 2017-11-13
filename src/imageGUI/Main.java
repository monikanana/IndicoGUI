package imageGUI;

import imageSort.CannotCreateDirectoryException;
import imageSort.FileNotMovedException;
import imageSort.NoImagesInDirectoryException;
import imageSort.Sort;
import io.indico.Indico;
import io.indico.api.results.IndicoResult;
import io.indico.api.utils.IndicoException;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class Main extends Application {

    private Stage stage;
    private ObservableList<String> photosList = FXCollections.observableArrayList();
    private final String[] EXTENSIONS = new String[]{"jpg", "png"}; //TODO: check which extensions are handled by indico
    private Map<String, Object> parameters = new HashMap<>();
    private ObservableList<PieChart.Data> pieChartData;
    private File directory;
    private Map<String, String> fileNamesAndPaths = new HashMap<>();

    @FXML
    private ListView<String> photosView;
    @FXML
    private ImageView imageView;
    @FXML
    private PieChart pieChart;
    @FXML
    private Label fileNameMessage;

    @FXML
    private void changeDirectory() {
        photosView.setItems(null);
        photosList.clear();

        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Choose directory");
        directoryChooser.setInitialDirectory(new File("C:\\Users\\ProBook\\Desktop\\fotoSort"));
        directory = directoryChooser.showDialog(stage);

        for (File image : directory.listFiles()) {      //TODO: NullPointerException
            for (final String extension : EXTENSIONS) {
                if (image.getPath().endsWith(extension)) {
                    fileNamesAndPaths.put(image.getName(),image.getPath());
                    photosList.add(image.getName());
                }
            }
        }
        photosView.setItems(photosList);
    }

    @FXML
    private void displayData() throws IndicoException, IOException {
        //display photo
        String name = photosView.getSelectionModel().getSelectedItem();
        fileNameMessage.setText(name);
        imageView.setImage(new Image("file:" + fileNamesAndPaths.get(name)));

        //display chart
        Indico indico = new Indico("1026d4b1cea8318b801f45df22bcb2b5");
        parameters.put("threshold", 0.01);   //only return category with likelihood greater than 0.01
        parameters.put("top_n", 7);          //only return 7 of the most likely category
        IndicoResult single = indico.imageRecognition.predict(fileNamesAndPaths.get(name), parameters);
        Map<String, Double> result = single.getImageRecognition();

        pieChartData = FXCollections.observableArrayList();

        double othersValue = 0.0;

        for (Map.Entry<String, Double> entry : result.entrySet()) {
            pieChartData.add(new PieChart.Data(entry.getKey() + " "
                    + (Math.round(entry.getValue() * 100) + "%"), entry.getValue()));
            othersValue += entry.getValue();
        }
        pieChartData.add(new PieChart.Data("Others " + Math.round(othersValue * 100) + "%", othersValue));

        pieChart.setData(pieChartData);
        pieChart.setTitle("What is on the picture?");
        pieChart.setLabelsVisible(false);
    }

    @FXML
    private void reset() {
        imageView.setImage(null);
        pieChartData.clear();
        pieChart.setData(null);
        photosView.setItems(null);
        fileNameMessage.setText("");
    }

    @FXML
    private void group() throws CannotCreateDirectoryException, NoImagesInDirectoryException, FileNotMovedException {
        Sort photos = new Sort();

        try {
            photos.sort(directory.getPath());
        } catch (FileNotFoundException e) {
            System.out.println("Directory not found.");
        } catch (NoImagesInDirectoryException e) {
            e.printException();
        } catch (CannotCreateDirectoryException e) {
            e.printException();
        } catch (FileNotMovedException e) {
            e.printException();
        } catch (io.indico.api.utils.IndicoException e) {
            System.out.println("Invalid API key.");
        } catch (java.net.UnknownHostException e) {
            System.out.println("Unknown host.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent layout = FXMLLoader.load(getClass().getResource("IndicoGUI.fxml"));
        stage = primaryStage;
        stage.setTitle("IndicoGUI");

        stage.setScene(new Scene(layout, 1000, 600));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
