
package validator;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIInput;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.FacesValidator;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;

@FacesValidator("confirmPasswordValidator")
public class ConfirmPasswordValidator implements Validator<Object> {

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        UIComponent passwordComponent = component.getAttributes().get("passwordComponent") != null ?
            (UIComponent) component.getAttributes().get("passwordComponent") : null;

        if (passwordComponent != null) {
            String password = (String) ((UIInput) passwordComponent).getSubmittedValue();
            String confirmPassword = (String) value;

            if (!confirmPassword.equals(password)) {
                throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Passwords do not match", null));
            }
        }
    }
}
