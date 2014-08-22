/**
 * Copyright (c) 2014 ControlsFX
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
package org.controlsfx.dialog.wizard;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Stack;

import com.sun.javafx.scene.control.skin.AccordionSkin;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Wizard {
    
    
    /**************************************************************************
     * 
     * Static fields
     * 
     **************************************************************************/
    
    
    
    /**************************************************************************
     * 
     * Private fields
     * 
     **************************************************************************/
    
    private Dialog<ButtonType> dialog;
    
    private final ObservableMap<String, Object> settings = FXCollections.observableHashMap();
    
    private final Stack<WizardPane> pageHistory = new Stack<>(); 
    
    private Optional<WizardPane> currentPage = Optional.empty();
    
//    private final ValidationSupport validationSupport = new ValidationSupport();
    
//    
    private final ButtonType BUTTON_PREVIOUS = new ButtonType("Previous", ButtonData.BACK_PREVIOUS);
    private final EventHandler<ActionEvent> BUTTON_PREVIOUS_ACTION_HANDLER = actionEvent -> {
        actionEvent.consume();
        currentPage = Optional.ofNullable( pageHistory.isEmpty()? null: pageHistory.pop() );
        updatePage(dialog,false);
    };
    
    private final ButtonType BUTTON_NEXT = new ButtonType("Next", ButtonData.NEXT_FORWARD);
    private final EventHandler<ActionEvent> BUTTON_NEXT_ACTION_HANDLER = actionEvent -> {
        actionEvent.consume();
        currentPage.ifPresent(page->pageHistory.push(page));
        currentPage = getFlow().advance(currentPage.orElse(null));
        updatePage(dialog,true);
    };
    
    
    
    /**************************************************************************
     * 
     * Constructors
     * 
     **************************************************************************/
    
    /**
     * 
     */
    public Wizard() {
        this(null);
    }
    
    /**
     * 
     * @param owner
     */
    private Wizard(Object owner) {
        this(owner, "");
    }
    
    /**
     * 
     * @param owner
     * @param title
     */
    private Wizard(Object owner, String title) {
//        this.owner = owner;
//        this.title = title;
        
//        validationSupport.validationResultProperty().addListener( (o, ov, nv) -> validateActionState());
        
        dialog = new Dialog<ButtonType>();
        dialog.setTitle(title);
//        hello.dialog.initOwner(owner); // TODO add initOwner API
        
    }
    
    
    
    /**************************************************************************
     * 
     * Public API
     * 
     **************************************************************************/
    
    public final void show() {
        dialog.show();
    }
    
    public final Optional<ButtonType> showAndWait() {
        return dialog.showAndWait();
    }
    
    // --- settings
    public final ObservableMap<String, Object> getSettings() {
        return settings;
    }
    
    
    
    /**************************************************************************
     * 
     * Properties
     * 
     **************************************************************************/
    
    // --- flow
    private ObjectProperty<Flow> flow = new SimpleObjectProperty<Flow>(new LinearWizardFlow()) {
        protected void invalidated() {
            updatePage(dialog,false);
        }
        
        public void set(Flow flow) {
        	super.set(flow);
        	pageHistory.clear();
        	if ( flow != null ) {
        		currentPage = flow.advance(currentPage.orElse(null));
        		updatePage(dialog,true);
        	}
        };
    };
    
    public final ObjectProperty<Flow> flowProperty() {
        return flow;
    }
    
    public final Flow getFlow() {
        return flow.get();
    }
    
    public final void setFlow(Flow flow) {
        this.flow.set(flow);
    }
    
    
    // --- Properties
    private static final Object USER_DATA_KEY = new Object();
    
    // A map containing a set of properties for this Wizard
    private ObservableMap<Object, Object> properties;

    /**
      * Returns an observable map of properties on this Wizard for use primarily
      * by application developers.
      *
      * @return an observable map of properties on this Wizard for use primarily
      * by application developers
     */
     public final ObservableMap<Object, Object> getProperties() {
        if (properties == null) {
            properties = FXCollections.observableMap(new HashMap<Object, Object>());
        }
        return properties;
    }
    
    /**
     * Tests if this Wizard has properties.
     * @return true if this Wizard has properties.
     */
     public boolean hasProperties() {
        return properties != null && !properties.isEmpty();
    }

     
    // --- UserData
    /**
     * Convenience method for setting a single Object property that can be
     * retrieved at a later date. This is functionally equivalent to calling
     * the getProperties().put(Object key, Object value) method. This can later
     * be retrieved by calling {@link helloworld.dialog.wizard.Wizard#getUserData()}.
     *
     * @param value The value to be stored - this can later be retrieved by calling
     *          {@link helloworld.dialog.wizard.Wizard#getUserData()}.
     */
    public void setUserData(Object value) {
        getProperties().put(USER_DATA_KEY, value);
    }

    /**
     * Returns a previously set Object property, or null if no such property
     * has been set using the {@link helloworld.dialog.wizard.Wizard#setUserData(Object)} method.
     *
     * @return The Object that was previously set, or null if no property
     *          has been set or if null was set.
     */
    public Object getUserData() {
        return getProperties().get(USER_DATA_KEY);
    }
    
    
