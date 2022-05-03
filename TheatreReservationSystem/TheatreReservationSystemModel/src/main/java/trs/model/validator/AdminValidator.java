package trs.model.validator;

import trs.model.Admin;

public class AdminValidator implements IValidator<Admin> {

    @Override
    public void validate(Admin admin) throws ValidatorException {
        String errors = "";
        if (admin.getUsername().equals("")) {
            errors += "Invalid username!\n";
        }
        if (admin.getPassword().equals("")) {
            errors += "Invalid password!\n";
        }
        if (errors.length() > 0) {
            throw new ValidatorException(errors);
        }
    }
}
