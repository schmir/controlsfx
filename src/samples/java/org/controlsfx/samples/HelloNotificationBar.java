/**
 * Copyright (c) 2013, ControlsFX
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *     * Neither the name of ControlsFX, any associated website, nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL CONTROLSFX BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.controlsfx.samples;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.controlsfx.Sample;
import org.controlsfx.control.NotificationBar;
import org.controlsfx.dialog.Dialog.Actions;

public class HelloNotificationBar extends Application implements Sample {
    
    private NotificationBar notificationBar;
    
    public static void main(String[] args) {
        launch(args);
    }
    
    @Override public String getSampleName() {
        return "Notification Bar";
    }
    
    @Override public String getJavaDocURL() {
        return Utils.JAVADOC_BASE + "org/controlsfx/control/NotificationBar.html";
    }
    
    @Override public boolean includeInSamples() {
        return true;
    }
    
    @Override public Node getPanel(Stage stage) {
        VBox root = new VBox(20);
//        root.setPadding(new Insets(30, 30, 30, 30));
        
        notificationBar = new NotificationBar(null);
        notificationBar.getActions().add(Actions.OK);
        
        
        root.getChildren().add(notificationBar);
        
        Button showBtn = new Button("Show / Hide");
        showBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent arg0) {
                if (notificationBar.isShowing()) {
                    notificationBar.hide();
                } else {
                    boolean useDarkTheme = ! notificationBar.getStyleClass().contains(NotificationBar.STYLE_CLASS_DARK);
                    
                    if (useDarkTheme) {
                        notificationBar.setText("Hello World! Using the dark theme");
                        notificationBar.getStyleClass().add(NotificationBar.STYLE_CLASS_DARK);
                    } else {
                        notificationBar.setText("Hello World! Using the light theme");
                        notificationBar.getStyleClass().remove(NotificationBar.STYLE_CLASS_DARK);
                    }
                    
                    notificationBar.show();
                }
            }
        });
        root.getChildren().add(showBtn);
        
        return root;
    }
    
    @Override public void start(Stage stage) {
        stage.setTitle("NotificationBar Demo");

        Scene scene = new Scene((Parent) getPanel(stage), 520, 360);

        stage.setScene(scene);
        stage.show();
    }
}
