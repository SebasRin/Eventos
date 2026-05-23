module com.eventos {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.eventos to javafx.fxml;
    exports com.eventos;
}