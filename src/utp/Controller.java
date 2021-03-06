package utp;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import sun.plugin.javascript.navig.Anchor;

import java.sql.*;

public class Controller {
    @FXML
    private TextField loginText;
    @FXML
    private PasswordField passwordText;
    @FXML
    private Button loginButton;
    @FXML
    private TextField registerLoginText;
    @FXML
    private PasswordField registerPasswordText;
    @FXML
    private PasswordField repeatPasswordText;
    @FXML
    private Button registerButton;
    @FXML
    private TextArea loginInfoText;
    @FXML
    private TextArea registerInfoText;
    @FXML
    private TextField firstNameText;
    @FXML
    private TextField lastNameText;
    @FXML
    private TextField emailText;
    @FXML
    private GridPane rootGridPane;

    private static final String USERNAME = "root";
    private static final String PASSWORD = "";
    private static final String CONN_STRING = "jdbc:mysql://localhost:3306/pwswlab05?serverTimezone=Europe/Warsaw&useLegacyDatetimeCode=false&useUnicode=true&characterEncoding=utf-8";
    private Connection conn;
    private String sql;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

    @FXML
    void initialize(){
        new Thread(() -> {
            conn = null;
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                conn = DriverManager.getConnection(CONN_STRING,USERNAME,PASSWORD);
                System.out.println("Connected");
            }catch (Exception e){
                System.err.println(e);
            }
        }).start();
    }
    @FXML
    void onActionHandler(ActionEvent e){
        if(e.getSource() == loginButton){
            try{
                sql = "SELECT id,login,password,firstname,lastname,email,type FROM users WHERE login = ? AND password = md5(?)";
                preparedStatement = conn.prepareStatement(sql);
                preparedStatement.setString(1,loginText.getText());
                preparedStatement.setString(2,passwordText.getText());
                resultSet = preparedStatement.executeQuery();
                if(resultSet.next()){
                    User loggedUser = new User(
                            resultSet.getLong(1),
                            resultSet.getString(2),
                            resultSet.getString(3),
                            resultSet.getString(4),
                            resultSet.getString(5),
                            resultSet.getString(6),
                            resultSet.getString(7)
                    );
                    FXMLLoader fxmlLoader = new FXMLLoader();
                    if(loggedUser.getUserAccountType().equals("U"))
                    {
                        fxmlLoader.setLocation(getClass().getResource("userview.fxml"));
                        UserController userController = new UserController();
                        userController.setLoggedUser(loggedUser);
                        fxmlLoader.setController(userController);
                    }
                    else{
                        fxmlLoader.setLocation(getClass().getResource("adminview.fxml"));
                        AdminController adminController = new AdminController();
                        adminController.setUser(loggedUser);
                        fxmlLoader.setController(adminController);
                    }
                    Parent root = fxmlLoader.load();
                    Scene dashboard = new Scene(root);
                    Stage window = (Stage)((Node)e.getSource()).getScene().getWindow();
                    window.setScene(dashboard);
                }
                else{
                    loginInfoText.appendText("Nie znaleziono uzytkownika\n");
                }
            }
            catch(Exception ex){
                System.out.println(ex);
            }

        }
        if(e.getSource() == registerButton){
            if(registerLoginText.getText().length() > 0 &&
                    firstNameText.getText().length() > 0 &&
                    lastNameText.getText().length() > 0 &&
                    emailText.getText().length() > 0 &&
                    registerPasswordText.getText().length() > 0 &&
                    repeatPasswordText.getText().length() > 0
            ){
                if(registerPasswordText.getText().equals(repeatPasswordText.getText())){
                    if(emailText.getText().matches("^\\S+@\\S+\\.\\S+$")){
                        try{
                        sql = "SELECT * FROM users WHERE login = ?";
                        preparedStatement = conn.prepareStatement(sql);
                        preparedStatement.setString(1, registerLoginText.getText());
                        resultSet = preparedStatement.executeQuery();
                        if(!resultSet.next())
                        {
                            sql = "INSERT INTO users (login, password, firstname, lastname, email, type)" +
                                    "VALUES (?, md5(?), ?, ?, ?, ?)";
                            PreparedStatement preparedStatement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                            preparedStatement.setString(1,registerLoginText.getText());
                            preparedStatement.setString(2,registerPasswordText.getText());
                            preparedStatement.setString(3,firstNameText.getText());
                            preparedStatement.setString(4,lastNameText.getText());
                            preparedStatement.setString(5,emailText.getText());
                            preparedStatement.setString(6,"U");
                            preparedStatement.executeUpdate();
                            resultSet = preparedStatement.getGeneratedKeys();
                            if(resultSet.next()){
                                User newUserLogged = new User(
                                        resultSet.getLong(1),
                                        registerLoginText.getText(),
                                        registerPasswordText.getText(),
                                        firstNameText.getText(),
                                        lastNameText.getText(),
                                        emailText.getText(),
                                        "U"
                                );
                                FXMLLoader fxmlLoader = new FXMLLoader();
                                fxmlLoader.setLocation(getClass().getResource("userview.fxml"));
                                UserController userController = new UserController();
                                userController.setLoggedUser(newUserLogged);
                                fxmlLoader.setController(userController);
                                Parent root = fxmlLoader.load();
                                Scene dashboard = new Scene(root);
                                Stage window = (Stage)((Node)e.getSource()).getScene().getWindow();
                                window.setScene(dashboard);
                            }
                        }
                        else{
                            registerInfoText.appendText("Blad: Podany uzytkownik juz istnieje\n");
                        }

                        }
                        catch (Exception ex){
                            System.out.println(ex);
                        }
                    }
                    else {
                        registerInfoText.appendText("Blad: zly format adresu email\n");
                    }

                }
                else{
                    registerInfoText.appendText("Blad: Podane hasla roznia sie\n");
                }

            }
            else{
                registerInfoText.appendText("Blad: Kazde pole musi byc wypelnione\n");
            }
        }
    }

}
