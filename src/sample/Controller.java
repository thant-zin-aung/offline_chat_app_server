package sample;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Controller {
    @FXML
    public JFXTextField msgBox;
    @FXML
    public JFXTextArea  msgField;
    @FXML
    public JFXButton sendMsgButton;
    @FXML
    public Label serverStatus;
    private String msgToClient;
    private String username;

    public static ServerSocket serverSocket;
    public static Socket socket;
    private Task<ObservableList<String>> task;
    @FXML
    public void initialize() {
        username = "Server: ";
        task = new Task<ObservableList<String>>() {
            @Override
            protected ObservableList<String> call() throws Exception {
                ObservableList<String> msgList = FXCollections.observableArrayList("Connected");
                try {
                    System.out.println("Server is waiting");
                    updateMessage("Server is waiting for client...");
                    serverSocket = new ServerSocket(1666);
                    socket = serverSocket.accept();
                    BufferedReader readFromSocket = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter writeToSocket = new PrintWriter(socket.getOutputStream(),true);
                    updateMessage(msgList.get(0));
                    msgBox.setOnKeyReleased( e -> {
                                if ( e.getCode() == KeyCode.ENTER ) {
                                    String msg = username+msgBox.getText();
                                    if ( !msgBox.getText().trim().equals("") ) {
                                        msgField.appendText(msg+"\n");
                                        writeToSocket.println(msg);
                                        msgBox.clear();
                                    }
                                }
                            }
                    );

                    sendMsgButton.setOnAction( e -> {
                        msgToClient=msgBox.getText();
                        msgField.appendText(username+msgToClient+"\n");
                        msgBox.clear();
                        writeToSocket.println(username+msgToClient);
                    });
                    String readData;
                    while ( !(readData = readFromSocket.readLine()).equals("exit") ) {
                        System.out.println(readData);
                        msgField.appendText(readData+"\n");
                        if ( msgToClient!=null && msgToClient.equals("") ) {
                            writeToSocket.println(username+msgToClient);
                        }
                    }
                    socket.close();
                } catch ( Exception e ) {
                    System.out.println(e.getMessage());
                } finally {
                    try {
                        serverSocket.close();
                        updateMessage("Client disconnected");
                    } catch ( Exception e ) {
                        System.out.println(e.getMessage());
                    }
                }
                return msgList;
            }
        };
        serverStatus.textProperty().bind(task.messageProperty());

        new Thread(task).start();
    }
}
