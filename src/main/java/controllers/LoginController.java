package controllers;

import com.jfoenix.controls.*;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

import command.AutoCalculateInterestCommand;
import command.LoginCommand;
import io.github.palexdev.materialfx.controls.MFXProgressSpinner;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;
import models.Account;
import navigation.Navigation;
import navigation.ScenePaths;
import stores.AppStore;
import utils.SnackBarUtils;


public class LoginController {

    LoginCommand loginCommand;
    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private StackPane root;

    @FXML
    private JFXTextField txtAccount;

    @FXML
    private JFXPasswordField txtPassword;

    @FXML
    private JFXCheckBox ckbRemember;

    @FXML
    private JFXButton btnLogin;

    @FXML
    private MFXProgressSpinner progressBar;

    @FXML
    void initialize() {
        btnLogin.disableProperty().bind(Bindings.or(txtAccount.textProperty().isEmpty(), txtPassword.textProperty().isEmpty()));
        AutoCalculateInterestCommand command = new AutoCalculateInterestCommand();
        String username = pref.get("Username","");
        String password = pref.get("Password","");
        txtAccount.setText(username);
        txtPassword.setText(password);
        command.setOnSucceed(new Callback() {
            @Override
            public Object call(Object param) {
                SnackBarUtils.getInstance().show(root, "Tự động tính lãi thành công!");
                return null;
            }
        });
        command.setOnFail(new Callback() {
            @Override
            public Object call(Object param) {
                if (command.getException().getMessage().equals("The statement did not return a result set.")) {
                    SnackBarUtils.getInstance().show(root, "Chào bạn đến với Savebase!");

                }
                else {
                    SnackBarUtils.getInstance().show(root, "Lỗi: " + command.getException().getMessage());
                }
                return null;
            }
        });
        command.execute();
    }

    void setOnLogin(boolean isLogining) {
        progressBar.setVisible(isLogining);
        btnLogin.setVisible(!isLogining);
        txtAccount.setDisable(isLogining);
        txtPassword.setDisable(isLogining);
    }
    public Preferences pref = Preferences.userRoot().node("ckbRemember");
    public void saveLogin(String username, String password){
        if(username == null || password == null){
            System.out.println("Nothing to save.");
        }
        else{
            String user = username;
            pref.put("Username", username);
            String pass = password;
            pref.put("Password", password);
            System.out.println("Login saved.");
        }
    }
    public final void checked(boolean rmb){
        if(rmb){
            saveLogin(txtAccount.getText(), txtPassword.getText());
        }
        else{
        }
    }
    public void onLogin() {
        loginCommand = new LoginCommand(txtAccount.getText(), txtPassword.getText());
        if(ckbRemember.isSelected()){
            checked(true);
        }
        else{
            checked(false);
        }

        setOnLogin(true);
        loginCommand.setOnSucceed(new Callback() {
            @Override
            public Object call(Object param) {
                setOnLogin(false);
                if (loginCommand.getResult() == null) {
                    SnackBarUtils.getInstance().show(root, "Tài khoản hoặc mật khẩu không hợp lệ.");
                } else {
                    AppStore.setCurrentAccount((Account) loginCommand.getResult());
                    Navigation.getInstance().pushAndRemoveAll(ScenePaths.HOME);
                }
                return null;
            }
        });
        loginCommand.setOnFail(new Callback() {
            @Override
            public Object call(Object param) {
                setOnLogin(false);
                SnackBarUtils.getInstance().show(root, "Lỗi: " + loginCommand.getException().getMessage());
                return null;
            }
        });

        loginCommand.execute();
    }

}