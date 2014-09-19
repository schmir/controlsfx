/**
 * Copyright (c) 2014, ControlsFX
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
package org.controlsfx.validation;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputControl;
import javafx.util.Callback;

import org.controlsfx.validation.decoration.GraphicValidationDecoration;
import org.controlsfx.validation.decoration.ValidationDecoration;

/**
 * Provides validation support for UI components. The idea is create an instance of this class the component group, usually a panel.<br>
 * Once created, {@link Validator}s can be registered for components, to provide the validation:
 * 
 *     <pre>
 *        ValidationSupport validationSupport = new ValidationSupport();
 *        validationSupport.registerValidator(textField, Validator.createEmptyValidator("Text is required"));
 *        validationSupport.registerValidator(combobox, Validator.createEmptyValidator( "ComboBox Selection required"));
 *        validationSupport.registerValidator(checkBox, (Control c, Boolean newValue) -> 
 *        	    ValidationResult.fromErrorIf( c, "Checkbox should be checked", !newValue)
 *         );
 *     </pre>
 *     
 *  validationResultProperty provides an ability to react on overall validation result changes:
 *  <pre>
 *     validationSupport.validationResultProperty().addListener( (o, oldValue, newValue) ->
        	 messageList.getItems().setAll(newValue.getMessages()));
 *  </pre>   
 *  
 *  Standard JavaFX UI controls are supported out of the box. There is also an ability to add support for custom controls.
 *  To do that "observable value extractor" should be added for specific controls. Such "extractor" consists of two functional interfaces:
 *  a {@link Predicate} to check the applicability of the control and a {@link Callback} to extract control's observable value. 
 *  Here is an sample of internal registration of such "extractor" for  a few  controls :
 *  <pre>
 *     addObservableValueExtractor( c -> c instanceof TextInputControl, c -> ((TextInputControl)c).textProperty());
 *     addObservableValueExtractor( c -> c instanceof ComboBox,         c -> ((ComboBox<?>)c).valueProperty());
 *  </pre>
 *   
 */
public class ValidationSupport {
	
    private static class ObservableValueExtractor {

        public final Predicate<Control> applicability;
        public final Callback<Control, ObservableValue<?>> extraction;

        public ObservableValueExtractor( Predicate<Control> applicability, Callback<Control, ObservableValue<?>> extraction ) {
            this.applicability = Objects.requireNonNull(applicability);
            this.extraction    = Objects.requireNonNull(extraction);
        }

    }

    private static List<ObservableValueExtractor> extractors = FXCollections.observableArrayList(); 

    /**
     * Add "obervable value extractor" for custom controls.
     * @param test applicability test
     * @param extract extraction of observable value
     */
    public static void addObservableValueExtractor( Predicate<Control> test, Callback<Control, ObservableValue<?>> extract ) {
        extractors.add( new ObservableValueExtractor(test, extract));
    }

    {
        addObservableValueExtractor( c -> c instanceof TextInputControl, c -> ((TextInputControl)c).textProperty());
        addObservableValueExtractor( c -> c instanceof ComboBox,         c -> ((ComboBox<?>)c).valueProperty());
        addObservableValueExtractor( c -> c instanceof ChoiceBox,        c -> ((ChoiceBox<?>)c).valueProperty());
        addObservableValueExtractor( c -> c instanceof CheckBox,         c -> ((CheckBox)c).selectedProperty());
        addObservableValueExtractor( c -> c instanceof Slider,           c -> ((Slider)c).valueProperty());
        addObservableValueExtractor( c -> c instanceof ColorPicker,      c -> ((ColorPicker)c).valueProperty());
        addObservableValueExtractor( c -> c instanceof DatePicker,       c -> ((DatePicker)c).valueProperty());

        addObservableValueExtractor( c -> c instanceof ListView,         c -> ((ListView<?>)c).itemsProperty());
        addObservableValueExtractor( c -> c instanceof TableView,        c -> ((TableView<?>)c).itemsProperty());

        // FIXME: How to listen for TreeView changes???
        //addObservableValueExtractor( c -> c instanceof TreeView,         c -> ((TreeView<?>)c).Property());
    }

    private static final String CTRL_REQUIRED_FLAG    = "$org.controlsfx.validation.required$"; //$NON-NLS-1$
    
    /**
     * Set control's required flag
     * @param c control
     * @param required flag
     */
    public static void setRequired( Control c, boolean required ) {
        c.getProperties().put(CTRL_REQUIRED_FLAG, required);
    }

    /**
     * Check control's required flag
     * @param c control
     * @return true if required 
     */
    public static boolean isRequired( Control c ) {
        Object value = c.getProperties().get(CTRL_REQUIRED_FLAG);
        return value instanceof Boolean? (Boolean)value: false;
    }

    private ObservableSet<Control> controls = FXCollections.observableSet();
    private ObservableMap<Control,ValidationResult> validationResults = 
            FXCollections.observableMap(new WeakHashMap<>());

