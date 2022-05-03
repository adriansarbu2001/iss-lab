package trs.model.validator;

import trs.model.TheatreShow;

public class TheatreShowValidator implements IValidator<TheatreShow> {

    @Override
    public void validate(TheatreShow theatreShow) throws ValidatorException {
        String errors = "";
        if (theatreShow.getName().equals("")) {
            errors += "Invalid name!\n";
        }
        if (errors.length() > 0) {
            throw new ValidatorException(errors);
        }
    }
}