//    public ValidationSupport getValidationSupport() {
//		return validationSupport;
//	}
    
    
    /**************************************************************************
     * 
     * Private implementation
     * 
     **************************************************************************/
    
    private void updatePage(Dialog<ButtonType> dialog, boolean advancing) {
        Flow flow = getFlow();
        if (flow == null) {
            return;
        }
        
        Optional<WizardPane> prevPage = Optional.ofNullable( pageHistory.isEmpty()? null: pageHistory.peek()); 
        prevPage.ifPresent( page -> {
	        // if we are going forward in the wizard, we read in the settings 
	        // from the page and store them in the settings map.
	        // If we are going backwards, we do nothing
	        if (advancing) {
	        	readSettings(page);
	        }
	        
	        // give the previous wizard page a chance to update the pages list
	        // based on the settings it has received
	        page.onExitingPage(this);
        });
        
        currentPage.ifPresent(currentPage -> {
            // put in default actions
            List<ButtonType> buttons = currentPage.getButtonTypes();
            if (! buttons.contains(BUTTON_PREVIOUS)) {
                buttons.add(BUTTON_PREVIOUS);
                Button button = (Button)currentPage.lookupButton(BUTTON_PREVIOUS);
                button.addEventFilter(ActionEvent.ACTION, BUTTON_PREVIOUS_ACTION_HANDLER);
            }
            if (! buttons.contains(BUTTON_NEXT)) {
                buttons.add(BUTTON_NEXT);
                Button button = (Button)currentPage.lookupButton(BUTTON_NEXT);
                button.addEventFilter(ActionEvent.ACTION, BUTTON_NEXT_ACTION_HANDLER);
            }
            if (! buttons.contains(ButtonType.FINISH)) buttons.add(ButtonType.FINISH);
            if (! buttons.contains(ButtonType.CANCEL)) buttons.add(ButtonType.CANCEL);
                
            // then give user a chance to modify the default actions
            currentPage.onEnteringPage(this);
            
            // and then switch to the new pane
            dialog.setDialogPane(currentPage);
        });
        
        validateActionState();
    }
    
    private void validateActionState() {
        final List<ButtonType> currentPaneButtons = dialog.getDialogPane().getButtonTypes();
        
        // TODO can't set a DialogButton to be disabled at present
//        BUTTON_PREVIOUS.setDisabled(pageHistory.isEmpty());
        
        // Note that we put the 'next' and 'finish' actions at the beginning of 
        // the actions list, so that it takes precedence as the default button, 
        // over, say, cancel. We will probably want to handle this better in the
        // future...
        
        if (!getFlow().canAdvance(currentPage.orElse(null))) {
            currentPaneButtons.remove(BUTTON_NEXT);
            
//            currentPaneActions.add(0, ACTION_FINISH);
//            ACTION_FINISH.setDisabled( validationSupport.isInvalid());
        } else {
            if (currentPaneButtons.contains(BUTTON_NEXT)) {
                currentPaneButtons.remove(BUTTON_NEXT);
                currentPaneButtons.add(0, BUTTON_NEXT);
                Button button = (Button)dialog.getDialogPane().lookupButton(BUTTON_NEXT);
                button.addEventFilter(ActionEvent.ACTION, BUTTON_NEXT_ACTION_HANDLER);
            }
            currentPaneButtons.remove(ButtonType.FINISH);
//            ACTION_NEXT.setDisabled( validationSupport.isInvalid());
        }
    }
    
    private int settingCounter;
    private void readSettings(WizardPane page) {
        // for now we cannot know the structure of the page, so we just drill down
        // through the entire scenegraph (from page.content down) until we get
        // to the leaf nodes. We stop only if we find a node that is a
        // ValueContainer (either by implementing the interface), or being 
        // listed in the internal valueContainers map.
        
        settingCounter = 0;
        checkNode(page.getContent());
    }
    
    private boolean checkNode(Node n) {
        boolean success = readSetting(n);
        
        if (success) {
            // we've added the setting to the settings map and we should stop drilling deeper
            return true;
        } else {
            // go into children of this node (if possible) and see if we can get
            // a value from them (recursively)
            List<Node> children = ImplUtils.getChildren(n, false);
            
            // we're doing a depth-first search, where we stop drilling down
            // once we hit a successful read
            boolean childSuccess = false;
            for (Node child : children) {
                childSuccess |= checkNode(child);
            }
            return childSuccess;
        }
    }
    
    private boolean readSetting(Node n) {
        if (n == null) {
            return false;
        }
        
        Object setting = ValueExtractor.getValue(n);
        
        if (setting != null) {
            // save it into the settings map.
            // if the node has an id set, we will use that as the setting name
            String settingName = n.getId();
            
            // but if the id is not set, we will use a generic naming scheme
            if (settingName == null || settingName.isEmpty()) {
                settingName = "page_" /*+ previousPageIndex*/ + ".setting_" + settingCounter; 
            }
            
            getSettings().put(settingName, setting);
            
            settingCounter++;
        }
        
        return setting != null;
    }
    
    
    
    /**************************************************************************
     * 
     * Support classes
     * 
     **************************************************************************/
    
    /**
     * 
     */
    // TODO this should just contain a ControlsFX Form, but for now it is hand-coded
    public static class WizardPane extends DialogPane {
        
        public WizardPane() {
            // TODO extract to CSS
            setGraphic(new ImageView(new Image("/com/sun/javafx/scene/control/skin/modena/dialog-confirm.png")));
        }

        // TODO we want to change this to an event-based API eventually
        public void onEnteringPage(Wizard wizard) {
            
        }
        
        // TODO same here - replace with events
        public void onExitingPage(Wizard wizard) {
            
        }
    }
    
    public interface Flow {
    	Optional<WizardPane> advance(WizardPane currentPage);
    	boolean canAdvance(WizardPane currentPage);
    }
    
}