    /**
     * Creates validation support instance
     */
    public ValidationSupport() {

        // notify validation result observers
        validationResults.addListener( (MapChangeListener.Change<? extends Control, ? extends ValidationResult> change) ->
        validationResultProperty.set(ValidationResult.fromResults(validationResults.values()))
                );

        // validation decoration
        validationResultProperty().addListener( (o, oldValue, validationResult) -> {
            invalidProperty.set(!validationResult.getErrors().isEmpty());
            redecorate();
        });
    }

    /**
     * Redecorates all known components
     * Only decorations related to validation are affected
     */
    // TODO needs optimization
    public void redecorate() {
        Optional<ValidationDecoration> odecorator = Optional.ofNullable(getValidationDecorator());
        for (Control target : getRegisteredControls()) {
            odecorator.ifPresent( decorator -> {
            	decorator.removeDecorations(target);
                decorator.applyRequiredDecoration(target);
                getHighestMessage(target).ifPresent(msg -> decorator.applyValidationDecoration(msg));
            });
        }
    }

    private ReadOnlyObjectWrapper<ValidationResult> validationResultProperty = 
            new ReadOnlyObjectWrapper<>();


    /**
     * Retrieves current validation result
     * @return validation result
     */
    public ValidationResult getValidationResult() {
        return validationResultProperty.get();
    }

    /**
     * Can be used to track validation result changes 
     * @return The Validation result property.
     */
    public ReadOnlyObjectProperty<ValidationResult> validationResultProperty() {
        return validationResultProperty.getReadOnlyProperty();
    }

    private BooleanProperty invalidProperty = new SimpleBooleanProperty();

    /**
     * Returns current validation state. 
     * @return true if there is at least one error
     */
    public Boolean isInvalid() {
        return invalidProperty.get();
    }

    /**
     * Validation state property 
     * @return validation state property
     */
    public ReadOnlyBooleanProperty invalidProperty() {
        return invalidProperty;
    }


    private ObjectProperty<ValidationDecoration> validationDecoratorProperty =
            new SimpleObjectProperty<ValidationDecoration>(this, "validationDecorator", new GraphicValidationDecoration()) { //$NON-NLS-1$
        @Override protected void invalidated() {
            // when the decorator changes, rerun the decoration to update the visuals immediately.
            redecorate();
        }
    };

    /**
     * @return The Validation decorator property
     */
    public ObjectProperty<ValidationDecoration> validationDecoratorProperty() {
        return validationDecoratorProperty;
    }

    /**
     * Returns current validation decorator
     * @return current validation decorator or null if none
     */
    public ValidationDecoration getValidationDecorator() {
        return validationDecoratorProperty.get();
    }

    /**
     * Sets new validation decorator
     * @param decorator new validation decorator. Null value is valid - no decoration will occur
     */
    public void setValidationDecorator( ValidationDecoration decorator ) {
        validationDecoratorProperty.set(decorator);
    }

    private Optional<ObservableValueExtractor> getExtractor(final Control c) {
        for( ObservableValueExtractor e: extractors ) {
            if ( e.applicability.test(c)) return Optional.of(e);
        }
        return Optional.empty();
    }

    /**
     * Registers {@link Validator} for specified control with additional possiblity to mark control as required or not.
     * @param c control to validate
     * @param required true if controls should be required
     * @param validator {@link Validator} to be used
     * @return true if registration is successful
     */
    @SuppressWarnings("unchecked")
    public <T> boolean registerValidator( final Control c, boolean required, final Validator<T> validator  ) {
    	
    	
    	Optional.ofNullable(c).ifPresent( ctrl -> {
    		ctrl.getProperties().addListener( new MapChangeListener<Object,Object>(){

				@Override
				public void onChanged(
						javafx.collections.MapChangeListener.Change<? extends Object, ? extends Object> change) {
					
					if ( CTRL_REQUIRED_FLAG.equals(change.getKey())) {
						redecorate();
					}
				}

    		});
    	});

        return getExtractor(c).map( e -> {

            ObservableValue<T> observable = (ObservableValue<T>) e.extraction.call(c);
            setRequired( c, required );

            Consumer<T> updateResults = value -> { 
                Platform.runLater(() -> validationResults.put(c, validator.apply(c, value)));
            };

            controls.add(c);

            observable.addListener( (o,oldValue,newValue) -> updateResults.accept(newValue));
            updateResults.accept(observable.getValue());

            return e;

        }).isPresent();
    }

    /**
     * Registers {@link Validator} for specified control and makes control required
     * @param c control to validate
     * @param validator {@link Validator} to be used
     * @return true if registration is successful
     */
    public <T> boolean registerValidator( final Control c, final Validator<T> validator  ) {
        return registerValidator(c, true, validator);
    }

    /**
     * Returns currently registered controls
     * @return set of currently registered controls
     */
    public Set<Control> getRegisteredControls() {
        return Collections.unmodifiableSet(controls);
    }

    /**
     * Returns optional highest severity message for a control
     * @param target control
     * @return Optional highest severity message for a control
     */
    public Optional<ValidationMessage> getHighestMessage(Control target) {
    	return Optional.ofNullable(validationResults.get(target)).flatMap( result ->
    	   result.getMessages().stream().max(ValidationMessage.COMPARATOR)   
    	);
    }
}
