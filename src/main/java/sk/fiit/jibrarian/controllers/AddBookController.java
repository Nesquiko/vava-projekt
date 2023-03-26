package sk.fiit.jibrarian.controllers;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import sk.fiit.jibrarian.data.CatalogRepository;
import sk.fiit.jibrarian.data.RepositoryFactory;
import sk.fiit.jibrarian.model.Item;
import sk.fiit.jibrarian.model.ItemType;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ResourceBundle;
import java.util.UUID;

public class AddBookController implements Initializable {
    @FXML
    private Button addBookButton;

    @FXML
    private TextField authorInput;

    @FXML
    private Button cancelButton;

    @FXML
    private ImageView chosenBookImage;

    @FXML
    private TextArea descriptionInput;

    @FXML
    private TextField languageInput;

    @FXML
    private Spinner<Integer> quantityInput;

    @FXML
    private TextField titleInput;
    private Image image;
    private FileChooser fileChooser;
    private File chosenImageFile;
    @FXML
    private TextField totalPagesInput;
    @FXML
    private TextField isbnInput;
    @FXML
    private Label isbnLabel;
    @FXML
    private ComboBox<String> itemTypeInput;
    @FXML
    private ComboBox<String> genreInput;
    private String[] itemTypes = {"Book", "Article", "Magazine"};
    private String[] itemGenres =
            {"Action and Adventure", "Classics", "Comic Book", "Detective and Mystery", "Fantasy", "Historical Fiction",//
             "Horror", "Literary Fiction", "Romance", "Science Fiction (Sci-Fi)", "Short Stories", "Suspense and Thrillers"};
    public CatalogRepository catalogRepository = RepositoryFactory.getCatalogRepository();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        fileChooser = setFileChooser();
        SpinnerValueFactory<Integer> valueFactory = //
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1);
        quantityInput.setValueFactory(valueFactory);
        ObservableList<String> genres = FXCollections.observableArrayList(itemGenres);
        genreInput.setItems(genres);
        ObservableList<String> types = FXCollections.observableArrayList(itemTypes);
        itemTypeInput.setItems(types);
        itemTypeInput.getSelectionModel().selectFirst();
        itemTypeInput.setOnAction(actionEvent -> {
            var selectedItem = itemTypeInput.getSelectionModel().getSelectedItem();
            if (selectedItem.equals("Book")) {
                isbnLabel.setVisible(true);
                isbnInput.setVisible(true);
            } else {
                isbnLabel.setVisible(false);
                isbnInput.setVisible(false);
            }
        });
        // force the field to be numeric only
        totalPagesInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue,
                                String newValue) {
                if (!newValue.matches("\\d*")) {
                    totalPagesInput.setText(newValue.replaceAll("[^\\d]", ""));
                }
            }
        });
    }

    @FXML
    private void chooseImage() {
        Stage stage = (Stage) (addBookButton.getScene()).getWindow();
        chosenImageFile = fileChooser.showOpenDialog(stage);
        if (chosenImageFile != null) {
            image = new Image(String.valueOf(chosenImageFile));
            chosenBookImage.setImage(image);
        }
    }

    @FXML
    private void addBookToDB() throws CatalogRepository.ItemAlreadyExistsException, IOException {
        if (titleInput.getText().isEmpty() || authorInput.getText().isEmpty() || totalPagesInput.getText().isEmpty()
                ||(Integer.parseInt(totalPagesInput.getText()) < 1 || Integer.parseInt(totalPagesInput.getText()) > 9999) ||
                languageInput.getText().isEmpty() || descriptionInput.getText().isEmpty() || chosenImageFile == null
                || genreInput.getSelectionModel().isEmpty()) {
            showDialog("FILL ALL FIELDS!!!");
        } else {
            Item newBook = new Item();
            newBook.setAuthor(authorInput.getText());
            newBook.setId(UUID.randomUUID());
            newBook.setTitle(titleInput.getText());
            newBook.setDescription(descriptionInput.getText());
            newBook.setLanguage(languageInput.getText());
            newBook.setItemType(getItemTypeFromSelected());
            newBook.setGenre(genreInput.getSelectionModel().getSelectedItem().toString());
            newBook.setAvailable(quantityInput.getValue());
            newBook.setReserved(0);
            newBook.setTotal(quantityInput.getValue());
            newBook.setPages(Integer.valueOf(totalPagesInput.getText()));

            byte[] imageBytes = Files.readAllBytes(chosenImageFile.toPath());
            newBook.setImage(imageBytes);
            catalogRepository.saveItem(newBook);
            showDialog("ADDED BOOK SUCCESSFULLY");
            clearFields();
        }
    }

    private FileChooser setFileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Book Cover Image");
        fileChooser.getExtensionFilters()
                .addAll(new FileChooser.ExtensionFilter("JPG,JPEG,PNG", "*.jpg", "*.jpeg", "*.png"));
        return fileChooser;
    }

    private ItemType getItemTypeFromSelected() {
        switch (itemTypeInput.getSelectionModel().getSelectedItem()) {
            case "Book":
                return ItemType.BOOK;
            case "Magazine":
                return ItemType.MAGAZINE;
            case "Article":
                return ItemType.ARTICLE;
        }
        return ItemType.BOOK;
    }

    private void showDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.NONE, message, ButtonType.OK);
        alert.showAndWait();

        if (alert.getResult() == ButtonType.OK) {
            alert.close();
        }
    }

    @FXML
    private void clearFields() {
        titleInput.clear();
        authorInput.clear();
        languageInput.clear();
        descriptionInput.clear();
        Image img = new Image(getClass().getResourceAsStream("../views/choose.png"));
        chosenBookImage.setImage(img);
    }

}
